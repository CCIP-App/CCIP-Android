package app.opass.ccip.fragment

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.opass.ccip.R
import app.opass.ccip.activity.CaptureActivity
import app.opass.ccip.activity.MainActivity
import app.opass.ccip.adapter.ScenarioAdapter
import app.opass.ccip.model.Attendee
import app.opass.ccip.network.CCIPClient
import app.opass.ccip.util.PreferenceUtil
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.zxing.integration.android.IntentIntegrator
import com.onesignal.OneSignal
import kotlinx.android.synthetic.main.fragment_main.*
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainFragment : Fragment() {
    internal lateinit var noNetworkView: View
    internal lateinit var notConfWifiView: View
    internal lateinit var loginView: View
    internal lateinit var loginTitle: TextView
    internal lateinit var scenarioView: RecyclerView
    internal lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var mActivity: Activity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        mActivity = requireActivity()
        noNetworkView = view.findViewById(R.id.no_network)
        notConfWifiView = view.findViewById(R.id.not_conf_wifi)
        loginView = view.findViewById(R.id.login)
        loginTitle = view.findViewById(R.id.login_title)

        val enterTokenButton = enter_token

        enterTokenButton.setOnClickListener(View.OnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_enter_token, null)
            val tokenInputLayout: TextInputLayout = dialogView.findViewById(R.id.token_input_layout)
            val tokenInput: TextInputEditText = dialogView.findViewById(R.id.token_input)

            val dialog = AlertDialog.Builder(mActivity)
                .setView(dialogView)
                .setTitle(R.string.enter_your_token)
                .setPositiveButton(R.string.positive_button, null)
                .setNegativeButton(R.string.negative_button, null)
                .create()

            dialog.setOnShowListener {
                val positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                val negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)

                positiveButton.setOnClickListener(View.OnClickListener {
                    val token = tokenInput.text.toString().trim()

                    if (token == "") {
                        tokenInputLayout.error = getString(R.string.token_required)
                        return@OnClickListener
                    }
                    PreferenceUtil.setIsNewToken(mActivity, true)
                    PreferenceUtil.setToken(mActivity, token)
                    dialog.dismiss()
                    updateStatus()
                })
                negativeButton.setOnClickListener { dialog.dismiss() }
            }

            dialog.show()
        })

        loginTitle.setOnClickListener {
            val integrator = IntentIntegrator(mActivity)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            integrator.setPrompt(getString(R.string.scan_kktix_qrcode))
            integrator.setCameraId(0)
            integrator.setBeepEnabled(false)
            integrator.setBarcodeImageEnabled(false)
            integrator.captureActivity = CaptureActivity::class.java
            integrator.initiateScan()
        }
        scenarioView = view.findViewById(R.id.scenarios)
        swipeRefreshLayout = view.findViewById(R.id.swipeContainer)

        scenarioView.layoutManager = LinearLayoutManager(mActivity)
        scenarioView.itemAnimator = DefaultItemAnimator()

        if (mActivity.intent.action == Intent.ACTION_VIEW) {
            val token = mActivity.intent.data!!.getQueryParameter("token")

            if (token != null) {
                PreferenceUtil.setIsNewToken(mActivity, true)
                PreferenceUtil.setToken(mActivity, token)
            }
        }

        if (PreferenceUtil.getToken(mActivity) == null) {
            loginView.visibility = View.VISIBLE
        }

        swipeRefreshLayout.setOnRefreshListener { updateStatus() }

        return view
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    internal fun updateStatus() {
        if (PreferenceUtil.getToken(mActivity) == null) {
            loginView.visibility = View.VISIBLE
            return
        }

        swipeRefreshLayout.post { swipeRefreshLayout.isRefreshing = true }
        loginView.visibility = View.GONE
        noNetworkView.visibility = View.GONE
        notConfWifiView.visibility = View.GONE

        val attendee = CCIPClient.get().status(PreferenceUtil.getToken(mActivity))
        attendee.enqueue(object : Callback<Attendee> {
            override fun onResponse(call: Call<Attendee>, response: Response<Attendee>) {
                swipeRefreshLayout.isRefreshing = false
                when {
                    response.isSuccessful -> {
                        val attendee = response.body()
                        val attr = attendee!!.attr.asJsonObject

                        if (PreferenceUtil.getIsNewToken(mActivity)) {
                            PreferenceUtil.setIsNewToken(mActivity, false)

                            val tags = JSONObject()
                            try {
                                tags.put("event_id", attendee.eventId)
                                tags.put("token", attendee.token)
                                tags.put("type", attendee.type)
                                OneSignal.sendTags(tags)
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }

                            AlertDialog.Builder(mActivity)
                                .setMessage(
                                    mActivity.getString(R.string.hi)
                                        + attendee.userId
                                        + mActivity.getString(R.string.login_success)
                                )
                                .setPositiveButton(android.R.string.ok, null)
                                .show()
                        }

                        val attrTitle = attr.get("title")
                        if (attrTitle != null) {
                            MainActivity.setUserTitle(attrTitle.asString)
                        }
                        MainActivity.setUserId(attendee.userId)

                        scenarioView.adapter = ScenarioAdapter(mActivity, attendee.scenarios)
                    }
                    response.code() == 403 -> {
                        swipeRefreshLayout.isRefreshing = false
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
                        loginView.visibility = View.VISIBLE
                    }
                }
            }

            override fun onFailure(call: Call<Attendee>, t: Throwable) {
                swipeRefreshLayout.isRefreshing = false
                noNetworkView.visibility = View.VISIBLE
                noNetworkView.setOnClickListener {
                    swipeRefreshLayout.isRefreshing = true
                    noNetworkView.visibility = View.GONE
                    updateStatus()
                }
            }
        })
    }
}
