package com.machiav3lli.fdroid.ui.fragments

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.format.DateUtils
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.machiav3lli.fdroid.EXTRA_REPOSITORY_ID
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.RepoManager
import com.machiav3lli.fdroid.databinding.SheetRepositoryBinding
import com.machiav3lli.fdroid.screen.MessageDialog
import com.machiav3lli.fdroid.service.Connection
import com.machiav3lli.fdroid.service.SyncService
import com.machiav3lli.fdroid.ui.activities.PrefsActivityX
import com.machiav3lli.fdroid.ui.viewmodels.RepositorySheetVM
import com.machiav3lli.fdroid.utility.extension.resources.getColorFromAttr
import kotlinx.coroutines.launch
import java.util.*

class RepositorySheetX() : FullscreenBottomSheetDialogFragment(), RepoManager {
    private lateinit var binding: SheetRepositoryBinding
    val viewModel: RepositorySheetVM by viewModels {
        RepositorySheetVM.Factory((requireActivity() as PrefsActivityX).db, repositoryId)
    }

    constructor(repositoryId: Long = 0) : this() {
        arguments = Bundle().apply {
            putLong(EXTRA_REPOSITORY_ID, repositoryId)
        }
    }

    private val repositoryId: Long
        get() = requireArguments().getLong(EXTRA_REPOSITORY_ID)

    private val syncConnection = Connection(SyncService::class.java)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SheetRepositoryBinding.inflate(layoutInflater)
        syncConnection.bind(requireContext())
        return binding.root
    }

    override fun setupLayout() {
        viewModel.repo.observe(viewLifecycleOwner) { updateSheet() }
        viewModel.appsCount.observe(viewLifecycleOwner) { updateSheet() }
        binding.delete.setOnClickListener {
            MessageDialog(MessageDialog.Message.DeleteRepositoryConfirm).show(
                childFragmentManager
            )
        }
        binding.editRepository.setOnClickListener {
            (context as PrefsActivityX).navigateEditRepo(repositoryId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        syncConnection.unbind(requireContext())
    }

    override fun updateSheet() {
        val repository = viewModel.repo.value

        if (repository == null) {
            binding.address.text = getString(R.string.unknown)
            binding.nameBlock.visibility = View.GONE
            binding.descriptionBlock.visibility = View.GONE
            binding.updatedBlock.visibility = View.GONE
            binding.appsBlock.visibility = View.GONE
        } else {
            binding.address.text = repository.address
            binding.descriptionBlock.visibility = View.VISIBLE
            if (repository.updated > 0L) {
                binding.nameBlock.visibility = View.VISIBLE
                binding.updatedBlock.visibility = View.VISIBLE
                binding.name.text = repository.name
                binding.description.text = repository.description.replace('\n', ' ')
                binding.updated.text = run {
                    val lastUpdated = repository.updated
                    if (lastUpdated > 0L) {
                        val date = Date(repository.updated)
                        val format =
                            if (DateUtils.isToday(date.time)) DateUtils.FORMAT_SHOW_TIME else
                                DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_DATE
                        DateUtils.formatDateTime(binding.root.context, date.time, format)
                    } else {
                        getString(R.string.unknown)
                    }
                }
                if (repository.enabled && (repository.lastModified.isNotEmpty() || repository.entityTag.isNotEmpty())) {
                    binding.appsBlock.visibility = View.VISIBLE
                    binding.apps.text = viewModel.appsCount.value.toString()
                } else {
                    binding.appsBlock.visibility = View.GONE
                }
            } else {
                binding.description.text = repository.description.replace('\n', ' ')
                binding.nameBlock.visibility = View.GONE
                binding.updatedBlock.visibility = View.GONE
                binding.appsBlock.visibility = View.GONE
            }

            if (repository.fingerprint.isEmpty()) {
                if (repository.updated > 0L) {
                    val builder =
                        SpannableStringBuilder(getString(R.string.repository_unsigned_DESC))
                    builder.setSpan(
                        ForegroundColorSpan(binding.root.context.getColorFromAttr(R.attr.colorError).defaultColor),
                        0, builder.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    binding.fingerprint.text = builder
                    binding.fingerprintBlock.visibility = View.VISIBLE
                } else
                    binding.fingerprintBlock.visibility = View.GONE
            } else {
                val fingerprint =
                    SpannableStringBuilder(repository.fingerprint.windowed(2, 2, false)
                        .take(32).joinToString(separator = " ") { it.uppercase(Locale.US) })
                fingerprint.setSpan(
                    TypefaceSpan("monospace"), 0, fingerprint.length,
                    SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                binding.fingerprint.text = fingerprint
                binding.fingerprintBlock.visibility = View.VISIBLE
            }
        }
    }

    override fun onDeleteConfirm() {
        lifecycleScope.launch {
            if (syncConnection.binder?.deleteRepository(repositoryId) == true)
                dismissAllowingStateLoss()
        }
    }
}
