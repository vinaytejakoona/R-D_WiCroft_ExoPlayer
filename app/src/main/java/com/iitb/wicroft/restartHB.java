package com.iitb.wicroft;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by swinky on 14/9/16.
 */
public class restartHB extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        MainActivity.heartbeat_enabled = true;
        Threads.writeToLogFile(MainActivity.debugfilename , "\n\n ***Working fine..\n");
    }
}
