package app.opass.ccip.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.opass.ccip.R
import app.opass.ccip.adapter.AnnouncementAdapter
import app.opass.ccip.extension.asyncExecute
import app.opass.ccip.network.CCIPClient
import app.opass.ccip.util.PreferenceUtil
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class AnnouncementFragment : Fragment(), CoroutineScope {
    private lateinit var announcementView: RecyclerView
    private lateinit var announcementEmptyView: View
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var mActivity: Activity
    private lateinit var mJob: Job
    override val coroutineContext: CoroutineContext
        get() = mJob + Dispatchers.Main

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_announcement, container, false)

        announcementView = view.findViewById(R.id.announcement)
        announcementEmptyView = view.findViewById(R.id.announcement_empty)
        swipeRefreshLayout = view.findViewById(R.id.swipeContainer)

        mActivity = requireActivity()
        mJob = Job()
        announcementView.layoutManager = LinearLayoutManager(mActivity)
        announcementView.itemAnimator = DefaultItemAnimator()

        swipeRefreshLayout.isEnabled = false
        swipeRefreshLayout.post { swipeRefreshLayout.isRefreshing = true }
        launch {
            try {
                CCIPClient.get().announcement(PreferenceUtil.getToken(mActivity)).asyncExecute().run {
                    if (isSuccessful && !body().isNullOrEmpty()) {
                        announcementView.adapter = AnnouncementAdapter(mActivity, body()!!)
                    } else {
                        announcementEmptyView.visibility = View.VISIBLE
                    }
                }
            } catch (t: Throwable) {
                Toast.makeText(mActivity, R.string.offline, Toast.LENGTH_LONG).show()
            } finally {
                swipeRefreshLayout.isRefreshing = false
            }
        }

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        mJob.cancel()
    }
}
