package com.crowdpp.nagisa.crowdpp2.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.crowdpp.nagisa.crowdpp2.MainActivity;
import com.crowdpp.nagisa.crowdpp2.R;

/**
 * Created by sugan on 8/15/2017.
 */

public class NotificationDisplayer {

    private  NotificationManager mNotificationManager;
    private Context basedContext;
    private int NOTIFICATIN_ID;
    private String title;
    private  String content;
    private int icon;

    public NotificationDisplayer(int notification_id, Context based_context, String title, String content, int icon){
        this.NOTIFICATIN_ID = notification_id;
        this.basedContext = based_context;
        this.title = title;
        this.content = content;
        this.icon = icon;
    }

    public void showInfo(){
//        NotificationManager manager = (NotificationManager)this.getSystemService(NOTIFICATION_SERVICE);
//        //get the service of the notification
//        Notification mNotification = new Notification();
//        mNotification.icon = R.drawable.ic_activity;
//        mNotification.flags |=Notification.FLAG_ONGOING_EVENT;// is running
//        Intent intent = new Intent(this,MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
//        mNotification.contentIntent = pendingIntent;
//        mNotification.setLatestEventInfo(this, "Activity", "The Service is Running", pendingIntent);
//        manager.notify(NOTIFICATIN_ID, mNotification);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(basedContext)
                        .setSmallIcon(icon)
                        .setContentTitle(title)
                        .setContentText(content);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(basedContext, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(basedContext);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager =
                (NotificationManager) basedContext.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification n;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            n = mBuilder.build();
        } else {
            n = mBuilder.getNotification();
        }

        n.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
// mNotificationId is a unique integer your app uses to identify the
// notification. For example, to cancel the notification, you can pass its ID
// number to NotificationManager.cancel().
        mNotificationManager.notify(NOTIFICATIN_ID, n);
    }

    public void stop(){
        mNotificationManager.cancel(NOTIFICATIN_ID);
    }
}
