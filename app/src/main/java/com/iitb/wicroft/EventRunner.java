package com.iitb.wicroft;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiInfo;
import android.os.AsyncTask;
import android.util.Log;
import java.util.Calendar;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * Created by swinky on 28/6/16.
 */

public class EventRunner extends AsyncTask< Void , Void, Void> {
    final Context ctx;
    public static AlarmManager hb_restartalarm;

    public EventRunner(Context app_ctx) {
        ctx = app_ctx;
    }

    protected Void doInBackground(Void... params) {
        String msg ="\n EventRunner : ";

        String data = "";
        Long localTimeInMillis;// = Calendar.getInstance().getTimeInMillis();

        try {

            data = ConnectionManager.readFromStream();

            if (data == null || data.equals("")) {
                // This happens when some exception caught in readFromStream()

                try {
                    Thread.sleep(5000);
                }
                catch (InterruptedException ex) {
                    msg +=" Exception caught in sleep. " + ex.toString();
                   // Log.d(Constants.LOGTAG, ex.toString());
                }
            } else {
                msg += "Data Received from Server : "+data;

                Map<String, String> jsonMap = Utils.ParseJson(data);
                String action = jsonMap.get(Constants.action);
               // Log.d(Constants.LOGTAG, "###  ### ### serverTimeDelta = " + MainActivity.serverTimeDelta / 1000 + " seconds");


                if (action.compareTo(Constants.action_connectToAp) == 0) {

                    boolean flag =true;
                    String _ssid = jsonMap.get(Constants.ssid);
                    String _bssid = jsonMap.get(Constants.bssid);
                    String _username = jsonMap.get(Constants.username);
                    String _password = jsonMap.get(Constants.password);
                    String _type = jsonMap.get(Constants.security);
                    // WifiManager mgr =  (WifiManager)getSystemService(Context.WIFI_SERVICE);

                    if (_ssid.length() == 0) {
                        msg += " NULL values in change AP config";
                    }

                    if (_ssid.length() == 0 && _bssid.length() == 0 && _username.length() == 0 && _password.length() == 0 && _type.length() == 0) {
                        msg += " NULL values inchange AP config";
                    } else {
                        /*
                        output.append("\nSSID : "+_ssid);
                        output.append("\nBSSID : " + _bssid);
                        output.append("\nUSRNAME : "+_username);
                        output.append("\nPASSWORD : " + _password);
                        output.append("\nTYPE : " + _type);
*/
                        WifiInfo info = MainActivity.wifimanager.getConnectionInfo();
                        String currentBssid = info.getBSSID();
                        int currentNetworkId = 0;

                        if (currentBssid.equalsIgnoreCase(_bssid)) {
                            msg += "\n Already Connected to Same WiFi Network";
                        } else {

                            currentNetworkId = info.getNetworkId();
                            // WifiManager mgr =  (WifiManager)getSystemService(Context.WIFI_SERVICE);
                            List<WifiConfiguration> list = MainActivity.wifimanager.getConfiguredNetworks();
                            for (WifiConfiguration i : list) {
                                if (i.SSID.equalsIgnoreCase(_ssid) ) {
                                    MainActivity.wifimanager.removeNetwork(i.networkId);
                                    MainActivity.wifimanager.saveConfiguration();
                                    msg += "\n Old network conf deleted";
                                }
                            }


                            WifiConfiguration conf = new WifiConfiguration();
                            conf.SSID = "\"" + _ssid + "\"";
                            conf.BSSID = _bssid;


                            if (_type.equalsIgnoreCase("open")) { // open network

                                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                                conf.allowedAuthAlgorithms.clear();
                                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

                            } else if (_type.equalsIgnoreCase("wep")) { // wep network

                                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                                conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                                conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                                conf.wepKeys[0] = "\"".concat(_password).concat("\"");
                                conf.wepTxKeyIndex = 0;

                            } else if (_type.equalsIgnoreCase("wpa-psk")) { // wpa psk network

                                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                                conf.preSharedKey = "\"".concat(_password).concat("\"");

                            } else if (_type.equalsIgnoreCase("eap")) {  // eap network


                                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

                                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
                                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
                                conf.enterpriseConfig.setIdentity(_username.toString());
                                conf.enterpriseConfig.setPassword(_password.toString());
                                conf.enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.PEAP);
                                conf.enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.MSCHAPV2);


                            } else {
                                msg += "\nInvalid Type";
                                flag =false;
                            }

                            if(flag==true) {
                                try {

                                    int netId = MainActivity.wifimanager.addNetwork(conf);
                                    if (netId == -1) {
                                        msg += "\n Adding Conf to Network Failed";
                                        MainActivity.wifimanager.enableNetwork(currentNetworkId, true);
                                        msg += "\n Connecting to previous WiFi!";
                                    } else {
                                        msg += "\n Adding Conf to Network Success";
                                        MainActivity.wifimanager.disconnect();
                                        String msgconn = "Connection lost to AP in EventRunner";
                                        if(MainActivity.debugging_on) {
                                            Calendar cal = Calendar.getInstance();
                                            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                                            Threads.writeToLogFile(MainActivity.debugfilename ,"\n"+format1.format(cal.getTime()) +" "+ Utils.sdf.format(cal.getTime())+": DEBUG_CONNECTION_WIFI_CHANGEAP:LOST: "+msgconn+"\n");

                                        }
                                        MainActivity.wifimanager.saveConfiguration();
                                        boolean result = MainActivity.wifimanager.enableNetwork(netId, true);
                                        if (!result) {
                                            MainActivity.wifimanager.enableNetwork(currentNetworkId, true);
                                            msg += "\n Connecting to previous WiFi!!";
                                        }



                                    }


                                } catch (Exception ex) {
                                    msg += "\n Exception : " + ex.toString();
                                }
                            }

                        }
                    }


                }

                else if (action.compareTo(Constants.getLogFiles) == 0) {
                    msg +="\n getLogFiles Action received ";

                    try {
                        final String logFileName = Long.toString(MainActivity.load.loadid);
                        Runnable r = new Runnable() {
                            public void run() {
                                Threads.sendLog(logFileName);
                            }
                        };
                        Thread t = new Thread(r);
                        t.start();
                    } catch (NullPointerException e) {
                        // e.printStackTrace();
                        // Log.d("send log", "null pointer execption");
                        msg +="\n Send Log Null Pointer Exception : " + e.toString();
                        msg +="\n Sending all pending log files. ";
                        //sending pending log files
                        Runnable r = new Runnable() {
                            public void run() {
                                Threads.sendLogFilesBackground(ctx);
                            }
                        };
                        Thread t = new Thread(r);
                        t.start();

                    }


                }
                else if(action.compareTo(Constants.action_controlFile) == 0 ){
                    String control_msg = jsonMap.get(Constants.message).toString();
                    String fileid = jsonMap.get(Constants.fileid).toString();

                    String status = Threads.saveControlFile(fileid , control_msg);
                    if(status != " control file write success\n"){
                        // ack
                        String ack = "{\"action\":\"" + Constants.acknowledgement + "\",\"" + Constants.fileid + "\":\"" + fileid + "\"}";
                        ConnectionManager.writeToStream(ack);
                        Log.d("Debugging", "Control file Ack sent: " + ack);
                    }
                    else{
                        //negative ack
                    }


                }

                else if (action.compareTo(Constants.startExperiment) == 0) {
                    // foreground...

                    //stop heartbeat during an experiment.
                    MainActivity.heartbeat_enabled = false;

                    //start HB timer: Its the time after which the HB service will restart even if it doesn't receive stop experiment signal
                    Calendar cal = Calendar.getInstance();
                    Intent intentAlarm = new Intent(ctx, restartHB.class);
                    long restart_hb_timeout = Integer.parseInt(jsonMap.get(Constants.hb_timer))*1000 ;
                    Log.d(Constants.LOGTAG, "THE HB restart timer is : " + restart_hb_timeout);
                    hb_restartalarm.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis() + restart_hb_timeout, PendingIntent.getBroadcast(ctx, 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));

                    MyBrowser.selective_logging = Boolean.parseBoolean(jsonMap.get(Constants.selectiveLog));

                    msg+="\n Disabling Heartbeat as experiment is being started. EXP N0 : ";

                    MainActivity.serverTimeInMillis = Long.parseLong(jsonMap.get(Constants.serverTime));
                    localTimeInMillis= Calendar.getInstance().getTimeInMillis();
                    MainActivity.serverTimeDelta = MainActivity.serverTimeInMillis - localTimeInMillis;

                    File logfile = new File(MainActivity.logDir, MainActivity.debugfilename);
                    logfile.delete();
                    Log.d("Event Runner", "log file deleted");


                    msg += "\n ServerTimeDelta : "+  MainActivity.serverTimeDelta / 1000 + " seconds";
                    if (MainActivity.running == true) {
                        //this should not happen. As one experiment is already running Send 300 response
                        Log.d(Constants.LOGTAG, "Experiment running but received another control file request");
                        msg+="\n Experiment running but received another control file request";
                        //TODO: Should be reset the experiment here.. problem with receiving stop experiment signal.. never received..
                        return null;
                    }
                    MainActivity.running = true;
                    //Log.d(Constants.LOGTAG, "Controlfile receieved...");
                    MainActivity.startExperimentIntent = new Intent(ctx, Experiment.class);
                    MainActivity.startExperimentIntent.putExtra("textfollow", Boolean.parseBoolean((jsonMap.get(Constants.textFileFollow))));
                    MainActivity.startExperimentIntent.putExtra("fileid", "" + jsonMap.get(Constants.fileid).toString());
                    MainActivity.exptno = Integer.parseInt(jsonMap.get(Constants.exptid));

                   // Log.d("Event Runner : ", jsonMap.get(Constants.fileid).toString());
                    /*starting foreground service:
                    Foreground service is required because we don't want the activity manager in android to force close the app,
                    which it can do with a background service
                    */
                    ctx.startService(MainActivity.startExperimentIntent);


                } else if (action.compareTo(Constants.action_stopExperiment) == 0) {
                    Log.d(Constants.LOGTAG, "MainActivity.running boolean set to false. Reset()");
                    msg+=" Stop Experiment Received : ";

                    boolean was_running =  MainActivity.context.stopService(MainActivity.startExperimentIntent);
                    Log.d(Constants.LOGTAG, " Stopping the service from my event runnner : stop expt received.. : "+ was_running);

                    if (MainActivity.running && MainActivity.load != null) {

                        msg +=" Calling MainActivity.reset() ";
                        MainActivity.reset(ctx);
                    }

                }

