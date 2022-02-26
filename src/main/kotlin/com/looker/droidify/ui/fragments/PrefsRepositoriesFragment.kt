package com.looker.droidify.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Scaffold
import androidx.fragment.app.viewModels
import com.looker.droidify.content.Preferences
import com.looker.droidify.databinding.FragmentRepositoriesXBinding
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
    private lateinit var binding: FragmentRepositoriesXBinding
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
        binding = FragmentRepositoriesXBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun setupAdapters() {
        syncConnection.bind(requireContext())
        binding.addRepository.setOnClickListener { viewModel.addRepository() }
    }

    override fun setupLayout() {
        viewModel.repositories.observe(requireActivity()) {
            binding.reposRecycler.setContent {
                AppTheme(
                    darkTheme = when (Preferences[Preferences.Key.Theme]) {
                        is Preferences.Theme.System -> isSystemInDarkTheme()
                        is Preferences.Theme.AmoledSystem -> isSystemInDarkTheme()
                        else -> isDarkTheme
                    }
                ) {
                    Scaffold { _ ->
                        RepositoriesRecycler(
                            repositoriesList = it.sortedBy { repo -> !repo.enabled },
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

    override fun onDestroyView() {
        super.onDestroyView()
        syncConnection.unbind(requireContext())
    }
}
