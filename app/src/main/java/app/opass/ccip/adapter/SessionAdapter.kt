package app.opass.ccip.adapter

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.opass.ccip.R
import app.opass.ccip.activity.SessionDetailActivity
import app.opass.ccip.model.Session
import app.opass.ccip.util.AlarmUtil
import app.opass.ccip.util.JsonUtil
import app.opass.ccip.util.PreferenceUtil
import com.google.gson.internal.bind.util.ISO8601Utils
import java.text.ParseException
import java.text.ParsePosition
import java.text.SimpleDateFormat

class SessionAdapter(private val mContext: Context, private val mSessionList: List<Session>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private val SDF = SimpleDateFormat("HH:mm")
        private const val FORMAT_ENDTIME = "~ %s, %d%s"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = LayoutInflater.from(parent.context)
        .inflate(R.layout.item_session, parent, false)
        .let(::ViewHolder)

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder
        val session = mSessionList[position]

        holder.room.text = session.room

        holder.subject.text = session.getSessionDetail(mContext).title

        try {
            val startDate = ISO8601Utils.parse(session.start, ParsePosition(0))
            val endDate = ISO8601Utils.parse(session.end, ParsePosition(0))
            holder.endTime.text = String.format(
                FORMAT_ENDTIME, SDF.format(endDate),
                (endDate.time - startDate.time) / 1000 / 60,
                mContext.resources.getString(R.string.min)
            )
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        try {
            holder.type.setText(Session.getTypeString(session.type))
        } catch (e: Resources.NotFoundException) {
            holder.type.text = ""
            e.printStackTrace()
        }

        if (!session.getSessionDetail(mContext).description.isEmpty()) {
            toggleStar(holder.star, isSessionStar(mContext, session))

            holder.star.setOnClickListener {
                updateStarSessions(mContext, session)
                toggleStar(holder.star, isSessionStar(mContext, session))
            }

            holder.card.isClickable = true
            holder.card.setOnClickListener {
                val intent = Intent()
                intent.setClass(mContext, SessionDetailActivity::class.java)
                intent.putExtra(SessionDetailActivity.INTENT_EXTRA_PROGRAM, JsonUtil.toJson(session))
                mContext.startActivity(intent)
            }
        }

    }

    override fun getItemCount() = mSessionList.size

    private fun isSessionStar(context: Context, session: Session): Boolean {
        val sessions = PreferenceUtil.loadStars(context)
        return sessions != null && sessions.contains(session)
    }

    private fun updateStarSessions(context: Context, session: Session) {
        val sessions = PreferenceUtil.loadStars(context)
        if (sessions.contains(session)) {
            sessions.remove(session)
            AlarmUtil.cancelSessionAlarm(context, session)
        } else {
            sessions.add(session)
            AlarmUtil.setSessionAlarm(context, session)
        }
        PreferenceUtil.saveStars(context, sessions)
    }

    private fun toggleStar(star: ImageView, isStar: Boolean) {
        if (isStar) {
            star.setImageResource(R.drawable.ic_bookmark_black_24dp)
            star.setColorFilter(ContextCompat.getColor(mContext, R.color.colorAccent))
        } else {
            star.setImageResource(R.drawable.ic_bookmark_border_black_24dp)
            star.setColorFilter(ContextCompat.getColor(mContext, R.color.colorGray))
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var card: CardView = itemView.findViewById(R.id.card)
        var subject: TextView = itemView.findViewById(R.id.subject)
        var type: TextView = itemView.findViewById(R.id.type)
        var room: TextView = itemView.findViewById(R.id.room)
        var endTime: TextView = itemView.findViewById(R.id.end_time)
        var lang: TextView = itemView.findViewById(R.id.lang)
        var star: ImageView = itemView.findViewById(R.id.star)
    }
}
