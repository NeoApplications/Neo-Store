package com.machiav3lli.fdroid.ui.compose.pages.settings.repository

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.ui.compose.RepositoriesRecycler
import com.machiav3lli.fdroid.ui.viewmodels.RepositoriesViewModelX

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepositoryPage(viewModel: RepositoriesViewModelX) {
    val repos by viewModel.repositories.collectAsState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = {
                viewModel.showRepositorySheet(
                    editMode = true,
                    addNew = true
                )
            }) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(id = R.string.add_repository)
                )
                Text(text = stringResource(id = R.string.add_repository))
            }
        }
    ) {
        val sortedRepoList = remember(repos) { repos.sortedBy { !it.enabled } }
        RepositoriesRecycler(
            repositoriesList = sortedRepoList,
            onClick = { viewModel.toggleRepository(it, it.enabled) },
            onLongClick = { viewModel.showRepositorySheet(it.id) }
        )

    }
}