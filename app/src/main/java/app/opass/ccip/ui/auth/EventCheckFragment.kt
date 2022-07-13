package app.opass.ccip.ui.auth

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import app.opass.ccip.R
import app.opass.ccip.databinding.FragmentEventCheckBinding
import app.opass.ccip.extension.asyncExecute
import app.opass.ccip.network.PortalClient
import app.opass.ccip.util.PreferenceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class EventCheckFragment : AuthActivity.PageFragment(R.layout.fragment_event_check), CoroutineScope {
    private var hasRequestEnd = false
    private var shouldRetry = false
    private val mActivity: AuthActivity by lazy { requireActivity() as AuthActivity }
    private var _binding: FragmentEventCheckBinding? = null
    private val binding get() = _binding!!

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
                val response = PortalClient.get().getEventConfig(requireArguments().getString(EXTRA_EVENT_ID)!!).asyncExecute()
                if (response.isSuccessful) {
                    val eventConfig = response.body()!!
                    PreferenceUtil.setCurrentEvent(mActivity, eventConfig)
                } else {
                    shouldRetry = true
                    binding.title.setText(R.string.couldnt_get_event_info)
                    binding.message.setText(R.string.unexpected_error_try_again)
                    binding.message.isGone = false
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                shouldRetry = true
                binding.title.setText(R.string.couldnt_get_event_info)
                binding.message.setText(R.string.offline)
                binding.message.isGone = false
            } finally {
                hasRequestEnd = true
                binding.progress.isGone = true
                if (shouldRetry) mActivity.updateButtonState()
                else mActivity.onEventChecked(true)
            }

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEventCheckBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
