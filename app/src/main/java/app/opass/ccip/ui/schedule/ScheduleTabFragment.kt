package app.opass.ccip.ui.schedule

import android.app.Activity
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.get
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.observe
import app.opass.ccip.R
import app.opass.ccip.databinding.FragmentScheduleTabBinding
import app.opass.ccip.extension.asyncExecute
import app.opass.ccip.model.ConfSchedule
import app.opass.ccip.util.JsonUtil
import app.opass.ccip.util.PreferenceUtil
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext

class ScheduleTabFragment : Fragment(), CoroutineScope {
    companion object {
        private val SDF_DATE = SimpleDateFormat("MM/dd", Locale.US)
        private const val EXTRA_URL = "EXTRA_URL"
        fun newInstance(url: String) : ScheduleTabFragment = ScheduleTabFragment().apply {
            arguments = Bundle().apply { putString(EXTRA_URL, url) }
        }
    }

    private var _binding: FragmentScheduleTabBinding? = null
    private val binding get() = _binding!!

    private lateinit var mActivity: Activity
    private lateinit var tabLayout: TabLayout

    private var scheduleTabAdapter: ScheduleTabAdapter? = null
    private lateinit var mJob: Job
    override val coroutineContext: CoroutineContext
        get() = mJob + Dispatchers.Main

    private val scheduleUrl by lazy { requireArguments().getString(EXTRA_URL)!! }
    private val vm: ScheduleViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScheduleTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mActivity = requireActivity()
        tabLayout = mActivity.findViewById(R.id.tabs)
        mJob = Job()
        setHasOptionsMenu(true)
        binding.swipeContainer.isEnabled = false

        vm.isScheduleReady
            .distinctUntilChanged()
            .observe(viewLifecycleOwner) { isReady ->
                if (isReady) {
                    setupViewPager()
                    mActivity.invalidateOptionsMenu()
                }
            }
        vm.showStarredOnly.observe(viewLifecycleOwner) {
            mActivity.invalidateOptionsMenu()
        }

        launch {
            binding.swipeContainer.isRefreshing = true
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(scheduleUrl)
                    .build()
                client.newCall(request).asyncExecute().run {
                    if (isSuccessful) {
                        // Strings that end with \n may cause problem with SharedPreferences. Trim
                        // first for safety.
                        // See https://issuetracker.google.com/issues/37032278
                        val cached = PreferenceUtil.loadRawSchedule(mActivity).trim()
                        val new = withContext(Dispatchers.IO) { body!!.string().trim() }
                        // try to parse first
                        JsonUtil.GSON.fromJson(new, ConfSchedule::class.java)

                        if (cached != new) {
                            PreferenceUtil.saveSchedule(mActivity, new)

                            if (!vm.isScheduleReady.value!!) return@run vm.reloadSessions()
                            Snackbar.make(binding.root, R.string.schedule_updated, Snackbar.LENGTH_INDEFINITE)
                                .setAction(R.string.reload) { vm.reloadSessions() }
                                .show()
                        }
                    } else {
                        Snackbar.make(binding.root, R.string.cannot_load_schedule, Snackbar.LENGTH_LONG).show()
                    }
                }
            } catch (_: CancellationException) {
                return@launch
            } catch (t: Throwable) {
                t.printStackTrace()
                Snackbar.make(binding.root, R.string.offline, Snackbar.LENGTH_LONG).show()
            }
            binding.swipeContainer.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        tabLayout.setupWithViewPager(null)
        tabLayout.isGone = true
        super.onDestroy()
        mJob.cancel()
    }

    private fun setupViewPager() {
        val dates = vm.sessionsGroupedByDate.value!!.keys
        scheduleTabAdapter = ScheduleTabAdapter(childFragmentManager, dates.toList())
        binding.pager.adapter = scheduleTabAdapter

        val today = SDF_DATE.format(Date())
        val index = dates.indexOfFirst { it == today }
        if (index != -1) {
            binding.pager.currentItem = index
        }

        tabLayout.isGone = dates.size <= 1
        tabLayout.setupWithViewPager(binding.pager)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.add("star").run {
            setIcon(R.drawable.ic_bookmark_border_black_24dp)
            setOnMenuItemClickListener { item ->
                vm.showStarredOnly.value = !vm.showStarredOnly.value!!
                false
            }
            isVisible = false
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val item = menu[0]
        item.isVisible = vm.sessionsGroupedByDate.value != null
        if (vm.showStarredOnly.value!!) {
            item.setIcon(R.drawable.ic_bookmark_black_24dp)
        } else {
            item.setIcon(R.drawable.ic_bookmark_border_black_24dp)
        }
        val filter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            ContextCompat.getColor(requireContext(), R.color.colorWhite),
            BlendModeCompat.SRC_ATOP
        )
        item.icon.colorFilter = filter
    }
}
