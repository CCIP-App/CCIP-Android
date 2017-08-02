package org.coscup.ccip.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.List;
import org.coscup.ccip.model.Submission;

public class RebootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        List<Submission> submissions = PreferenceUtil.loadStars(context);
        for (Submission submission : submissions) {
            AlarmUtil.setSubmissionAlarm(context, submission);
        }
    }
}
