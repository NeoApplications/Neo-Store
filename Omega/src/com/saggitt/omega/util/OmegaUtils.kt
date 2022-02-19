/*
 *  This file is part of Omega Launcher.
 *  Copyright (c) 2021   Saul Henriquez
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.*
import android.content.pm.PackageInfo.REQUESTED_PERMISSION_GRANTED
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.text.format.DateFormat
import android.util.Property
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.children
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceGroup
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.util.Executors.MAIN_EXECUTOR
import com.android.launcher3.util.Executors.UI_HELPER_EXECUTOR
import com.android.launcher3.util.Themes
import com.android.launcher3.views.OptionsPopupView
import com.android.systemui.shared.system.QuickStepContract
import com.saggitt.omega.search.SearchProvider
import com.saggitt.omega.search.WebSearchProvider
import com.saggitt.omega.search.providers.AppsSearchProvider
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import kotlin.math.ceil
import kotlin.random.Random
import kotlin.reflect.KMutableProperty0

// TODO break into specialised files/classes

val Context.omegaPrefs get() = Utilities.getOmegaPrefs(this)

val Context.launcherAppState get() = LauncherAppState.getInstance(this)

@ColorInt
fun Context.getColorAccent(): Int {
    return getColorAttr(R.attr.colorAccent)
}

fun AlertDialog.applyAccent() {
    val color = Utilities.getOmegaPrefs(context).accentColor
    val buttons = listOf(
        getButton(DialogInterface.BUTTON_NEGATIVE),
        getButton(DialogInterface.BUTTON_NEUTRAL),
        getButton(DialogInterface.BUTTON_POSITIVE)
    )
    buttons.forEach {
        it?.setTextColor(color)
    }
}

fun <T, A> ensureOnMainThread(creator: (A) -> T): (A) -> T {
    return { it ->
        if (Looper.myLooper() == Looper.getMainLooper()) {
            creator(it)
        } else {
            try {
                MAIN_EXECUTOR.submit(Callable { creator(it) }).get()
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            } catch (e: ExecutionException) {
                throw RuntimeException(e)
            }

        }
    }
}

fun <T> useApplicationContext(creator: (Context) -> T): (Context) -> T {
    return { it -> creator(it.applicationContext) }
}

@JvmOverloads
fun makeBasicHandler(preferMyLooper: Boolean = false, callback: Handler.Callback? = null): Handler =
    if (preferMyLooper)
        Handler(Looper.myLooper() ?: Looper.getMainLooper(), callback)
    else
        Handler(Looper.getMainLooper(), callback)

fun Context.checkPackagePermission(packageName: String, permissionName: String): Boolean {
    try {
        val info = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
        info.requestedPermissions.forEachIndexed { index, s ->
            if (s == permissionName) {
                return info.requestedPermissionsFlags[index].hasFlag(REQUESTED_PERMISSION_GRANTED)
            }
        }
    } catch (e: NameNotFoundException) {
    }
    return false
}

fun openPopupMenu(view: View, rect: RectF?, vararg items: OptionsPopupView.OptionItem) {
    val launcher = Launcher.getLauncher(view.context)
    OptionsPopupView.show(
        launcher,
        rect ?: RectF(launcher.getViewBounds(view)),
        items.toList(),
        true
    )
}

fun Context.getLauncherOrNull(): Launcher? {
    return try {
        Launcher.getLauncher(this)
    } catch (e: IllegalArgumentException) {
        null
    }
}

fun createRipplePill(context: Context, color: Int, radius: Float): Drawable {
    return RippleDrawable(
        ContextCompat.getColorStateList(context, R.color.focused_background)!!,
        createPill(color, radius), createPill(color, radius)
    )
}

fun createPill(color: Int, radius: Float): Drawable {
    return GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(color)
        cornerRadius = radius
    }
}

val mainHandler by lazy { makeBasicHandler() }
val uiWorkerHandler: Handler by lazy { UI_HELPER_EXECUTOR.handler }
fun runOnUiWorkerThread(r: () -> Unit) {
    runOnThread(uiWorkerHandler, r)
}

fun runOnMainThread(r: () -> Unit) {
    runOnThread(mainHandler, r)
}

fun runOnThread(handler: Handler, r: () -> Unit) {
    if (handler.looper.thread.id == Looper.myLooper()?.thread?.id) {
        r()
    } else {
        handler.post(r)
    }
}

val Context.hasStoragePermission
    get() = PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
        this, android.Manifest.permission.READ_EXTERNAL_STORAGE
    )

inline fun <T> Iterable<T>.safeForEach(action: (T) -> Unit) {
    val tmp = ArrayList<T>()
    tmp.addAll(this)
    for (element in tmp) action(element)
}

var View.isVisible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }

fun Context.getBooleanAttr(attr: Int): Boolean {
    val ta = obtainStyledAttributes(intArrayOf(attr))
    val value = ta.getBoolean(0, false)
    ta.recycle()
    return value
}

@ColorInt
fun Context.getColorAttr(attr: Int): Int {
    val ta = obtainStyledAttributes(intArrayOf(attr))
    @ColorInt val colorAccent = ta.getColor(0, 0)
    ta.recycle()
    return colorAccent
}

fun Context.getThemeAttr(attr: Int): Int {
    val ta = obtainStyledAttributes(intArrayOf(attr))
    val theme = ta.getResourceId(0, 0)
    ta.recycle()
    return theme
}

fun Context.getDimenAttr(attr: Int): Int {
    val ta = obtainStyledAttributes(intArrayOf(attr))
    val size = ta.getDimensionPixelSize(0, 0)
    ta.recycle()
    return size
}

fun Context.getIcon(): Drawable = packageManager.getApplicationIcon(applicationInfo)

fun ImageView.tintDrawable(color: Int) {
    val drawable = drawable.mutate()
    drawable.setTint(color)
    setImageDrawable(drawable)
}

fun GradientDrawable.getCornerRadiiCompat(): FloatArray? {
    return try {
        cornerRadii
    } catch (e: NullPointerException) {
        null
    }
}

inline fun ViewGroup.forEachChildReversedIndexed(action: (View, Int) -> Unit) {
    val count = childCount
    for (i in (0 until count).reversed()) {
        action(getChildAt(i), i)
    }
}

inline fun ViewGroup.forEachChildReversed(action: (View) -> Unit) {
    forEachChildReversedIndexed { view, _ -> action(view) }
}

@SuppressLint("PrivateResource")
fun Switch.applyColor(color: Int) {
    val colorForeground = Themes.getAttrColor(context, android.R.attr.colorForeground)
    val alphaDisabled = Themes.getAlpha(context, android.R.attr.disabledAlpha)
    val switchThumbNormal =
        context.resources.getColor(
            R.color.switch_thumb_normal_material_light,
            null
        )
    val switchThumbDisabled =
        context.resources.getColor(
            R.color.switch_thumb_disabled_material_light,
            null
        )
    val thstateList = ColorStateList(
        arrayOf(
            intArrayOf(-android.R.attr.state_enabled),
            intArrayOf(android.R.attr.state_checked),
            intArrayOf()
        ),
        intArrayOf(
            switchThumbDisabled,
            color,
            switchThumbNormal
        )
    )
    val trstateList = ColorStateList(
        arrayOf(
            intArrayOf(-android.R.attr.state_enabled),
            intArrayOf(android.R.attr.state_checked),
            intArrayOf()
        ),
        intArrayOf(
            ColorUtils.setAlphaComponent(colorForeground, alphaDisabled),
            color,
            colorForeground
        )
    )
    DrawableCompat.setTintList(thumbDrawable, thstateList)
    DrawableCompat.setTintList(trackDrawable, trstateList)
}

fun Button.applyColor(color: Int) {
    val rippleColor = ColorStateList.valueOf(ColorUtils.setAlphaComponent(color, 31))
    background?.let {
        (it as RippleDrawable).setColor(rippleColor)
        DrawableCompat.setTint(background, color)
    }
    val tintList = ColorStateList.valueOf(color)
    if (this is RadioButton) {
        buttonTintList = tintList
    }
}

fun String.toTitleCase(): String = splitToSequence(" ").map {
    it.replaceFirstChar { ch ->
        if (ch.isLowerCase()) ch.titlecase(
            Locale.getDefault()
        ) else ch.toString()
    }
}.joinToString(" ")

inline fun <T> listWhileNotNull(generator: () -> T?): List<T> = mutableListOf<T>().apply {
    while (true) {
        add(generator() ?: break)
    }
}

fun formatTime(calendar: Calendar, context: Context? = null): String {
    return when (context) {
        null -> String.format(
            "%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.HOUR_OF_DAY)
        )
        else -> if (DateFormat.is24HourFormat(context)) String.format(
            "%02d:%02d", calendar.get(
                Calendar.HOUR_OF_DAY
            ), calendar.get(Calendar.MINUTE)
        ) else String.format(
            "%02d:%02d %s",
            if (calendar.get(
                    Calendar.HOUR_OF_DAY
                ) % 12 == 0
            ) 12 else calendar.get(
                Calendar.HOUR_OF_DAY
            ) % 12,
            calendar.get(
                Calendar.MINUTE
            ),
            if (calendar.get(
                    Calendar.HOUR_OF_DAY
                ) < 12
            ) "AM" else "PM"
        )
    }
}

inline val Calendar.hourOfDay get() = get(Calendar.HOUR_OF_DAY)
inline val Calendar.dayOfYear get() = get(Calendar.DAY_OF_YEAR)

fun ViewGroup.getAllChilds() = ArrayList<View>().also { getAllChilds(it) }

fun ViewGroup.getAllChilds(list: MutableList<View>) {
    for (i in (0 until childCount)) {
        val child = getChildAt(i)
        if (child is ViewGroup) {
            child.getAllChilds(list)
        } else {
            list.add(child)
        }
    }
}

fun StatusBarNotification.loadSmallIcon(context: Context): Drawable? {
    return notification.smallIcon?.loadDrawable(context)
}

operator fun PreferenceGroup.get(index: Int): Preference = getPreference(index)
inline fun PreferenceGroup.forEachIndexed(action: (i: Int, pref: Preference) -> Unit) {
    for (i in 0 until preferenceCount) action(i, this[i])
}

val Configuration.usingNightMode get() = uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

fun Int.hasFlags(vararg flags: Int): Boolean {
    return flags.all { hasFlag(it) }
}

infix fun Int.hasFlag(flag: Int) = (this and flag) != 0

fun Int.removeFlag(flag: Int): Int {
    return this and flag.inv()
}

fun Int.addFlag(flag: Int): Int {
    return this or flag
}

fun Int.setFlag(flag: Int, value: Boolean): Int {
    return if (value) {
        addFlag(flag)
    } else {
        removeFlag(flag)
    }
}

fun Context.checkLocationAccess(): Boolean {
    return Utilities.hasPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ||
            Utilities.hasPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
}

inline fun ViewGroup.forEachChildIndexed(action: (View, Int) -> Unit) {
    val count = childCount
    for (i in (0 until count)) {
        action(getChildAt(i), i)
    }
}

inline fun ViewGroup.forEachChild(action: (View) -> Unit) {
    forEachChildIndexed { view, _ -> action(view) }
}

fun View.runOnAttached(runnable: Runnable) {
    if (isAttachedToWindow) {
        runnable.run()
    } else {
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {

            override fun onViewAttachedToWindow(v: View?) {
                runnable.run()
                removeOnAttachStateChangeListener(this)
            }

            override fun onViewDetachedFromWindow(v: View?) {
                removeOnAttachStateChangeListener(this)
            }
        })

    }
}

fun Float.ceilToInt() = ceil(this).toInt()

val Int.luminance get() = ColorUtils.calculateLuminance(this)

val Int.isDark get() = luminance < 0.5f

fun dpToPx(size: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        size,
        Resources.getSystem().displayMetrics
    )
}

fun pxToDp(size: Float): Float {
    return size / dpToPx(1f)
}


fun Context.createDisabledColor(color: Int): ColorStateList {
    return ColorStateList(
        arrayOf(
            intArrayOf(-android.R.attr.state_enabled),
            intArrayOf()
        ),
        intArrayOf(
            getDisabled(getColorAttr(android.R.attr.colorForeground)),
            color
        )
    )
}

@ColorInt
fun Context.getDisabled(inputColor: Int): Int {
    return applyAlphaAttr(android.R.attr.disabledAlpha, inputColor)
}

@ColorInt
fun Context.applyAlphaAttr(attr: Int, inputColor: Int): Int {
    val ta = obtainStyledAttributes(intArrayOf(attr))
    val alpha = ta.getFloat(0, 0f)
    ta.recycle()
    return applyAlpha(alpha, inputColor)
}

@ColorInt
fun applyAlpha(a: Float, inputColor: Int): Int {
    var alpha = a
    alpha *= Color.alpha(inputColor)
    return Color.argb(
        alpha.toInt(), Color.red(inputColor), Color.green(inputColor),
        Color.blue(inputColor)
    )
}

fun JSONObject.asMap() = JSONMap(this)
fun JSONObject.getNullable(key: String): Any? {
    return opt(key)
}

fun String.asNonEmpty(): String? {
    if (TextUtils.isEmpty(this)) return null
    return this
}

fun <E> MutableSet<E>.addOrRemove(obj: E, exists: Boolean): Boolean {
    if (contains(obj) != exists) {
        if (exists) add(obj)
        else remove(obj)
        return true
    }
    return false
}

val Long.Companion.random get() = Random.nextLong()

fun getWindowCornerRadius(context: Context): Float {
    val prefs = Utilities.getOmegaPrefs(context)
    if (prefs.customWindowCorner) {
        return prefs.windowCornerRadius
    }
    return QuickStepContract.getWindowCornerRadius(context.resources)
}

fun supportsRoundedCornersOnWindows(context: Context): Boolean {
    val pref = Utilities.getOmegaPrefs(context)
    if (!Utilities.ATLEAST_R || pref.customWindowCorner) {
        return true
    }
    return QuickStepContract.supportsRoundedCornersOnWindows(context.resources)
}

val ViewGroup.recursiveChildren: Sequence<View>
    get() = children.flatMap {
        if (it is ViewGroup) {
            it.recursiveChildren + sequenceOf(it)
        } else sequenceOf(it)
    }

class KFloatPropertyCompat(private val property: KMutableProperty0<Float>, name: String) :
    FloatPropertyCompat<Any>(name) {

    override fun getValue(`object`: Any) = property.get()

    override fun setValue(`object`: Any, value: Float) {
        property.set(value)
    }
}

class KFloatProperty(private val property: KMutableProperty0<Float>, name: String) :
    Property<Any, Float>(Float::class.java, name) {

    override fun get(`object`: Any) = property.get()

    override fun set(`object`: Any, value: Float) {
        property.set(value)
    }
}

fun <T, U : Comparable<U>> comparing(extractKey: (T) -> U): Comparator<T> {
    return Comparator { o1, o2 -> extractKey(o1).compareTo(extractKey(o2)) }
}

fun <T, U : Comparable<U>> Comparator<T>.then(extractKey: (T) -> U): Comparator<T> {
    return kotlin.Comparator { o1, o2 ->
        val res = compare(o1, o2)
        if (res != 0) res else extractKey(o1).compareTo(extractKey(o2))
    }
}

/*Compose*/
inline fun Modifier.addIf(
    condition: Boolean,
    crossinline factory: Modifier.() -> Modifier
): Modifier =
    if (condition) factory() else this

