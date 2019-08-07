package app.opass.ccip.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.opass.ccip.R
import app.opass.ccip.activity.MainActivity
import app.opass.ccip.extension.asyncExecute
import app.opass.ccip.model.Event
import app.opass.ccip.network.CCIPClient
import app.opass.ccip.network.PortalClient
import app.opass.ccip.util.PreferenceUtil
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EventAdapter(private val mContext: Context, private val eventList: List<Event>?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder
        val event: Event = eventList!![position]

        holder.title.text = event.displayName.findBestMatch(mContext)
        Picasso.get().load(event.logoUrl).into(viewHolder.logo)
        holder.itemView.setOnClickListener {
            launch {
                try {
                    PortalClient.get().getEventConfig(event.eventId).asyncExecute().run {
                        if (!isSuccessful) return@run

                        PreferenceUtil.setCurrentEvent(mContext, body()!!)
                        CCIPClient.setBaseUrl(PreferenceUtil.getCurrentEvent(mContext).serverBaseUrl)

                        val intent = Intent()
                        intent.setClass(mContext, MainActivity::class.java)
                        mContext.startActivity(intent)
                        (mContext as Activity).finish()
                    }
                } catch (e: Exception) {
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return eventList!!.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.event_name)
        val logo: ImageView = itemView.findViewById(R.id.event_logo)
    }
}
