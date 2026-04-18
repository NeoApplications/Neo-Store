package com.machiav3lli.fdroid.ui.pages

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.entity.ColoringState
import com.machiav3lli.fdroid.data.entity.SyncRequest
import com.machiav3lli.fdroid.manager.work.BatchSyncWorker
import com.machiav3lli.fdroid.manager.work.SyncWorker
import com.machiav3lli.fdroid.ui.components.ActionButton
import com.machiav3lli.fdroid.ui.components.OutlinedActionButton
import com.machiav3lli.fdroid.ui.components.RepositoryItem
import com.machiav3lli.fdroid.ui.components.WideSearchField
import com.machiav3lli.fdroid.ui.components.prefs.PreferenceGroupHeading
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowCircleRight
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowsClockwise
import com.machiav3lli.fdroid.utils.extension.koinNeoViewModel
import com.machiav3lli.fdroid.viewmodels.PrefsVM
import kotlinx.coroutines.launch

@Composable
fun OnboardingReposPage(
    viewModel: PrefsVM = koinNeoViewModel(),
    onComplete: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pageState by viewModel.reposState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
        ) {
            item {
                WideSearchField(
                    query = pageState.query,
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(R.string.search_for_repository),
                    onCleanQuery = { viewModel.setSearchQuery("") },
                    onQueryChanged = { newQuery ->
                        if (newQuery != pageState.query) viewModel.setSearchQuery(newQuery)
                    }
                )
            }

            stickyHeader(key = "enabledTitle") {
                PreferenceGroupHeading(
                    heading = stringResource(id = R.string.enabled),
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .fillMaxWidth()
                )
            }

            items(items = pageState.enabledRepos, key = { it.id }) {
                RepositoryItem(
                    modifier = Modifier,
                    repository = it,
                    onSwitch = { repo ->
                        scope.launch {
                            SyncWorker.enableRepo(repo, !repo.enabled, sync = false)
                        }
                    },
                    onClick = { }
                )
            }

            stickyHeader(key = "disabledTitle") {
                PreferenceGroupHeading(
                    heading = stringResource(id = R.string.disabled),
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .fillMaxWidth()
                )
            }

            items(items = pageState.disabledRepo, key = { it.id }) {
                RepositoryItem(
                    modifier = Modifier,
                    repository = it,
                    onSwitch = { repo ->
                        scope.launch {
                            SyncWorker.enableRepo(repo, !repo.enabled, sync = false)
                        }
                    },
                    onClick = { }
                )
            }

            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedActionButton(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.action_start_sans_sync),
                icon = Phosphor.ArrowCircleRight,
                coloring = ColoringState.Negative,
                onClick = onComplete
            )
            ActionButton(
                text = stringResource(id = R.string.action_start_sync),
                icon = Phosphor.ArrowsClockwise,
                modifier = Modifier.weight(1f),
                coloring = ColoringState.Positive,
                onClick = {
                    BatchSyncWorker.enqueue(SyncRequest.MANUAL)
                    Preferences[Preferences.Key.InitialSync] = true
                    onComplete()
                }
            )
        }
    }
}
