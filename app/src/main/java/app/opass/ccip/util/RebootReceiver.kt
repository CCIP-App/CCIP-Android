package app.opass.ccip.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.gson.internal.bind.util.ISO8601Utils
import java.text.ParseException
import java.text.ParsePosition

class RebootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val submissions = PreferenceUtil.loadStars(context)
        for (submission in submissions) {
            try {
                val date = ISO8601Utils.parse(submission.start, ParsePosition(0))
                if (System.currentTimeMillis() < date.time) {
                    AlarmUtil.setSubmissionAlarm(context, submission)
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
    }
}
