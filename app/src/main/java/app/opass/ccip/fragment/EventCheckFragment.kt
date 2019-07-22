package app.opass.ccip.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import app.opass.ccip.R
import app.opass.ccip.activity.AuthActivity
import app.opass.ccip.extension.asyncExecute
import app.opass.ccip.network.PortalClient
import app.opass.ccip.util.PreferenceUtil
import kotlinx.android.synthetic.main.fragment_event_check.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class EventCheckFragment : AuthActivity.PageFragment(), CoroutineScope {
    private var hasRequestEnd = false
    private var shouldRetry = false
    private val mActivity: AuthActivity by lazy { requireActivity() as AuthActivity }

    private val mJob: Job by lazy { Job() }
    override val coroutineContext: CoroutineContext
        get() = mJob + Dispatchers.Main

    override fun shouldShowPreviousButton() = false
    override fun shouldShowNextButton() = hasRequestEnd && shouldRetry
    override fun getNextButtonText() = R.string.kontinue

    override fun onBackPressed(): Boolean {
        if (hasRequestEnd) onNextButtonClicked()
        return true
    }

    override fun onNextButtonClicked() {
        mActivity.onEventChecked(false)
    }

    override fun onSelected() {
        if (hasRequestEnd) mActivity.finish()
        launch {
            try {
                val response = PortalClient.get().getEventConfig(arguments!!.getString(EXTRA_EVENT_ID)!!).asyncExecute()
                if (response.isSuccessful) {
                    val eventConfig = response.body()!!
                    PreferenceUtil.setCurrentEvent(mActivity, eventConfig)
                } else {
                    shouldRetry = true
                    title.setText(R.string.couldnt_get_event_info)
                    message.setText(R.string.unexpected_error_try_again)
                    message.isGone = false
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                shouldRetry = true
                title.setText(R.string.couldnt_get_event_info)
                message.setText(R.string.offline)
                message.isGone = false
            } finally {
                hasRequestEnd = true
                progress.isGone = true
                if (shouldRetry) mActivity.updateButtonState()
                else mActivity.onEventChecked(true)
            }

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_check, container, false)
    }

    override fun onDestroy() {
        mJob.cancel()
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_EVENT_ID = "EXTRA_EVENT_ID"
        fun newInstance(eventId: String) = EventCheckFragment().apply {
            arguments = Bundle().apply {
                putString(EXTRA_EVENT_ID, eventId)
            }
        }
    }
}
