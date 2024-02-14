package app.opass.ccip.ui.wifi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import app.opass.ccip.R
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
    }
}

class WifiNetworkViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val name: TextView = view.findViewById(R.id.network_name)
    val password: TextView = view.findViewById(R.id.network_password)
}
