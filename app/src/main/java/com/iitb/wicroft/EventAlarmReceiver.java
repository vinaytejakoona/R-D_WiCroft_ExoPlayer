package com.iitb.wicroft;

/**
 * Created by swinky on 3/7/16.
 */

import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

//Triggered when alarm is received. Handles two events : normal alarm event to process next download event using DownloaderService
public class EventAlarmReceiver extends WakefulBroadcastReceiver
{
    // Prevents instantiation
    public EventAlarmReceiver() {
    }

    // Called when the BroadcastReceiver gets an Intent it's registered to receive
    @Override
    public void onReceive(final Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String msg = "EventAlarm Receiver : ";

            if(!MainActivity.running){
               // Log.d(Constants.LOGTAG, "Alarm Receiver : alarm just received. But experiment not running");
                msg +=" alarm just received. But experiment not running ";

                return;
            }

            int eventid = bundle.getInt("eventid");

            if(eventid >= 0){
               // Log.d(Constants.LOGTAG, "Alarm Receiver : alarm just received (eventid=" + eventid + ") Now preparing to handle event");

                msg+="\n alarm just received (eventid=" + eventid + ") Now preparing to handle event";
                Intent callingIntent = new Intent(context, DownloaderService.class);
                callingIntent.putExtra("eventid", (int)eventid);
                startWakefulService(context, callingIntent);


                msg+=" Started the Downloader Service";
            }
            else{
                msg += " eventid=" + eventid + " Setting up first alarm";
            }

        Log.d(Constants.LOGTAG, msg);
        if(MainActivity.debugging_on) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
            Threads.writeToLogFile(MainActivity.debugfilename, format1.format(cal.getTime()) + " " + Utils.sdf.format(cal.getTime()) + msg);
        }

            scheduleNextAlarm(context);
        }


    //Looks at next event from eventlist and schedules next alarm
    void scheduleNextAlarm(Context context){
        String msg = "EventAlarmeceiver - ScheduleNextAlarm ";

        if(!MainActivity.running){
            msg+= "scheduleNextAlarm : Experiment not 'running'";
            return;
        }


        if(MainActivity.load == null){
            msg+= "scheduleNextAlarm : load null";
            return;
        }

        if(MainActivity.currEvent >= MainActivity.load.events.size()) {
            msg+= "scheduleNextAlarm : All alarms over. Curr Experiment finished";
            return;
        }


       // RequestEvent e = MainActivity.load.events.get(MainActivity.currEvent);
        RequestEvent e = new RequestEvent( MainActivity.load.events.get(MainActivity.currEvent) ) ;
        Intent intent = new Intent(context, EventAlarmReceiver.class);
        intent.putExtra("eventid", (int)MainActivity.currEvent);

        PendingIntent sender = PendingIntent.getBroadcast(context, Constants.alarmRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

     msg +="\n e.cal time in millisec : "+ Long.toString(e.cal.getTimeInMillis()) + "MainActivity.serverTimeDelta : " + Long.toString(MainActivity.serverTimeDelta);
        msg+="\n Scheduling " + MainActivity.currEvent + "@" + MainActivity.sdf.format(e.cal.getTime());
        msg+= "\n current time in ms :" + Long.toString(Calendar.getInstance().getTimeInMillis());
        msg+= "\n alarmwakeup in ms"+ Long.toString(e.cal.getTimeInMillis() - MainActivity.serverTimeDelta);
		MainActivity.am.set(AlarmManager.RTC_WAKEUP, e.cal.getTimeInMillis() - MainActivity.serverTimeDelta, sender);
       // Log.d(Constants.LOGTAG, MainActivity.sdf.format(cal.getTime()) + "Scheduling " + MainActivity.currEvent + "@" + MainActivity.sdf.format(e.cal.getTime()) + "\n");


		/*
		 * [event_time_stamp - (server - local)] gives when alarm should be scheduled.
		 * Why ? Details ahead :
		 * For e.g if local 2.00, server 2.10. Difference (server - local) = 10
		 * Now server says schedule alarm at 2.15. Alarms follow local time.
		 * So now according to local time, alarm should be scheduled at
		 * time 2.05 (because at that moment servertime will be 2.05 + 10 = 2.15)
		 */

        MainActivity.currEvent++;
       // Log.d(Constants.LOGTAG, "Current event" + MainActivity.currEvent);

        Log.d(Constants.LOGTAG, msg);
        if(MainActivity.debugging_on) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
            Threads.writeToLogFile(MainActivity.debugfilename, format1.format(cal.getTime()) + " " + Utils.sdf.format(cal.getTime()) + msg);
        }


    }
}
