package app.opass.ccip.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import app.opass.ccip.R
import app.opass.ccip.activity.AuthActivity
import app.opass.ccip.extension.asyncExecute
import app.opass.ccip.network.CCIPClient
import app.opass.ccip.util.PreferenceUtil
import com.onesignal.OneSignal
import kotlinx.android.synthetic.main.fragment_token_check.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import kotlin.coroutines.CoroutineContext

class TokenCheckFragment : AuthActivity.PageFragment(), CoroutineScope {
    private var hasRequestEnd = false
    private var hasErrorOccurred = false
    private val isRetryDisabled: Boolean by lazy { requireArguments().getBoolean(EXTRA_DISABLE_RETRY) }
    private val mActivity: AuthActivity by lazy { requireActivity() as AuthActivity }

    private val mJob: Job by lazy { Job() }
    override val coroutineContext: CoroutineContext
        get() = mJob + Dispatchers.Main

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
        mActivity.hideKeyboard()

        val token = arguments!!.getString(EXTRA_TOKEN)
        launch {
            try {
                val response = CCIPClient.get().status(token).asyncExecute()
                when {
                    response.isSuccessful -> {
                        val attendee = response.body()!!
                        title.text = getString(R.string.hi) + attendee.userId
                        message.text = getString(
                            R.string.login_success,
                            PreferenceUtil.getCurrentEvent(mActivity).displayName.findBestMatch(mActivity)
                        )
                        message.isGone = false

                        PreferenceUtil.setToken(mActivity, token)
                        try {
                            JSONObject()
                                .put(attendee.eventId + attendee.type, attendee.token)
                                .let(OneSignal::sendTags)
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    response.code() == 403 -> {
                        title.setText(R.string.couldnt_verify_your_identity)
                        message.setText(R.string.connect_to_conference_wifi)
                        message.isGone = false
                        hasErrorOccurred = true
                    }
                    else -> {
                        title.setText(R.string.couldnt_verify_your_identity)
                        message.setText(R.string.invalid_token)
                        message.isGone = false
                        hasErrorOccurred = true
                    }
                }
            } catch (t: Throwable) {
                // TODO: Should check network status before method selection, so we can assume the exception caught
                //  here can't be fixed by user. (should display "Unexpected Error")
                t.printStackTrace()
                title.setText(R.string.couldnt_verify_your_identity)
                message.setText(R.string.offline)
                message.isGone = false
                hasErrorOccurred = true
            } finally {
                progress.isGone = true
                hasRequestEnd = true
                mActivity.updateButtonState()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_token_check, container, false)
    }

    override fun onDestroy() {
        mJob.cancel()
        super.onDestroy()
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
