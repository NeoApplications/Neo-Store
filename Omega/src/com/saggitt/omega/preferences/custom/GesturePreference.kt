package com.saggitt.omega.preferences.custom

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference
import com.android.launcher3.R
import com.saggitt.omega.gestures.BlankGestureHandler
import com.saggitt.omega.gestures.GestureController
import com.saggitt.omega.gestures.NavSwipeUpGesture

class GesturePreference(context: Context, attrs: AttributeSet?) :
    ListPreference(context, attrs) {

    private var isSwipeUp = false
    private val blankGestureHandler = BlankGestureHandler(context, null)

    private val handler
        get() = GestureController.createGestureHandler(
            context,
            value,
            blankGestureHandler
        )

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.GesturePreference)
        when (ta.getString(R.styleable.GesturePreference_gestureClass) ?: "") {
            NavSwipeUpGesture::class.java.name -> isSwipeUp = true
        }
        ta.recycle()

        val gestures = GestureController
            .getGestureHandlers(context, isSwipeUp, true)
        entries = gestures.map { it.displayName }.toTypedArray()
        entryValues = gestures.map { it.toString() }.toTypedArray()
    }

    override fun getSummary() = handler.displayName

    /* TODO implement an onPreferenceChangeListener to manage handler that need extra config
         (and manage saving its config value: maybe just using class for this preference and an extra for config)
    needed methods:
        startActivityForResult(handler.configIntent, requestCode)
        selectedHandler?.onConfigResult(data)
     */
}