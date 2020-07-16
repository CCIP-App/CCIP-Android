package app.opass.ccip.ui.schedule

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.opass.ccip.R
import app.opass.ccip.model.Session
import app.opass.ccip.ui.sessiondetail.SessionDetailActivity
import app.opass.ccip.util.AlarmUtil
import app.opass.ccip.util.PreferenceUtil

class ScheduleFragment : Fragment() {
    companion object {
        private const val ARG_DATE = "ARG_DATE"

        fun newInstance(date: String) =
            ScheduleFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DATE, date)
                }
            }
    }

    private lateinit var adapter: ScheduleAdapter
    private lateinit var mActivity: Activity
    private lateinit var date: String
    private lateinit var vm: ScheduleViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mActivity = requireActivity()
        date = requireArguments().getString(ARG_DATE)!!
        vm = ViewModelProvider(requireParentFragment()).get()

        val view = inflater.inflate(R.layout.fragment_schedule, container, false)
        val scheduleView = view.findViewById<RecyclerView>(R.id.schedule)
        scheduleView.layoutManager = LinearLayoutManager(mActivity)
        adapter = ScheduleAdapter(
            mActivity,
            emptyList(),
            ::onSessionClicked,
            ::onToggleStarState,
            ::isSessionStarred
        )
        scheduleView.adapter = adapter

        vm.groupedSessionsToShow.observe(viewLifecycleOwner) { sessionsMap ->
            val grouped = sessionsMap?.get(date)?.let(::toSessionsGroupedByTime) ?: emptyList()
            adapter.update(grouped)
        }

        return view
    }

    private fun onSessionClicked(session: Session) {
        val intent = Intent(mActivity, SessionDetailActivity::class.java).apply {
            putExtra(SessionDetailActivity.INTENT_EXTRA_SESSION_ID, session.id)
        }
        startActivity(intent)
    }

    private fun onToggleStarState(session: Session): Boolean {
        val sessionIds = PreferenceUtil.loadStarredIds(mActivity).toMutableList()
        val isAlreadyStarred = sessionIds.contains(session.id)
        if (isAlreadyStarred) {
            sessionIds.remove(session.id)
            AlarmUtil.cancelSessionAlarm(mActivity, session)
        } else {
            sessionIds.add(session.id)
            AlarmUtil.setSessionAlarm(mActivity, session)
        }
        PreferenceUtil.saveStarredIds(mActivity, sessionIds)
        return !isAlreadyStarred
    }

    private fun isSessionStarred(session: Session): Boolean {
        return PreferenceUtil.loadStarredIds(mActivity).contains(session.id)
    }

    private fun toSessionsGroupedByTime(sessions: List<Session>): List<List<Session>> {
        return sessions
            .filter { it.start != null }
            .groupBy { it.start }
            .values
            .sortedBy { it[0].start }
            .map { it.sortedWith(Comparator { (_, room1), (_, room2) -> room1.id.compareTo(room2.id) }) }
    }

    override fun onResume() {
        super.onResume()
        // Force RV to reload star state :(
        adapter.notifyDataSetChanged()
    }
}
