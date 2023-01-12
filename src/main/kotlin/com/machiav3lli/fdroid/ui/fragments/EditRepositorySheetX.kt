package com.machiav3lli.fdroid.ui.fragments

import android.content.ClipboardManager
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import android.os.Bundle
import android.text.Selection
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.machiav3lli.fdroid.EXTRA_REPOSITORY_ID
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.RepoManager
import com.machiav3lli.fdroid.databinding.SheetEditRepositoryBinding
import com.machiav3lli.fdroid.network.Downloader
import com.machiav3lli.fdroid.screen.MessageDialog
import com.machiav3lli.fdroid.service.Connection
import com.machiav3lli.fdroid.service.SyncService
import com.machiav3lli.fdroid.ui.activities.PrefsActivityX
import com.machiav3lli.fdroid.ui.viewmodels.RepositorySheetVM
import com.machiav3lli.fdroid.utility.RxUtils
import com.machiav3lli.fdroid.utility.extension.resources.getColorFromAttr
import com.machiav3lli.fdroid.utility.extension.text.nullIfEmpty
import com.machiav3lli.fdroid.utility.extension.text.pathCropped
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import java.net.URI
import java.net.URL
import java.nio.charset.Charset
import java.util.*
import kotlin.math.min

class EditRepositorySheetX() : FullscreenBottomSheetDialogFragment(), RepoManager {
    private lateinit var binding: SheetEditRepositoryBinding
    val viewModel: RepositorySheetVM by viewModels {
        RepositorySheetVM.Factory((requireActivity() as PrefsActivityX).db, repositoryId)
    }

    companion object {

        private val checkPaths = listOf("", "fdroid/repo", "repo")
    }

    constructor(repositoryId: Long?) : this() {
        arguments = Bundle().apply {
            repositoryId?.let { putLong(EXTRA_REPOSITORY_ID, it) }
        }
    }

    private val repositoryId: Long
        get() = requireArguments().getLong(EXTRA_REPOSITORY_ID)

    private lateinit var errorColorFilter: PorterDuffColorFilter

    private val syncConnection = Connection(SyncService::class.java)
    private var checkDisposable: Disposable? = null

