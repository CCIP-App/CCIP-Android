package app.opass.ccip.ui.announcement

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.opass.ccip.R
import app.opass.ccip.model.Announcement
import app.opass.ccip.util.LocaleUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AnnouncementAdapter(private val mContext: Context, private val announcementList: List<Announcement>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val mTypedValue = TypedValue()
    private val mBackground = mTypedValue.resourceId

    init {
        mContext.theme.resolveAttribute(android.R.attr.selectableItemBackground, mTypedValue, true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_announcement, parent, false)

        itemView.setBackgroundResource(mBackground)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder
        val sdf = SimpleDateFormat("MM/dd HH:mm")
        val (datetime, msgEn, msgZh, uri) = announcementList[position]

        if (LocaleUtil.getCurrentLocale(mContext).toString().startsWith(Locale.TAIWAN.toString())) {
            holder.msg.text = msgZh
        } else {
            holder.msg.text = msgEn
        }
        holder.time.text = sdf.format(Date(datetime * 1000L))
        holder.itemView.setOnClickListener {
            if (!uri.isEmpty()) {
                mContext.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
            }
        }
    }

    override fun getItemCount(): Int {
        return announcementList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val msg: TextView = itemView.findViewById(R.id.invalid_token_msg)
        val time: TextView = itemView.findViewById(R.id.time)
    }
}
