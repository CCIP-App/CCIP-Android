package org.pycontw.ccip.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import org.pycontw.ccip.R;
import org.pycontw.ccip.activity.SubmissionDetailActivity;
import org.pycontw.ccip.model.Submission;

public class SubmissionAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Submission submission = JsonUtil
                .fromJson(intent.getStringExtra(SubmissionDetailActivity.INTENT_EXTRA_PROGRAM),
                        Submission.class);

        intent.setClass(context, SubmissionDetailActivity.class);
        intent.putExtra(SubmissionDetailActivity.INTENT_EXTRA_PROGRAM, JsonUtil.toJson(submission));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        String notificationContent = String
                .format(context.getString(R.string.notification_submission_start), submission.getSubject(),
                        submission.getRoom());

        NotificationCompat.Builder builder = new Builder(context)
                .setSmallIcon(R.drawable.conf_logo)
                .setContentTitle(context.getString(R.string.conf_name))
                .setContentText(notificationContent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationContent))
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(Notification.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_ALARM)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent);

        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager manager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify((int) System.currentTimeMillis(), notification);
    }
}
