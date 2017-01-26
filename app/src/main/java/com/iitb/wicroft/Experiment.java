package com.iitb.wicroft;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by swinky on 7/10/16.
 */
public class Experiment extends Service {

    public Experiment() {
        super();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override

    public void onCreate() {

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent myintent, int flags, int startId) {

        final int myID = 1234;
        Notification notification;

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.wicroft)
                        .setContentTitle("Loadgenerator")
                        .setContentText("Experiment in Progress..!!");

        notification = mBuilder.build();
        if (Integer.parseInt(Build.VERSION.SDK) >= Build.VERSION_CODES.DONUT) {
        // Build.VERSION.SDK_INT requires API 4 and would cause issues below API 4

        } else {
            //   Notification notice = new Notification(R.mipmap.icon, "Ticker text", System.currentTimeMillis());
            //   notice.setLatestEventInfo(this, "Title text", "Content text", pendIntent);

        }
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

        //starting Experiment

        boolean textFileFollow = myintent.getBooleanExtra("textfollow", true);
        // boolean textFileFollow = Boolean.parseBoolean((String) jsonMap.get(Constants.textFileFollow));
        String msg ="";

        if (textFileFollow) {
            String fileid = myintent.getStringExtra("fileid");
            Log.d("Foreground", "Control file received setting up alarms..!!!");
            String controlFile = Threads.getcontrolfile(fileid);

            msg += " Setting Up Alarms for the experiment.";
            startForeground(myID, notification);

            /*
            if(controlFile==null)
                Log.d("Foreground ", "its null..!!");
            Log.d(" Contrl FIle.." , controlFile);
            */

            MainActivity.load = RequestEventParser.parseEvents(controlFile);
            MainActivity.numDownloadOver = 0;
            MainActivity.currEvent = 0;

            Log.d("Foreground", "Setting up alarams..........here.....");
            //send broadcast to trigger alarms
            Intent localIntent = new Intent(Constants.BROADCAST_ALARM_ACTION);
            localIntent.putExtra("eventid", (int) -1); //this is just to trigger first scheduleNextAlarm
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
            //restart the alarm for session timeout
            // MainActivity.setKillTimeoutAlarm(ctx);


            String ack = "{\"action\":\"" + "expack" + "\",\"" + Constants.experimentNumber + "\":\"" + MainActivity.load.loadid + "\"}";
            ConnectionManager.writeToStream(ack);
            Log.d("Debugging", " Ack sent: " + ack);

            msg += "\n Written Ack:  "+ack+"for Experiment_no : "+MainActivity.load.loadid ;

        } else {
            //Log.d(Constants.LOGTAG, "eventRunner : No control file in response");
            msg += "\n eventRunner : No control file in response";
        }

        return super.onStartCommand(myintent, flags, startId);

    }

    @Override
    public void onDestroy() {

            Log.d(" Foreground ", " The service is destroying here.......");
            stopForeground( true);
        super.onDestroy();
    }
}
