package app.opass.ccip.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.gson.internal.bind.util.ISO8601Utils;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.List;
import app.opass.ccip.model.Submission;

public class RebootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        List<Submission> submissions = PreferenceUtil.loadStars(context);
        for (Submission submission : submissions) {
            try {
                Date date = ISO8601Utils.parse(submission.getStart(), new ParsePosition(0));
                if (System.currentTimeMillis() < date.getTime()) {
                    AlarmUtil.setSubmissionAlarm(context, submission);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}
