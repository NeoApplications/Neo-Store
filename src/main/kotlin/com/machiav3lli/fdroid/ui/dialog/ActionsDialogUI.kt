package com.machiav3lli.fdroid.ui.dialog

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.database.entity.Release
import com.machiav3lli.fdroid.data.entity.DialogKey
import com.machiav3lli.fdroid.ui.components.ActionButton
import com.machiav3lli.fdroid.ui.components.FlatActionButton
import com.machiav3lli.fdroid.utils.extension.android.Android

@Composable
fun ActionsDialogUI(
    titleText: String,
    messageText: String,
    primaryText: String,
    primaryIcon: ImageVector? = null,
    primaryAction: (() -> Unit) = {},
    secondaryText: String = "",
    secondaryIcon: ImageVector? = null,
    secondaryAction: (() -> Unit)? = null,
    @StringRes dismissTextId: Int = R.string.cancel,
    onDismiss: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = titleText, style = MaterialTheme.typography.titleLarge)
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .fillMaxWidth()
                    .weight(1f, false)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = messageText,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }

            Row(
                Modifier.fillMaxWidth()
            ) {
                FlatActionButton(text = stringResource(id = dismissTextId), onClick = onDismiss)
                Spacer(Modifier.weight(1f))
                if (secondaryAction != null && secondaryText.isNotEmpty()) {
                    ActionButton(
                        text = secondaryText,
                        icon = secondaryIcon,
                        positive = false
                    ) {
                        secondaryAction()
                        onDismiss()
                    }
                    Spacer(Modifier.requiredWidth(8.dp))
                }
                ActionButton(
                    text = primaryText,
                    icon = primaryIcon,
                ) {
                    primaryAction()
                    onDismiss()
                }
            }
        }
    }
}

@Composable
fun KeyDialogUI(
    key: DialogKey?,
    openDialog: MutableState<Boolean>,
    primaryAction: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    ActionsDialogUI(
        titleText = when (key) {
            is DialogKey.ReleaseIncompatible,
            is DialogKey.ReleaseIssue,
                 -> stringResource(id = R.string.incompatible_version)

            is DialogKey.Action,
            is DialogKey.BatchDownload,
            is DialogKey.Link,
                 -> stringResource(id = R.string.confirmation)

            else -> ""
        }.toString(),
        messageText = when (key) {
            is DialogKey.ReleaseIssue        -> stringResource(id = key.resId)
            is DialogKey.Link                -> stringResource(
                id = R.string.open_DESC_FORMAT,
                key.uri
            )

            is DialogKey.Uninstall           -> stringResource(
                id = R.string.confirm_uninstall_DESC_FORMAT,
                key.label
            )

            is DialogKey.Download            -> stringResource(
                id = R.string.confirm_download_DESC_FORMAT,
                key.label
            )

            is DialogKey.BatchDownload       -> stringResource(
                id = R.string.confirm_download_DESC_FORMAT,
                key.labels.joinToString()
            )

            is DialogKey.ReleaseIncompatible -> {
                val builder = StringBuilder()
                val minSdkVersion = if (Release.Incompatibility.MinSdk in key.incompatibilities)
                    key.minSdkVersion else null
                val maxSdkVersion = if (Release.Incompatibility.MaxSdk in key.incompatibilities)
                    key.maxSdkVersion else null
                if (minSdkVersion != null || maxSdkVersion != null) {
                    val versionMessage = minSdkVersion?.let {
                        stringResource(
                            R.string.incompatible_api_min_DESC_FORMAT,
                            it
                        )
                    } ?: maxSdkVersion?.let {
                        stringResource(
                            R.string.incompatible_api_max_DESC_FORMAT,
                            it
                        )
                    }
                    builder.append(
                        stringResource(
                            R.string.incompatible_api_DESC_FORMAT,
                            Android.name, Android.sdk, versionMessage.orEmpty()
                        )
                    ).append("\n\n")
                }
                if (Release.Incompatibility.Platform in key.incompatibilities) {
                    builder.append(
                        stringResource(
                            R.string.incompatible_platforms_DESC_FORMAT,
                            Android.primaryPlatform ?: stringResource(R.string.unknown),
                            key.platforms.joinToString(separator = ", ")
                        )
                    ).append("\n\n")
                }
                val features = key.incompatibilities
                    .mapNotNull { it as? Release.Incompatibility.Feature }
                if (features.isNotEmpty()) {
                    builder.append(stringResource(R.string.incompatible_features_DESC))
                    for (feature in features) {
                        builder.append("\n\u2022 ").append(feature.feature)
                    }
                    builder.append("\n\n")
                }
                if (builder.isNotEmpty()) {
                    builder.delete(builder.length - 2, builder.length)
                }
                builder.toString()
            }

            else                             -> ""
        }.toString(),
        onDismiss = { openDialog.value = false },
        primaryText = when (key) {
            is DialogKey.ReleaseIssue,
            is DialogKey.ReleaseIncompatible,
            is DialogKey.Link,
            is DialogKey.Action,
            is DialogKey.BatchDownload,
                 -> stringResource(id = R.string.ok)

            else -> ""
        },
        primaryAction = primaryAction,
        secondaryAction = onDismiss,
    )
}