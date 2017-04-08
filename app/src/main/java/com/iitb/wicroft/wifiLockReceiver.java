package com.iitb.wicroft;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.net.wifi.WifiManager.WifiLock;
import android.util.Log;

/**
 * Created by swinky on 28/2/17.
 */

//don't forget to register it in the manifest later

public class wifiLockReceiver extends WakefulBroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        //acquire lock

        WifiLock lock = MainActivity.wifimanager.createWifiLock(WifiManager.WIFI_MODE_FULL, "LockTag");
        lock.acquire();
        //set the lock acquired flag to true

        // set an alarm that will help realese the lock : set the time limit as 60 mins for now.




    }
}
