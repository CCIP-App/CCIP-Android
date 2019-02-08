package app.opass.ccip.activity

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import app.opass.ccip.R
import app.opass.ccip.model.Scenario
import app.opass.ccip.util.JsonUtil
import kotlinx.android.synthetic.main.activity_countdown.*
import java.text.SimpleDateFormat
import java.util.*

class CountdownActivity : AppCompatActivity() {
    companion object {
        const val INTENT_EXTRA_SCENARIO = "scenario"
        private val SDF = SimpleDateFormat("HH:mm:ss")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_countdown)

        val attrText = attr
        val countdownText = countdown

        val (_, _, _, _, attr1, _, countdown1, used) = JsonUtil.fromJson(
            intent.getStringExtra(INTENT_EXTRA_SCENARIO),
            Scenario::class.java
        )

        button.setOnClickListener { finish() }

        val attr = attr1.asJsonObject
        val elemDiet = attr.get("diet")
        if (elemDiet != null) {
            val diet = elemDiet.asString
            if (diet == "meat") {
                countdown_layout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorDietMeat))
                attrText.setText(R.string.meal)
            } else {
                countdown_layout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorDietVegetarian))
                attrText.setText(R.string.vegan)
            }
        } else {
            val entries = attr.entrySet()
            for ((_, value) in entries) {
                attrText.append(value.toString() + "\n")
            }
        }

        val countdown = if (used == null) {
            countdown1 * 1000L
        } else {
            (used + countdown1) * 1000L - Date().time
        }

        object : CountDownTimer(countdown, 1000L) {

            override fun onTick(l: Long) {
                countdownText.text = (l / 1000).toString()
                current_time.text = SDF.format(Date().time)
            }

            override fun onFinish() {
                countdownText.text = "0"
                current_time.visibility = View.GONE
                countdown_layout.setBackgroundColor(Color.RED)
            }
        }.start()
    }
}
