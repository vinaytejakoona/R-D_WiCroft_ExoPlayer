package com.iitb.wicroft;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by swinky on 7/3/17.
 */
public class BackgroundAppReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Backgroundappreceiver" , "the alarm recieved");

        MainActivity.move_to_background = true;
        //start heartbeat service as normal
        Intent startServiceIntent = new Intent(context, Heartbeat.class);
        context.startService(startServiceIntent);
    }
}
