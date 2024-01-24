package app.opass.ccip.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.opass.ccip.R
import app.opass.ccip.databinding.FragmentMethodSelectionBinding
import app.opass.ccip.databinding.IncludeAuthHeaderBinding
import app.opass.ccip.extension.isInverted
import app.opass.ccip.ui.event.EventActivity
import app.opass.ccip.util.PreferenceUtil
import coil.load

class MethodSelectionFragment : AuthActivity.PageFragment() {
    private val mActivity: AuthActivity by lazy { requireActivity() as AuthActivity }
    override fun shouldShowNextButton() = false
    override fun getPreviousButtonText() = R.string.cancel

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
        header.notThisEvent.setOnClickListener {
            startActivity(Intent(requireContext(), EventActivity::class.java))
        }

        val context = requireContext()
        val event = PreferenceUtil.getCurrentEvent(context)
        header.confName.text = event.displayName.findBestMatch(context)
        header.confLogo.isInverted = true
        header.confLogo.load(event.logoUrl)

        return binding.root
    }

    private fun sendResult(method: AuthActivity.AuthMethod) = mActivity.onAuthMethodSelected(method)
}
