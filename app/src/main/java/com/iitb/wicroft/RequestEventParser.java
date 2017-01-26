package com.iitb.wicroft;

/**
 * Created by swinky on 28/6/16.
 */

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import android.util.Log;

enum RequestType{
    GET,
    POST,
    NONE
};

enum DownloadMode{
    SOCKET,
    WEBVIEW,
    NONE
};


//Event class. stores time(as Calendar object), url, request type
class RequestEvent{
    Calendar cal;
    String url;
    RequestType type;
    DownloadMode mode;
    String postDataSize;
    RequestEvent(Calendar tcal, String turl, RequestType ttype, DownloadMode tmode){
        cal = tcal;
        url = turl;
        type = ttype;
        mode = tmode;
    }

    RequestEvent(RequestEvent e)
    {
        cal = e.cal;
        url = e.url;
        type = e.type;
        mode = e.mode;
        postDataSize = e.postDataSize;
    }

    RequestEvent(Calendar tcal, String turl, RequestType ttype, DownloadMode tmode, String size){
        cal = tcal;
        url = turl;
        type = ttype;
        mode = tmode;
        postDataSize = size;
    }


}

//Load contains all information about an experiment i.e event id and list of all events
class Load{
    long loadid;
    Vector<RequestEvent> events;
    Load(long tloadid, Vector<RequestEvent> tevents){
        loadid = tloadid;
        events = tevents;
    }
}

//Parser class which parses lines from control file and creates event objects
public class RequestEventParser {

    private static final Map<String, RequestType> typeMap;
    private static final Map<String, DownloadMode> modeMap;
    static
    {
        typeMap = new HashMap<String, RequestType>();
        typeMap.put("GET", RequestType.GET);
        typeMap.put("POST", RequestType.POST);

        modeMap = new HashMap<String, DownloadMode>();
        modeMap.put("SOCKET", DownloadMode.SOCKET);
        modeMap.put("WEBVIEW", DownloadMode.WEBVIEW);
    }

    public static RequestType getRequestEnum(String key){
        RequestType type = typeMap.get(key);
        if(type == null){
            return RequestType.NONE;
        }
        return type;
    }

    public static DownloadMode getDownloadModeEnum(String key){
        DownloadMode type = modeMap.get(key);
        if(type == null){
            return DownloadMode.NONE;
        }
        return type;
    }

    //Just for testing parseLine
    public static String generateLine(int seconds){
        String line = "GET ";

        Calendar cal = Utils.getServerCalendarInstance();
        cal.add(Calendar.SECOND, seconds);
        line += cal.get(Calendar.YEAR) + " ";
        line += cal.get(Calendar.MONTH) + " ";
        line += cal.get(Calendar.DAY_OF_MONTH) + " ";
        line += cal.get(Calendar.HOUR_OF_DAY) + " ";
        line += cal.get(Calendar.MINUTE) + " ";
        line += cal.get(Calendar.SECOND) + " ";
        line += cal.get(Calendar.MILLISECOND) + " ";

        line += "http://www.cse.iitb.ac.in/~ashishsonone/serve.php?user=ashish@" + seconds;
        return line;
    }

    public static RequestEvent parseLine(String line){
        String[] fields = line.split(" ");
        Log.d("RequestEventParser ", fields[0] + fields.length);

        if(fields.length < 4) {
            Log.d("RequestEventParser:", "No of fields less than expected");
            return null; //No valid event could be found
        }
        // atleast there are 10 fields(DOWNLOADMODE CAN BE "SOCKET/WEBVIEW"
        // TYPE relative_time DOWNLOADMODE URL
        // 0     1              2           3    4
        RequestType type = getRequestEnum(fields[0]);
       // Log.d("Request type ...... ", type.toString() + " " + fields[8] + " " + fields[9]);

        //Calendar cal = Utils.getServerCalendarInstance();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis( MainActivity.serverTimeInMillis + (Integer.parseInt(fields[1]) *1000 )) ;

       // Log.d("Swinky", "getServerCalendarInstance event: " + MainActivity.sdf.format(cal.getTime()));
        DownloadMode mode = getDownloadModeEnum(fields[2]);
        String url = fields[3];

        if(fields.length == 4){
//		  String retmsg = Threads.writeToLogFile(MainActivity.logfilename,"LINE5 : "+line+"-"+fields[10]+"\n");
//		  Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtra(Constants.BROADCAST_MESSAGE, "LINE6 : "+line+"-"+fields[10]);

            return new RequestEvent(cal, url, type, mode,"0");
        } else if (fields.length == 5){

//		  int size = Integer.parseInt(fields[10].trim());

//			return new RequestEvent(cal, url, type, mode, Integer.parseInt(fields[10]));
//		  String retmsg = Threads.writeToLogFile(MainActivity.logfilename,"LINE3 : "+line+"-"+fields[10]+"\n");
//		  Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtra(Constants.BROADCAST_MESSAGE, "LINE4 : "+line+"-"+fields[10]);

            return new RequestEvent(cal, url, type, mode, fields[4]);
        }
        return null;
    }

    public static Load parseEvents(String s){
        String msg =" RequestEventParser : Entered Here. ";
       // Log.d("Return event entering", "HERE");
        Vector<RequestEvent> events = new Vector<RequestEvent>();
        Scanner scanner = new Scanner(s);
        String line = scanner.nextLine();

       // long id = Long.parseLong(line.split(" ")[0]);
        long id = MainActivity.exptno ;
//		Bundle bundle = new Bundle();
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
//		  bundle.putString(Constants.BROADCAST_MESSAGE,"LINE : '"+line+"'\n");
//		  String retmsg = Threads.writeToLogFile(MainActivity.logfilename,"LINE1 : "+line+"\n");
//		  Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtra(Constants.BROADCAST_MESSAGE, "LINE2 : "+line);
            Log.d(" HERE.. : " , line+"\n");

            RequestEvent event = parseLine(line);
            if(event != null) events.add(event);
            // process the line
        }
        scanner.close();
        msg+=" Return Event: "+ events.size() +"";
       // Log.d("Return event", events.size() + "");

        Log.d(Constants.LOGTAG, msg);
        if(MainActivity.debugging_on) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
            Threads.writeToLogFile(MainActivity.debugfilename ,"\n"+format1.format(cal.getTime()) +" "+ Utils.sdf.format(cal.getTime())+": Heartbeat : Initialized everything. Now starting Backgroundservices.");
            //Threads.writeToLogFile(MainActivity.debugfilename , Utils.sdf.format(cal.getTime())+": Heartbeat : Initializing everything ");
        }

        return new Load(id, events);
    }
}
