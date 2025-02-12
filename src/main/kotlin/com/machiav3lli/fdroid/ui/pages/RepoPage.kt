package com.machiav3lli.fdroid.ui.pages

import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.manager.service.worker.SyncWorker
import com.machiav3lli.fdroid.ui.components.ActionButton
import com.machiav3lli.fdroid.ui.components.BlockText
import com.machiav3lli.fdroid.ui.components.CheckChip
import com.machiav3lli.fdroid.ui.components.QrCodeImage
import com.machiav3lli.fdroid.ui.components.SelectChip
import com.machiav3lli.fdroid.ui.components.TitleText
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowSquareOut
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CaretDown
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Check
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.GearSix
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.TrashSimple
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.X
import com.machiav3lli.fdroid.ui.dialog.ActionsDialogUI
import com.machiav3lli.fdroid.ui.dialog.BaseDialog
import com.machiav3lli.fdroid.ui.dialog.DIALOG_NONE
import com.machiav3lli.fdroid.ui.dialog.ProductsListDialogUI
import com.machiav3lli.fdroid.ui.dialog.StringInputDialogUI
import com.machiav3lli.fdroid.utils.extension.text.nullIfEmpty
import com.machiav3lli.fdroid.utils.extension.text.pathCropped
import com.machiav3lli.fdroid.utils.getLocaleDateString
import kotlinx.coroutines.launch
import java.net.URI
import java.net.URL
import java.util.Locale

const val DIALOG_ADDRESS = 1
const val DIALOG_FINGERPRINT = 2
const val DIALOG_USERNAME = 3
const val DIALOG_PASSWORD = 4
const val DIALOG_PRODUCTS = 5

