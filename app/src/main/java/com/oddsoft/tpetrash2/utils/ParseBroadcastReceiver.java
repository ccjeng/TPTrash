package com.oddsoft.tpetrash2.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.oddsoft.tpetrash2.Application;
import com.oddsoft.tpetrash2.MainActivity;
import com.oddsoft.tpetrash2.R;
import com.parse.ParseAnalytics;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by andycheng on 2015/7/11.
 */
public class ParseBroadcastReceiver extends ParsePushBroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {

                JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));

                String notificationTitle = context.getString(R.string.app_name);;
                String notificationContent ="";
                String notificationURI ="";

                if (json.has("title")) {
                    notificationTitle = json.getString("title").toString();
                }
                if (json.has("alert")) {
                    notificationContent = json.getString("alert").toString();
                }

                if(json.has("uri")) {
                    notificationURI = json.getString("uri");
                }

                Log.d(Application.APPTAG, notificationContent);

                Intent resultIntent = null;
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

                resultIntent = new Intent(context, MainActivity.class);
                resultIntent.putExtra("alert", notificationContent);

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
                                .setContentIntent(resultPendingIntent);

                int mNotificationId = 001;
                NotificationManager mNotifyMgr =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotifyMgr.notify(mNotificationId, builder.build());

                //ParseAnalytics.trackAppOpened(resultIntent);

            } catch (JSONException e) {
                Log.d(Application.APPTAG, e.getMessage());
            }

        }
}
