package app.opass.ccip.fragment

import android.app.Activity
import android.graphics.PorterDuff.Mode
import android.os.Build.VERSION
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import app.opass.ccip.R
import app.opass.ccip.adapter.ScheduleTabAdapter
import app.opass.ccip.extension.asyncExecute
import app.opass.ccip.model.ConfSchedule
import app.opass.ccip.model.Session
import app.opass.ccip.util.JsonUtil
import app.opass.ccip.util.PreferenceUtil
import com.google.android.material.tabs.TabLayout
import com.google.gson.internal.bind.util.ISO8601Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.ParseException
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator
import kotlin.coroutines.CoroutineContext

class ScheduleTabFragment : Fragment(), CoroutineScope {
    companion object {
        private val SDF_DATE = SimpleDateFormat("MM/dd")
    }

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var menuItemStar: MenuItem
    private lateinit var mActivity: Activity
    private var starFilter = false
    private var mSchedule: ConfSchedule? = null
    private var scheduleTabAdapter: ScheduleTabAdapter? = null
    private lateinit var mJob: Job
    override val coroutineContext: CoroutineContext
        get() = mJob + Dispatchers.Main

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_schedule_tab, container, false)

        swipeRefreshLayout = view.findViewById(R.id.swipeContainer)
        tabLayout = view.findViewById(R.id.tabs)
        viewPager = view.findViewById(R.id.pager)

        mActivity = requireActivity()
        mJob = Job()

        if (VERSION.SDK_INT >= 21) {
            mActivity.findViewById<View>(R.id.appbar).elevation = 0f
        }

        setHasOptionsMenu(true)

        swipeRefreshLayout.isEnabled = false
        swipeRefreshLayout.post { swipeRefreshLayout.isRefreshing = true }

        launch {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(PreferenceUtil.getCurrentEvent(mActivity).scheduleUrl)
                    .build()
                client.newCall(request).asyncExecute().run {
                    if (isSuccessful) {
                        swipeRefreshLayout.isRefreshing = false

                        val scheduleJson = body!!.string()
                        mSchedule = JsonUtil.GSON.fromJson(scheduleJson, ConfSchedule::class.java)
                        PreferenceUtil.saveSchedule(mActivity, scheduleJson)
                    } else {
                        loadOfflineSchedule()
                    }
                }
            } catch (t: Throwable) {
                loadOfflineSchedule()
            }
            setupViewPager()
        }

        return view
    }

    override fun onDestroy() {
        tabLayout.setupWithViewPager(null)
        super.onDestroy()
        mJob.cancel()
    }

    private fun setupViewPager() {
        if (isAdded) {
            viewPager.isSaveFromParentEnabled = false
            scheduleTabAdapter = ScheduleTabAdapter(childFragmentManager)
            viewPager.adapter = scheduleTabAdapter
            mSchedule?.sessions?.let(::addSessionFragments)
            tabLayout.setupWithViewPager(viewPager)
            menuItemStar.isVisible = true
        }
    }

    private fun addSessionFragments(sessions: List<Session>) {
        val getDateOrNull: (String?) -> String? = {
            try {
                it?.let { SDF_DATE.format(ISO8601Utils.parse(it, ParsePosition(0))) }
            } catch (e: ParseException) {
                e.printStackTrace()
                null
            }
        }

        val sessionsGroupedByDate = sessions
            .groupBy { getDateOrNull(it.start!!) }
            .filterKeys { it != null }
            .toSortedMap(Comparator { start1, start2 -> start1!!.compareTo(start2!!) })
        sessionsGroupedByDate.forEach { (date, sessions) ->
            scheduleTabAdapter!!.addFragment(ScheduleFragment.newInstance(date!!, sessions), date)
        }
        scheduleTabAdapter!!.notifyDataSetChanged()

        val today = SDF_DATE.format(Date())
        val index = sessionsGroupedByDate.keys.indexOfFirst { it == today }

        if (index != -1) {
            viewPager.currentItem = index
        }

        if (sessionsGroupedByDate.size <= 1) {
            tabLayout.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.add("star")
            .setIcon(R.drawable.ic_bookmark_border_black_24dp)
            .setOnMenuItemClickListener { item ->
                starFilter = !starFilter
                if (starFilter) {
                    item.setIcon(R.drawable.ic_bookmark_black_24dp)
                } else {
                    item.setIcon(R.drawable.ic_bookmark_border_black_24dp)
                }
                item.icon.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.colorWhite),
                    Mode.SRC_ATOP
                )
                scheduleTabAdapter!!.toggleStarFilter(starFilter)
                false
            }
            .setVisible(false)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        menuItemStar = menu.getItem(0)
        menuItemStar.icon.setColorFilter(
            ContextCompat.getColor(requireContext(), R.color.colorWhite),
            Mode.SRC_ATOP
        )
    }

    private fun loadOfflineSchedule() {
        swipeRefreshLayout.post { swipeRefreshLayout.isRefreshing = false }
        Toast.makeText(mActivity, R.string.offline, Toast.LENGTH_LONG).show()
        val schedule = PreferenceUtil.loadSchedule(mActivity)
        if (schedule != null) {
            mSchedule = schedule
        }
    }
}
