package com.machiav3lli.fdroid.ui.dialog

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.utils.startLauncherActivity

class LaunchDialog(val packageName: String) : DialogFragment() { // TODO replace with composable
    companion object {
        private const val EXTRA_NAMES = "names"
        private const val EXTRA_LABELS = "labels"
    }

    constructor(packageName: String, launcherActivities: List<Pair<String, String>>) : this(
        packageName
    ) {
        arguments = Bundle().apply {
            putStringArrayList(EXTRA_NAMES, ArrayList(launcherActivities.map { it.first }))
            putStringArrayList(EXTRA_LABELS, ArrayList(launcherActivities.map { it.second }))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val names = requireArguments().getStringArrayList(EXTRA_NAMES)!!
        val labels = requireArguments().getStringArrayList(EXTRA_LABELS)!!
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.launch)
            .setItems(labels.toTypedArray()) { _, position ->
                requireContext().startLauncherActivity(packageName, names[position])
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
    }
}