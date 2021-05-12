package com.saggitt.omega.backup

import android.Manifest.permission.*
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.google.android.material.snackbar.Snackbar
import com.saggitt.omega.settings.SettingsBaseActivity
import com.saggitt.omega.settings.SettingsBottomSheet

class BackupListActivity : SettingsBaseActivity(), BackupListAdapter.Callbacks {

    private val permissionRequestReadExternalStorage = 0

    private val recyclerView by lazy { findViewById<RecyclerView>(R.id.recyclerView) }
    private val adapter by lazy { BackupListAdapter(this) }

    private var currentPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup_list)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (!isPermissionGranted(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:" + this.opPackageName)
                startActivity(intent)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(
                    READ_EXTERNAL_STORAGE,
                    WRITE_EXTERNAL_STORAGE,
                    ACCESS_NETWORK_STATE
                ),
                        1337)
            }

        }
        adapter.callbacks = this
        loadLocalBackups()
        recyclerView.layoutManager = GridLayoutManager(this, 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int) = if (position == 0) 2 else 1
            }
        }
        recyclerView.adapter = adapter

        Utilities.checkRestoreSuccess(this)
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
        if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            READ_EXTERNAL_STORAGE)) {
                Snackbar.make(findViewById(android.R.id.content), R.string.read_external_storage_required,
                        Snackbar.LENGTH_SHORT).show()
            }
            ActivityCompat.requestPermissions(this,
                    arrayOf(READ_EXTERNAL_STORAGE),
                    permissionRequestReadExternalStorage)
        } else {
            adapter.setData(OmegaBackup.listLocalBackups(this))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            permissionRequestReadExternalStorage -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    adapter.setData(OmegaBackup.listLocalBackups(this))
                }
            }
            else -> {
            }
        }
    }

    override fun openBackup() {
        openBackupResult.launch(Intent(this, NewBackupActivity::class.java))
    }

    private val openBackupResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val resultData = result.data
                if (resultData!!.data != null) {
                    adapter.addItem(OmegaBackup(this, resultData.data!!))
                    saveChanges()
                }
            }
        }

    override fun openRestore() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = OmegaBackup.MIME_TYPE
        intent.putExtra(Intent.EXTRA_MIME_TYPES, OmegaBackup.EXTRA_MIME_TYPES)

        openRestoreResult.launch(intent)
    }

    private val openRestoreResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val resultData = result.data
                if (resultData!!.data != null) {
                    val takeFlags = intent.flags and
                            (Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    contentResolver.takePersistableUriPermission(resultData.data!!, takeFlags)
                    val uri = resultData.data
                    if (!Utilities.getOmegaPrefs(this).recentBackups.contains(uri!!)) {
                        adapter.addItem(OmegaBackup(this, uri))
                        saveChanges()
                    }
                    openRestore(0)
                }
            }
        }

    override fun openRestore(position: Int) {
        startActivity(Intent(this, RestoreBackupActivity::class.java).apply {
            putExtra(RestoreBackupActivity.EXTRA_URI, adapter[position].uri.toString())
        })
    }

    override fun openEdit(position: Int) {
        currentPosition = position
        val visibility = if (adapter[position].meta != null) View.VISIBLE else View.GONE

        val bottomSheetView = layoutInflater.inflate(R.layout.backup_bottom_sheet,
                findViewById(android.R.id.content), false)
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

        val bottomSheet = SettingsBottomSheet.inflate(this)
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
        shareIntent.type = OmegaBackup.MIME_TYPE
        shareIntent.putExtra(Intent.EXTRA_STREAM, adapter[position].uri)
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareTitle)
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)
        startActivity(Intent.createChooser(shareIntent, shareTitle))
    }

    private fun saveChanges() {
        Utilities.getOmegaPrefs(this).blockingEdit {
            recentBackups.replaceWith(adapter.toUriList())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.onDestroy()
    }
}
