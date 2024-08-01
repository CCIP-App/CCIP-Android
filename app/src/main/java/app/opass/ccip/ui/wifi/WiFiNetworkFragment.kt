package app.opass.ccip.ui.wifi

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import androidx.core.content.getSystemService
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.opass.ccip.R
import app.opass.ccip.model.WifiNetworkInfo
import app.opass.ccip.util.WifiUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class WiFiNetworkFragment(private val wifiNetworkInfoList: List<WifiNetworkInfo>) :
    DialogFragment() {

    companion object {
        private const val TAG = "WiFiNetworkFragment"

        fun show(wifiNetworkInfoList: List<WifiNetworkInfo>, fragmentManager: FragmentManager) {
            WiFiNetworkFragment(wifiNetworkInfoList).show(fragmentManager, TAG)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val recyclerView = RecyclerView(requireContext()).apply {
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.MATCH_PARENT
            )
            layoutManager = LinearLayoutManager(requireContext())
            adapter = WifiNetworkAdapter(wifiNetworkInfoList) { info ->
                onWifiSelected(info)
                dialog?.dismiss()
            }
        }
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.choose_network_to_connect)
            .setView(recyclerView)
            .create()
    }

    private fun onWifiSelected(info: WifiNetworkInfo) {
        val rootView = requireActivity().window.decorView.rootView

        // Return early if password is null or empty
        if (info.password.isNullOrEmpty()) {
            Snackbar.make(rootView, R.string.failed_to_save_wifi, Snackbar.LENGTH_LONG).show()
            return
        }

        if (WifiUtil.installNetwork(requireContext(), info)) {
            Snackbar.make(rootView, R.string.wifi_saved, Snackbar.LENGTH_SHORT).show()
        } else {
            requireContext().getSystemService<ClipboardManager>()?.run {
                setPrimaryClip(ClipData.newPlainText("", info.password))
            } ?: return
            Snackbar.make(
                rootView,
                R.string.failed_to_save_wifi_copied_to_clipboard,
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
}
