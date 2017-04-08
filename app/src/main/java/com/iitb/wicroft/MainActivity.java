package com.iitb.wicroft;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.app.AlarmManager;
import android.util.Log;
import android.net.wifi.WifiManager;
import android.view.View;
import android.webkit.WebView;

import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.rampo.updatechecker.UpdateChecker;
import com.rampo.updatechecker.notice.Notice;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ParserException;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSourceInputStream;
import com.google.android.exoplayer2.upstream.DataSpec;

import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SampleChooserActivity";
    Intent intent;

    static SimpleDateFormat sdf = new SimpleDateFormat("ZZZZ HH:mm:s:S", Locale.US);
    public static HashMap<Integer, WebView> webViewMap = new HashMap<Integer, WebView>();

   // public static String serverip = "10.129.28.176";
    public static int serverport = 8001;
    public static int port = 8080;

    public static boolean debugging_on = true;
    static AlarmManager am ;
    static WifiManager wifimanager;
    static File logDir; //directory containing log files
    static File controlDir; //dirctory containing control files

    public static Socket serverConnection = null;
    public static int heartbeat_duration = 10; //by default the device sends one heartbeat per 60 seconds.
    public static DataInputStream dis = null;
    public static DataOutputStream dout =null;
    public static String myIp= "";
    public static int myPort = 0;
    public static int noOfStalls =0;



    static long serverTimeDelta = 0; //(serverTime - clientTime)
  //  static boolean experimentOn = true; //whether to listen as server(session is on)
    static boolean running = false; //whether scheduling alarms and downloading is going on
    static boolean heartbeat_enabled = true ;
    static int numDownloadOver = 0; //indicates for how many events download in thread is over
    static boolean app_in_foreground = false;
    static boolean move_to_background = false;
    static boolean is_running_in_foreground = false;
    static Load load = null; //this stores info about current experiment such as exp id and all events(get requests) with resp scheduled time
    static int currEvent = 0; //which event is currently being processed

    public static String debugfilename = "Debug_logs";
    public static String logfilename;
    public static Context context;

//TODO : make memory efficient...
    /*********remove this********/
    static int rssi;
    static String bssid;
    static String  ssid;
    static int linkSpeed;  // Mbps
    static String apInfo = "";
    static int exptno = -1;
    static Long serverTimeInMillis;

    static String wifis[]; // list of available wifi

    static Intent startExperimentIntent ;
    static boolean email_sent = false;
    static String user_email ="";

    //static UpdateChecker checker ;


 //  public static ConditionVariable ConnectionCondition = new ConditionVariable(false); // for wait-notify

//    static TextView textbox; //scrollable textview displays important messages

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Log.d("Checking flow", " MainActivity on create.....");

        context = getApplicationContext();
        Log.d("Main Activity", "Starting the heartbeat service");
        Intent startServiceIntent = new Intent(this, Heartbeat.class);
        startService(startServiceIntent);

    }

    @Override
    public void onBackPressed() {
        Log.d(Constants.LOGTAG, "onBackPressed Called");
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public void onStop() {
        super.onStop();


    }

    public static void reset(Context ctx){
        if(running){
            load = null;
            currEvent = 0;
            running = false;
            numDownloadOver = 0;
            heartbeat_enabled = true;
           // TODO : schedule alarms first

            //cancel scheduled alarms
            Intent intent = new Intent(ctx, AlarmReceiver.class);
            PendingIntent sender = PendingIntent.getBroadcast(ctx, Constants.alarmRequestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            am.cancel(sender);

            cancelTimeoutAlarm(ctx);


        }
    }


    static void cancelTimeoutAlarm(Context ctx){
        Log.d("DEBUG_MAIN_ACTIVITY", "cancel timeout alarm ");
        //cancel timeout alarm
        Intent timeoutintent = new Intent(ctx, AlarmReceiver.class);
        PendingIntent timeoutsender = PendingIntent.getBroadcast(ctx, Constants.timeoutAlarmRequestCode, timeoutintent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(timeoutsender);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    synchronized public static void removeWebView(int eventid){
        webViewMap.remove(eventid);
    }

    public void clickexit(View v)
    {
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }



//    public void start(String uri) {
//        //startActivity(sample.buildIntent(this));
//
//        String sampleName = null;
//
//        String extension = "mpd";
//        UUID drmUuid = null;
//        String drmLicenseUrl = null;
//        String[] drmKeyRequestProperties = null;
//        boolean preferExtensionDecoders = false;
//
//
//
//        SampleChooser.Sample sample = new SampleChooser.UriSample(sampleName, drmUuid, drmLicenseUrl, drmKeyRequestProperties,
//                preferExtensionDecoders, uri, extension);
//
//        this.intent= sample.buildIntent(this);
//        startService(sample.buildIntent(this));
//
//
//        //this.intent=sample.buildIntent(this);
//
//    }

}


