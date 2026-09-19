package app.opass.ccip.ui.auth

import android.app.NotificationManager
import android.Manifest
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import app.opass.ccip.R
import app.opass.ccip.databinding.FragmentTokenCheckBinding
import app.opass.ccip.databinding.IncludeAuthHeaderBinding
import app.opass.ccip.extension.asyncExecute
import app.opass.ccip.extension.getFastPassUrl
import app.opass.ccip.extension.isInverted
import app.opass.ccip.network.CCIPClient
import app.opass.ccip.push.PushTopicManager
import app.opass.ccip.util.PreferenceUtil
import coil3.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class TokenCheckFragment : AuthActivity.PageFragment() {
    private var hasRequestEnd = false
    private var requestStarted = false
    private var hasErrorOccurred = false
    private val isRetryDisabled: Boolean by lazy { requireArguments().getBoolean(EXTRA_DISABLE_RETRY) }
    private val mActivity: AuthActivity by lazy { requireActivity() as AuthActivity }

    private var _binding: FragmentTokenCheckBinding? = null
    private val binding get() = _binding!!
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun shouldShowNextButton() = hasRequestEnd
    override fun shouldShowPreviousButton() = false
    override fun getNextButtonText() =
        if (hasErrorOccurred && !isRetryDisabled) R.string.try_again
        else R.string.kontinue

    override fun onBackPressed(): Boolean {
        if (hasRequestEnd) {
            if (hasErrorOccurred && !isRetryDisabled) return false
            else mActivity.onAuthFinished()
        }

        // Don't pop the fragment before the request end
        return true
    }

    override fun onNextButtonClicked() {
        if (hasErrorOccurred) mActivity.onBackPressedDispatcher.onBackPressed()
        else mActivity.onAuthFinished()
    }

    override fun onSelected() {
        if (_binding == null || requestStarted) return
        requestStarted = true
        val token = requireArguments().getString(EXTRA_TOKEN) ?: return mActivity.finish()
        val event = PreferenceUtil.getCurrentEvent(mActivity)
        val snapshot = PreferenceUtil.captureIdentity(mActivity, event.eventId)
        val baseUrl = event.getFastPassUrl() ?: return mActivity.finish()
        val push = PushTopicManager.get(mActivity)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = CCIPClient.withBaseUrl(baseUrl).status(token).asyncExecute()
                if (!PreferenceUtil.isCurrent(mActivity, snapshot)) {
                    hasErrorOccurred = true
                    binding.title.setText(R.string.couldnt_verify_your_identity)
                    return@launch
                }
                when {
                    response.isSuccessful -> {
                        val attendee = response.body() ?: error("Missing attendee")
                        if (!push.applyVerifiedIdentity(snapshot, token, attendee, newLogin = true)) {
                            hasErrorOccurred = true
                            binding.title.setText(R.string.couldnt_verify_your_identity)
                            return@launch
                        }
                        binding.title.text = getString(R.string.hi) + attendee.userId
                        binding.message.text = getString(
                            R.string.login_success,
                            event.displayName.findBestMatch(mActivity)
                        )
                        binding.message.isGone = false
                        val header = IncludeAuthHeaderBinding.bind(binding.root)
                        header.confName.isGone = true

                        val manager = mActivity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        if (!manager.areNotificationsEnabled()) {
                            MaterialAlertDialogBuilder(mActivity)
                                .setMessage(R.string.on_login_request_notification_permission)
                                .setPositiveButton(android.R.string.ok) { _, _ ->
                                    if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    else startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${mActivity.packageName}")))
                                }
                                .setNegativeButton(android.R.string.cancel) { _, _ -> }
                                .show()
                        }
                    }
                    response.code() == 403 -> {
                        binding.title.setText(R.string.couldnt_verify_your_identity)
                        binding.message.setText(R.string.connect_to_conference_wifi)
                        binding.message.isGone = false
                        hasErrorOccurred = true
                    }
                    else -> {
                        binding.title.setText(R.string.couldnt_verify_your_identity)
                        binding.message.setText(if (push.isInvalidCredentials(response)) R.string.invalid_token else R.string.unexpected_error_try_again)
                        binding.message.isGone = false
                        hasErrorOccurred = true
                    }
                }
            } catch (t: Throwable) {
                if (_binding == null) return@launch
                binding.title.setText(R.string.couldnt_verify_your_identity)
                binding.message.setText(R.string.offline)
                binding.message.isGone = false
                hasErrorOccurred = true
            } finally {
                if (_binding != null) {
                    binding.progress.isGone = true
                    hasRequestEnd = true
                    mActivity.updateButtonState()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTokenCheckBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val header = IncludeAuthHeaderBinding.bind(binding.root)
        header.notThisEvent.isVisible = false

        val context = requireContext()
        val event = PreferenceUtil.getCurrentEvent(context)
        header.confName.text = event.displayName.findBestMatch(context)
        header.confLogo.isInverted = true
        header.confLogo.load(event.logoUrl)
        onSelected()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val EXTRA_TOKEN = "EXTRA_TOKEN"
        private const val EXTRA_DISABLE_RETRY = "EXTRA_DISABLE_RETRY"
        fun newInstance(token: String, disableRetry: Boolean) = TokenCheckFragment().apply {
            arguments = Bundle().apply {
                putString(EXTRA_TOKEN, token)
                putBoolean(EXTRA_DISABLE_RETRY, disableRetry)
            }
        }
    }
}
