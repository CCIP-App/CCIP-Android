package app.opass.ccip.ui.wifi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.marginTop
import androidx.recyclerview.widget.RecyclerView
import app.opass.ccip.R
import app.opass.ccip.extension.updateMargin
import app.opass.ccip.model.WifiNetworkInfo

class WifiNetworkAdapter(
    private val items: List<WifiNetworkInfo>,
    private val onItemClick: (WifiNetworkInfo) -> Unit
) : RecyclerView.Adapter<WifiNetworkViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WifiNetworkViewHolder =
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wifi_network, parent, false)
            .let(::WifiNetworkViewHolder)
            .apply {
                itemView.setOnClickListener {
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION) onItemClick(items[pos])
                }
            }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: WifiNetworkViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.ssid

        val hasPassword = !item.password.isNullOrEmpty()
        if (!hasPassword) {
            holder.password.isGone = true
            return
        }

        holder.password.text = item.password
        holder.password.isGone = false

        if (position == 0) {
            holder.networkItem.updateMargin(top = 36)
        }

        if (position == items.size - 1) {
            holder.networkItem.updateMargin(bottom = 36)
        }
    }
}

class WifiNetworkViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val networkItem: LinearLayout = view.findViewById(R.id.network_item)
    val name: TextView = view.findViewById(R.id.network_name)
    val password: TextView = view.findViewById(R.id.network_password)
}
