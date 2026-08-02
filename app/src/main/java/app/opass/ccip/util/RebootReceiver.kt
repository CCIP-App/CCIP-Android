package app.opass.ccip.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RebootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ScheduleUtil.rescheduleStarredSessionAlarms(context)
    }
}
