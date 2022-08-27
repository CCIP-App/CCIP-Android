package app.opass.ccip.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.text.buildSpannedString
import app.opass.ccip.R
import app.opass.ccip.databinding.FragmentTokenEntryBinding
import app.opass.ccip.databinding.IncludeAuthHeaderBinding
import app.opass.ccip.extension.clickable
import app.opass.ccip.extension.focusAndShowKeyboard
import app.opass.ccip.extension.isInverted
import app.opass.ccip.util.PreferenceUtil
import com.squareup.picasso.Picasso
import me.saket.bettermovementmethod.BetterLinkMovementMethod

class TokenEntryFragment : AuthActivity.PageFragment() {
    private val mActivity: AuthActivity by lazy { requireActivity() as AuthActivity }
    private var _binding: FragmentTokenEntryBinding? = null
    private val binding get() = _binding!!
    override fun shouldShowNextButton() = true
    override fun getNextButtonText() = android.R.string.ok
    override fun getPreviousButtonText() = R.string.back

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTokenEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tokenInput.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onNextButtonClicked()
                return@OnEditorActionListener true
            }
            false
        })
        val header = IncludeAuthHeaderBinding.bind(binding.root)
        val built = buildSpannedString {
            clickable(mActivity::switchEvent) {
                append(getString(R.string.not_this_event))
            }
        }
        header.notThisEvent.text = built
        header.notThisEvent.movementMethod = BetterLinkMovementMethod.newInstance()

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

    override fun onNextButtonClicked() {
        val input = binding.tokenInput.text.toString().trim()
        if (input.isEmpty()) {
            binding.tokenInputLayout.error = getString(R.string.token_required)
            return
        }
        mActivity.processToken(input)
        binding.tokenInputLayout.error = ""
        binding.tokenInput.text?.clear()
    }

    override fun onSelected() {
        binding.tokenInput.focusAndShowKeyboard()
    }
}
