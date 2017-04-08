package com.iitb.wicroft;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by swinky on 7/3/17.
 * not required : delete this.
 */
public class ForegroundAppService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //bringing heartbeat service in foreground
        MainActivity.app_in_foreground = true;

        Log.d("ForegroundAppService" , " I am in foreground app service..!!!!!!!!!!!!!!!!!!!!!!!!!");
        //start heartbeat service as normal
        Intent startServiceIntent = new Intent(this, Heartbeat.class);
        startService(startServiceIntent);

        return super.onStartCommand(intent, flags, startId);
    }
}
