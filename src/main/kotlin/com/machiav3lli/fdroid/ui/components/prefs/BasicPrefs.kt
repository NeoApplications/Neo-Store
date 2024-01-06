package com.machiav3lli.fdroid.ui.components.prefs

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.machiav3lli.fdroid.content.BooleanPrefsMeta
import com.machiav3lli.fdroid.content.NonBooleanPrefsMeta
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.content.PrefsDependencies
import com.machiav3lli.fdroid.content.PrefsEntries
import com.machiav3lli.fdroid.ui.compose.utils.addIf
import com.machiav3lli.fdroid.utility.Utils
import com.machiav3lli.fdroid.utility.Utils.getLocaleOfCode
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun BasePreference(
    modifier: Modifier = Modifier,
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    summary: String? = null,
    isEnabled: Boolean = true,
    index: Int = 0,
    groupSize: Int = 1,
    startWidget: (@Composable () -> Unit)? = null,
    endWidget: (@Composable () -> Unit)? = null,
    bottomWidget: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val base = index.toFloat() / groupSize
    val rank = (index + 1f) / groupSize

    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    topStart = if (base == 0f) 16.dp else 6.dp,
                    topEnd = if (base == 0f) 16.dp else 6.dp,
                    bottomStart = if (rank == 1f) 16.dp else 6.dp,
                    bottomEnd = if (rank == 1f) 16.dp else 6.dp
                )
            )
            .addIf(onClick != null) {
                clickable(enabled = isEnabled, onClick = onClick!!)
            }
            .addIf(!isEnabled) {
                alpha(0.3f)
            },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        leadingContent = startWidget,
        headlineContent = {
            Text(
                text = stringResource(id = titleId),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 16.sp
            )
        },
        supportingContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (summaryId != -1 || summary != null) {
                    Text(
                        text = summary ?: stringResource(id = summaryId),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                bottomWidget?.let { it() }
            }
        },
        trailingContent = endWidget
    )
}

@Composable
fun SwitchPreference(
    modifier: Modifier = Modifier,
    prefKey: Preferences.Key<Boolean>,
    index: Int = 0,
    groupSize: Int = 1,
    onCheckedChange: ((Boolean) -> Unit) = {},
) {
    val context = LocalContext.current
    val (checked, check) = remember(Preferences[prefKey]) { mutableStateOf(Preferences[prefKey]) }
    val dependency = PrefsDependencies[prefKey]
    var isEnabled by remember {
        mutableStateOf(
            dependency?.let { Preferences[dependency.first] in dependency.second }
                ?: true
        )
    }

    SideEffect {
        CoroutineScope(Dispatchers.Default).launch {
            Preferences.subject.collect {
                when (it) {
                    dependency?.first -> isEnabled = Preferences[it] in dependency.second
                    else              -> {}
                }
            }
        }
    }

    BasePreference(
        modifier = modifier,
        titleId = BooleanPrefsMeta[prefKey]?.first ?: -1,
        summaryId = BooleanPrefsMeta[prefKey]?.second ?: -1,
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = {
            onCheckedChange(!checked)
            Preferences[prefKey] = !checked
            check(!checked)
        },
        endWidget = {
            Switch(
                checked = checked,
                onCheckedChange = {
                    onCheckedChange(it)
                    Preferences[prefKey] = it
                    check(it)
                },
                enabled = isEnabled,
            )
        }
    )
}

@Composable
fun LanguagePreference(
    modifier: Modifier = Modifier,
    prefKey: Preferences.Key<String>,
    index: Int = 1,
    groupSize: Int = 1,
    onClick: (() -> Unit) = {},
) {
    val context = LocalContext.current
    val dependency = PrefsDependencies[prefKey]
    var isEnabled by remember {
        mutableStateOf(
            dependency?.let { Preferences[dependency.first] in dependency.second }
                ?: true)
    }

    SideEffect {
        CoroutineScope(Dispatchers.Default).launch {
            Preferences.subject.collect {
                when (it) {
                    dependency?.first -> isEnabled = Preferences[it] in dependency.second
                    else              -> {}
                }
            }
        }
    }

    BasePreference(
        modifier = modifier,
        titleId = NonBooleanPrefsMeta[prefKey] ?: -1,
        summary = Utils.translateLocale(context.getLocaleOfCode(Preferences[prefKey])),
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = onClick
    )
}

