package com.looker.droidify.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class FullscreenBottomSheetDialogFragment() : BottomSheetDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val sheet = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        sheet.setOnShowListener {
            val parentLayout =
                sheet.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { view ->
                val behaviour = BottomSheetBehavior.from(view)
                val layoutParams = view.layoutParams
                layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
                view.layoutParams = layoutParams
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return sheet
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLayout()
    }

    protected abstract fun setupLayout()
    protected abstract fun updateSheet()
}
