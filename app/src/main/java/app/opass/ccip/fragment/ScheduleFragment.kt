package app.opass.ccip.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.opass.ccip.R
import app.opass.ccip.adapter.ScheduleAdapter
import app.opass.ccip.model.Submission
import app.opass.ccip.util.PreferenceUtil
import com.google.gson.internal.bind.util.ISO8601Utils

import java.text.ParseException
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ScheduleFragment : Fragment() {
    companion object {
        private val SDF_DATE = SimpleDateFormat("MM/dd")

        fun newInstance(date: String, submissions: List<Submission>): Fragment {
            val scheduleFragment = ScheduleFragment()
            scheduleFragment.date = date
            scheduleFragment.mSubmissions = submissions
            return scheduleFragment
        }
    }

    private var scheduleView: RecyclerView? = null
    private lateinit var mActivity: Activity
    private var mSubmissions: List<Submission>? = null
    private var date: String? = null
    private var starFilter = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_schedule, container, false)

        scheduleView = view.findViewById(R.id.schedule)

        mActivity = requireActivity()
        scheduleView?.layoutManager = LinearLayoutManager(mActivity)
        scheduleView?.itemAnimator = DefaultItemAnimator()

        if (mSubmissions != null) {
            scheduleView?.adapter = ScheduleAdapter(mActivity, transformSubmissions(mSubmissions))
        }

        return view
    }

    private fun loadStarSubmissions(): List<Submission> {
        val tmp = ArrayList<Submission>()
        val starSubmissions = PreferenceUtil.loadStars(mActivity)
        for (submission in starSubmissions) {
            try {
                val tmpDate = SDF_DATE
                    .format(ISO8601Utils.parse(submission.start, ParsePosition(0)))
                if (tmpDate == date) {
                    tmp.add(submission)
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
        return tmp
    }

    private fun transformSubmissions(submissions: List<Submission>?): List<List<Submission>> {
        val map = HashMap<String, ArrayList<Submission>>()
        for (submission in submissions!!) {
            if (submission.start == null) continue

            if (map.containsKey(submission.start)) {
                val tmp = map[submission.start]
                tmp!!.add(submission)
                tmp.sortWith(Comparator { (_, room1), (_, room2) -> room1.compareTo(room2) })
                map[submission.start] = tmp
            } else {
                val list = ArrayList<Submission>()
                list.add(submission)
                map[submission.start] = list
            }
        }

        val keys = TreeSet(map.keys)
        val submissionSlotList = ArrayList<ArrayList<Submission>>()
        for (key in keys) {
            submissionSlotList.add(map[key]!!)
        }
        return submissionSlotList
    }

    fun toggleStarFilter(isStar: Boolean) {
        this.starFilter = isStar
        (scheduleView?.adapter as? ScheduleAdapter)?.update(
            transformSubmissions(if (isStar) loadStarSubmissions() else mSubmissions)
        )
    }

    override fun onResume() {
        super.onResume()
        toggleStarFilter(starFilter)
    }
}
