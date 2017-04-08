package com.iitb.wicroft;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
//import android.app.ActivityManager;
//import android.content.Context;
import android.content.pm.PackageInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileFilter;
import java.net.NetworkInterface;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
//import java.io.UnsupportedEncodingException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.ContainerFactory;

import android.util.Log;


/**
 * Created by swinky on 16/6/16.
 */
public class Utils {


    //time formatter
    static SimpleDateFormat sdf = new SimpleDateFormat("ZZZZ HH:mm:s.S", Locale.US);


    //returns current time in proper format as defined above
    static String getTimeInFormat(){
        Calendar cal = Utils.getServerCalendarInstance();
        return sdf.format(cal.getTime());
    }



    //pings the given network
    public static boolean ping(String net){
       // Log.d(Constants.LOGTAG, "ping() : entered.");
        Runtime runtime = Runtime.getRuntime();
        try
        {
            String pingcommand = "/system/bin/ping -c 1 " + net;
          //  Log.d(Constants.LOGTAG, "ping() command : " + pingcommand);

            Process  mIpAddrProcess = runtime.exec(pingcommand);
            int exitValue = mIpAddrProcess.waitFor();
          //  Log.d(Constants.LOGTAG, "ping() mExitValue " + exitValue);
            if(exitValue==0){ //exit value 0 means normal termination
                return true;
            }else{
                return false;
            }
        }
        catch (InterruptedException ignore)
        {
            ignore.printStackTrace();
            System.out.println(" Exception:"+ignore);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.println(" Exception:"+e);
        }
        return false;
    }

    //returns httpclient object setting the default timeout params
    static HttpClient getClient(){

        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, Constants.timeoutConnection);
        HttpConnectionParams.setSoTimeout(httpParameters, Constants.timeoutSocket);

        DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

        return httpClient;
    }


    /*
    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) Context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    */

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }






    public static String getIP(){
        WifiInfo info;
        int ip;
        int port;

        try {
            info = MainActivity.wifimanager.getConnectionInfo();
            ip = info.getIpAddress();

        } catch (Exception e) {
            Log.d(Constants.LOGTAG, "Utils: getIP() :  Exception caught " + e.toString());
            ip =0;
            if(MainActivity.debugging_on) {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                Threads.writeToLogFile(MainActivity.debugfilename ,"\n"+format1.format(cal.getTime()) +" "+ Utils.sdf.format(cal.getTime())+": Utils : getIp() " + e.toString());
                //Threads.writeToLogFile(MainActivity.debugfilename , Utils.sdf.format(cal.getTime())+": Heartbeat : Initializing everything ");
            }
        }
       // MainActivity.myIp = Integer.toString(ip);
               MainActivity.myIp = Formatter.formatIpAddress(ip);
        return Formatter.formatIpAddress(ip);
    }


