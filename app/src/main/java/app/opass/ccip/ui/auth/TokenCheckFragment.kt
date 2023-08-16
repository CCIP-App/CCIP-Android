package app.opass.ccip.ui.auth

import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import app.opass.ccip.R
import app.opass.ccip.databinding.FragmentTokenCheckBinding
import app.opass.ccip.databinding.IncludeAuthHeaderBinding
import app.opass.ccip.extension.asyncExecute
import app.opass.ccip.extension.getFastPassUrl
import app.opass.ccip.extension.isInverted
import app.opass.ccip.network.CCIPClient
import app.opass.ccip.util.PreferenceUtil
import com.onesignal.OneSignal
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class TokenCheckFragment : AuthActivity.PageFragment() {
    private var hasRequestEnd = false
    private var hasErrorOccurred = false
    private val isRetryDisabled: Boolean by lazy { requireArguments().getBoolean(EXTRA_DISABLE_RETRY) }
    private val mActivity: AuthActivity by lazy { requireActivity() as AuthActivity }

    private var _binding: FragmentTokenCheckBinding? = null
    private val binding get() = _binding!!

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
        if (hasErrorOccurred) mActivity.onBackPressed()
        else mActivity.onAuthFinished()
    }

    override fun onSelected() {
        val token = requireArguments().getString(EXTRA_TOKEN)
        val baseUrl = PreferenceUtil.getCurrentEvent(mActivity).getFastPassUrl() ?: return mActivity.finish()
        lifecycleScope.launch {
            try {
                val response = CCIPClient.withBaseUrl(baseUrl).status(token).asyncExecute()
                when {
                    response.isSuccessful -> {
                        val attendee = response.body()!!
                        binding.title.text = getString(R.string.hi) + attendee.userId
                        binding.message.text = getString(
                            R.string.login_success,
                            PreferenceUtil.getCurrentEvent(mActivity).displayName.findBestMatch(mActivity)
                        )
                        binding.message.isGone = false
                        val header = IncludeAuthHeaderBinding.bind(binding.root)
                        header.confName.isGone = true

                        PreferenceUtil.setToken(mActivity, token)
                        PreferenceUtil.setRole(mActivity, attendee.role)
                        try {
                            JSONObject()
                                .put(attendee.eventId + attendee.role, attendee.token)
                                .let(OneSignal::sendTags)
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                        val manager = mActivity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        if (!manager.areNotificationsEnabled()) {
                            AlertDialog.Builder(mActivity)
                                .setMessage(R.string.on_login_request_notification_permission)
                                .setPositiveButton(android.R.string.ok) { _, _ -> OneSignal.promptForPushNotifications() }
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
                        binding.message.setText(R.string.invalid_token)
                        binding.message.isGone = false
                        hasErrorOccurred = true
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                binding.title.setText(R.string.couldnt_verify_your_identity)
                binding.message.setText(R.string.offline)
                binding.message.isGone = false
                hasErrorOccurred = true
            } finally {
                binding.progress.isGone = true
                hasRequestEnd = true
                mActivity.updateButtonState()
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
        Picasso.get().load(event.logoUrl).into(header.confLogo)
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
