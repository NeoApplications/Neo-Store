package com.machiav3lli.fdroid.ui.components.appsheet

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.machiav3lli.fdroid.ANTIFEATURES_WEBSITE
import com.machiav3lli.fdroid.EXODUS_TRACKER_WEBSITE
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.TC_INTENT_EXTRA_SEARCH
import com.machiav3lli.fdroid.TC_PACKAGENAME
import com.machiav3lli.fdroid.TC_PACKAGENAME_FDROID
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.entity.AntiFeature
import com.machiav3lli.fdroid.data.entity.PermissionGroup
import com.machiav3lli.fdroid.data.entity.SourceInfo
import com.machiav3lli.fdroid.data.entity.TrackersGroup.Companion.getTrackersGroup
import com.machiav3lli.fdroid.data.entity.toAntiFeature
import com.machiav3lli.fdroid.ui.components.ExpandableItemsBlock
import com.machiav3lli.fdroid.ui.components.privacy.PrivacyCard
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowSquareOut
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Download
import com.machiav3lli.fdroid.utils.extension.grantedPermissions
import com.machiav3lli.fdroid.utils.extension.text.nullIfEmpty
import com.machiav3lli.fdroid.utils.getLabelsAndDescriptions
import com.machiav3lli.fdroid.utils.openPermissionPage
import com.machiav3lli.fdroid.utils.privacyPoints
import com.machiav3lli.fdroid.viewmodels.AppSheetVM
import kotlinx.coroutines.Job

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PrivacyPanel(
    modifier: Modifier,
    packageName: String,
    viewModel: AppSheetVM,
    copyLinkToClipboard: (String) -> Job,
    onUriClick: (Uri, Boolean) -> Boolean,
) {
    val context = LocalContext.current
    val panelState by viewModel.privacyPanelState.collectAsState()

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
    ) {
        val requestedPermissions by derivedStateOf {
            try {
                if (panelState.isInstalled) context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_PERMISSIONS
                ).grantedPermissions else emptyMap()
            } catch (_: PackageManager.NameNotFoundException) {
                emptyMap()
            }
        }

        item {
            panelState.privacyData.physicalDataPermissions.let { list ->
                val privacyPoints = list.privacyPoints
                PrivacyCard(
                    heading = stringResource(id = R.string.permission_physical_data) + if (list.isNotEmpty()) " ${
                        pluralStringResource(
                            id = R.plurals.privacy_points_FORMAT,
                            privacyPoints,
                            privacyPoints
                        )
                    }" else "",
                    preExpanded = true,
                    actionText = if (panelState.isInstalled && list.isNotEmpty())
                        stringResource(id = R.string.action_change_permissions)
                    else "",
                    onAction = { context.openPermissionPage(packageName) }
                ) {
                    if (list.isNotEmpty()) {
                        list.forEach { (group, ps) ->
                            ExpandableItemsBlock(
                                heading = stringResource(id = group.labelId),
                                icon = group.icon,
                            ) {
                                val descriptions =
                                    ps.getLabelsAndDescriptions(context)
                                Row(modifier = Modifier.padding(horizontal = 8.dp)) {
                                    Text(
                                        text = ps
                                            .mapIndexed { index, perm ->
                                                "\u2023 ${
                                                    if (panelState.isInstalled) "(${
                                                        stringResource(
                                                            if (requestedPermissions[perm.name] == true) R.string.permission_granted
                                                            else if (perm.name !in requestedPermissions.keys) R.string.permission_not_present
                                                            else R.string.permission_not_granted
                                                        )
                                                    }) " else ""
                                                }${descriptions[index]}"
                                            }
                                            .joinToString(separator = "\n") { it }
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = stringResource(id = R.string.no_permissions_identified))
                        }
                    }
                }
            }
        }
        item {
            panelState.privacyData.identificationDataPermissions.let { list ->
                val privacyPoints = list.privacyPoints
                PrivacyCard(
                    heading = stringResource(id = R.string.permission_identification_data) + if (list.isNotEmpty()) " ${
                        pluralStringResource(
                            id = R.plurals.privacy_points_FORMAT,
                            privacyPoints,
                            privacyPoints
                        )
                    }" else "",
                    preExpanded = true,
                    actionText = if (panelState.isInstalled && list.isNotEmpty())
                        stringResource(id = R.string.action_change_permissions)
                    else "",
                    onAction = { context.openPermissionPage(packageName) }
                ) {
                    if (list.isNotEmpty()) {
                        list.forEach { (group, ps) ->
                            ExpandableItemsBlock(
                                heading = stringResource(id = group.labelId),
                                icon = group.icon,
                            ) {
                                val descriptions =
                                    ps.getLabelsAndDescriptions(context)
                                Row(modifier = Modifier.padding(horizontal = 8.dp)) {
                                    Text(
                                        text = ps
                                            .mapIndexed { index, perm ->
                                                "\u2023 ${
                                                    if (panelState.isInstalled) "(${
                                                        stringResource(
                                                            if (requestedPermissions[perm.name] == true) R.string.permission_granted
                                                            else if (perm.name !in requestedPermissions.keys) R.string.permission_not_present
                                                            else R.string.permission_not_granted
                                                        )
                                                    }) " else ""
                                                }${descriptions[index]}"
                                            }
                                            .joinToString(separator = "\n") { it }
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = stringResource(id = R.string.no_permissions_identified))
                        }
                    }
                }
            }
        }
        if (panelState.privacyData.otherPermissions.isNotEmpty()) {
            item {
                panelState.privacyData.otherPermissions.let { list ->
                    val privacyPoints = list.privacyPoints
                    PrivacyCard(
                        heading = stringResource(id = R.string.permission_other) + if (list.isNotEmpty()) " ${
                            pluralStringResource(
                                id = R.plurals.privacy_points_FORMAT,
                                privacyPoints,
                                privacyPoints
                            )
                        }" else "",
                        preExpanded = false,
                    ) {
                        list[PermissionGroup.Other]?.let { ps ->
                            val descriptions = ps.getLabelsAndDescriptions(context)
                            Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Text(
                                    text = ps
                                        .mapIndexed { index, perm ->
                                            "\u2023 ${
                                                if (panelState.isInstalled) "(${
                                                    stringResource(
                                                        if (requestedPermissions[perm.name] == true) R.string.permission_granted
                                                        else if (perm.name !in requestedPermissions.keys) R.string.permission_not_present
                                                        else R.string.permission_not_granted
                                                    )
                                                }) " else ""
                                            }${descriptions[index]}"
                                        }
                                        .joinToString(separator = "\n") { it }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
        if (Preferences[Preferences.Key.ShowTrackers]) {
            item {
                val tcIntent = context.packageManager
                    .getLaunchIntentForPackage(TC_PACKAGENAME)
                    ?: context.packageManager
                        .getLaunchIntentForPackage(TC_PACKAGENAME_FDROID)
                val privacyPoints = panelState.trackers.privacyPoints
                PrivacyCard(
                    heading = stringResource(
                        id = R.string.trackers_in,
                        panelState.exodusInfo?.version_name.orEmpty()
                    ) + if (panelState.trackers.isNotEmpty()) " ${
                        pluralStringResource(
                            id = R.plurals.privacy_points_FORMAT,
                            privacyPoints,
                            privacyPoints
                        )
                    }" else "",
                    preExpanded = true,
                    actionText = if (panelState.isInstalled) stringResource(
                        id = if (tcIntent == null) R.string.action_install_tc
                        else R.string.action_open_tc
                    ) else "",
                    actionIcon = if (tcIntent == null) Phosphor.Download
                    else Phosphor.ArrowSquareOut,
                    onAction = {
                        if (tcIntent == null) {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    "market://search?q=$TC_PACKAGENAME".toUri()
                                )
                            )
                        } else context.startActivity(
                            tcIntent.putExtra(
                                TC_INTENT_EXTRA_SEARCH,
                                packageName
                            )
                        )
                    }
                ) {
                    if (panelState.trackers.isNotEmpty()) {
                        panelState.trackers
                            .map { it.categories }
                            .flatten()
                            .distinct()
                            .associateWith { group -> panelState.trackers.filter { group in it.categories } }
                            .forEach { (group, groupTrackers) ->
                                val groupItem = group.getTrackersGroup()
                                ExpandableItemsBlock(
                                    heading = stringResource(groupItem.labelId),
                                    icon = groupItem.icon,
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                                        Text(
                                            text = stringResource(groupItem.descriptionId)
                                        )
                                        groupTrackers.forEach {
                                            Text(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .combinedClickable(
                                                        onClick = {
                                                            onUriClick(
                                                                "$EXODUS_TRACKER_WEBSITE${it.key}".toUri(),
                                                                true
                                                            )
                                                        },
                                                        onLongClick = {
                                                            copyLinkToClipboard(it.code_signature)
                                                        }
                                                    ),
                                                text = "\u2023 ${it.name}"
                                            )
                                        }
                                    }
                                }
                            }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(
                                    id =
                                        if (panelState.exodusInfo != null) R.string.trackers_none
                                        else R.string.no_trackers_data_available
                                )
                            )
                        }
                    }
                }
            }
        }
        item {
            PrivacyCard(
                heading = stringResource(id = R.string.source_code),
                preExpanded = true,
            ) {
                (if (panelState.privacyNote.sourceType.open) SourceInfo.Open else SourceInfo.Proprietary).let {
                    ExpandableItemsBlock(
                        heading = stringResource(id = it.labelId),
                        icon = it.icon,
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp)) {
                            Text(
                                text = stringResource(it.descriptionId)
                            )
                        }
                    }
                }
                if (panelState.privacyNote.sourceType.free) SourceInfo.Copyleft.let {
                    ExpandableItemsBlock(
                        heading = stringResource(id = it.labelId),
                        icon = it.icon,
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp)) {
                            Text(
                                text = stringResource(it.descriptionId)
                            )
                        }
                    }
                }
                else SourceInfo.Copyright.let { si ->
                    ExpandableItemsBlock(
                        heading = stringResource(id = si.labelId),
                        icon = si.icon,
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp)) {
                            val dependencyItems = panelState.privacyData.antiFeatures
                                .filter {
                                    it.name == AntiFeature.NON_FREE_UPSTREAM.key ||
                                            it.name == AntiFeature.NON_FREE_NET.key
                                }

                            Text(
                                text = "${stringResource(si.descriptionId)}${
                                    dependencyItems
                                        .map {
                                            it.label.nullIfEmpty()
                                                ?: it.name.toAntiFeature()
                                                    ?.let { stringResource(id = it.titleResId) }
                                                ?: it.name
                                        }
                                        .joinToString { "\n\u2023 $it" }
                                }"
                            )
                        }
                    }
                }
                if (!panelState.privacyNote.sourceType.independent) SourceInfo.Dependency.let { si ->
                    ExpandableItemsBlock(
                        heading = stringResource(id = si.labelId),
                        icon = si.icon,
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp)) {
                            val dependencyItems = panelState.privacyData.antiFeatures
                                .filter {
                                    it.name == AntiFeature.NON_FREE_ASSETS.key ||
                                            it.name == AntiFeature.NON_FREE_DEP.key
                                }

                            Text(
                                text = "${stringResource(si.descriptionId)}${
                                    dependencyItems
                                        .map {
                                            it.label.nullIfEmpty()
                                                ?: it.name.toAntiFeature()
                                                    ?.let { stringResource(id = it.titleResId) }
                                                ?: it.name
                                        }
                                        .joinToString { "\n\u2023 $it" }
                                }"
                            )
                        }
                    }
                }
            }
        }
        if (panelState.privacyData.antiFeatures.isNotEmpty()) {
            item {
                PrivacyCard(
                    heading = stringResource(id = R.string.anti_features),
                    preExpanded = false
                ) {
                    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                        panelState.privacyData.antiFeatures.forEach {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onUriClick(
                                            "$ANTIFEATURES_WEBSITE${it.name}".toUri(),
                                            true
                                        )
                                    },
                                text = "\u2023 ${
                                    it.label.nullIfEmpty()
                                        ?: it.name.toAntiFeature()
                                            ?.let { stringResource(id = it.titleResId) }
                                        ?: it.name
                                }"
                            )
                        }
                    }
                }
            }
        }
    }
}