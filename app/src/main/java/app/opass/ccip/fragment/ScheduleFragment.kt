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
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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
            scheduleView?.adapter = ScheduleAdapter(mActivity, transformSessions(mSessions))
        }

        return view
    }

    private fun loadStarSessions(): List<Session> {
        val tmp = ArrayList<Session>()
        val starSessions = PreferenceUtil.loadStars(mActivity)
        for (session in starSessions) {
            try {
                val tmpDate = SDF_DATE
                    .format(ISO8601Utils.parse(session.start, ParsePosition(0)))
                if (tmpDate == date) {
                    tmp.add(session)
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
        return tmp
    }

    private fun transformSessions(sessions: List<Session>?): List<List<Session>> {
        val map = HashMap<String, ArrayList<Session>>()
        for (session in sessions!!) {
            if (session.start == null) continue

            if (map.containsKey(session.start)) {
                val tmp = map[session.start]
                tmp!!.add(session)
                tmp.sortWith(Comparator { (_, room1), (_, room2) -> room1.compareTo(room2) })
                map[session.start] = tmp
            } else {
                val list = ArrayList<Session>()
                list.add(session)
                map[session.start] = list
            }
        }

        val keys = TreeSet(map.keys)
        val sessionSlotList = ArrayList<ArrayList<Session>>()
        for (key in keys) {
            sessionSlotList.add(map[key]!!)
        }
        return sessionSlotList
    }

    fun toggleStarFilter(isStar: Boolean) {
        this.starFilter = isStar
        (scheduleView?.adapter as? ScheduleAdapter)?.update(
            transformSessions(if (isStar) loadStarSessions() else mSessions)
        )
    }

    override fun onResume() {
        super.onResume()
        toggleStarFilter(starFilter)
    }
}
