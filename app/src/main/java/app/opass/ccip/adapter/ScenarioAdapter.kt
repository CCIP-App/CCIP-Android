package app.opass.ccip.adapter

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.opass.ccip.R
import app.opass.ccip.activity.CountdownActivity
import app.opass.ccip.model.Attendee
import app.opass.ccip.model.Scenario
import app.opass.ccip.network.CCIPClient
import app.opass.ccip.network.ErrorUtil
import app.opass.ccip.util.JsonUtil
import app.opass.ccip.util.LocaleUtil
import app.opass.ccip.util.PreferenceUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ScenarioAdapter(private val mContext: Context, private var mScenarioList: List<Scenario>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private val SDF = SimpleDateFormat("MM/dd HH:mm")
        private const val FORMAT_TIMERANGE = "%s ~ %s"
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var scenarioIcon: ImageView = itemView.findViewById(R.id.icon)
        var tickIcon: ImageView = itemView.findViewById(R.id.tick)
        var scenarioName: TextView = itemView.findViewById(R.id.scenario_name)
        var allowTimeRange: TextView = itemView.findViewById(R.id.allow_time_range)
        var card: CardView = itemView.findViewById(R.id.card)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scenario, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder

        val scenario = mScenarioList[position]

        try {
            val iconResId = mContext.resources.getIdentifier(
                if (scenario.id.indexOf("lunch") > 0) "lunch" else scenario.id,
                "drawable",
                mContext.packageName
            )
            holder.scenarioIcon.setImageDrawable(ContextCompat.getDrawable(mContext, iconResId))
        } catch (e: Resources.NotFoundException) {
            holder.scenarioIcon.setImageResource(R.drawable.doc)
        }

        holder.scenarioIcon.alpha = 1f

        if (LocaleUtil.getCurrentLocale(mContext).toString().startsWith(Locale.TAIWAN.toString())) {
            holder.scenarioName.text = scenario.displayText.zhTW
        } else {
            holder.scenarioName.text = scenario.displayText.enUS
        }

        holder.scenarioName.setTextColor(mContext.resources.getColor(android.R.color.black))
        holder.allowTimeRange.text = String.format(
            FORMAT_TIMERANGE,
            SDF.format(Date(scenario.availableTime * 1000L)),
            SDF.format(Date(scenario.expireTime * 1000L))
        )

        if (scenario.disabled != null) {
            setCardDisabled(holder, scenario.disabled)
            return
        }

        if (scenario.used == null) {
            holder.card.isClickable = true
            holder.card.setOnClickListener {
                if (scenario.countdown > 0) {
                    showConfirmDialog(scenario)
                } else {
                    use(scenario)
                }
            }
        } else {
            if (scenario.countdown > 0) {
                holder.card.isClickable = true
                holder.card.setOnClickListener { startCountdownActivity(scenario) }
            } else {
                holder.card.isClickable = false
                holder.card.setOnClickListener(null)
            }

            if (Date().time / 1000 > scenario.used + scenario.countdown) {
                setCardUsed(holder)
            }
        }
    }

    override fun getItemCount(): Int {
        return mScenarioList.size
    }

    fun showConfirmDialog(scenario: Scenario) {
        AlertDialog.Builder(mContext)
            .setTitle(R.string.confirm_dialog_title)
            .setPositiveButton(R.string.positive_button) { dialogInterface, i -> use(scenario) }
            .setNegativeButton(R.string.negative_button, null)
            .show()
    }

    fun startCountdownActivity(scenario: Scenario) {
        val intent = Intent()
        intent.setClass(mContext, CountdownActivity::class.java)
        intent.putExtra(CountdownActivity.INTENT_EXTRA_SCENARIO, JsonUtil.toJson(scenario))
        mContext.startActivity(intent)
    }

    fun use(scenario: Scenario) {
        val attendeeCall = CCIPClient.get().use(scenario.id, PreferenceUtil.getToken(mContext))
        attendeeCall.enqueue(object : Callback<Attendee> {
            override fun onResponse(call: Call<Attendee>, response: Response<Attendee>) {
                if (response.isSuccessful) {
                    val attendee = response.body()
                    mScenarioList = attendee!!.scenarios
                    notifyDataSetChanged()

                    if (scenario.countdown > 0) {
                        startCountdownActivity(scenario)
                    }
                } else {
                    if (response.code() == 400) {
                        val (message) = ErrorUtil.parseError(response)
                        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show()
                    } else if (response.code() == 403) {
                        AlertDialog.Builder(mContext)
                            .setTitle(R.string.connect_to_conference_wifi)
                            .setPositiveButton(android.R.string.ok, null)
                            .show()
                    } else {
                        Toast.makeText(mContext, "Unexpected response", Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<Attendee>, t: Throwable) {
                Toast.makeText(mContext, "Use req fail, " + t.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setCardUsed(holder: ViewHolder) {
        holder.tickIcon.visibility = View.VISIBLE
        holder.scenarioIcon.alpha = 0.4f
        holder.scenarioName.setTextColor(Color.parseColor("#FF9B9B9B"))
    }

    private fun setCardDisabled(holder: ViewHolder, reason: String) {
        holder.allowTimeRange.text = reason
        holder.card.isClickable = false
        holder.card.setOnClickListener(null)
        holder.scenarioIcon.alpha = 0.4f
        holder.scenarioName.setTextColor(Color.parseColor("#FF9B9B9B"))
    }
}
