package app.opass.ccip.ui.event

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.opass.ccip.R
import app.opass.ccip.extension.asyncExecute
import app.opass.ccip.extension.setOnApplyWindowInsetsListenerCompat
import app.opass.ccip.model.Event
import app.opass.ccip.network.PortalClient
import app.opass.ccip.ui.MainActivity
import app.opass.ccip.util.PreferenceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class EventActivity : AppCompatActivity(), CoroutineScope {
    private lateinit var mActivity: Activity
    private lateinit var noNetworkView: RelativeLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var mJob: Job
    override val coroutineContext: CoroutineContext
        get() = mJob + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)

        mActivity = this
        swipeRefreshLayout = findViewById(R.id.swipeContainer)
        recyclerView = findViewById(R.id.events)
        mJob = Job()
        setSupportActionBar(findViewById(R.id.toolbar))
        setTitle(R.string.select_event)

        swipeRefreshLayout.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        recyclerView.setOnApplyWindowInsetsListenerCompat { v, insets, insetsCompat ->
            v.updatePadding(bottom = insetsCompat.systemGestureInsets.bottom)
            insets
        }

        noNetworkView = findViewById(R.id.no_network)
        noNetworkView.setOnClickListener {
            swipeRefreshLayout.isRefreshing = true
            swipeRefreshLayout.isEnabled = true
            getEvents()
        }
        noNetworkView.setOnApplyWindowInsetsListenerCompat { v, insets, insetsCompat ->
            v.updatePadding(bottom = insetsCompat.systemGestureInsets.bottom)
            insets
        }

        getEvents()
    }

    override fun onDestroy() {
        super.onDestroy()
        mJob.cancel()
    }

    private fun getEvents() {
        viewManager = LinearLayoutManager(mActivity)

        swipeRefreshLayout.isRefreshing = true
        swipeRefreshLayout.isEnabled = true
        noNetworkView.visibility = View.GONE
        recyclerView.visibility = View.GONE

        launch {
            try {
                val response = PortalClient.get().getEvents().asyncExecute()
                if (response.isSuccessful) {
                    swipeRefreshLayout.isRefreshing = false
                    swipeRefreshLayout.isEnabled = false
                    noNetworkView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE

                    if (response.body()?.size == 1 && PreferenceUtil.getCurrentEvent(mActivity).eventId == "") {
                        val event = (response.body() as List<Event>)[0]
                        val eventConfig = PortalClient.get().getEventConfig(event.eventId).asyncExecute()
                        if (eventConfig.isSuccessful) {
                            val eventConfig = eventConfig.body()!!
                            PreferenceUtil.setCurrentEvent(mActivity, eventConfig)

                            val intent = Intent()
                            intent.setClass(mActivity, MainActivity::class.java)
                            mActivity.startActivity(intent)
                            mActivity.finish()
                        }
                    }

                    viewAdapter = EventAdapter(mActivity, response.body())

                    recyclerView.apply {
                        setHasFixedSize(true)
                        layoutManager = viewManager
                        adapter = viewAdapter
                    }
                }
            } catch (t: Throwable) {
                swipeRefreshLayout.isRefreshing = false
                swipeRefreshLayout.isEnabled = false
                recyclerView.visibility = View.GONE
                noNetworkView.visibility = View.VISIBLE
            }
        }
    }
}
