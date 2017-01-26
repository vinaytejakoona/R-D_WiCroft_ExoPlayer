package com.iitb.wicroft;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by swinky on 24/1/17.
 */
public class UpdateManager extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //The intent is received after the application has been updated, re-start the heartbeat service here.
        Intent startServiceIntent = new Intent(context, Heartbeat.class);
        context.startService(startServiceIntent);
        Log.d(" UpdateManager" , " The intent received , HB service has been started ." );

    }
}
