package app.opass.ccip.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import app.opass.ccip.R
import app.opass.ccip.activity.AuthActivity
import kotlinx.android.synthetic.main.dialog_enter_token.*

class TokenEntryFragment : AuthActivity.PageFragment() {
    private val mActivity: AuthActivity by lazy { requireActivity() as AuthActivity }
    override fun shouldShowNextButton() = true
    override fun getNextButtonText() = android.R.string.ok
    override fun getPreviousButtonText() = R.string.back

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_token_entry, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        token_input.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onNextButtonClicked()
                return@OnEditorActionListener true
            }
            false
        })
    }

    override fun onNextButtonClicked() {
        val input = token_input.text.toString().trim()
        if (input.isEmpty()) {
            token_input_layout.error = getString(R.string.token_required)
            return
        }
        mActivity.processToken(input)
        token_input_layout.error = ""
        token_input.text?.clear()
    }

    override fun onSelected() {
        token_input.requestFocus()
        mActivity.showKeyboard()
    }
}
