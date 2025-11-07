package com.machiav3lli.fdroid.ui.pages

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.machiav3lli.fdroid.BuildConfig
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.content.SAFFile
import com.machiav3lli.fdroid.data.database.entity.Extras
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.data.entity.LinkRef
import com.machiav3lli.fdroid.data.repository.ProductsRepository
import com.machiav3lli.fdroid.ui.components.LinkChip
import com.machiav3lli.fdroid.ui.components.prefs.BasePreference
import com.machiav3lli.fdroid.ui.components.prefs.PreferenceGroup
import com.machiav3lli.fdroid.utils.currentTimestamp
import com.machiav3lli.fdroid.utils.extension.koinNeoViewModel
import com.machiav3lli.fdroid.viewmodels.PrefsVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun PrefsOtherPage(
    viewModel: PrefsVM = koinNeoViewModel(),
    productRepo: ProductsRepository = koinInject(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hidingCounter = rememberSaveable { mutableIntStateOf(0) }
    val pageState by viewModel.otherPrefsState.collectAsState()

    val startExportExtrasResult =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(SAFFile.EXTRAS_MIME_TYPE)) { resultUri ->
            if (resultUri != null) {
                context.contentResolver.takePersistableUriPermission(
                    resultUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                SAFFile.write(
                    context,
                    resultUri,
                    pageState.extras.joinToString(separator = ">") { it.toJSON() }
                )
                // TODO add notification about success or failure
            }
        }
    val startExportReposResult =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(SAFFile.REPOS_MIME_TYPE)) { resultUri ->
            if (resultUri != null) {
                context.contentResolver.takePersistableUriPermission(
                    resultUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                SAFFile.write(
                    context,
                    resultUri,
                    pageState.repos.joinToString(separator = ">") { it.toJSON() }
                )
                // TODO add notification about success or failure
            }
        }
    val startExportInstalledResult =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(SAFFile.APPS_MIME_TYPE)) { resultUri ->
            if (resultUri != null) {
                context.contentResolver.takePersistableUriPermission(
                    resultUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                SAFFile.write(
                    context,
                    resultUri,
                    pageState.installedMap.keys.joinToString(separator = ">")
                )
                // TODO add notification about success or failure
            }
        }
    val startImportExtrasResult =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { resultUri ->
            if (resultUri != null) {
                context.contentResolver.takePersistableUriPermission(
                    resultUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                val content = SAFFile(context, resultUri).read()
                if (content != null) {
                    val extras = content
                        .split(">")
                        .map { Extras.fromJson(it) }
                        .toTypedArray()
                    viewModel.insertExtras(*extras)
                }
                // TODO add notification about success or failure
            }
        }
    val startImportReposResult =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { resultUri ->
            if (resultUri != null) {
                context.contentResolver.takePersistableUriPermission(
                    resultUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                val content = SAFFile(context, resultUri).read()
                if (content != null) {
                    val repos = content
                        .split(">")
                        .map { Repository.fromJson(it) }
                        .toTypedArray()
                    viewModel.insertRepos(*repos)
                }
                // TODO add notification about success or failure
            }
        }
    val startImportInstalledResult =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { resultUri ->
            if (resultUri != null) {
                context.contentResolver.takePersistableUriPermission(
                    resultUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                SAFFile(context, resultUri).read()
                    ?.split(">")
                    ?.filterNot { pageState.installedMap.keys.contains(it) }
                    ?.forEach { packageName ->
                        scope.launch(Dispatchers.IO) {
                            productRepo.loadProduct(packageName)
                                .maxByOrNull { it.product.suggestedVersionCode }?.toItem()?.let {
                                    NeoApp.wm.install(
                                        Pair(it.packageName, it.repositoryId)
                                    )
                                }
                        }
                    }
                // TODO add notification about success or failure
            }
        }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ListItem(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent,
                    ),
                    leadingContent = {
                        ResourcesCompat.getDrawable(
                            LocalContext.current.resources,
                            R.mipmap.ic_launcher,
                            LocalContext.current.theme
                        )?.let { drawable ->
                            val bitmap = Bitmap.createBitmap(
                                drawable.intrinsicWidth,
                                drawable.intrinsicHeight,
                                Bitmap.Config.ARGB_8888
                            )
                            val canvas = Canvas(bitmap)
                            drawable.setBounds(0, 0, canvas.width, canvas.height)
                            drawable.draw(canvas)
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .requiredSize(72.dp)
                                    .clip(MaterialTheme.shapes.large)
                            )
                        }
                    },
                    overlineContent = {
                        Text(
                            text = stringResource(
                                id = R.string.about_build_FORMAT,
                                BuildConfig.VERSION_NAME,
                                BuildConfig.VERSION_CODE,
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.clickable {
                                if (Preferences[Preferences.Key.KidsMode])
                                    hidingCounter.intValue += 1
                                if (hidingCounter.intValue >= 6) {
                                    Preferences[Preferences.Key.KidsMode] = false
                                    hidingCounter.intValue = 0
                                }
                            },
                        )
                    },
                    headlineContent = {
                        Text(
                            text = stringResource(id = R.string.application_name),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                    supportingContent = {
                        Text(
                            text = BuildConfig.APPLICATION_ID,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(8.dp),
                ) {
                    items(LinkRef.entries) { link ->
                        LinkChip(
                            icon = link.icon,
                            label = stringResource(id = link.titleId),
                            url = link.url,
                        )
                    }
                }
            }
        }
        item {
            PreferenceGroup(heading = stringResource(id = R.string.tools)) {
                BasePreference(
                    titleId = R.string.extras_export,
                    index = 0,
                    groupSize = 6,
                    onClick = {
                        startExportExtrasResult
                            .launch("NS_$currentTimestamp.${SAFFile.EXTRAS_EXTENSION}")
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                BasePreference(
                    titleId = R.string.extras_import,
                    index = 1,
                    groupSize = 6,
                    onClick = {
                        startImportExtrasResult.launch(SAFFile.EXTRAS_MIME_ARRAY)
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                BasePreference(
                    titleId = R.string.repos_export,
                    index = 2,
                    groupSize = 6,
                    onClick = {
                        startExportReposResult
                            .launch("NS_$currentTimestamp.${SAFFile.REPOS_EXTENSION}")
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                BasePreference(
                    titleId = R.string.repos_import,
                    index = 3,
                    groupSize = 6,
                    onClick = {
                        startImportReposResult.launch(SAFFile.REPOS_MIME_ARRAY)
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                BasePreference(
                    titleId = R.string.installed_export,
                    index = 4,
                    groupSize = 6,
                    onClick = {
                        startExportInstalledResult
                            .launch("NS_$currentTimestamp.${SAFFile.APPS_EXTENSION}")
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                BasePreference(
                    titleId = R.string.installed_import,
                    index = 5,
                    groupSize = 6,
                    onClick = {
                        startImportInstalledResult.launch(SAFFile.APPS_MIME_ARRAY)
                    }
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
