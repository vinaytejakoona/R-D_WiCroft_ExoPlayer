package com.iitb.wicroft;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by swinky on 24/1/17.
 */
public class UpdateManager extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if(MainActivity.debugging_on) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
            Threads.writeToLogFile(MainActivity.debugfilename, "\n" + format1.format(cal.getTime()) + " " + Utils.sdf.format(cal.getTime()) + ": UpdateManager : Intent Received. " + "\n");

        }

            //The intent is received after the application has been updated, re-start the heartbeat service here.
        Intent startServiceIntent = new Intent(context, Heartbeat.class);
        context.startService(startServiceIntent);

        Log.d(" UpdateManager" , " The intent received , HB service has been started ." );


    }
}
