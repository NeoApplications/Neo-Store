/*
 * This file is part of Omega Launcher
 * Copyright (c) 2022   Omega Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.iconpack

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Handler
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import com.android.launcher3.*
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.util.Executors.MODEL_EXECUTOR
import com.android.launcher3.views.ActivityContext
import com.android.launcher3.views.BaseDragLayer
import com.saggitt.omega.theme.ThemeOverride
import com.saggitt.omega.util.Config
import com.saggitt.omega.util.runOnMainThread
import com.saggitt.omega.util.runOnThread
import com.saggitt.omega.wallpaper.WallpaperPreviewProvider

class IconPackPreview @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(PreviewContext(context), attrs, defStyleAttr), WorkspaceLayoutManager,
        InvariantDeviceProfile.OnIDPChangeListener, ViewTreeObserver.OnScrollChangedListener {
    private val wallpaper: Drawable
    private val viewLocation = IntArray(2)

    private val previewContext = this.context as PreviewContext
    private val previewApps = Config.getPreviewAppInfos(context)
    private var iconsLoaded = false
    private val idp = previewContext.idp
    private val homeElementInflater = LayoutInflater.from(ContextThemeWrapper(previewContext, R.style.HomeScreenElementTheme))
    private lateinit var workspace: CellLayout
    private val columns = idp.numColumns

    init {
        orientation = HORIZONTAL
        wallpaper = WallpaperPreviewProvider.getInstance(context).wallpaper

        runOnThread(Handler(MODEL_EXECUTOR.looper)) {
            val iconCache = LauncherAppState.getInstance(context).iconCache
            previewApps.forEach { iconCache.getTitleAndIcon(it, false) }
            iconsLoaded = true
            runOnMainThread(::populatePreview)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        workspace = findViewById(R.id.workspace)

        populatePreview()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        idp.addOnChangeListener(this)

        viewTreeObserver.addOnScrollChangedListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        idp.removeOnChangeListener(this)

        viewTreeObserver.removeOnScrollChangedListener(this)
    }

    override fun dispatchDraw(canvas: Canvas) {
        val width = wallpaper.intrinsicWidth
        val height = wallpaper.intrinsicHeight
        if (width == 0 || height == 0) {
            super.dispatchDraw(canvas)
            return
        }
        getLocationInWindow(viewLocation)
        val dm = resources.displayMetrics
        val scaleX = dm.widthPixels.toFloat() / width
        val scaleY = dm.heightPixels.toFloat() / height
        val scale = scaleX.coerceAtLeast(scaleY)
        canvas.save()
        canvas.translate(0f, (-viewLocation[1]).toFloat())
        canvas.scale(scale, scale)
        wallpaper.setBounds(0, 0, width, height)
        wallpaper.draw(canvas)
        canvas.restore()
        super.dispatchDraw(canvas)
    }

    override fun onIdpChanged(profile: InvariantDeviceProfile?) {
        populatePreview()
    }

    private fun populatePreview() {
        val dp = idp.getDeviceProfile(previewContext)
        val leftPadding = dp.workspacePadding.left + dp.workspaceCellPaddingXPx
        val rightPadding = dp.workspacePadding.right + dp.workspaceCellPaddingXPx
        val verticalPadding = (leftPadding + rightPadding) / 2 + dp.iconDrawablePaddingPx

        if (!iconsLoaded || !isAttachedToWindow) return

        workspace.removeAllViews()
        workspace.setGridSize(columns, 2)
        workspace.setPadding(
            leftPadding,
            verticalPadding,
            rightPadding,
            verticalPadding
        )

        val apps = columns * 2
        var cellY = 0
        var cellX: Int

        previewApps.take(apps).forEachIndexed { index, info ->
            if ((index + 1) > columns) {
                cellY = 1
                cellX = index - columns
            } else {
                cellX = index
            }

            info.container = LauncherSettings.Favorites.CONTAINER_DESKTOP
            info.screenId = 0
            info.cellX = cellX
            info.cellY = cellY

            inflateAndAddIcon(info)
        }
    }

    private fun inflateAndAddIcon(info: AppInfo) {
        val icon = homeElementInflater.inflate(
                R.layout.app_icon, workspace, false) as BubbleTextView
        icon.applyFromApplicationInfo(info)
        addInScreenFromBind(icon, info)
    }

    override fun getScreenWithId(screenId: Int) = workspace

    override fun getHotseat() = null

    override fun onScrollChanged() {
        invalidate()
    }

    class PreviewContext(base: Context) : ContextThemeWrapper(
        base, ThemeOverride.Launcher().getTheme(base)
    ), ActivityContext {

        val idp = LauncherAppState.getIDP(this)!!
        val dp get() = idp.getDeviceProfile(this)!!

        override fun getDeviceProfile(): DeviceProfile {
            return dp
        }

        override fun getDragLayer(): BaseDragLayer<*> {
            throw UnsupportedOperationException()
        }
    }
}