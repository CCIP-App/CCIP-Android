package app.opass.ccip.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import app.opass.ccip.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class NotificationDialogFragment (private val callback: () -> Unit) : DialogFragment() {

    companion object {
        const val TAG = "NotificationDialogFragment"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.get_notified_title))
            .setMessage(getString(R.string.get_notified_desc))
            .setPositiveButton(android.R.string.ok) { _, _ ->
                callback()
            }
            .setNegativeButton(android.R.string.cancel)  { _, _ ->
                dismiss()
            }
            .create()
    }
}
