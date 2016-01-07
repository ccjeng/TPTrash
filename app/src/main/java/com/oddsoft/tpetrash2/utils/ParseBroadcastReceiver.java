package com.oddsoft.tpetrash2.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.oddsoft.tpetrash2.Application;
import com.oddsoft.tpetrash2.view.MainActivity;
import com.oddsoft.tpetrash2.R;
import com.parse.ParseAnalytics;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by andycheng on 2015/7/11.
 */
public class ParseBroadcastReceiver extends ParsePushBroadcastReceiver {

    private static final String TAG= Application.class.getSimpleName();

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        super.getNotification(context, intent);

        String notificationTitle = context.getString(R.string.app_name);
        String notificationContent = getNotificationValue(intent);

        Log.d(TAG, notificationContent);

        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.putExtra("alert", notificationContent);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        //show custom notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.icon)
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationContent)
                        .setStyle(inboxStyle)
                        .setContentIntent(resultPendingIntent)
                        .setAutoCancel(true);

        return builder.build();
    }
    @Override
    protected void onPushOpen(Context context, Intent intent) {
        super.onPushOpen(context, intent);
        Log.v(TAG, "onPushOpen called");

        String notificationContent = getNotificationValue(intent);;

        Intent i = new Intent(context, MainActivity.class);
        //i.putExtras(intent.getExtras());
        i.putExtra("alert", notificationContent);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        Log.v(TAG, "onPushReceive called");
        super.onPushReceive(context, intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive Called");
        super.onReceive(context, intent);
        ParseAnalytics.trackAppOpened(intent);

    }


    private String getNotificationValue(Intent intent) {
        String notificationContent ="";
        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            notificationContent = json.getString("alert").toString();

        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }

        return notificationContent;
    }

}
