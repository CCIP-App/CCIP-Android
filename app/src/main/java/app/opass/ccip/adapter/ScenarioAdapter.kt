package app.opass.ccip.adapter

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.opass.ccip.R
import app.opass.ccip.model.Scenario
import app.opass.ccip.util.LocaleUtil
import java.text.SimpleDateFormat
import java.util.*

class ScenarioAdapter(
    private val mContext: Context,
    private val mScenarioList: MutableList<Scenario>,
    private val onItemClick: (Scenario) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = LayoutInflater.from(parent.context)
        .inflate(R.layout.item_scenario, parent, false)
        .let(::ViewHolder)

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

        val typedArr = mContext.obtainStyledAttributes(intArrayOf(R.attr.colorOnSurface))
        val colorOnSurface = typedArr.getColor(0, 0)
        typedArr.recycle()

        holder.scenarioName.setTextColor(colorOnSurface)
        holder.allowTimeRange.text = String.format(
            FORMAT_TIMERANGE,
            SDF.format(Date(scenario.availableTime * 1000L)),
            SDF.format(Date(scenario.expireTime * 1000L))
        )

        if (scenario.disabled != null) {
            setCardDisabled(holder, scenario.disabled)
            return
        }

        holder.card.setOnClickListener { onItemClick(scenario) }

        if (scenario.used == null) {
            holder.card.isClickable = true
        } else {
            if (scenario.countdown > 0) {
                holder.card.isClickable = true
            } else {
                setCardUsed(holder)
                holder.card.isClickable = false
                holder.card.setOnClickListener(null)
            }

            if (Date().time / 1000 >= scenario.used + scenario.countdown) {
                setCardUsed(holder)
                holder.card.setOnClickListener(null)
            }
        }
    }

    override fun getItemCount(): Int {
        return mScenarioList.size
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

    fun setItems(scenarioList: List<Scenario>) {
        mScenarioList.clear()
        mScenarioList.addAll(scenarioList)
        notifyDataSetChanged()
    }
}