@Composable
fun LaunchPreference(
    modifier: Modifier = Modifier,
    prefKey: Preferences.Key<String>,
    index: Int = 1,
    groupSize: Int = 1,
    onClick: (() -> Unit) = {},
) {
    val context = LocalContext.current
    val dependency = PrefsDependencies[prefKey]
    var isEnabled by remember {
        mutableStateOf(
            dependency?.let { Preferences[dependency.first] in dependency.second }
                ?: true)
    }
    var prefValue by remember {
        mutableStateOf(Preferences[prefKey])
    }

    SideEffect {
        CoroutineScope(Dispatchers.Default).launch {
            Preferences.subject.collect {
                when (it) {
                    prefKey           -> prefValue = Preferences[prefKey]
                    dependency?.first -> isEnabled = Preferences[it] in dependency.second
                    else              -> {}
                }
            }
        }
    }

    BasePreference(
        modifier = modifier,
        titleId = NonBooleanPrefsMeta[prefKey] ?: -1,
        summary = prefValue,
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = onClick
    )
}

@Composable
fun EnumPreference(
    modifier: Modifier = Modifier,
    prefKey: Preferences.Key<Preferences.Enumeration<*>>,
    index: Int = 1,
    groupSize: Int = 1,
    onClick: (() -> Unit) = {},
) {
    val dependency = PrefsDependencies[prefKey]
    var isEnabled by remember {
        mutableStateOf(
            dependency?.let { Preferences[dependency.first] in dependency.second }
                ?: true)
    }
    var prefValue by remember {
        mutableStateOf(Preferences[prefKey])
    }
    SideEffect {
        CoroutineScope(Dispatchers.Default).launch {
            Preferences.subject.collect {
                when (it) {
                    Preferences.Key.Installer -> {
                        prefValue = Preferences[prefKey]
                        if (Preferences[prefKey] == Preferences.Installer.Root && Shell.isAppGrantedRoot() != true) {
                            Shell.getShell().isRoot
                        }
                    }

                    prefKey                   -> prefValue = Preferences[prefKey]
                    dependency?.first         -> isEnabled = Preferences[it] in dependency.second
                    else                      -> {}
                }
            }
        }
    }

    BasePreference(
        modifier = modifier,
        titleId = NonBooleanPrefsMeta[prefKey] ?: -1,
        summary = stringResource(id = PrefsEntries[prefKey]?.get(prefValue) ?: -1),
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = onClick
    )
}

@Composable
fun IntPreference(
    modifier: Modifier = Modifier,
    prefKey: Preferences.Key<Int>,
    index: Int = 1,
    groupSize: Int = 1,
    onClick: (() -> Unit) = {},
) {
    val dependency = PrefsDependencies[prefKey]
    var isEnabled by remember {
        mutableStateOf(
            dependency?.let { Preferences[dependency.first] in dependency.second }
                ?: true)
    }
    var prefValue by remember {
        mutableStateOf(Preferences[prefKey])
    }
    SideEffect {
        CoroutineScope(Dispatchers.Default).launch {
            Preferences.subject.collect {
                when (it) {
                    prefKey           -> prefValue = Preferences[prefKey]
                    dependency?.first -> isEnabled = Preferences[it] in dependency.second
                    else              -> {}
                }
            }
        }
    }

    BasePreference(
        modifier = modifier,
        titleId = NonBooleanPrefsMeta[prefKey] ?: -1,
        summary = prefValue.toString(),
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = onClick
    )
}

@Composable
fun StringPreference(
    modifier: Modifier = Modifier,
    prefKey: Preferences.Key<String>,
    index: Int = 1,
    groupSize: Int = 1,
    onClick: (() -> Unit) = {},
) {
    val dependency = PrefsDependencies[prefKey]
    var isEnabled by remember {
        mutableStateOf(
            dependency?.let { Preferences[dependency.first] in dependency.second }
                ?: true)
    }
    var prefValue by remember {
        mutableStateOf(Preferences[prefKey])
    }
    SideEffect {
        CoroutineScope(Dispatchers.Default).launch {
            Preferences.subject.collect {
                when (it) {
                    prefKey           -> prefValue = Preferences[prefKey]
                    dependency?.first -> isEnabled = Preferences[it] in dependency.second
                    else              -> {}
                }
            }
        }
    }

    BasePreference(
        modifier = modifier,
        titleId = NonBooleanPrefsMeta[prefKey] ?: -1,
        summary = prefValue,
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = onClick
    )
}