                //these funcationalities are not used
                else if(action.compareTo(Constants.action_hbDuration) == 0) //change heartbeat duration
                {
                    MainActivity.heartbeat_duration = Integer.parseInt(jsonMap.get(Constants.hearbeat_duration));
                   // Heartbeat.Heartbeat_scheduleAlarm();
                }
                else if(action.compareTo(Constants.action_changeServer) == 0) //change server-ip&port duration
                {
                    if(jsonMap.get(Constants.serverip)!="")
                        MainActivity.serverip = jsonMap.get(Constants.serverip);
                    if(jsonMap.get(Constants.connport)!="")
                        MainActivity.serverport = Integer.parseInt(jsonMap.get(Constants.connport));
                    if(jsonMap.get(Constants.serverport)!="")
                        MainActivity.port = Integer.parseInt(jsonMap.get(Constants.serverport));


                }
                else {
                    //Log.d(Constants.LOGTAG, "eventRunner() : Wrong action code");
                    msg +=" Wrong Action Code ";
                }
            }

        } catch (Exception e) {

            //Somehow experiment could not be started due to some IOException in socket transfer. So again reset running variable to false
            MainActivity.running = false;
            msg+= "\n Somehow experiment could not be started due to some IOException in socket transfer : " + e.toString();
           // e.printStackTrace();
        }


        Log.d(Constants.LOGTAG, msg);
        if(MainActivity.debugging_on ) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
            Threads.writeToLogFile(MainActivity.debugfilename, format1.format(cal.getTime()) + " " + Utils.sdf.format(cal.getTime()) + msg);
        }
        return null;

    }


    @Override
    protected void onPostExecute(Void aVoid) {

      /*  String msg = "\n EventRunner : On post execute";
        Log.d(Constants.LOGTAG, msg);
        if(MainActivity.debugging_on) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
            Threads.writeToLogFile(MainActivity.debugfilename, format1.format(cal.getTime()) + " " + Utils.sdf.format(cal.getTime()) + msg);
        }
        */
        EventRunner runEvent = new EventRunner(ctx);
        runEvent.execute();

    }
}

