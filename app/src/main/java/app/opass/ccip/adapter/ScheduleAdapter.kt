package app.opass.ccip.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.opass.ccip.R
import app.opass.ccip.model.Session
import com.google.gson.internal.bind.util.ISO8601Utils
import java.text.ParseException
import java.text.ParsePosition
import java.text.SimpleDateFormat

private val SDF = SimpleDateFormat("HH:mm")
private const val FORMAT_ENDTIME = "~ %s, %d%s"

class ScheduleAdapter(
    private val mContext: Context,
    sessionSlotList: List<List<Session>>,
    private val onSessionClicked: (Session) -> Unit,
    private val onToggleStarState: (Session) -> Boolean,
    private val isSessionStarred: (Session) -> Boolean
) : RecyclerView.Adapter<ScheduleViewHolder>() {
    private val differ = AsyncListDiffer(this, ScheduleDiffCallback)

    init {
        update(sessionSlotList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val inflater = LayoutInflater.from(mContext)
        return when (viewType) {
            R.layout.item_session -> ScheduleViewHolder.SessionViewHolder(
                inflater.inflate(viewType, parent, false)
            )
            R.layout.item_start_time -> ScheduleViewHolder.StartTimeViewHolder(
                inflater.inflate(viewType, parent, false)
            )
            else -> throw IllegalStateException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val item = differ.currentList[position]
        when (holder) {
            is ScheduleViewHolder.StartTimeViewHolder -> {
                val date = ISO8601Utils.parse(item as String, ParsePosition(0))
                holder.startTime.text = SDF.format(date)
            }
            is ScheduleViewHolder.SessionViewHolder -> {
                updateStarState(holder.star, isSessionStarred(item as Session))

                val detail = item.getSessionDetail(mContext)
                if (detail.description.isNotEmpty()) {
                    holder.card.setOnClickListener {
                        onSessionClicked(item)
                    }
                    holder.star.setOnClickListener {
                        updateStarState(it as ImageView, onToggleStarState(item))
                    }
                    holder.star.isGone = false
                } else {
                    holder.card.setOnClickListener(null)
                    holder.card.isClickable = false
                    holder.star.isGone = true
                }

                holder.room.text = item.room.getDetails(mContext).name
                holder.title.text = item.getSessionDetail(mContext).title
                holder.type.text = item.type?.getDetails(mContext)?.name ?: ""

                try {
                    val startDate = ISO8601Utils.parse(item.start, ParsePosition(0))
                    val endDate = ISO8601Utils.parse(item.end, ParsePosition(0))
                    holder.endTime.text = String.format(
                        FORMAT_ENDTIME, SDF.format(endDate),
                        (endDate.time - startDate.time) / 1000 / 60,
                        mContext.resources.getString(R.string.min)
                    )
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun getItemCount() = differ.currentList.size

    override fun getItemViewType(position: Int): Int {
        return when (differ.currentList[position]) {
            is Session -> R.layout.item_session
            is String -> R.layout.item_start_time
            else -> throw IllegalStateException("Unknown view type at position $position")
        }
    }

    fun update(sessionSlotList: List<List<Session>>) {
        val list = sessionSlotList.map {
            mutableListOf<Any>(it[0].start!!).apply { addAll(it) }
        }.flatten()
        differ.submitList(list)
    }

    private fun updateStarState(view: ImageView, isStarred: Boolean) {
        if (isStarred) {
            view.setImageResource(R.drawable.ic_bookmark_black_24dp)
            view.setColorFilter(ContextCompat.getColor(mContext, R.color.colorAccent))
        } else {
            view.setImageResource(R.drawable.ic_bookmark_border_black_24dp)
            view.setColorFilter(ContextCompat.getColor(mContext, R.color.colorGray))
        }
    }
}

sealed class ScheduleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    class StartTimeViewHolder(view: View) : ScheduleViewHolder(view) {
        val startTime: TextView = itemView.findViewById(R.id.start_time)
    }

    class SessionViewHolder(view: View) : ScheduleViewHolder(view) {
        val card: CardView = itemView.findViewById(R.id.card)
        val title: TextView = itemView.findViewById(R.id.title)
        val type: TextView = itemView.findViewById(R.id.type)
        val room: TextView = itemView.findViewById(R.id.room)
        val endTime: TextView = itemView.findViewById(R.id.end_time)
        val lang: TextView = itemView.findViewById(R.id.lang)
        val star: ImageView = itemView.findViewById(R.id.star)
    }
}

object ScheduleDiffCallback : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        return when {
            (oldItem is Session && newItem is Session) -> oldItem.id == newItem.id
            (oldItem is String && newItem is String) -> oldItem == newItem
            else -> false
        }
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        return when {
            (oldItem is Session && newItem is Session) -> oldItem == newItem
            (oldItem is String && newItem is String) -> oldItem == newItem
            else -> false
        }
    }

}