//***** for android 6...
    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            Log.d(Constants.LOGTAG,ex.toString());
        }
        return "02:00:00:00:00:00";
    }




    public static String getMACAddress(){
        WifiInfo info;
        String address;
        try {
            info = MainActivity.wifimanager.getConnectionInfo();
            address = info.getMacAddress();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(Constants.LOGTAG, "Utils: getMacAddress() :  Exception caught " + e.toString());
            address ="";
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Call for marshmallow and above
            String macAddress = getMacAddr(); //version 6

            Log.d(Constants.LOGTAG, "Getting mac for version 6 and above : " + macAddress);
            // String macAddress = android.provider.Settings.Secure.getString(MainActivity.context.getContentResolver(), "bluetooth_address");

            return macAddress;
        } else {
            // Implement this feature without material design
            return address;
        }



    }

    public static String getWifiStrength(){
        WifiInfo info = MainActivity.wifimanager.getConnectionInfo();
        int level;

        try {
            level = WifiManager.calculateSignalLevel(info.getRssi(), 10);
        } catch (Exception e) {
            Log.d(Constants.LOGTAG, "Utils: getWifiStrength() : rssi : Exception caught " + e.toString());
            level = 0;
        }

        return Integer.toString(level);
    }

    public static String getAvailableStorage(){
        File path = Environment.getDataDirectory(); //internal storage
        StatFs sf = new StatFs(path.getPath());
        @SuppressWarnings("deprecation")
        int blocks = sf.getAvailableBlocks();
        @SuppressWarnings("deprecation")
        int blocksize = sf.getBlockSize();
        long availStorage = blocks * blocksize/(1024 * 1024); //Mega bytes
        return Long.toString(availStorage);
    }

    public static String getTotalRAM() {
        RandomAccessFile reader = null;
        String load = "0";
        try {
            reader = new RandomAccessFile("/proc/meminfo", "r");
            load = reader.readLine();
            String[] tokens = load.split(" +");
            load = tokens[1].trim(); //here is the memory
            int ram = Integer.parseInt(load); //KB
            ram = ram/1024;
            load = Integer.toString(ram);
            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return load;
    }

    /**
     * Gets the number of cores available in this device, across all processors.
     * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     * @return The number of cores, or 1 if failed to get result
     */
    public static int getNumCores() {
        //Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                //Check if filename is "cpu", followed by a single digit number
                if(Pattern.matches("cpu[0-9]+", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch(Exception e) {
            //Default to return 1 core
            return 1;
        }
    }

    public static String getProcessorSpeed() {
        RandomAccessFile reader = null;
        String load = "0";
        try {
            reader = new RandomAccessFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq", "r");
            load = reader.readLine();
            int speed = Integer.parseInt(load); //Khz
            speed = speed / 1000; //Mhz
            load = Integer.toString(speed);
            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return load;
    }


    public static String getExpOverJson()
    {
        JSONObject obj = new JSONObject();

        obj.put("action", "expOver");
        obj.put("exp", Long.toString(MainActivity.load.loadid));
        obj.put("ip", MainActivity.myIp);
        obj.put("port", Integer.toString(MainActivity.myPort));
        obj.put("macAddress" ,Utils.getMACAddress());
        String jsonString = obj.toJSONString();
        return  jsonString;

    }

    public static String getAppVersion(){
        String version ="";
        int verCode =-1;

        try {

            PackageInfo pInfo = MainActivity.context.getPackageManager().getPackageInfo(MainActivity.context.getPackageName(), 0);
            version = pInfo.versionName;
            verCode = pInfo.versionCode;
            Log.d("Utils" , "Version Name : "+ version+ "  Version Number : "+ Integer.toString(verCode));
        }
        catch (Exception ex){
            Log.d("Utils" , "Version NUmber exception");

        }

        return version;
    }



    public static String getMyDetailsJson()
    {
        JSONObject obj = new JSONObject();
        getIP();

        obj.put("action", "heartBeat");
        obj.put("ip", MainActivity.myIp);
        obj.put("appversion", getAppVersion()) ;
        obj.put("androidVersion" , Integer.toString( Build.VERSION.SDK_INT ));
        obj.put("isInForeground", String.valueOf(MainActivity.is_running_in_foreground));
        obj.put("devicename", getDeviceName()) ;

        if(MainActivity.serverConnection == null) {
            obj.put("port", "");
        }
        else {
            MainActivity.myPort = MainActivity.serverConnection.getLocalPort();
            obj.put("port", Integer.toString(MainActivity.myPort));
        }
        obj.put("macAddress", Utils.getMACAddress());

        try {
            MainActivity.rssi = MainActivity.wifimanager.getConnectionInfo().getRssi();
        } catch (Exception e) {
            Log.d(Constants.LOGTAG,"Utils: getmyDetailsJson() : rssi : Exception caught "+e.toString() );
            MainActivity.rssi = 0;
        }

        try {
            MainActivity.bssid = MainActivity.wifimanager.getConnectionInfo().getBSSID();
            MainActivity.bssid=MainActivity.bssid.replaceAll("\"","");
        } catch (Exception e) {
            Log.d(Constants.LOGTAG,"Utils: getmyDetailsJson() : bssid : Exception caught "+e.toString() );
            MainActivity.bssid = "";
        }

        try {
            MainActivity.ssid = MainActivity.wifimanager.getConnectionInfo().getSSID();
            MainActivity.ssid=MainActivity.ssid.replaceAll("\"", "");
        } catch (Exception e) {
            Log.d(Constants.LOGTAG,"Utils: getmyDetailsJson() : ssid : Exception caught "+e.toString() );
            MainActivity.ssid = "";
        }

        try {
            MainActivity.linkSpeed = MainActivity.wifimanager.getConnectionInfo().getLinkSpeed();
        } catch (Exception e) {
            Log.d(Constants.LOGTAG,"Utils: getmyDetailsJson() : linkspeed : Exception caught "+e.toString() );
            MainActivity.linkSpeed = 0;
        }

        obj.put(Constants.rssi,Integer.toString(MainActivity.rssi));
        obj.put(Constants.bssid, MainActivity.bssid);
        obj.put(Constants.ssid, MainActivity.ssid);
        obj.put(Constants.linkSpeed, Integer.toString(MainActivity.linkSpeed));

        obj.put(Constants.processorSpeed, getProcessorSpeed());
        obj.put(Constants.numberOfCores, Integer.toString(getNumCores()));
       // obj.put(Constants.wifiSignalStrength, getWifiStrength());

        obj.put(Constants.storageSpace, getAvailableStorage());
        obj.put(Constants.memory, getTotalRAM());
        //nameValuePairs.add(new BasicNameValuePair(Constants.packetCaptureAppUsed, (new Boolean(false)).toString()));

        obj.put("bssidList",MainActivity.apInfo);

        String jsonString = obj.toJSONString();
        return  jsonString;

    }


    static Calendar getServerCalendarInstance(){
        Calendar cal = Calendar.getInstance();
        Log.d("UTILS", "getServerCalendarInstance local " + MainActivity.sdf.format(cal.getTime()));
        cal.add(Calendar.MILLISECOND, (int)MainActivity.serverTimeDelta);
        Log.d("UTILS", "getServerCalendarInstance offset = " + MainActivity.serverTimeDelta + " | server " + MainActivity.sdf.format(cal.getTime()));
        return cal;
    }



    //parse the json string sent by server in form of map of key value pairs
    @SuppressWarnings("unchecked")
    static Map<String, String> ParseJson(String json){
        Map<String, String> jsonMap = null;
        JSONParser parser = new JSONParser();
        ContainerFactory containerFactory = new ContainerFactory(){
            @SuppressWarnings("rawtypes")
            public List creatArrayContainer() {
                return new LinkedList();
            }
            @SuppressWarnings("rawtypes")
            public Map createObjectContainer() {
                return new LinkedHashMap();
            }

        };

        try {
            jsonMap = (Map<String, String>) parser.parse(json, containerFactory);
        } catch (ParseException e) {
            System.out.println();
            e.printStackTrace();
        }
        return jsonMap;
    }

    public static class NewConnection extends AsyncTask< Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            MainActivity.heartbeat_enabled = true ;
            if(MainActivity.serverConnection != null){
                try{
                    MainActivity.serverConnection.close();
                    MainActivity.serverConnection = null;
                    MainActivity.dis = null;
                    MainActivity.dout = null;
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }

                    try {
                    //MainActivity.serverConnection = new Socket("wicroft.cse.iitb.ac.in", MainActivity.serverport);
                        MainActivity.serverConnection = new Socket("10.0.0.3", MainActivity.serverport);
                    Log.d("Utils", "creating new socket...");

                    try {
                        MainActivity.dis = new DataInputStream( MainActivity.serverConnection.getInputStream());
                        MainActivity.dout = new DataOutputStream( MainActivity.serverConnection.getOutputStream());
                       // MainActivity.ConnectionCondition.open();
                        Log.d("Utils", "setting data streams...");
                    }
                    catch (Exception e) {

                        Log.d("Utils", "Exceptionwhile initalizing datastream " + e.toString());

                       // e.printStackTrace();
                    }

                    } catch (Exception e) {
                    Log.d("Utils", "Exceptionwhile creating a new connection" + e.toString());
                    //e.printStackTrace();
                        }

            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d("Utils New COnnection :", "I am in post Exceute");
            super.onPostExecute(aVoid);

        }
    }



}
