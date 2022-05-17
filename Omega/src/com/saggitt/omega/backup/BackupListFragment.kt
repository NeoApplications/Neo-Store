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

package com.saggitt.omega.backup

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.databinding.FragmentBackupListBinding
import com.google.android.material.snackbar.Snackbar
import com.saggitt.omega.preferences.OmegaPreferences
import com.saggitt.omega.views.SettingsBottomSheet

class BackupListFragment : Fragment(), BackupListAdapter.Callbacks {

    private lateinit var binding: FragmentBackupListBinding
    private lateinit var prefs: OmegaPreferences
    private val adapter by lazy { BackupListAdapter(requireActivity()) }

    private val perms by lazy {
        arrayOf(
            READ_EXTERNAL_STORAGE,
            WRITE_EXTERNAL_STORAGE
        )
    }
    private var currentPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBackupListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = Utilities.getOmegaPrefs(requireContext())
        onRecyclerViewCreated(binding.recyclerView)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().title = requireActivity().getString(R.string.backups)
    }

    private fun onRecyclerViewCreated(recyclerView: RecyclerView) {
        if (!isPermissionGranted(requireActivity())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                val intent = Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    uri
                )
                startActivity(intent)
            } else {
                permissionRequestLauncher.launch(perms)
            }

        }
        adapter.callbacks = this
        loadLocalBackups()

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int) = if (position == 0) 2 else 1
            }
        }

        recyclerView.adapter = adapter
        (recyclerView.itemAnimator as? DefaultItemAnimator)?.supportsChangeAnimations = true
        checkRestoreSuccess()
    }

    private fun checkRestoreSuccess() {
        if (prefs.restoreSuccess) {
            prefs.restoreSuccess = false
            val fragment = RestoreBackupFragment()
            val bundle = Bundle(2)
            bundle.putBoolean(RestoreBackupFragment.EXTRA_SUCCESS, true)
            fragment.arguments = bundle

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(BackupListAdapter.toString())
                .commit()
        }
    }

    private val permissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value
            }
            if (!granted) {
                Snackbar.make(requireView(), R.string.permission_denied, Snackbar.LENGTH_LONG)
                    .show()
            }
        }

    private fun isPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                context,
                WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun loadLocalBackups() {
        if (ContextCompat.checkSelfPermission(requireContext(), READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            readPermissionRequest.launch(READ_EXTERNAL_STORAGE)
        } else {
            adapter.setData(BackupFile.listLocalBackups(requireContext()))
        }
    }

    private val readPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                loadLocalBackups()
            } else {
                Snackbar.make(requireView(), R.string.permission_denied, Snackbar.LENGTH_LONG)
                    .show()
            }
        }

    override fun openBackup() {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, NewBackupFragment())
            .addToBackStack(BackupListAdapter.toString())
            .commit()
    }

    override fun openRestore() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = BackupFile.MIME_TYPE
        intent.putExtra(Intent.EXTRA_MIME_TYPES, BackupFile.EXTRA_MIME_TYPES)

        openRestoreResult.launch(intent)
    }

    private val openRestoreResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val resultData = result.data
                if (resultData!!.data != null) {
                    val takeFlags = requireActivity().intent.flags and
                            (Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    requireActivity().contentResolver.takePersistableUriPermission(
                        resultData.data!!,
                        takeFlags
                    )
                    val uri = resultData.data
                    if (!prefs.recentBackups.contains(uri!!) ||
                        !adapter.toUriList().contains(uri)
                    ) {
                        adapter.addItem(BackupFile(requireContext(), uri))
                        saveChanges()
                    }
                    openRestore(0)
                }
            }
        }

    override fun openRestore(position: Int) {
        val fragment = RestoreBackupFragment().apply {
            val bundle = Bundle()
            bundle.putString(RestoreBackupFragment.EXTRA_URI, adapter[position].uri.toString())
            arguments = bundle
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(BackupListAdapter.toString())
            .commit()
    }

    override fun openEdit(position: Int) {
        currentPosition = position
        val visibility = if (adapter[position].meta != null) View.VISIBLE else View.GONE

        val bottomSheetView = layoutInflater.inflate(
            R.layout.backup_bottom_sheet,
            requireView().findViewById(android.R.id.content), false
        )
        bottomSheetView.findViewById<TextView>(android.R.id.title).text =
            adapter[position].meta?.name ?: getString(R.string.backup_invalid)
        bottomSheetView.findViewById<TextView>(android.R.id.summary).text =
            adapter[position].meta?.localizedTimestamp ?: getString(R.string.backup_invalid)

        val restoreBackup = bottomSheetView.findViewById<View>(R.id.action_restore_backup)
        val shareBackup = bottomSheetView.findViewById<View>(R.id.action_share_backup)
        val removeBackup = bottomSheetView.findViewById<View>(R.id.action_remove_backup_from_list)
        val divider = bottomSheetView.findViewById<View>(R.id.divider)
        restoreBackup.visibility = visibility
        shareBackup.visibility = visibility
        divider.visibility = visibility

        val bottomSheet = SettingsBottomSheet.inflate(requireContext())
        restoreBackup.setOnClickListener {
            bottomSheet.close(true)
            openRestore(currentPosition)
        }
        shareBackup.setOnClickListener {
            bottomSheet.close(true)
            shareBackup(currentPosition)
        }
        removeBackup.setOnClickListener {
            bottomSheet.close(true)
            removeItem(currentPosition)
        }
        bottomSheet.show(bottomSheetView, true)
    }

    private fun removeItem(position: Int) {
        adapter.removeItem(position)
        saveChanges()
    }

    private fun shareBackup(position: Int) {
        val shareTitle = getString(R.string.backup_share_title)
        val shareText = getString(R.string.backup_share_text)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = BackupFile.MIME_TYPE
        shareIntent.putExtra(Intent.EXTRA_STREAM, adapter[position].uri)
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareTitle)
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)
        startActivity(Intent.createChooser(shareIntent, shareTitle))
    }

    private fun saveChanges() {
        prefs.blockingEdit {
            recentBackups.replaceWith(adapter.toUriList())
        }
    }
}
