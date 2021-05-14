package com.saggitt.omega.backup

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.saggitt.omega.settings.SettingsBaseActivity
import com.saggitt.omega.util.applyColor

class RestoreBackupActivity : SettingsBaseActivity(), OmegaBackup.MetaLoader.Callback {

    private val backupName by lazy { findViewById<EditText>(R.id.name) }
    private val backupTimestamp by lazy { findViewById<EditText>(R.id.timestamp) }

    private val backupHomescreen by lazy { findViewById<CheckBox>(R.id.content_homescreen) }
    private val backupSettings by lazy { findViewById<CheckBox>(R.id.content_settings) }
    private val backupWallpaper by lazy { findViewById<CheckBox>(R.id.content_wallpaper) }

    private val backup by lazy {
        if (intent.hasExtra(EXTRA_URI))
            OmegaBackup(this, Uri.parse(intent.getStringExtra(EXTRA_URI)))
        else
            OmegaBackup(this, intent.data!!)
    }

    private val backupMetaLoader by lazy { OmegaBackup.MetaLoader(backup) }

    private val config by lazy { findViewById<View>(R.id.config) }
    private val startButton by lazy { findViewById<FloatingActionButton>(R.id.fab) }
    private val progress by lazy { findViewById<View>(R.id.progress) }
    private val progressBar by lazy { findViewById<View>(R.id.progressBar) }
    private val progressText by lazy { findViewById<TextView>(R.id.progress_text) }
    private val successIcon by lazy { findViewById<ImageView>(R.id.success_icon) }

    private var fromExternal = false

    private var inProgress = false
        set(value) {
            if (value) {
                supportActionBar?.setDisplayShowHomeEnabled(false)
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
            } else {
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            }
            field = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restore_backup)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        when {
            intent.hasExtra(EXTRA_URI) -> {
            }
            intent.data != null -> {
                fromExternal = true
            }
            intent.hasExtra(EXTRA_SUCCESS) -> {
                inProgress = true
                showMessage(R.drawable.ic_check, R.string.restore_success)
                Utilities.getOmegaPrefs(this).blockingEdit { restoreSuccess = false }
                Handler(Looper.getMainLooper()).postDelayed({ finish() }, 2000)
                return
            }
            else -> {
                finish()
                return
            }
        }

        startButton.setOnClickListener {
            startRestore()
        }
        startButton.applyColor(Utilities.getOmegaPrefs(this).accentColor)

        loadMeta()
    }

    private fun loadMeta() {
        backupMetaLoader.callback = this
        backupMetaLoader.loadMeta()

        config.visibility = View.GONE
        startButton.visibility = View.GONE
        progress.visibility = View.VISIBLE
        progressText.visibility = View.GONE
    }

    override fun onMetaLoaded() {
        config.visibility = View.VISIBLE
        startButton.visibility = View.VISIBLE
        progress.visibility = View.GONE
        backupMetaLoader.callback = null
        if (backup.meta != null) {
            backupName.setText(backup.meta?.name)
            backupTimestamp.setText(backup.meta?.timestamp)
            val contents = backup.meta!!.contents
            val includeHomescreen = contents and OmegaBackup.INCLUDE_HOME_SCREEN != 0
            backupHomescreen.isEnabled = includeHomescreen
            backupHomescreen.isChecked = includeHomescreen
            val includeSettings = contents and OmegaBackup.INCLUDE_SETTINGS != 0
            backupSettings.isEnabled = includeSettings
            backupSettings.isChecked = includeSettings
            val includeWallpaper = contents and OmegaBackup.INCLUDE_WALLPAPER != 0
            backupWallpaper.isEnabled = includeWallpaper
            backupWallpaper.isChecked = includeWallpaper
        } else {
            showMessage(R.drawable.ic_close, R.string.restore_read_meta_fail)
        }
    }

    private fun validateOptions(): Int {
        return if (backupName.text == null || backupName.text.toString() == "") {
            R.string.backup_error_blank_name
        } else if (!backupHomescreen.isChecked && !backupSettings.isChecked && !backupWallpaper.isChecked) {
            R.string.backup_error_blank_contents
        } else {
            0
        }
    }

    private fun startRestore() {
        val error = validateOptions()
        if (error == 0) {
            RestoreBackupTask().execute()
        } else {
            Snackbar.make(findViewById(R.id.content), error, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        if (!inProgress) super.onBackPressed()
    }

    @SuppressLint("StaticFieldLeak")
    private inner class RestoreBackupTask() : AsyncTask<Void, Void, Int>() {

        override fun onPreExecute() {
            super.onPreExecute()

            config.visibility = View.GONE
            startButton.visibility = View.GONE

            progress.visibility = View.VISIBLE
            progressText.visibility = View.VISIBLE

            inProgress = true
        }

        override fun doInBackground(vararg params: Void?): Int {
            var contents = 0
            if (backupHomescreen.isChecked) {
                contents = contents or OmegaBackup.INCLUDE_HOME_SCREEN
            }
            if (backupSettings.isChecked) {
                contents = contents or OmegaBackup.INCLUDE_SETTINGS
            }
            if (backupWallpaper.isChecked) {
                contents = contents or OmegaBackup.INCLUDE_WALLPAPER
            }
            return if (backup.restore(contents)) contents else -1
        }

        override fun onPostExecute(result: Int) {
            super.onPostExecute(result)

            if (result > -1) {
                progressText.text = getString(R.string.backup_restarting)

                if (result and OmegaBackup.INCLUDE_SETTINGS == 0) {
                    Utilities.getOmegaPrefs(this@RestoreBackupActivity).blockingEdit {
                        restoreSuccess = true
                    }
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    if (fromExternal) {
                        val intent = Intent(
                            this@RestoreBackupActivity,
                            RestoreBackupActivity::class.java
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

    }

    private fun showMessage(icon: Int, text: Int) {
        config.visibility = View.GONE
        startButton.visibility = View.GONE
        progress.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        progressText.visibility = View.VISIBLE
        successIcon.visibility = View.VISIBLE
        successIcon.setImageDrawable(AppCompatResources.getDrawable(this, icon))
        progressText.setText(text)
    }

    companion object {
        const val EXTRA_URI = "uri"
        const val EXTRA_SUCCESS = "success"
    }
}