    private var takenAddresses = emptySet<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SheetEditRepositoryBinding.inflate(layoutInflater)
        syncConnection.bind(requireContext())
        return binding.root
    }

    override fun setupLayout() {
        errorColorFilter = PorterDuffColorFilter(
            requireContext().getColorFromAttr(R.attr.colorError).defaultColor,
            PorterDuff.Mode.SRC_IN
        )

        val validChar: (Char) -> Boolean =
            { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }

        binding.fingerprint.doAfterTextChanged { text ->
            fun logicalPosition(text: String, position: Int): Int {
                return if (position > 0) text.asSequence().take(position)
                    .count(validChar) else position
            }

            fun realPosition(text: String, position: Int): Int {
                return if (position > 0) {
                    var left = position
                    val index = text.indexOfFirst {
                        validChar(it) && run {
                            left -= 1
                            left <= 0
                        }
                    }
                    if (index >= 0) min(index + 1, text.length) else text.length
                } else {
                    position
                }
            }

            val inputString = text.toString()
            val outputString = inputString.uppercase(Locale.US)
                .filter(validChar).windowed(2, 2, true).take(32).joinToString(separator = " ")
            if (inputString != outputString) {
                val inputStart = logicalPosition(inputString, Selection.getSelectionStart(text))
                val inputEnd = logicalPosition(inputString, Selection.getSelectionEnd(text))
                text?.replace(0, text.length, outputString)
                Selection.setSelection(
                    text,
                    realPosition(outputString, inputStart),
                    realPosition(outputString, inputEnd)
                )
            }
        }
        binding.address.doAfterTextChanged { invalidateAddress() }
        binding.fingerprint.doAfterTextChanged { invalidateFingerprint() }
        binding.username.doAfterTextChanged { invalidateUsernamePassword() }
        binding.password.doAfterTextChanged { invalidateUsernamePassword() }

        viewModel.repo.observe(viewLifecycleOwner) { updateSheet() }
        binding.save.setOnClickListener { onSaveRepositoryClick() }
        binding.delete.setOnClickListener {
            MessageDialog(MessageDialog.Message.DeleteRepositoryConfirm(repositoryId))
                .show(childFragmentManager)
        }

        lifecycleScope.launchWhenStarted {
            val reposFlow = viewModel.db.repositoryDao.getAllRepositories()
            reposFlow.collectLatest { list ->
                takenAddresses = list.asSequence().filter { it.id != repositoryId }
                    .flatMap { (it.mirrors + it.address).asSequence() }
                    .map { it.withoutKnownPath }.toSet()
                MainScope().launch { invalidateAddress() }
            }
        }
    }

    override fun updateSheet() {
        val repository = viewModel.repo.value
        if (repository == null || repository.address == "") {
            val clipboardManager =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val text = clipboardManager.primaryClip
                ?.let { if (it.itemCount > 0) it else null }
                ?.getItemAt(0)?.text?.toString().orEmpty()
            val (addressText, fingerprintText) = try {
                val uri = Uri.parse(URL(text.replaceFirst("fdroidrepos:", "https:")).toString())
                val fingerprintText = uri.getQueryParameter("fingerprint")?.nullIfEmpty()
                    ?: uri.getQueryParameter("FINGERPRINT")?.nullIfEmpty()
                Pair(
                    uri.buildUpon().path(uri.path?.pathCropped)
                        .query(null).fragment(null).build().toString(), fingerprintText
                )
            } catch (e: Exception) {
                Pair(null, null)
            }
            binding.address.setText(addressText)
            binding.fingerprint.setText(fingerprintText)
        } else {
            binding.address.setText(repository.address)
            val mirrors = repository.mirrors.map { it.withoutKnownPath }
            if (mirrors.isNotEmpty()) {
                binding.addressMirror.visibility = View.VISIBLE
                binding.address.apply {
                    setPaddingRelative(
                        paddingStart, paddingTop,
                        paddingEnd + binding.addressMirror.layoutParams.width, paddingBottom
                    )
                }
                binding.addressMirror.setOnClickListener {
                    SelectMirrorDialog(mirrors)
                        .show(childFragmentManager, SelectMirrorDialog::class.java.name)
                }
            }
            binding.fingerprint.setText(repository.fingerprint)
            val (usernameText, passwordText) = repository.authentication.nullIfEmpty()
                ?.let { if (it.startsWith("Basic ")) it.substring(6) else null }
                ?.let {
                    try {
                        Base64.decode(it, Base64.NO_WRAP).toString(Charset.defaultCharset())
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
                ?.let {
                    val index = it.indexOf(':')
                    if (index >= 0) Pair(
                        it.substring(0, index),
                        it.substring(index + 1)
                    ) else null
                }
                ?: Pair(null, null)
            binding.username.setText(usernameText)
            binding.password.setText(passwordText)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        syncConnection.unbind(requireContext())
        checkDisposable?.dispose()
        checkDisposable = null
    }

    private var addressError = false
    private var fingerprintError = false
    private var usernamePasswordError = false

    private fun invalidateAddress() {
        try {
            invalidateAddress(binding.address.text.toString())
        } catch (_: IllegalStateException) {
        } // Ignore
    }

    private fun invalidateAddress(addressText: String) {
        val normalizedAddress = normalizeAddress(addressText)
        val addressErrorResId = if (normalizedAddress != null) {
            if (normalizedAddress.withoutKnownPath in takenAddresses) {
                R.string.already_exists
            } else {
                null
            }
        } else {
            R.string.invalid_address
        }
        addressError = addressErrorResId != null
        addressErrorResId?.let { binding.address.error = getString(it) }
        invalidateState()
    }

    private fun invalidateFingerprint() {
        val fingerprint = binding.fingerprint.text.toString().replace(" ", "")
        val fingerprintInvalid = fingerprint.isNotEmpty() && fingerprint.length != 64
        fingerprintError = fingerprintInvalid
        invalidateState()
    }

    private fun invalidateUsernamePassword() {
        val username = binding.username.text.toString()
        val password = binding.password.text.toString()
        val usernameInvalid = username.contains(':')
        val usernameEmpty = username.isEmpty() && password.isNotEmpty()
        val passwordEmpty = username.isNotEmpty() && password.isEmpty()
        usernamePasswordError = usernameInvalid || usernameEmpty || passwordEmpty
        invalidateState()
    }

    private fun invalidateState() {
        binding.save.isEnabled =
            !addressError && !fingerprintError && !usernamePasswordError && checkDisposable == null
        binding.apply {
            sequenceOf(address, addressMirror, fingerprint, username, password)
                .forEach { it.isEnabled = checkDisposable == null }
        }
    }

    private val String.withoutKnownPath: String
        get() {
            val cropped = pathCropped
            val endsWith = checkPaths.asSequence().filter { it.isNotEmpty() }
                .sortedByDescending { it.length }.find { cropped.endsWith("/$it") }
            return if (endsWith != null) cropped.substring(
                0,
                cropped.length - endsWith.length - 1
            ) else cropped
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

    private fun setMirror(address: String) {
        binding.address.setText(address)
    }

    private fun onSaveRepositoryClick() {
        if (checkDisposable == null) {
            val address = normalizeAddress(binding.address.text.toString())!!
            val fingerprint = binding.fingerprint.text.toString().replace(" ", "")
            val username = binding.username.text.toString().nullIfEmpty()
            val password = binding.password.text.toString().nullIfEmpty()
            val paths = sequenceOf("", "fdroid/repo", "repo")
            val authentication = username?.let { u ->
                password
                    ?.let { p ->
                        Base64.encodeToString(
                            "$u:$p".toByteArray(Charset.defaultCharset()),
                            Base64.NO_WRAP
                        )
                    }
            }
                ?.let { "Basic $it" }.orEmpty()

            checkDisposable = paths
                .fold(Single.just("")) { oldAddressSingle, checkPath ->
                    oldAddressSingle
                        .flatMap { oldAddress ->
                            if (oldAddress.isEmpty()) {
                                val builder = Uri.parse(address).buildUpon()
                                    .let {
                                        if (checkPath.isEmpty()) it else it.appendEncodedPath(
                                            checkPath
                                        )
                                    }
                                val newAddress = builder.build()
                                val indexAddress = builder.appendPath("index.jar").build()
                                RxUtils
                                    .callSingle {
                                        Downloader
                                            .createCall(
                                                Request.Builder().method("HEAD", null)
                                                    .url(indexAddress.toString().toHttpUrl()),
                                                authentication,
                                                null
                                            )
                                    }
                                    .subscribeOn(Schedulers.io())
                                    .map { if (it.code == 200) newAddress.toString() else "" }
                            } else {
                                Single.just(oldAddress)
                            }
                        }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result, throwable ->
                    checkDisposable = null
                    throwable?.printStackTrace()
                    val resultAddress = result?.nullIfEmpty() ?: address
                    val allow = resultAddress == address || run {
                        binding.address.setText(resultAddress)
                        invalidateAddress(resultAddress)
                        !addressError
                    }
                    if (allow) {
                        onSaveRepositoryProceedInvalidate(
                            resultAddress,
                            fingerprint,
                            authentication
                        )
                    } else {
                        invalidateState()
                    }
                }
            invalidateState()
        }
    }

    private fun onSaveRepositoryProceedInvalidate(
        address: String,
        fingerprint: String,
        authentication: String,
    ) = lifecycleScope.launch {
        val binder = syncConnection.binder
        if (binder != null) {
            if (binder.isCurrentlySyncing(repositoryId)) {
                MessageDialog(MessageDialog.Message.CantEditSyncing).show(childFragmentManager)
                invalidateState()
            } else {
                viewModel.updateRepo(
                    viewModel.repo.value?.copy(
                        address = address,
                        fingerprint = fingerprint,
                        authentication = authentication
                    )
                )
                dismissAllowingStateLoss()
            }
        } else {
            invalidateState()
        }
    }

    class SelectMirrorDialog() : DialogFragment() {
        companion object {
            private const val EXTRA_MIRRORS = "mirrors"
        }

        constructor(mirrors: List<String>) : this() {
            arguments = Bundle().apply {
                putStringArrayList(EXTRA_MIRRORS, ArrayList(mirrors))
            }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
            val mirrors = requireArguments().getStringArrayList(EXTRA_MIRRORS)!!
            return MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.select_mirror)
                .setItems(mirrors.toTypedArray()) { _, position ->
                    (parentFragment as EditRepositorySheetX)
                        .setMirror(mirrors[position])
                }
                .setNegativeButton(R.string.cancel, null)
                .create()
        }
    }

    override fun onDeleteConfirm(repositoryId: Long) {
        lifecycleScope.launch {
            if (syncConnection.binder?.deleteRepository(repositoryId) == true)
                dismissAllowingStateLoss()
        }
    }
}
