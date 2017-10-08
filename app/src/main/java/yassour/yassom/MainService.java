package yassour.yassom;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;


import java.util.Calendar;


public class MainService extends Service  {
    public static int CHECK_MODE_MINUTES = 15;

    public static int MAX_EMAIL_MINUTES = 120;
    public static long mLastMailMillis = 0;
    public static long mRequestedChangeToAirplaneModeMillis = 0;

    public static String HOME_WIFI = "linksys8954";

    public static String TAG = "yassom";

    private String wifi;

    public MainService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Service", "========================= onStartCommand");
       // final ApplicationState appState = (ApplicationState) getApplicationContext();
       // Log.d("Service", "========================= diff " + getTimePassedInSeconds(mRequestedChangeToAirplaneModeMillis);
        if ((getTimePassedInSeconds(mRequestedChangeToAirplaneModeMillis) < 200)) {
            Log.d("Service", "========================= Turning up wifi");
            turnupWifi();
        }

        Log.d("Service", "========================= onStartCommand " + Calendar.getInstance().getTime());

        if (isHomeWifi() && !isAirplaneModeOn()) {
            Log.d("Service", "========================= notify user airplane mode");
            NotifyUserChangeAirplaneMode();

            mRequestedChangeToAirplaneModeMillis = System.currentTimeMillis();
            Log.d("Service", "========================= mRequestedChangeToAirplaneModeMillis" + mRequestedChangeToAirplaneModeMillis);
        }

         wifi = GetWifi().replace("\"", "");;

        Log.d("Service", "========================= onStartCommand");


        if (wifi.isEmpty() ||  wifi.equals("0x")) {

            // If was at home and not disconnectd, ask user to remove airplane mode.
           // if (appState.getWifi().equals(HOME_WIFI) && isAirplaneModeOn()) {
            if (wifi.equals(HOME_WIFI) && isAirplaneModeOn()) {
                NotifyUserChangeAirplaneMode();
            }
            //appState.setWifi("");


        } else {

            // Send email wifi.
            Log.d("Service", "========================= have wifi don't report location: " + wifi);

            //appState.setWifi(wifi);
        }


        notifyUI();

        // Here you can return one of some different constants.
        // This one in particular means that if for some reason
        // this service is killed, we don't want to start it
        // again automatically

        //Log.d("Service", "========================= onStartCommand");



        // I don't want this service to stay in memory, so I stop it
        // immediately after doing what I wanted it to do.
        stopSelf();

        return START_NOT_STICKY;

    }

    private void NotifyUserChangeAirplaneMode() {
        Intent intent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 millisecondsappState.setWifi(wifi)1000

        for (int i=0 ; i < 3 ; i++ ) {
            v.vibrate(500);
            try {
                Thread.sleep(100);
                // Do some stuff
            } catch (Exception e) {
                e.getLocalizedMessage();

            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        //Log.d("Service", "========================= onDestory");

        int nextCheckTimeSecond = CHECK_MODE_MINUTES * 60;

        // If not home with airplane mode or at home without airplane mode then nag.
        if (isHomeWifi() != isAirplaneModeOn()) {
            nextCheckTimeSecond = 20;
        }

        // I want to restart this service again in one hour
        AlarmManager alarm = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarm.set(
                alarm.RTC_WAKEUP,
                getMillisInFutureInSeconds(nextCheckTimeSecond),
                PendingIntent.getService(this, 0, new Intent(this, MainService.class), 0)
        );
    }

    private boolean isHomeWifi() {
        return GetWifi().equals("\"" + HOME_WIFI + "\"");
    }

    private String GetWifi() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        if (info != null && info.getSSID() != null && info.getSSID().toString() != null) {
            return info.getSSID().toString();
        }
        return "";
    }

    private boolean isAirplaneModeOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.Global.getInt(this.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
        }
        return false;
    }






    private void notifyUI() {

        Intent local = new Intent();

        local.setAction("status.refresh");

        this.sendBroadcast(local);
    }

    private String wifiToLocationString(String wifi) {
        String label = wifiLabel(wifi);
        if (label.isEmpty()) {
            return "Location: wifi = " + wifi;
        }
        return "Location: " + label;
    }

    private String wifiLabel(String wifi) {
        if (wifi.equals("linksys8954")) {
            return "Home";
        } else if (wifi.equals("GoogleGuestPSK")) {
            return "Google";
        }

        return "";
    }

    private void turnupWifi() {
        try {
            WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
            wifiManager.setWifiEnabled(true);
        } catch (Exception e) {
            Log.i(TAG, "============================= Failed to turnup wifi: " + e.toString());
        }
     }

    private double getTimePassedInSeconds(double millis) {
        return (System.currentTimeMillis() - millis) / 1000;
    }

    private long getMillisInFutureInSeconds(long seconds) {
        return System.currentTimeMillis() + seconds * 1000;
    }

    private double getTimePassedInMinutes(double millis) {
        return getTimePassedInSeconds(millis) / 60;
    }
}