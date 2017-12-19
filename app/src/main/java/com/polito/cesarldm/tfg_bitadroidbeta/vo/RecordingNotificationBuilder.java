package com.polito.cesarldm.tfg_bitadroidbeta.vo;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.polito.cesarldm.tfg_bitadroidbeta.R;
import com.polito.cesarldm.tfg_bitadroidbeta.SelectDevicesActivity;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Cesar on 27/11/2017.
 */

public class RecordingNotificationBuilder {
    Context context;
    NotificationManager mNotifyMgr;
    int notificationId;
    NotificationCompat.Builder mBuilder;
    Class cls;

    public RecordingNotificationBuilder(Context context, int notificationId, Class cls){
        this.context=context;
        this.notificationId=notificationId;
        this.cls=cls;
       this.mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Bitadroid")
                        .setContentText("Recording in progress");

        Intent intent = new Intent(context,cls);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                intent, 0);
        this.mBuilder.setContentIntent(pendingIntent);
       this.mNotifyMgr =(NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
    }

    public void launchNotification(){
        NotificationCompat.Builder mBuilder;

    // Sets an ID for the notification
        int mNotificationId = notificationId;
    // Gets an instance of the NotificationManager service

    // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, this.mBuilder.build());
    }
    public void closeNotification(){
        mNotifyMgr.cancel(notificationId);
    }

}
