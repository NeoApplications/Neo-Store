package com.looker.droidify.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.format.DateUtils
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.looker.droidify.R
import com.looker.droidify.databinding.SheetRepositoryBinding
import com.looker.droidify.screen.MessageDialog
import com.looker.droidify.service.Connection
import com.looker.droidify.service.SyncService
import com.looker.droidify.ui.activities.PrefsActivityX
import com.looker.droidify.ui.viewmodels.RepositoryViewModelX
import com.looker.droidify.utility.extension.resources.getColorFromAttr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class RepositorySheetX() : BottomSheetDialogFragment() {
    private lateinit var binding: SheetRepositoryBinding
    val viewModel: RepositoryViewModelX by viewModels {
        RepositoryViewModelX.Factory((requireActivity() as PrefsActivityX).db, repositoryId)
    }

    companion object {
        private const val EXTRA_REPOSITORY_ID = "repositoryId"
    }

    constructor(repositoryId: Long = 0) : this() {
        arguments = Bundle().apply {
            putLong(EXTRA_REPOSITORY_ID, repositoryId)
        }
    }

    private val repositoryId: Long
        get() = requireArguments().getLong(EXTRA_REPOSITORY_ID)

    private val syncConnection = Connection(SyncService::class.java)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val sheet = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        sheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        return sheet
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SheetRepositoryBinding.inflate(layoutInflater)
        syncConnection.bind(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.repo.observe(viewLifecycleOwner) { updateRepositoryView() }
        binding.delete.setOnClickListener {
            MessageDialog(MessageDialog.Message.DeleteRepositoryConfirm).show(
                childFragmentManager
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        syncConnection.unbind(requireContext())
    }

    private fun updateRepositoryView() {
        val repository = viewModel.repo.value?.trueData

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
                binding.description.text = getString(R.string.repository_not_used_DESC)
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

    internal fun onDeleteConfirm() {
        GlobalScope.launch(Dispatchers.IO) {
            if (syncConnection.binder?.deleteRepository(repositoryId) == true)
                dismissAllowingStateLoss()
        }
    }
}
