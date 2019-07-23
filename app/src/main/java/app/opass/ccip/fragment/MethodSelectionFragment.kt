package app.opass.ccip.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import app.opass.ccip.R
import app.opass.ccip.activity.AuthActivity

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
        val view = inflater.inflate(R.layout.fragment_method_selection, container, false)
        view.findViewById<LinearLayout>(R.id.from_camera)
            .setOnClickListener { sendResult(AuthActivity.AuthMethod.SCAN_FROM_CAMERA) }
        view.findViewById<LinearLayout>(R.id.from_gallery)
            .setOnClickListener { sendResult(AuthActivity.AuthMethod.SCAN_FROM_GALLERY) }
        view.findViewById<LinearLayout>(R.id.enter_token)
            .setOnClickListener { sendResult(AuthActivity.AuthMethod.ENTER_TOKEN) }

        return view
    }

    private fun sendResult(method: AuthActivity.AuthMethod) = mActivity.onAuthMethodSelected(method)
}
