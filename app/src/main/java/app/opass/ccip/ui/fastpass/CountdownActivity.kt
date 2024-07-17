package app.opass.ccip.ui.fastpass

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import app.opass.ccip.R
import app.opass.ccip.databinding.ActivityCountdownBinding
import app.opass.ccip.model.Scenario
import app.opass.ccip.util.JsonUtil
import java.text.SimpleDateFormat
import java.util.Date

class CountdownActivity : AppCompatActivity() {
    companion object {
        const val INTENT_EXTRA_SCENARIO = "scenario"
        private val SDF = SimpleDateFormat("HH:mm:ss")
    }

    private lateinit var binding: ActivityCountdownBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCountdownBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val attrText = binding.attr
        val countdownText = binding.countdown

        val (_, _, _, _, attr1, _, countdown1, used) = JsonUtil.fromJson(
            intent.getStringExtra(INTENT_EXTRA_SCENARIO).toString(),
            Scenario::class.java
        )

        binding.button.setOnClickListener { finish() }

        val attr = attr1.asJsonObject
        val elemDiet = attr.get("diet")
        if (elemDiet != null) {
            val diet = elemDiet.asString
            if (diet == "meat") {
                binding.countdownLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorDietMeat))
                attrText.setText(R.string.meal)
            } else {
                binding.countdownLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorDietVegetarian))
                attrText.setText(R.string.vegan)
            }
        } else {
            val entries = attr.entrySet()
            attrText.text = entries.joinToString("\n") { (key, value) -> "$key: $value" }
        }

        val countdown = if (used == null) {
            countdown1 * 1000L
        } else {
            (used + countdown1) * 1000L - Date().time
        }

        object : CountDownTimer(countdown, 1000L) {

            override fun onTick(l: Long) {
                countdownText.text = (l / 1000).toString()
                binding.currentTime.text = SDF.format(Date().time)
            }

            override fun onFinish() {
                countdownText.text = "0"
                binding.currentTime.visibility = View.GONE
                binding.countdownLayout.setBackgroundColor(Color.RED)
            }
        }.start()
    }
}
