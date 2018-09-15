package app.opass.ccip.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import app.opass.ccip.R;
import app.opass.ccip.activity.SubmissionDetailActivity;
import app.opass.ccip.model.Submission;

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
                .format(context.getString(R.string.notification_submission_start), submission.getSubmissionDetail(context).getSubject(),
                        submission.getRoom());

        NotificationManager manager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        String CHANNEL_ID = "submission_bookmark";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    context.getString(R.string.bookmark_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_bookmark_black_24dp)
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

        manager.notify((int) System.currentTimeMillis(), notification);
    }
}
