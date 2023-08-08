package app.opass.ccip.ui.schedule

import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.opass.ccip.R
import app.opass.ccip.extension.doOnApplyWindowInsets
import app.opass.ccip.extension.dpToPx
import app.opass.ccip.extension.updateMargin
import app.opass.ccip.model.Session
import app.opass.ccip.ui.sessiondetail.SessionDetailActivity
import app.opass.ccip.util.AlarmUtil
import app.opass.ccip.util.PreferenceUtil

class ScheduleFragment : Fragment() {

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            context?.let { AlarmUtil.setSessionAlarm(it, session) }
        }

    companion object {
        private const val ARG_DATE = "ARG_DATE"

        fun newInstance(date: String) =
            ScheduleFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DATE, date)
                }
            }
    }

    lateinit var date: String
        private set
    private lateinit var adapter: ScheduleAdapter
    private lateinit var mActivity: Activity
    private lateinit var vm: ScheduleViewModel
    private lateinit var scheduleView: RecyclerView
    private lateinit var session: Session

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mActivity = requireActivity()
        date = requireArguments().getString(ARG_DATE)!!
        vm = ViewModelProvider(requireParentFragment()).get()

        val view = inflater.inflate(R.layout.fragment_schedule, container, false)
        val emptyView = view.findViewById<TextView>(R.id.emptyView)
        scheduleView = view.findViewById(R.id.schedule)
        scheduleView.layoutManager = LinearLayoutManager(mActivity)
        val tagViewPool = RecyclerView.RecycledViewPool()
        adapter = ScheduleAdapter(
            mActivity,
            tagViewPool,
            ::onSessionClicked,
            ::onToggleStarState,
            ::isSessionStarred
        )
        scheduleView.adapter = adapter

        vm.groupedSessionsToShow.observe(viewLifecycleOwner) { sessionsMap ->
            val grouped = sessionsMap?.get(date)?.let(::toSessionsGroupedByTime).orEmpty()
            adapter.update(grouped)
            val shouldShowEmptyView = sessionsMap != null && grouped.isEmpty()
            val confSpansMultipleDay = sessionsMap?.size?.let { size -> size > 1 } ?: false
            emptyView.isVisible = shouldShowEmptyView
            emptyView.text = if (confSpansMultipleDay) {
                getString(R.string.no_matching_sessions_multiple_day)
            } else {
                getString(R.string.no_matching_sessions)
            }
        }
        vm.shouldShowSearchPanel
            .distinctUntilChanged()
            .observe(viewLifecycleOwner) {
                requireView().requestApplyInsets()
            }

        scheduleView.doOnApplyWindowInsets { v, insets, padding, _ ->
            val panelInset =
                if (vm.shouldShowSearchPanel.value == true) 56F.dpToPx(resources) else 0
            v.updatePadding(bottom = insets.systemGestureInsets.bottom + padding.bottom + panelInset)
        }
        emptyView.doOnApplyWindowInsets { v, insets, _, margin ->
            val panelInset =
                if (vm.shouldShowSearchPanel.value == true) 56F.dpToPx(resources) else 0
            v.updateMargin(bottom = insets.systemGestureInsets.bottom + margin.bottom + panelInset)
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
        this.session = session
        val sessionIds = PreferenceUtil.loadStarredIds(mActivity).toMutableList()
        val isAlreadyStarred = sessionIds.contains(session.id)
        if (isAlreadyStarred) {
            sessionIds.remove(session.id)
            AlarmUtil.cancelSessionAlarm(mActivity, session)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (alarmManager.canScheduleExactAlarms()) {
                    AlarmUtil.setSessionAlarm(mActivity, session)
                } else {
                    val uri = Uri.parse("package:" + requireContext().packageName)
                    startForResult.launch(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, uri))
                }
            } else {
                AlarmUtil.setSessionAlarm(mActivity, session)
            }
        }
        vm.hasStarredSessions.value = sessionIds.isNotEmpty()
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
            .map { it.sortedWith { (_, room1), (_, room2) -> room1.id.compareTo(room2.id) } }
    }

    fun scrollToTop() {
        scheduleView.smoothScrollToPosition(0)
    }

    override fun onResume() {
        super.onResume()
        vm.hasStarredSessions.value = PreferenceUtil.loadStarredIds(mActivity).isNotEmpty()
        // Force RV to reload star state :(
        adapter.notifyDataSetChanged()
    }
}
