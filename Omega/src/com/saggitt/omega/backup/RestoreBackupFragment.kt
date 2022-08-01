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

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.databinding.FragmentBackupRestoreBinding
import com.google.android.material.snackbar.Snackbar
import com.saggitt.omega.preferences.OmegaPreferences

class RestoreBackupFragment : Fragment(), BackupFile.MetaLoader.Callback {

    private lateinit var binding: FragmentBackupRestoreBinding
    private val restoreTask: BackupTaskViewModel by lazy { ViewModelProvider(this)[BackupTaskViewModel::class.java] }
    private lateinit var mUriString: String
    private var mRestoreState = false
    private lateinit var prefs: OmegaPreferences
    private val backup by lazy { BackupFile(requireContext(), Uri.parse(mUriString)) }

    private val backupMetaLoader by lazy { BackupFile.MetaLoader(backup) }

    private var fromExternal = false

    private var inProgress = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBackupRestoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = Utilities.getOmegaPrefs(requireContext())
        val color = ColorStateList.valueOf(prefs.themeAccentColor.onGetValue())

        binding.restoreButton.backgroundTintList = color
        binding.restoreButton.setOnClickListener {
            startRestore()
        }

        if (mRestoreState) {
            showMessage(R.drawable.ic_close, R.string.restore_success)
            requireActivity().supportFragmentManager.popBackStack()
        } else {
            loadMeta()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mUriString = requireArguments().getString(EXTRA_URI).toString()
        mRestoreState = requireArguments().getBoolean(EXTRA_SUCCESS)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().title = requireActivity().getString(R.string.backup_restoring)
    }

    private fun loadMeta() {
        backupMetaLoader.callback = this
        backupMetaLoader.loadMeta()

        binding.config.visibility = View.GONE
        binding.restoreButton.visibility = View.GONE
        binding.progress.visibility = View.VISIBLE
        binding.progressText.visibility = View.GONE
    }

    override fun onMetaLoaded() {
        binding.config.visibility = View.VISIBLE
        binding.restoreButton.visibility = View.VISIBLE
        binding.progress.visibility = View.GONE
        backupMetaLoader.callback = null
        if (backup.meta != null) {
            binding.name.setText(backup.meta?.name)
            binding.timestamp.setText(backup.meta?.timestamp)
            val contents = backup.meta!!.contents
            val includeHomeScreen = contents and BackupFile.INCLUDE_HOME_SCREEN != 0
            binding.contentHomescreen.isEnabled = includeHomeScreen
            binding.contentHomescreen.isChecked = includeHomeScreen
            val includeSettings = contents and BackupFile.INCLUDE_SETTINGS != 0
            binding.contentSettings.isEnabled = includeSettings
            binding.contentSettings.isChecked = includeSettings
            val includeWallpaper = contents and BackupFile.INCLUDE_WALLPAPER != 0
            binding.contentWallpaper.isEnabled = includeWallpaper
            binding.contentWallpaper.isChecked = includeWallpaper
        } else {
            showMessage(R.drawable.ic_close, R.string.restore_read_meta_fail)
        }
    }

    private fun validateOptions(): Int {
        return if (binding.name.text == null || binding.name.text.toString() == "") {
            R.string.backup_error_blank_name
        } else if (
            !binding.contentHomescreen.isChecked
            && !binding.contentSettings.isChecked
            && !binding.contentWallpaper.isChecked
        ) {
            R.string.backup_error_blank_contents
        } else {
            0
        }
    }

    private fun startRestore() {
        val error = validateOptions()
        if (error == 0) {
            restoreTask.execute(
                onPreExecute = {
                    binding.config.visibility = View.GONE
                    binding.restoreButton.visibility = View.GONE

                    binding.progress.visibility = View.VISIBLE
                    binding.progressText.visibility = View.VISIBLE

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
                    if (backup.restore(contents)) contents else -1
                },
                onPostExecute = {
                    if (it > -1) {
                        binding.progressText.text = getString(R.string.backup_restarting)

                        if (it and BackupFile.INCLUDE_SETTINGS == 0) {
                            prefs.blockingEdit {
                                restoreSuccess = true
                            }
                        }

                        Handler(Looper.getMainLooper()).postDelayed({
                            if (fromExternal) {
                                val intent = Intent(
                                    requireContext(),
                                    RestoreBackupFragment::class.java
                                ).putExtra(EXTRA_SUCCESS, true)
                                startActivity(intent)
                            }
                            Utilities.killLauncher()
                        }, 500)
                    } else {
                        inProgress = false
                        showMessage(R.drawable.ic_close, R.string.restore_failed)
                    }
                }
            )
        } else {
            Snackbar.make(requireView().findViewById(R.id.content), error, Snackbar.LENGTH_SHORT)
                .show()
        }
    }

    private fun showMessage(icon: Int, text: Int) {
        binding.config.visibility = View.GONE
        binding.restoreButton.visibility = View.GONE
        binding.progress.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        binding.progressText.visibility = View.VISIBLE
        binding.successIcon.visibility = View.VISIBLE
        binding.successIcon.setImageDrawable(AppCompatResources.getDrawable(requireContext(), icon))
        binding.progressText.setText(text)
    }

    companion object {
        const val EXTRA_URI = "uri"
        const val EXTRA_SUCCESS = "success"
    }
}
