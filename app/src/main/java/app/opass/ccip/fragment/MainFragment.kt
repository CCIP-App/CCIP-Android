package app.opass.ccip.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.opass.ccip.R
import app.opass.ccip.activity.AuthActivity
import app.opass.ccip.activity.CountdownActivity
import app.opass.ccip.activity.MainActivity
import app.opass.ccip.adapter.ScenarioAdapter
import app.opass.ccip.extension.asyncExecute
import app.opass.ccip.model.Scenario
import app.opass.ccip.network.CCIPClient
import app.opass.ccip.network.ErrorUtil
import app.opass.ccip.util.JsonUtil
import app.opass.ccip.util.PreferenceUtil
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MainFragment : Fragment(), CoroutineScope {
    companion object {
        private const val EXTRA_URL = "EXTRA_URL"
        fun newInstance(url: String): MainFragment = MainFragment().apply {
            arguments = Bundle().apply { putString(EXTRA_URL, url) }
        }
    }

    private lateinit var noNetworkView: View
    private lateinit var notConfWifiView: View
    private lateinit var loginView: View
    private lateinit var scenarioView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var mActivity: MainActivity
    private lateinit var mJob: Job
    override val coroutineContext: CoroutineContext
        get() = mJob + Dispatchers.Main
    private lateinit var mAdapter: ScenarioAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        mActivity = requireActivity() as MainActivity
        noNetworkView = view.findViewById(R.id.no_network)
        notConfWifiView = view.findViewById(R.id.not_conf_wifi)
        loginView = view.findViewById(R.id.login_view)
        mJob = Job()

        noNetworkView.setOnClickListener {
            noNetworkView.visibility = View.GONE
            updateStatus()
        }

        view.findViewById<View>(R.id.login_button).setOnClickListener {
            mActivity.startActivity(Intent(mActivity, AuthActivity::class.java))
        }

        scenarioView = view.findViewById(R.id.scenarios)
        swipeRefreshLayout = view.findViewById(R.id.swipeContainer)

        scenarioView.layoutManager = LinearLayoutManager(mActivity)
        scenarioView.itemAnimator = DefaultItemAnimator()

        if (PreferenceUtil.getToken(mActivity) == null) {
            loginView.visibility = View.VISIBLE
            swipeRefreshLayout.visibility = View.GONE
        }

        swipeRefreshLayout.setOnRefreshListener { updateStatus() }

        return view
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        mJob.cancel()
    }

    private fun updateStatus() {
        val token = PreferenceUtil.getToken(mActivity)
        if (token == null) {
            loginView.visibility = View.VISIBLE
            swipeRefreshLayout.visibility = View.GONE
            return
        }

        swipeRefreshLayout.visibility = View.VISIBLE
        swipeRefreshLayout.post { swipeRefreshLayout.isRefreshing = true }
        loginView.visibility = View.GONE
        noNetworkView.visibility = View.GONE
        notConfWifiView.visibility = View.GONE

        launch {
            try {
                val response = CCIPClient.get().status(token).asyncExecute()

                when {
                    response.isSuccessful -> {
                        val attendee = response.body()
                        val attr = attendee!!.attr.asJsonObject

                        attr.get("title")?.let {
                            mActivity.setUserTitle(it.asString)
                        }
                        mActivity.setUserId(attendee.userId)

                        mAdapter = ScenarioAdapter(mActivity, attendee.scenarios) {
                            val isUsed = it.used != null
                            val hasCountdown = it.countdown > 0

                            if (isUsed && hasCountdown) {
                                startCountdownActivity(it)
                                return@ScenarioAdapter
                            }

                            if (hasCountdown) {
                                showConfirmDialog(it)
                            } else {
                                useScenario(it)
                            }
                        }
                        scenarioView.adapter = mAdapter
                    }
                    response.code() == 403 -> {
                        notConfWifiView.visibility = View.VISIBLE
                        notConfWifiView.setOnClickListener {
                            swipeRefreshLayout.isRefreshing = true
                            notConfWifiView.visibility = View.GONE
                            updateStatus()
                        }
                    }
                    else -> {
                        Snackbar.make(view!!, getString(R.string.invalid_token), Snackbar.LENGTH_LONG).show()
                        PreferenceUtil.setToken(mActivity, null)
                        PreferenceUtil.setRole(mActivity, null)
                        loginView.visibility = View.VISIBLE
                    }
                }
            } catch (t: Throwable) {
                noNetworkView.visibility = View.VISIBLE
            } finally {
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun useScenario(scenario: Scenario) {
        val token = PreferenceUtil.getToken(mActivity)

        launch {
            try {
                val response = CCIPClient.get().use(scenario.id, token).asyncExecute()
                when {
                    response.isSuccessful -> {
                        val attendee = response.body()
                        mAdapter.setItems(attendee!!.scenarios)

                        if (scenario.countdown > 0) {
                            startCountdownActivity(scenario)
                        }
                    }
                    response.code() == 400 -> {
                        val (message) = ErrorUtil.parseError(response)
                        Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show()
                    }
                    response.code() == 403 -> {
                        AlertDialog.Builder(mActivity)
                            .setTitle(R.string.connect_to_conference_wifi)
                            .setPositiveButton(android.R.string.ok, null)
                            .show()
                    }
                    else -> Toast.makeText(mActivity, "Unexpected response", Toast.LENGTH_LONG).show()
                }
            } catch (t: Throwable) {
                Toast.makeText(mActivity, "Use req fail, " + t.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showConfirmDialog(scenario: Scenario) {
        AlertDialog.Builder(mActivity)
            .setTitle(R.string.confirm_dialog_title)
            .setPositiveButton(R.string.positive_button) { dialogInterface, i -> useScenario(scenario) }
            .setNegativeButton(R.string.negative_button, null)
            .show()
    }

    private fun startCountdownActivity(scenario: Scenario) {
        val intent = Intent()
        intent.setClass(mActivity, CountdownActivity::class.java)
        intent.putExtra(CountdownActivity.INTENT_EXTRA_SCENARIO, JsonUtil.toJson(scenario))
        mActivity.startActivity(intent)
    }
}
