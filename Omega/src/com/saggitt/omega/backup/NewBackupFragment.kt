/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2022   Omega Launcher Team
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

package com.saggitt.omega.backup

import android.Manifest
import android.app.Activity
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.databinding.FragmentBackupNewBinding
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class NewBackupFragment : Fragment() {
    private lateinit var binding: FragmentBackupNewBinding
    private val backupTask: BackupTaskViewModel by lazy { ViewModelProvider(this)[BackupTaskViewModel::class.java] }
    private val perms by lazy {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    private var backupUri = Uri.parse("/")
    private var inProgress = false
        set(value) {
            if (value) {
                requireActivity().actionBar?.setDisplayShowHomeEnabled(false)
                requireActivity().actionBar?.setDisplayHomeAsUpEnabled(false)
            } else {
                requireActivity().actionBar?.setDisplayHomeAsUpEnabled(true)
            }
            field = value
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBackupNewBinding.inflate(inflater, container, false)
        requireActivity().actionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.name.setText(getTimestamp())

        val color = ColorStateList.valueOf(Utilities.getOmegaPrefs(requireContext()).accentColor)
        binding.createButton.backgroundTintList = color
        binding.createButton.setOnClickListener {
            onStartBackup()
        }

        if (ContextCompat.getSystemService(
                requireContext(),
                WallpaperManager::class.java
            )?.wallpaperInfo != null
        ) {
            binding.contentWallpaper.isChecked = false
            binding.contentWallpaper.isEnabled = false
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().title = requireActivity().getString(R.string.backup_create_new)
    }

    override fun onDestroyView() {
        requireActivity().actionBar?.setDisplayHomeAsUpEnabled(false)
        setHasOptionsMenu(false)
        super.onDestroyView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                requireActivity().onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onStartBackup() {
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

        } else {
            val error = validateOptions()
            if (error == 0) {
                val fileName = "${binding.name.text}.${BackupFile.EXTENSION}"
                if (binding.locationDevice.isChecked) {
                    backupUri =
                        Uri.fromFile(File(BackupFile.getFolder(requireContext()), fileName))
                    startBackup()
                } else {
                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = BackupFile.MIME_TYPE
                    intent.putExtra(Intent.EXTRA_TITLE, fileName)

                    startBackupResult.launch(intent)
                }
            } else {
                Snackbar.make(
                    requireView().findViewById(R.id.content),
                    error,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun isPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private val startBackupResult =
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
                    backupUri = resultData.data
                    startBackup()
                }
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

    private fun startBackup() {
        backupTask.execute(
            onPreExecute = {
                binding.config.visibility = View.GONE
                binding.createButton.visibility = View.GONE
                binding.progress.visibility = View.VISIBLE
                inProgress = true
            },
            doInBackground = {
                var contents = 0
                if (binding.contentHomescreen.isChecked) {
                    contents = contents or BackupFile.INCLUDE_HOME_SCREEN
                }
                if (binding.contentSettings.isChecked) {
                    contents = contents or BackupFile.INCLUDE_SETTINGS
                }
                if (binding.contentWallpaper.isChecked) {
                    contents = contents or BackupFile.INCLUDE_WALLPAPER
                }
                BackupFile.create(
                    context = requireContext(),
                    name = binding.name.text.toString(),
                    location = backupUri,
                    contents = contents
                )
            },
            onPostExecute = {
                if (it) {
                    inProgress = false
                    binding.progress.visibility = View.GONE
                    requireActivity().supportFragmentManager.popBackStack()
                } else {
                    inProgress = false
                    binding.progress.visibility = View.GONE
                    Snackbar.make(
                        requireView().findViewById(R.id.content),
                        R.string.backup_failed,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun validateOptions(): Int {
        return if (binding.name.text == null || binding.name.text.toString() == "") {
            R.string.backup_error_blank_name
        } else if (!binding.contentHomescreen.isChecked && !binding.contentSettings.isChecked && !binding.contentWallpaper.isChecked) {
            R.string.backup_error_blank_contents
        } else {
            0
        }
    }

    private fun getTimestamp(): String {
        val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.US)
        return simpleDateFormat.format(Date())
    }
}
