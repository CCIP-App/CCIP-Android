package app.opass.ccip.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.opass.ccip.R
import app.opass.ccip.model.Submission
import com.google.gson.internal.bind.util.ISO8601Utils
import java.text.ParseException
import java.text.ParsePosition
import java.text.SimpleDateFormat

class ScheduleAdapter(private val mContext: Context, private var mSubmissionSlotList: List<List<Submission>>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private val SDF = SimpleDateFormat("HH:mm")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = LayoutInflater.from(parent.context)
        .inflate(R.layout.item_schedule, parent, false)
        .let(::ViewHolder)

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder

        holder.submissionView.layoutManager = LinearLayoutManager(mContext)
        holder.submissionView.itemAnimator = DefaultItemAnimator()

        val submissions = mSubmissionSlotList[position]
        try {
            val date = ISO8601Utils.parse(submissions[0].start, ParsePosition(0))
            holder.startTimeText.text = SDF.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        holder.submissionView.adapter = SubmissionAdapter(mContext, submissions)
    }

    override fun getItemCount() = mSubmissionSlotList.size

    fun update(submissionSlotList: List<List<Submission>>) {
        mSubmissionSlotList = submissionSlotList
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val startTimeText: TextView = itemView.findViewById(R.id.start_time)
        val submissionView: RecyclerView = itemView.findViewById(R.id.programs)
    }
}
