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
import app.opass.ccip.model.Session
import app.opass.ccip.util.PreferenceUtil
import com.google.gson.internal.bind.util.ISO8601Utils
import java.text.ParseException
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*

class ScheduleFragment : Fragment() {
    companion object {
        private val SDF_DATE = SimpleDateFormat("MM/dd")

        fun newInstance(date: String, sessions: List<Session>): Fragment {
            val scheduleFragment = ScheduleFragment()
            scheduleFragment.date = date
            scheduleFragment.mSessions = sessions
            return scheduleFragment
        }
    }

    private var scheduleView: RecyclerView? = null
    private lateinit var mActivity: Activity
    private var mSessions: List<Session>? = null
    private var date: String? = null
    private var starFilter = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_schedule, container, false)

        scheduleView = view.findViewById(R.id.schedule)

        mActivity = requireActivity()
        scheduleView?.layoutManager = LinearLayoutManager(mActivity)
        scheduleView?.itemAnimator = DefaultItemAnimator()

        if (mSessions != null) {
            scheduleView?.adapter = ScheduleAdapter(mActivity, toSessionsGroupedByTime(mSessions))
        }

        return view
    }

    private fun loadStarSessions(): List<Session> {
        val getDateOrNull: (String?) -> String? = {
            try {
                it?.let { SDF_DATE.format(ISO8601Utils.parse(it, ParsePosition(0))) }
            } catch (e: ParseException) {
                e.printStackTrace()
                null
            }
        }

        return PreferenceUtil.loadStars(mActivity).filter {
            getDateOrNull(it.start) == date
        }
    }

    private fun toSessionsGroupedByTime(sessions: List<Session>?): List<List<Session>> {
        return sessions!!
            .filter { it.start != null }
            .groupBy { it.start }
            .values
            .sortedBy { it[0].start }
            .map { it.sortedWith(Comparator { (_, room1), (_, room2) -> room1.id.compareTo(room2.id) }) }
    }

    fun toggleStarFilter(isStar: Boolean) {
        this.starFilter = isStar
        (scheduleView?.adapter as? ScheduleAdapter)?.update(
            toSessionsGroupedByTime(if (isStar) loadStarSessions() else mSessions)
        )
    }

    override fun onResume() {
        super.onResume()
        toggleStarFilter(starFilter)
    }
}
