package com.machiav3lli.fdroid.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class FullscreenBottomSheetDialogFragment(private val expanded: Boolean = true) :
    BottomSheetDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val sheet = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        if (expanded) sheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        else sheet.behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        return sheet
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLayout()
    }

    protected abstract fun setupLayout()
    protected abstract fun updateSheet()
}
