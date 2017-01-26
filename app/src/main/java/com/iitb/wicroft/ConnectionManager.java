package com.iitb.wicroft;

import android.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by swinky on 9/7/16.
 */
public class ConnectionManager {

    private static ConnectionManager sync_obj = new ConnectionManager();
/*
    private static Socket socket;
    private static DataInputStream din;
    private static DataOutputStream dout ;

    // creation of connection
    public synchronized static int ConnManager() {

        int success = 0;
        try {
            if(socket != null){
                try{
                    socket.close();
                }catch(Exception ex){
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.BROADCAST_MESSAGE, "\n##2## "+ ex.toString());
                    Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtras(bundle);
                    LocalBroadcastManager.getInstance(MainActivity.context).sendBroadcast(localIntent);
                }
            }

            for(int i =0;i<3;i++){ //  currently trying for 3 times..needs to be looped forever
                socket = new Socket(MainActivity.serverip, MainActivity.serverport);
                //socket = new Socket(MainActivity.serverip, MainActivity.destPort);
                if(socket == null){
                    try {
                        Thread.sleep(5000);
                    }catch(InterruptedException ex){
                        Bundle bundle = new Bundle();
                        bundle.putString(Constants.BROADCAST_MESSAGE,"\n##3## "+  ex.toString());
                        Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtras(bundle);
                        LocalBroadcastManager.getInstance(MainActivity.context).sendBroadcast(localIntent);
                    }
                }else {
                    break;
                }

            }


            if(socket != null){

                MainActivity.myPort = socket.getLocalPort();
                MainActivity.myIp = socket.getLocalAddress().toString().split("/")[1];
                din = new DataInputStream(socket.getInputStream());
                dout = new DataOutputStream(socket.getOutputStream());
                success = 1;
            }else {
                din = null;
                dout = null;
                success = 0;
            }

        }catch(UnknownHostException ex){
            Bundle bundle = new Bundle();
            bundle.putString(Constants.BROADCAST_MESSAGE,"\n##4## "+  ex.toString());
            Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtras(bundle);
            LocalBroadcastManager.getInstance(MainActivity.context).sendBroadcast(localIntent);
            success = 0 ;
        }catch(IOException ex){
            Bundle bundle = new Bundle();
            bundle.putString(Constants.BROADCAST_MESSAGE,"\n##5## "+  ex.toString());
            Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtras(bundle);
            LocalBroadcastManager.getInstance(MainActivity.context).sendBroadcast(localIntent);
            success = 0 ;
        }
        return success;
    }
    */

   // public synchronized static String readFromStream() {
   public static String readFromStream() {

        String data  = "";
       String msg =" ConnectionManager Read :";
        try {
           // if (MainActivity.dis.available()>0){

                int length = MainActivity.dis.readInt();

            msg += " Reading "+length+ " Bytes from the socket";
                for (int i = 0; i < length; ++i) {
                    data += (char) MainActivity.dis.readByte();

                }
           // }
        }catch(IOException ex){
            data = null;
            msg +=" Caught an exception while reading from Socket"+ ex.toString();
            if(!MainActivity.heartbeat_enabled)
                MainActivity.heartbeat_enabled = true; // some socket error is there. enable heartbeat so that it can create a new connection
            /*
            Bundle bundle = new Bundle();
            bundle.putString(Constants.BROADCAST_MESSAGE, "\n##10## " + ex.toString());
            Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtras(bundle);
            LocalBroadcastManager.getInstance(MainActivity.context).sendBroadcast(localIntent);
            */
            Log.d(Constants.LOGTAG, ex.toString());

        }catch(Exception ex){
            data = null;

            msg +=" Caught an exception 2 in socket read "+ ex.toString();
            if(!MainActivity.heartbeat_enabled)
                MainActivity.heartbeat_enabled = true;
            /*
            Bundle bundle = new Bundle();
            bundle.putString(Constants.BROADCAST_MESSAGE, "\n##11## " + ex.toString());
            Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtras(bundle);
            LocalBroadcastManager.getInstance(MainActivity.context).sendBroadcast(localIntent);
            */
            Log.d(Constants.LOGTAG,ex.toString());
        }

       msg +="The received data is :"+data ;
       Log.d(Constants.LOGTAG, msg);
       if(MainActivity.debugging_on) {
           Calendar cal = Calendar.getInstance();
           SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
           Threads.writeToLogFile(MainActivity.debugfilename, "\n"+format1.format(cal.getTime()) + " " + Utils.sdf.format(cal.getTime()) + msg);
       }

        return data;
    }

   // public synchronized static int writeToStream(String  data) {
        public static int writeToStream(String  data) {
        //return dout;
        int response = -1;
            String msg =" ConnectionManager Write:";
        try {

            synchronized (sync_obj)
            {
                msg += " Length : " + data.length() ;//+ " Data :" + data;
                Log.d("Socket write", "--------length : " + data.length() + "Data :" + data);
                MainActivity.dout.writeInt(data.length());
                MainActivity.dout.writeBytes(data);
                MainActivity.dout.flush();
                response = 200;
            }
            //    response =din.readInt();
        }catch(IOException ex){
            response = -1;
            if(!MainActivity.heartbeat_enabled)
                MainActivity.heartbeat_enabled = true; //socket error, enabling heartbeat so that it may create a new connection.

            ex.printStackTrace();
            msg +=" Caught an exception while writing to Socket"+ ex.toString();
/*
            Bundle bundle = new Bundle();
            bundle.putString(Constants.BROADCAST_MESSAGE, "\n##6## " + ex.toString());
            Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtras(bundle);
            LocalBroadcastManager.getInstance(MainActivity.context).sendBroadcast(localIntent);
            */
        }catch(Exception ex){
            response = -1;
            if(!MainActivity.heartbeat_enabled)
                MainActivity.heartbeat_enabled = true; //socket error, enabling heartbeat so that it may create a new connection.

            msg +=" Caught an exception 2 while writing to Socket"+ ex.toString();
            /*
            Bundle bundle = new Bundle();
            bundle.putString(Constants.BROADCAST_MESSAGE, "\n##7## " + ex.toString());
            Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtras(bundle);
            LocalBroadcastManager.getInstance(MainActivity.context).sendBroadcast(localIntent);
            */

        }
            msg+="\n response Code : "+response;
            Log.d(Constants.LOGTAG, msg);
            if(MainActivity.debugging_on) {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                Threads.writeToLogFile(MainActivity.debugfilename, "\n"+format1.format(cal.getTime()) + " " + Utils.sdf.format(cal.getTime()) + msg);
            }
        return response;
    }


    public synchronized static void writeStatusCode(int code) {
        //return dout;
        //  int response = 408;
        try {
            MainActivity.dout.writeInt(code);
        }catch(IOException ex){
            /*
            Bundle bundle = new Bundle();
            bundle.putString(Constants.BROADCAST_MESSAGE,"\n##12## "+  ex.toString());
            Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtras(bundle);
            LocalBroadcastManager.getInstance(MainActivity.context).sendBroadcast(localIntent);
            */
        }catch(Exception ex){
            Log.d(Constants.LOGTAG,ex.toString());
            /*
            Bundle bundle = new Bundle();
            bundle.putString(Constants.BROADCAST_MESSAGE, "\n##13## "+ ex.toString());
            Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtras(bundle);
            LocalBroadcastManager.getInstance(MainActivity.context).sendBroadcast(localIntent);
            */
        }
        //  return response;
    }


}
