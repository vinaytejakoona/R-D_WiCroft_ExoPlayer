package com.iitb.wicroft;

//import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.app.Service;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
/*
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Map;
*/


/**
 * Created by swinky on 22/6/16.
 */
public class BackgroundServices extends Service {

    public BackgroundServices() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void move_to_foreground(){

        //final static int myID = 1234;

        //The intent to launch when the user clicks the expanded notification
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, intent, 0);

        //This constructor is deprecated. Use Notification.Builder instead
        Notification notice = new Notification(R.mipmap.ic_launcher, "Ticker text", System.currentTimeMillis());

        //This method is deprecated. Use Notification.Builder instead.
     //   notice.setLatestEventInfo(this, "Experiment Running", "PLease Don't force close app", pendIntent);

        notice.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(1234, notice);

    }

    @Override
    public void onCreate() {
        super.onCreate();

        final Context ctx = getApplicationContext();
        Log.d("Backgroundservices", "Starting event Runner Async Task");
        if(MainActivity.debugging_on) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
            Threads.writeToLogFile(MainActivity.debugfilename ,"\n"+format1.format(cal.getTime()) +" "+ Utils.sdf.format(cal.getTime())+": Backgroundservices: Starting event Runner Async Task.");
            //Threads.writeToLogFile(MainActivity.debugfilename , Utils.sdf.format(cal.getTime())+": Heartbeat : Initializing everything ");
        }
        EventRunner runEvent = new EventRunner(ctx);
        runEvent.execute();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }


}
