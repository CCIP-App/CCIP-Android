package app.opass.ccip.ui.schedule

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.observe
import app.opass.ccip.R
import app.opass.ccip.databinding.FragmentScheduleTabBinding
import app.opass.ccip.extension.asyncExecute
import app.opass.ccip.extension.doOnApplyWindowInsets
import app.opass.ccip.extension.updateMargin
import app.opass.ccip.model.ConfSchedule
import app.opass.ccip.ui.MainActivity
import app.opass.ccip.util.JsonUtil
import app.opass.ccip.util.PreferenceUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext

class ScheduleTabFragment : Fragment(), CoroutineScope, MainActivity.BackPressAwareFragment {
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
        binding.swipeContainer.isEnabled = false

        val sheetBehavior = BottomSheetBehavior.from(requireView().findViewById<View>(R.id.filterSheet))
        binding.fab.setOnClickListener {
            sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        binding.fab.doOnApplyWindowInsets { v, insets, _, margin ->
            v.updateMargin(bottom = margin.bottom + insets.systemGestureInsets.bottom)
        }

        vm.isScheduleReady
            .distinctUntilChanged()
            .observe(viewLifecycleOwner) { isReady ->
                if (isReady) {
                    setupViewPager()
                    binding.fab.show()
                } else {
                    binding.fab.hide()
                }
            }
        vm.filtersActivated.observe(viewLifecycleOwner) { activated ->
            if (vm.isScheduleReady.value != true) return@observe
            if (activated) binding.fab.hide()
            else binding.fab.show()
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
                        withContext(Dispatchers.Default) { JsonUtil.GSON.fromJson(new, ConfSchedule::class.java) }

                        if (cached != new) {
                            PreferenceUtil.saveSchedule(mActivity, new)
                            vm.reloadSchedule()
                        }
                    } else {
                        Snackbar.make(binding.root, R.string.cannot_load_schedule, Snackbar.LENGTH_LONG)
                            .setAnchorView(binding.fab)
                            .show()
                    }
                }
            } catch (_: CancellationException) {
                return@launch
            } catch (t: Throwable) {
                t.printStackTrace()
                Snackbar.make(binding.root, R.string.offline, Snackbar.LENGTH_LONG)
                    .setAnchorView(binding.fab)
                    .show()
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

    override fun onBackPressed(): Boolean {
        val sheetBehavior = BottomSheetBehavior.from(requireView().findViewById<ConstraintLayout>(R.id.filterSheet))
        if (sheetBehavior.state == BottomSheetBehavior.STATE_DRAGGING
            || sheetBehavior.state == BottomSheetBehavior.STATE_SETTLING) return true
        if (sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.state = if (sheetBehavior.skipCollapsed) BottomSheetBehavior.STATE_HIDDEN
                else BottomSheetBehavior.STATE_COLLAPSED
            return true
        }
        return false
    }
}
