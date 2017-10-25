package com.textnext.koshal.asl;

import android.*;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.textnext.koshal.asl.DataBase.ASLContract;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by me on 10/19/2017.
 */

public class ASLUtils {


    public static void setShared(String key, String value) {

        ASL.mSharedEditor.putString(key, value);
        ASL.mSharedEditor.apply();
    }


    public static String time(long time) {
        time/=1000;
        final int FOR_HOURS = 3600000;
        final int FOR_MIN = 60000;
        //SimpleDateFormat formatDate = new SimpleDateFormat("HH:mm a");
        Date df = new java.util.Date(time * 1000);
        SimpleDateFormat formatDate = new SimpleDateFormat("hh:mm a", Locale.ROOT);
        return formatDate.format(df);

    }

    public static String getShared(String key) {
        if (key != null)
            return ASL.mSharedPreference.getString(key, "");
        return "";
    }
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean checkPermission(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
    public static void deleteAllMessage(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(ASLContract.MessagesEntry.CONTENT_URI, "1", new String[0]);
        contentResolver.notifyChange(ASLContract.MessagesEntry.CONTENT_URI, null);
    }

    public static String getOnlineStaus(String thOnlineStatus) {

        if(thOnlineStatus==null)
            return "";
        String stat="";
        if(thOnlineStatus.contains("O")|| thOnlineStatus.contains("T"))
        {
            return thOnlineStatus;
        }

        return getLastSeen(Long.parseLong(thOnlineStatus));


       /* if(dist<60)
        {
            return "Online";
        }
        else if(dist>=60 && dist<3600)
        {
            stat =(dist/60)+" MIN";
        }

        else if(dist>=3600 && dist<3600*24)
        {
            stat =(dist/3600)+" HR";
        }
        else if(dist>=3600*24 && dist<3600*24*30) {
            stat = (dist / 3600) + " DAY";
        }
            return stat;*/
    }


    private static String getLastSeen(long lastSeen){


        long now= System.currentTimeMillis();
        long dist=(now-lastSeen)/1000;

        long timePassedToday=now % (24 * 60 * 60 * 1000);

        if(dist<=timePassedToday)
        {
            return "today "+time(lastSeen);
        }
        else{
            try{
                DateFormat sdf = new SimpleDateFormat("dd MMM");
                Date netDate = (new Date(lastSeen));
                return "Last Seen "+sdf.format(netDate);
            }
            catch(Exception ex){
                return "";
            }
        }


    }
}
