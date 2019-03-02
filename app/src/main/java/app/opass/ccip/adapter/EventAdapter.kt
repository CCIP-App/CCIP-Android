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
import app.opass.ccip.model.Event
import app.opass.ccip.model.EventConfig
import app.opass.ccip.network.CCIPClient
import app.opass.ccip.network.PortalClient
import app.opass.ccip.util.PreferenceUtil
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EventAdapter(private val mContext: Context, private val eventList: List<Event>?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder
        val event: Event = eventList!![position]

        holder.title.text = event.displayName.getDisplayName(mContext)
        Picasso.get().load(event.logoUrl).into(viewHolder.logo)
        holder.itemView.setOnClickListener {
            val eventConfig = PortalClient.get().getEventConfig(event.eventId)
            eventConfig.enqueue(object : Callback<EventConfig> {
                override fun onResponse(call: Call<EventConfig>, response: Response<EventConfig>) {
                    when {
                        response.isSuccessful -> {
                            val eventConfig = response.body()
                            PreferenceUtil.setCurrentEvent(mContext, eventConfig!!)
                            CCIPClient.setBaseUrl(PreferenceUtil.getCurrentEvent(mContext).serverBaseUrl)

                            val intent = Intent()
                            intent.setClass(mContext, MainActivity::class.java)
                            mContext.startActivity(intent)
                            (mContext as Activity).finish()
                        }
                    }
                }

                override fun onFailure(call: Call<EventConfig>, t: Throwable) {

                }
            })
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
