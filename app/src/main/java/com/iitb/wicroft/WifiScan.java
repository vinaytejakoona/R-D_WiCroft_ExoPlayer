package com.iitb.wicroft;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by swinky on 1/8/16.
 */
public class WifiScan extends BroadcastReceiver {
    public void onReceive(Context c, final Intent intent) {
        String msg =" WiFiScan : ";
        String all_bssid_formatted = "";
        try {
            List<ScanResult> wifiScanList = MainActivity.wifimanager.getScanResults();

            MainActivity.wifis = new String[wifiScanList.size()];

            for (int i = 0; i < wifiScanList.size(); i++) {
                MainActivity.wifis[i] = ((wifiScanList.get(i)).toString());
               //todo : log properly
                all_bssid_formatted += get_bssidInfo(MainActivity.wifis[i]);
            }
            MainActivity.apInfo = all_bssid_formatted;
        } catch (Exception e) {
            msg += "Exception in Scan Results. "+e.toString();

        }

        if(MainActivity.debugging_on) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
            Threads.writeToLogFile(MainActivity.debugfilename ,format1.format(cal.getTime()) +" "+ Utils.sdf.format(cal.getTime())+msg);
        }

        Log.d(Constants.LOGTAG , msg);

        //SENDING HB HERE

        Thread thread = new Thread() {
            public void run() {
                String msg = "WiFiScan : Sending Device Info Thread";

                int status = DeviceInfo.sendDeviceInfo( intent);
                msg+= " Status Code"+status;

                if (status != 200) {

                    try {
                        //MainActivity.serverConnection = new Socket("wicroft.cse.iitb.ac.in", MainActivity.serverport);
                        MainActivity.serverConnection = new Socket("10.0.0.8", MainActivity.serverport);

                        msg += " Creating New Socket ";

                        try {
                            MainActivity.dis = new DataInputStream(MainActivity.serverConnection.getInputStream());
                            MainActivity.dout = new DataOutputStream(MainActivity.serverConnection.getOutputStream());
                            msg += " Setting Data Streams ";
                        } catch (Exception e) {
                            msg += "\n Exception handling while initalizing datastream objects for a new connection "+ e.toString();
                           // e.printStackTrace();
                        }

                    } catch (Exception e) {
                        msg += "\n Exception handling while creating a new connection" + e.toString();
                        e.printStackTrace();
                    }

                } else {

                    msg += " Heartbeat Sent Successfully.";
                }

                if(MainActivity.debugging_on) {
                    Calendar cal = Calendar.getInstance();
                    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                    Threads.writeToLogFile(MainActivity.debugfilename ,format1.format(cal.getTime()) +" "+ Utils.sdf.format(cal.getTime())+msg);
                }
                Log.d(Constants.LOGTAG , msg);

            }
        };
        thread.start();
    }

    public static String get_bssidInfo( String bssid_info)
    {
        String ssid="";
        String bssid="";
        String level="";
        StringTokenizer st = new StringTokenizer(bssid_info,",");
        while (st.hasMoreTokens()) {
            String temp = st.nextToken();

            if(temp.contains("SSID:") && !temp.contains("BSSID:"))
            {
                ssid = temp.replaceAll("SSID: ","" );
            }

            if(temp.contains("BSSID:"))
            {
                bssid = temp.replaceAll("BSSID: ","" );
            }


            if(temp.contains("level:"))
            {
                level = temp.replaceAll("level: ","" );
            }

        }

        return (ssid+","+bssid+","+level+";");
    }


}

