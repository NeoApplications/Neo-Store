package com.looker.droidify.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.looker.droidify.content.Preferences
import com.looker.droidify.ui.activities.PrefsActivityX
import com.looker.droidify.ui.compose.pages.settings.repository.RepositoryPage
import com.looker.droidify.ui.compose.theme.AppTheme
import com.looker.droidify.ui.viewmodels.RepositoriesViewModelX
import com.looker.droidify.utility.isDarkTheme
import kotlinx.coroutines.flow.collectLatest

class PrefsRepositoriesFragment : BaseNavFragment() {

    val viewModel: RepositoriesViewModelX by viewModels {
        RepositoriesViewModelX.Factory(prefsActivityX.db.repositoryDao)
    }

    private val prefsActivityX: PrefsActivityX
        get() = requireActivity() as PrefsActivityX

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenStarted {
            viewModel.showSheet.collectLatest {
                RepositorySheetX(it).showNow(childFragmentManager, "Repository $it")
            }
        }
        return ComposeView(requireContext()).apply {
            setContent { ReposPage() }
        }
    }

    override fun setupLayout() {
        viewModel.bindConnection(requireContext())
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
        viewModel.syncConnection.unbind(requireContext())
    }

    @Composable
    fun ReposPage() {
        AppTheme(
            darkTheme = when (Preferences[Preferences.Key.Theme]) {
                is Preferences.Theme.System -> isSystemInDarkTheme()
                is Preferences.Theme.AmoledSystem -> isSystemInDarkTheme()
                else -> isDarkTheme
            }
        ) {
            RepositoryPage(viewModel = viewModel)
        }
    }
}
