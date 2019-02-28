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
import app.opass.ccip.activity.SubmissionDetailActivity
import app.opass.ccip.model.Submission
import app.opass.ccip.util.AlarmUtil
import app.opass.ccip.util.JsonUtil
import app.opass.ccip.util.PreferenceUtil
import com.google.gson.internal.bind.util.ISO8601Utils
import java.text.ParseException
import java.text.ParsePosition
import java.text.SimpleDateFormat

class SubmissionAdapter(private val mContext: Context, private val mSubmissionList: List<Submission>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private val SDF = SimpleDateFormat("HH:mm")
        private const val FORMAT_ENDTIME = "~ %s, %d%s"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = LayoutInflater.from(parent.context)
        .inflate(R.layout.item_submission, parent, false)
        .let(::ViewHolder)

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder
        val submission = mSubmissionList[position]

        holder.room.text = submission.room

        holder.subject.text = submission.getSubmissionDetail(mContext).title

        try {
            val startDate = ISO8601Utils.parse(submission.start, ParsePosition(0))
            val endDate = ISO8601Utils.parse(submission.end, ParsePosition(0))
            holder.endTime.text = String.format(
                FORMAT_ENDTIME, SDF.format(endDate),
                (endDate.time - startDate.time) / 1000 / 60,
                mContext.resources.getString(R.string.min)
            )
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        try {
            holder.type.setText(Submission.getTypeString(submission.type))
        } catch (e: Resources.NotFoundException) {
            holder.type.text = ""
            e.printStackTrace()
        }

        if (!submission.getSubmissionDetail(mContext).description.isEmpty()) {
            toggleStar(holder.star, isSubmissionStar(mContext, submission))

            holder.star.setOnClickListener {
                updateStarSubmissions(mContext, submission)
                toggleStar(holder.star, isSubmissionStar(mContext, submission))
            }

            holder.card.isClickable = true
            holder.card.setOnClickListener {
                val intent = Intent()
                intent.setClass(mContext, SubmissionDetailActivity::class.java)
                intent.putExtra(SubmissionDetailActivity.INTENT_EXTRA_PROGRAM, JsonUtil.toJson(submission))
                mContext.startActivity(intent)
            }
        }

    }

    override fun getItemCount() = mSubmissionList.size

    private fun isSubmissionStar(context: Context, submission: Submission): Boolean {
        val submissions = PreferenceUtil.loadStars(context)
        return submissions != null && submissions.contains(submission)
    }

    private fun updateStarSubmissions(context: Context, submission: Submission) {
        val submissions = PreferenceUtil.loadStars(context)
        if (submissions.contains(submission)) {
            submissions.remove(submission)
            AlarmUtil.cancelSubmissionAlarm(context, submission)
        } else {
            submissions.add(submission)
            AlarmUtil.setSubmissionAlarm(context, submission)
        }
        PreferenceUtil.saveStars(context, submissions)
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