fun Fragment.recreate() {
    parentFragmentManager
        .beginTransaction()
        .detach(this)
        .commit()
    parentFragmentManager
        .beginTransaction()
        .attach(this)
        .commit()
}

fun Activity.recreateAnimated() = startActivity(
    Intent.makeRestartActivityTask(
        ComponentName(this, this::class.java)
    ), ActivityOptions.makeCustomAnimation(
        this, android.R.anim.fade_in, android.R.anim.fade_out
    ).toBundle()
)

val Context.locale: Locale
    get() = this.resources.configuration.locales[0]

@Suppress("UNCHECKED_CAST")
fun <T> JSONArray.toArrayList(): ArrayList<T> {
    val arrayList = ArrayList<T>()
    for (i in (0 until length())) {
        arrayList.add(get(i) as T)
    }
    return arrayList
}

fun openURLinBrowser(context: Context, url: String?) {
    openURLinBrowser(context, url, null, null)
}

fun openURLinBrowser(context: Context, url: String?, sourceBounds: Rect?, options: Bundle?) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (context !is AppCompatActivity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        intent.sourceBounds = sourceBounds
        if (options == null) {
            context.startActivity(intent)
        } else {
            context.startActivity(intent, options)
        }
    } catch (exc: ActivityNotFoundException) {
        Toast.makeText(context, R.string.error_no_browser, Toast.LENGTH_SHORT).show()
    }
}

fun PackageManager.isAppEnabled(packageName: String?, flags: Int): Boolean {
    return try {
        val info = getApplicationInfo(packageName!!, flags)
        info != null && info.enabled
    } catch (e: NameNotFoundException) {
        false
    }
}

fun Context.shouldUseDrawerSearch(provider: SearchProvider) =
    !Utilities.getOmegaPrefs(this).allAppsGlobalSearch
            || provider is AppsSearchProvider
            || provider is WebSearchProvider