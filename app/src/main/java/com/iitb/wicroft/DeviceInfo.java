package com.iitb.wicroft;
import android.util.Log;
import android.app.IntentService;
import android.content.Intent;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by swinky on 16/6/16.
 */


public class DeviceInfo extends IntentService {


    public DeviceInfo() {
        super("DeviceInfo");
        // TODO Auto-generated constructor stub
    }

    protected void send_user_email(){
        if(!MainActivity.email_sent ){

            String email_msg = "{\"action\":\"" + "userEmail" + "\",\"" + "macAddress"+"\":\"" +Utils.getMACAddress() + "\",\""  + "email" + "\":\"" + MainActivity.user_email + "\"}";
            int write_status;
            Log.d("Heartbeat", "Account INfo : " + email_msg);
            write_status = ConnectionManager.writeToStream(email_msg);
            if(write_status == 200 )
                MainActivity.email_sent = true;
        }

    }

    @Override
    protected void onHandleIntent(Intent intent) {

        //send the email
        send_user_email();

        String msg = "DeviceInfo : ";

        if(MainActivity.heartbeat_enabled) {

            msg += "Starting Wifi Scan.";

            try {
                MainActivity.wifimanager.startScan();
            } catch (NullPointerException e) {
                msg+= " Starting scan : wifimanager is null";
            }
        }
        else
        {
            Utils.getMyDetailsJson();
            msg += "Heartbeat is Disabled.";

        }

        Log.d(Constants.LOGTAG, msg);
        if(MainActivity.debugging_on) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
            Threads.writeToLogFile(MainActivity.debugfilename, "\n"+format1.format(cal.getTime()) + " " + Utils.sdf.format(cal.getTime()) + msg);
        }
    }


    public static int sendDeviceInfo(Intent intent){

        int statuscode = 408;
        String heartBeat = null;
        heartBeat = Utils.getMyDetailsJson();
        //String heartBeat = "{\"action\":\"heartBeat\",\"ip\":\""+MainActivity.myIp +"\",\"port\":\""+MainActivity.myPort+"\",\"macAddress\":\""+Utils.getMACAddress()+"\"}";
        statuscode = ConnectionManager.writeToStream(heartBeat);

        if(MainActivity.debugging_on) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
            Threads.writeToLogFile(MainActivity.debugfilename ,format1.format(cal.getTime()) +" "+ Utils.sdf.format(cal.getTime())+"HB status code : " + statuscode );
        }

        AlarmReceiver.completeWakefulIntent(intent);

        return  statuscode;

    }

}
