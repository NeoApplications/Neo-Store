package com.machiav3lli.fdroid.ui.fragments

import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.machiav3lli.fdroid.EXTRA_REPOSITORY_EDIT
import com.machiav3lli.fdroid.EXTRA_REPOSITORY_ID
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.RepoManager
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.screen.MessageDialog
import com.machiav3lli.fdroid.service.Connection
import com.machiav3lli.fdroid.service.SyncService
import com.machiav3lli.fdroid.ui.activities.PrefsActivityX
import com.machiav3lli.fdroid.ui.compose.components.ActionButton
import com.machiav3lli.fdroid.ui.compose.components.BlockText
import com.machiav3lli.fdroid.ui.compose.components.TitleText
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Check
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.GearSix
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.TrashSimple
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.X
import com.machiav3lli.fdroid.ui.compose.theme.AppTheme
import com.machiav3lli.fdroid.ui.viewmodels.RepositorySheetVM
import com.machiav3lli.fdroid.utility.extension.text.pathCropped
import com.machiav3lli.fdroid.utility.isDarkTheme
import kotlinx.coroutines.launch
import java.net.URI
import java.util.*

class RepoSheet() : FullscreenBottomSheetDialogFragment(false), RepoManager {
    val viewModel: RepositorySheetVM by viewModels {
        RepositorySheetVM.Factory((requireActivity() as PrefsActivityX).db, repositoryId)
    }

    constructor(repositoryId: Long = 0, editMode: Boolean = false) : this() {
        arguments = Bundle().apply {
            putLong(EXTRA_REPOSITORY_ID, repositoryId)
            putBoolean(EXTRA_REPOSITORY_EDIT, editMode)
        }
    }

    private val repositoryId: Long
        get() = requireArguments().getLong(EXTRA_REPOSITORY_ID)

    private val initEditMode: Boolean
        get() = requireArguments().getBoolean(EXTRA_REPOSITORY_EDIT, false)

