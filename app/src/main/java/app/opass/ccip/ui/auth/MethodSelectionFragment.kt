package app.opass.ccip.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.buildSpannedString
import app.opass.ccip.R
import app.opass.ccip.databinding.FragmentMethodSelectionBinding
import app.opass.ccip.databinding.IncludeAuthHeaderBinding
import app.opass.ccip.extension.clickable
import app.opass.ccip.extension.isInverted
import app.opass.ccip.util.PreferenceUtil
import com.squareup.picasso.Picasso
import me.saket.bettermovementmethod.BetterLinkMovementMethod

class MethodSelectionFragment : AuthActivity.PageFragment() {
    private val mActivity: AuthActivity by lazy { requireActivity() as AuthActivity }
    override fun shouldShowNextButton() = false
    override fun getPreviousButtonText() = R.string.cancel

    override fun onSelected() {
        if (isAdded) mActivity.hideKeyboard()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentMethodSelectionBinding.inflate(inflater, container, false)
        binding.fromCamera
            .setOnClickListener { sendResult(AuthActivity.AuthMethod.SCAN_FROM_CAMERA) }
        binding.fromGallery
            .setOnClickListener { sendResult(AuthActivity.AuthMethod.SCAN_FROM_GALLERY) }
        binding.enterToken
            .setOnClickListener { sendResult(AuthActivity.AuthMethod.ENTER_TOKEN) }

        val header = IncludeAuthHeaderBinding.bind(binding.root)
        val built = buildSpannedString {
            clickable(mActivity::switchEvent) {
                append(getString(R.string.not_this_event))
            }
        }
        header.notThisEvent.text = built
        header.notThisEvent.movementMethod = BetterLinkMovementMethod.getInstance()

        val context = requireContext()
        val event = PreferenceUtil.getCurrentEvent(context)
        header.confName.text = event.displayName.findBestMatch(context)
        header.confLogo.isInverted = true
        Picasso.get().load(event.logoUrl).into(header.confLogo)

        return binding.root
    }

    private fun sendResult(method: AuthActivity.AuthMethod) = mActivity.onAuthMethodSelected(method)
}
