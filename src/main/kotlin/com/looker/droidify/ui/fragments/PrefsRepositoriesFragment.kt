package com.looker.droidify.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import com.looker.droidify.R
import com.looker.droidify.content.Preferences
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.databinding.FragmentComposeBinding
import com.looker.droidify.service.Connection
import com.looker.droidify.service.SyncService
import com.looker.droidify.ui.activities.PrefsActivityX
import com.looker.droidify.ui.compose.RepositoriesRecycler
import com.looker.droidify.ui.compose.theme.AppTheme
import com.looker.droidify.ui.viewmodels.RepositoriesViewModelX
import com.looker.droidify.utility.isDarkTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PrefsRepositoriesFragment : BaseNavFragment() {
    private lateinit var binding: FragmentComposeBinding
    val viewModel: RepositoriesViewModelX by viewModels {
        RepositoriesViewModelX.Factory(prefsActivityX.db)
    }

    private val prefsActivityX: PrefsActivityX
        get() = requireActivity() as PrefsActivityX

    private val syncConnection = Connection(SyncService::class.java)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreate(savedInstanceState)
        binding = FragmentComposeBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun setupLayout() {
        syncConnection.bind(requireContext())
        viewModel.repositories.observe(requireActivity()) {
            redrawPage(it)
        }
        viewModel.toLaunch.observe(viewLifecycleOwner) {
            if (it?.first == true) {
                EditRepositorySheetX(it.second)
                    .showNow(parentFragmentManager, "Repository ${it.second}")
                viewModel.emptyToLaunch()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        syncConnection.unbind(requireContext())
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun redrawPage(repos: List<Repository>) {
        binding.composeView.setContent {
            AppTheme(
                darkTheme = when (Preferences[Preferences.Key.Theme]) {
                    is Preferences.Theme.System -> isSystemInDarkTheme()
                    is Preferences.Theme.AmoledSystem -> isSystemInDarkTheme()
                    else -> isDarkTheme
                }
            ) {
                Scaffold { padding ->
                    Column(
                        modifier = Modifier.padding(padding)
                    ) {
                        OutlinedButton(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.background
                            ),
                            onClick = { viewModel.addRepository() }
                        ) {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = stringResource(id = R.string.add_repository),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.ic_add),
                                contentDescription = stringResource(id = R.string.add_repository)
                            )
                        }

                        RepositoriesRecycler(
                            repositoriesList = repos.sortedBy { repo -> !repo.enabled },
                            onClick = { repo ->
                                repo.enabled = !repo.enabled
                                GlobalScope.launch(Dispatchers.IO) {
                                    syncConnection.binder?.setEnabled(repo, repo.enabled)
                                }
                            },
                            onLongClick = { repo ->
                                RepositorySheetX(repo.id)
                                    .showNow(parentFragmentManager, "Repository ${repo.id}")
                            })
                    }
                }
            }
        }
    }
}