    private val syncConnection = Connection(SyncService::class.java)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        syncConnection.bind(requireContext())
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme(
                    darkTheme = when (Preferences[Preferences.Key.Theme]) {
                        is Preferences.Theme.System      -> isSystemInDarkTheme()
                        is Preferences.Theme.SystemBlack -> isSystemInDarkTheme()
                        else                             -> isDarkTheme
                    }
                ) {
                    RepoPage()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        syncConnection.unbind(requireContext())
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun RepoPage() { // TODO add clipboard handler
        val repo by viewModel.repo.observeAsState()
        val appsCount by viewModel.appsCount.observeAsState()
        var editMode by remember { mutableStateOf(initEditMode) }

        val focusManager = LocalFocusManager.current

        var addressFieldValue by remember(repo) {
            mutableStateOf(
                TextFieldValue(
                    repo?.address.orEmpty(),
                    TextRange(repo?.address.orEmpty().length),
                )
            )
        }
        var fingerprintFieldValue by remember(repo) {
            mutableStateOf(
                TextFieldValue(
                    repo?.fingerprint.orEmpty(),
                    TextRange(repo?.fingerprint.orEmpty().length),
                )
            )
        }
        var usernameFieldValue by remember(repo) {
            mutableStateOf(
                TextFieldValue(
                    repo?.authenticationPair?.first.orEmpty(),
                    TextRange(repo?.authenticationPair?.first.orEmpty().length),
                )
            )
        }
        var passwordFieldValue by remember(repo) {
            mutableStateOf(
                TextFieldValue(
                    repo?.authenticationPair?.second.orEmpty(),
                    TextRange(repo?.authenticationPair?.second.orEmpty().length),
                )
            )
        }

        val addressValidity = remember { mutableStateOf(false) }
        val fingerprintValidity = remember { mutableStateOf(false) }
        val usernameValidity = remember { mutableStateOf(false) }
        val passwordValidity = remember { mutableStateOf(false) }
        val validations =
            listOf(addressValidity, fingerprintValidity, usernameValidity, passwordValidity)

        SideEffect {
            invalidateAddress(addressValidity, addressFieldValue.text)
            invalidateFingerprint(fingerprintValidity, fingerprintFieldValue.text)
            invalidateAuthentication(
                passwordValidity,
                usernameFieldValue.text,
                passwordFieldValue.text,
            )
            invalidateAuthentication(
                usernameValidity,
                usernameFieldValue.text,
                passwordFieldValue.text,
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {

            if ((repo?.updated ?: -1) > 0L && !editMode) {
                item {
                    TitleText(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = R.string.name),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BlockText(text = repo?.name)
                }
            }
            if (!editMode) {
                item {
                    TitleText(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = R.string.description),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BlockText(text = repo?.description?.replace("\n", " "))
                }
            }
            if ((repo?.updated ?: -1) > 0L && !editMode) {
                item {
                    TitleText(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = R.string.recently_updated),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BlockText(text = if (repo != null && repo?.updated != null) {
                        val date = Date(repo?.updated ?: 0)
                        val format =
                            if (DateUtils.isToday(date.time)) DateUtils.FORMAT_SHOW_TIME else
                                DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_DATE
                        DateUtils.formatDateTime(context, date.time, format)
                    } else stringResource(R.string.unknown)
                    )
                }
            }
            if (!editMode && repo?.enabled == true &&
                (repo?.lastModified.orEmpty().isNotEmpty() ||
                        repo?.entityTag.orEmpty().isNotEmpty())
            ) {
                item {
                    TitleText(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = R.string.number_of_applications),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BlockText(text = appsCount.toString())
                }
            }
            item {
                TitleText(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.address),
                )
                Spacer(modifier = Modifier.height(8.dp))
                AnimatedVisibility(visible = !editMode) {
                    BlockText(text = repo?.address)
                }
                AnimatedVisibility(visible = editMode) {
                    OutlinedTextField( // TODO add mirror option
                        modifier = Modifier.fillMaxWidth(),
                        value = addressFieldValue,
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        onValueChange = {
                            addressFieldValue = it
                            invalidateAddress(addressValidity, addressFieldValue.text)
                        }
                    )
                }
            }
            item {
                TitleText(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.fingerprint),
                )
                Spacer(modifier = Modifier.height(8.dp))
                AnimatedVisibility(visible = !editMode) {
                    BlockText(
                        text = if ((repo?.updated
                                ?: -1) > 0L && repo?.fingerprint.isNullOrEmpty()
                        ) stringResource(id = R.string.repository_unsigned_DESC)
                        else repo?.fingerprint
                            ?.windowed(2, 2, false)
                            ?.joinToString(separator = " ") { it.uppercase(Locale.US) + " " },
                        color = if ((repo?.updated ?: -1) > 0L
                            && repo?.fingerprint?.isEmpty() == true
                        ) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        monospace = true,
                    )
                }
                AnimatedVisibility(visible = editMode) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = fingerprintFieldValue,
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        onValueChange = {
                            fingerprintFieldValue = it
                            invalidateFingerprint(fingerprintValidity, fingerprintFieldValue.text)
                        }
                    )
                }
            }
            if (editMode) {
                item {
                    TitleText(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = R.string.username),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = usernameFieldValue,
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        isError = usernameValidity.value,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        onValueChange = {
                            usernameFieldValue = it
                            invalidateAuthentication(
                                usernameValidity,
                                usernameFieldValue.text,
                                passwordFieldValue.text,
                            )
                        }
                    )
                }
                item {
                    TitleText(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = R.string.password),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth(),
                        value = passwordFieldValue,
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        isError = passwordValidity.value,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        onValueChange = {
                            passwordFieldValue = it
                            invalidateAuthentication(
                                passwordValidity,
                                usernameFieldValue.text,
                                passwordFieldValue.text,
                            )
                        }
                    )
                }
            }
            item {
                Divider(thickness = 2.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(id = if (!editMode) R.string.delete
                        else R.string.cancel),
                        icon = if (!editMode) Phosphor.TrashSimple
                        else Phosphor.X,
                        positive = false
                    ) {
                        if (!editMode) MessageDialog(MessageDialog.Message.DeleteRepositoryConfirm(
                            repositoryId))
                            .show(childFragmentManager)
                        else {
                            editMode = false
                            addressFieldValue = TextFieldValue(
                                repo?.address.orEmpty(),
                                TextRange(repo?.address.orEmpty().length),
                            )
                            fingerprintFieldValue = TextFieldValue(
                                repo?.fingerprint.orEmpty(),
                                TextRange(repo?.fingerprint.orEmpty().length),
                            )
                            usernameFieldValue = TextFieldValue(
                                repo?.authenticationPair?.first.orEmpty(),
                                TextRange(repo?.authenticationPair?.first.orEmpty().length),
                            )
                            passwordFieldValue = TextFieldValue(
                                repo?.authenticationPair?.second.orEmpty(),
                                TextRange(repo?.authenticationPair?.second.orEmpty().length),
                            )
                        }
                    }
                    ActionButton(
                        text = stringResource(id = if (!editMode) R.string.edit
                        else R.string.save),
                        icon = if (!editMode) Phosphor.GearSix
                        else Phosphor.Check,
                        modifier = Modifier.weight(1f),
                        positive = true,
                        enabled = !editMode || validations.all { it.value },
                        onClick = {
                            if (!editMode) editMode = true
                            else {
                                // TODO respect validity
                                viewModel.updateRepo(repo?.apply {
                                    address = addressFieldValue.text
                                    fingerprint = fingerprintFieldValue.text
                                    setAuthentication(
                                        usernameFieldValue.text,
                                        passwordFieldValue.text,
                                    )
                                })
                            }
                        }
                    )
                }
            }
        }
    }

    override fun setupLayout() {}
    override fun updateSheet() {}

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
        validity: MutableState<Boolean>,
        username: String,
        password: String,
    ) {
        val usernameInvalid = username.contains(':')
        val usernameEmpty = username.isEmpty() && password.isNotEmpty()
        val passwordEmpty = username.isNotEmpty() && password.isEmpty()
        validity.value = !(usernameInvalid || usernameEmpty || passwordEmpty)
    }

    override fun onDeleteConfirm(repositoryId: Long) {
        lifecycleScope.launch {
            if (syncConnection.binder?.deleteRepository(repositoryId) == true)
                dismissAllowingStateLoss()
        }
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
}
