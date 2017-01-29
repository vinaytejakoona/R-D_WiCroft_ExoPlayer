package com.iitb.wicroft;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.rampo.updatechecker.*;

/**
 * Created by swinky on 28/1/17.
 */
public class UpdateService extends Service {

    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("UpdateService" , " Service started");

/*
        Intent dialogIntent = new Intent(this, UpdateChecker.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialogIntent);
*/

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.wicroft)
                        .setContentTitle("WiCroft : Update Available")
                        .setContentText("Click here to update to latest version");

        mBuilder.setSound(alarmSound);
        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
        Intent myIntent;

        try {
            myIntent =new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));

        } catch (android.content.ActivityNotFoundException anfe) {
            myIntent =new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));

        }

        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent intent2 = PendingIntent.getActivity(this, 1,myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(intent2);
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(1, mBuilder.build());



/*

        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object

        try {
            Intent myIntent =new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
            myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(myIntent);
        } catch (android.content.ActivityNotFoundException anfe) {
            Intent myIntent =new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
            myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(myIntent);

        }
*/
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