@Composable
fun RepoPage(
    repositoryId: Long,
    initEditMode: Boolean,
    onDismiss: () -> Unit,
    updateRepo: (Repository?) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repoState = NeoApp.db.getRepositoryDao().getFlow(repositoryId)
        .collectAsState(initial = null)
    val repo by remember {
        derivedStateOf {
            repoState.value
        }
    }
    val appsCount by NeoApp.db.getProductDao().countForRepositoryFlow(repositoryId)
        .collectAsState(0)
    var editMode by remember { mutableStateOf(initEditMode) }
    val openDeleteDialog = remember { mutableStateOf(false) }
    val openDialog = remember { mutableStateOf(false) }
    val dialogProps = remember {
        mutableIntStateOf(DIALOG_NONE)
    }

    var addressFieldValue by remember(repo) {
        mutableStateOf(repo?.address.orEmpty())
    }
    var fingerprintFieldValue by remember(repo) {
        mutableStateOf(repo?.fingerprint.orEmpty())
    }
    var usernameFieldValue by remember(repo) {
        mutableStateOf(repo?.authenticationPair?.first.orEmpty())
    }
    var passwordFieldValue by remember(repo) {
        mutableStateOf(repo?.authenticationPair?.second.orEmpty())
    }

    val addressValidity = remember { mutableStateOf(false) }
    val fingerprintValidity = remember { mutableStateOf(false) }
    val usernameValidity = remember { mutableStateOf(false) }
    val passwordValidity = remember { mutableStateOf(false) }
    val validations =
        listOf(addressValidity, fingerprintValidity, usernameValidity, passwordValidity)

    SideEffect {
        if (editMode && repo?.address.isNullOrEmpty()) {
            val clipboardManager =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val text = clipboardManager.primaryClip
                ?.let { if (it.itemCount > 0) it else null }
                ?.getItemAt(0)?.text?.toString().orEmpty()
            val (addressText, fingerprintText) = try {
                val uri = Uri.parse(URL(text.replaceFirst("fdroidrepos:", "https:")).toString())
                val fingerprintText =
                    uri.getQueryParameter("fingerprint")?.uppercase()?.nullIfEmpty()
                        ?: uri.getQueryParameter("FINGERPRINT")?.uppercase()?.nullIfEmpty()
                Pair(
                    uri.buildUpon().path(uri.path?.pathCropped)
                        .query(null).fragment(null).build().toString(), fingerprintText
                )
            } catch (e: Exception) {
                Pair(null, null)
            }
            if (addressText != null)
                addressFieldValue = addressText
            if (fingerprintText != null)
                fingerprintFieldValue = fingerprintText
        }

        invalidateAddress(addressValidity, addressFieldValue)
        invalidateFingerprint(fingerprintValidity, fingerprintFieldValue)
        invalidateAuthentication(
            usernameValidity,
            passwordValidity,
            usernameFieldValue,
            passwordFieldValue,
        )
        invalidateAuthentication(
            usernameValidity,
            passwordValidity,
            usernameFieldValue,
            passwordFieldValue,
        )
    }

    repo?.let { repo ->
        val enabled by remember {
            derivedStateOf {
                repoState.value?.enabled ?: false
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f, true)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                if ((repo.updated) > 0L && !editMode) {
                    item {
                        TitleText(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(id = R.string.name),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BlockText(text = repo.name)
                    }
                }
                if (!editMode && repo.description.isNotEmpty()) {
                    item {
                        TitleText(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(id = R.string.description),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BlockText(text = repo.description.replace("\n", " "))
                    }
                }
                if ((repo.updated) > 0L && !editMode) {
                    item {
                        TitleText(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(id = R.string.recently_updated),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BlockText(
                            text = context.getLocaleDateString(repo.updated)
                        )
                    }
                }
                if (!editMode && enabled &&
                    (repo.lastModified.isNotEmpty() ||
                            repo.entityTag.isNotEmpty())
                ) {
                    item {
                        TitleText(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(id = R.string.number_of_applications),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    dialogProps.intValue = DIALOG_PRODUCTS
                                    openDialog.value = true
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            BlockText(
                                text = appsCount.toString()
                            )
                            Icon(
                                imageVector = Phosphor.ArrowSquareOut,
                                contentDescription = stringResource(id = R.string.list_apps)
                            )
                        }
                    }
                }
                item {
                    val strokeColor by animateColorAsState(
                        if (addressValidity.value) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.tertiary,
                        label = "strokeColorAddress"
                    )

                    TitleText(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = R.string.address),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AnimatedVisibility(visible = !editMode) {
                        BlockText(text = repo.address)
                    }
                    AnimatedVisibility(visible = editMode) {
                        Column {
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.large,
                                border = BorderStroke(1.dp, strokeColor),
                                onClick = {
                                    dialogProps.intValue = DIALOG_ADDRESS
                                    openDialog.value = true
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 16.dp
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(text = addressFieldValue)
                                }
                            }
                            if (repo.mirrors.isNotEmpty()) LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(items = repo.mirrors, key = { it }) { text ->
                                    SelectChip(
                                        text = text,
                                        checked = text == addressFieldValue,
                                        alwaysShowIcon = false,
                                    ) {
                                        addressFieldValue = text
                                        invalidateAddress(addressValidity, addressFieldValue)
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    val strokeColor by animateColorAsState(
                        if (fingerprintValidity.value) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.tertiary,
                        label = "strokeColorFP"
                    )

                    TitleText(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = R.string.fingerprint),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AnimatedVisibility(visible = !editMode) {
                        BlockText(
                            text = if (
                                repo.updated > 0L
                                && repo.fingerprint.isEmpty()
                            ) stringResource(id = R.string.repository_unsigned_DESC)
                            else repo.fingerprint
                                .windowed(2, 2, false)
                                .joinToString(separator = " ") { it.uppercase(Locale.US) + " " },
                            color = if (repo.updated > 0L && repo.fingerprint.isEmpty())
                                MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface,
                            monospace = true,
                        )
                    }
                    AnimatedVisibility(visible = editMode) {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            border = BorderStroke(1.dp, strokeColor),
                            onClick = {
                                dialogProps.intValue = DIALOG_FINGERPRINT
                                openDialog.value = true
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(text = fingerprintFieldValue
                                    .windowed(2, 2, false)
                                    .joinToString(separator = " ") { it.uppercase(Locale.US) + " " }
                                )
                            }
                        }
                    }
                }
                if (!editMode) {
                    item {
                        TitleText(
                            modifier = Modifier,
                            text = stringResource(id = R.string.repo_qr_code),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            QrCodeImage(
                                content = repo.intentAddress,
                                modifier = Modifier.fillMaxWidth(0.5f),
                                contentDescription = stringResource(id = R.string.repo_qr_code)
                            )
                        }
                    }
                }
                if (editMode) {
                    item {
                        val strokeColor by animateColorAsState(
                            if (usernameValidity.value) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.tertiary,
                            label = "strokeColorUN"
                        )

                        TitleText(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(id = R.string.username),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            border = BorderStroke(1.dp, strokeColor),
                            onClick = {
                                dialogProps.intValue = DIALOG_USERNAME
                                openDialog.value = true
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(text = usernameFieldValue)
                            }
                        }
                    }
                    item {
                        val strokeColor by animateColorAsState(
                            if (passwordValidity.value) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.tertiary,
                            label = "strokeColorPW"
                        )

                        TitleText(
                            modifier = Modifier
                                .clickable {
                                    dialogProps.intValue = DIALOG_PASSWORD
                                    openDialog.value = true
                                }
                                .fillMaxWidth(),
                            text = stringResource(id = R.string.password),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            border = BorderStroke(1.dp, strokeColor),
                            onClick = {
                                dialogProps.intValue = DIALOG_PASSWORD
                                openDialog.value = true
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(text = passwordFieldValue)
                            }
                        }
                    }
                }
            }
            HorizontalDivider(thickness = 2.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!editMode) Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    CheckChip(
                        checked = enabled,
                        text = stringResource(
                            id = if (enabled) R.string.enabled
                            else R.string.enable
                        ),
                        fullWidth = false,
                    ) {
                        scope.launch {
                            SyncWorker.enableRepo(repo, !enabled)
                        }
                    }
                    ActionButton(
                        text = stringResource(id = R.string.delete),
                        icon = Phosphor.TrashSimple,
                        positive = false
                    ) {
                        openDeleteDialog.value = true
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(
                            id = if (!editMode) R.string.dismiss
                            else R.string.cancel
                        ),
                        icon = if (!editMode) Phosphor.CaretDown
                        else Phosphor.X,
                        positive = false
                    ) {
                        if (!editMode)
                            onDismiss()
                        else {
                            editMode = false
                            addressFieldValue = repo.address
                            fingerprintFieldValue = repo.fingerprint
                            usernameFieldValue = repo.authenticationPair.first.orEmpty()
                            passwordFieldValue = repo.authenticationPair.second.orEmpty()
                        }
                    }
                    ActionButton(
                        text = stringResource(
                            id = if (!editMode) R.string.edit
                            else R.string.save
                        ),
                        icon = if (!editMode) Phosphor.GearSix
                        else Phosphor.Check,
                        modifier = Modifier.weight(1f),
                        positive = true,
                        enabled = !editMode || validations.all { it.value },
                        onClick = {
                            if (!editMode) editMode = true
                            else {
                                // TODO show readable error
                                updateRepo(repo.apply {
                                    address = addressFieldValue
                                    fingerprint = fingerprintFieldValue.uppercase()
                                    setAuthentication(
                                        usernameFieldValue,
                                        passwordFieldValue,
                                    )
                                })
                                // TODO sync a new when is already active
                                editMode = false
                            }
                        }
                    )
                }
            }
        }
    }


    if (openDeleteDialog.value) {
        BaseDialog(openDialogCustom = openDeleteDialog) {
            ActionsDialogUI(
                titleText = stringResource(id = R.string.confirmation),
                messageText = "${repo?.name}: ${stringResource(id = R.string.delete_repository_DESC)}",
                openDialogCustom = openDeleteDialog,
                primaryText = stringResource(id = R.string.delete),
                primaryIcon = Phosphor.TrashSimple,
                primaryAction = {
                    scope.launch {
                        SyncWorker.deleteRepo(repositoryId)
                        onDismiss()
                    }
                },
            )
        }
    }

    if (openDialog.value) BaseDialog(openDialogCustom = openDialog) {
        dialogProps.intValue.let { dialogMode ->
            when (dialogMode) {
                DIALOG_ADDRESS -> {
                    StringInputDialogUI(
                        titleText = stringResource(id = R.string.address),
                        initValue = addressFieldValue,
                        openDialogCustom = openDialog
                    ) {
                        addressFieldValue = it
                        invalidateAddress(addressValidity, addressFieldValue)
                    }
                }

                DIALOG_FINGERPRINT -> {
                    StringInputDialogUI(
                        titleText = stringResource(id = R.string.fingerprint),
                        initValue = fingerprintFieldValue,
                        openDialogCustom = openDialog
                    ) {
                        fingerprintFieldValue = it
                        invalidateFingerprint(fingerprintValidity, fingerprintFieldValue)
                    }
                }

                DIALOG_USERNAME -> {
                    StringInputDialogUI(
                        titleText = stringResource(id = R.string.username),
                        initValue = usernameFieldValue,
                        openDialogCustom = openDialog
                    ) {
                        usernameFieldValue = it
                        invalidateAuthentication(
                            usernameValidity,
                            passwordValidity,
                            usernameFieldValue,
                            passwordFieldValue,
                        )
                    }
                }

                DIALOG_PASSWORD -> {
                    StringInputDialogUI(
                        titleText = stringResource(id = R.string.password),
                        initValue = passwordFieldValue,
                        openDialogCustom = openDialog
                    ) {
                        passwordFieldValue = it
                        invalidateAuthentication(
                            usernameValidity,
                            passwordValidity,
                            usernameFieldValue,
                            passwordFieldValue,
                        )
                    }
                }

                DIALOG_PRODUCTS -> ProductsListDialogUI(
                    repositoryId = repositoryId,
                    title = repo?.name.orEmpty(),
                )
            }
        }
    }
}


private fun invalidateAddress(
    validity: MutableState<Boolean>,
    address: String,
) {
    // TODO check if already used
    validity.value = normalizeAddress(address) != null
}


private fun invalidateFingerprint(validity: MutableState<Boolean>, fingerprint: String) {
    validity.value = fingerprint.isEmpty() || fingerprint.length == 64
}


private fun invalidateAuthentication(
    usernameValidity: MutableState<Boolean>,
    passwordValidity: MutableState<Boolean>,
    username: String,
    password: String,
) {
    val usernameInvalid = username.contains(':')
    val usernameEmpty = username.isEmpty() && password.isNotEmpty()
    val passwordEmpty = username.isNotEmpty() && password.isEmpty()
    usernameValidity.value = !usernameInvalid && !usernameEmpty
    passwordValidity.value = !passwordEmpty
}

private fun normalizeAddress(address: String): String? {
    val uri = try {
        val uri = URI(address)
        if (uri.isAbsolute) uri.normalize() else null
    } catch (e: Exception) {
        null
    }
    val path = uri?.path?.pathCropped
    return if (uri != null && path != null) {
        try {
            URI(
                uri.scheme,
                uri.userInfo,
                uri.host,
                uri.port,
                path,
                uri.query,
                uri.fragment
            ).toString()
        } catch (e: Exception) {
            null
        }
    } else {
        null
    }
}
