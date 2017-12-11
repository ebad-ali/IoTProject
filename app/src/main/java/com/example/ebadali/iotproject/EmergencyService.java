package com.example.ebadali.iotproject;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class EmergencyService extends Service {

    String url = "http://virtualeye.io/iot/?action=show";
    Context context;

    String text;

    // Right now it check after every 10 seconds
    public static final int notify = 1000 * 10;  //interval between two services(Here Service run every 10 seconds
    // change 10 to 5 for checking service every 5 seconds or 1 to
    // check every 1 second )

    private Handler mHandler = new Handler();   //run on another Thread to avoid crash
    private Timer mTimer = null;    //timer handling


    @Override
    public void onCreate() {
        if (mTimer != null) // Cancel if already existed
            mTimer.cancel();
        else
            mTimer = new Timer();   //recreate new
        mTimer.scheduleAtFixedRate(new TimeDisplay(), 0, notify);   //Schedule task
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        this.context = this;

        Log.e("onStartCommand", "Service Started");
        return START_STICKY;
    }


    void jsonRequest() {

        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String string) {
                Log.e("service jsonrequest", string);
                JsonParser(string);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Some error occurred!!", Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(context);
        rQueue.add(request);

    }

    void JsonParser(String jsonString) {
        Gson gson = new Gson();
        Emergency emergency = gson.fromJson(jsonString, Emergency.class);

        Log.e("service JSON", emergency.Status);

        if (emergency.Status != null) {
            text = emergency.Status;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();    //For Cancel Timer
        Log.e("Emergency Service", "onCreate() , service stopped...");
    }

    private class TimeDisplay extends TimerTask {
        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    if (isOnline()) {
                        jsonRequest();
                    } else {
                        Log.e("EmergencyService", "Cannot connect to internet so json not called");
                    }

                    handlerset();
                }
            });
        }
    }


    void handlerset() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkerFunction();
            }
        }, 9000);
    }

    boolean applicationChecker() {
        ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> services = activityManager
                .getRunningTasks(Integer.MAX_VALUE);
        boolean isActivityFound = false;

        if (services.get(0).topActivity.getPackageName().toString()
                .equalsIgnoreCase(this.getPackageName().toString())) {
            isActivityFound = true;
        }

        if (isActivityFound) {
            return true;
        } else {
            // write your code to build a notification.
            // return the notification you built here
            return false;
        }
    }

    void checkerFunction() {
        if (text != null) {

            if (text.equals("Normal")) {

              /*  if(applicationChecker())
                {
                    //notificationSender("Fire");


                    BlinkLogoActivity.blinkLogoServiceReciever("Cigarette");
                }
*/

                Log.e("Service recalling", "Nothing Found");
            }
            else if (text.equalsIgnoreCase("Only Smoke") || text.equalsIgnoreCase("Fire Alarm") || text.equalsIgnoreCase("Kitchen Smoke")) {
                Log.e("Service recalling", "Emergency Found");

                // if application is closed then push the notification to the user
                if (!applicationChecker()) {

                    notificationSender(text);
                    // notificationSender("Fire Alarm");
                }

                // if application is opened notify blinkActivity about something is received
                else {
                    BlinkLogoActivity.blinkLogoServiceReciever(text);
                }
            }
        }
    }

    void notificationSender(String emergenceValue) {

        int smallIconFile = 0, largeIconFile = 0;
        String title = "", content = "";

        // Change value here
        if (emergenceValue.equals("Only Smoke") || emergenceValue.equals("Fire Alarm") || emergenceValue.equals("Kitchen Smoke")) {


            // For Ciggarette Smoke
            if (emergenceValue.equals("Only Smoke")) {
                smallIconFile = R.drawable.ciga;
                largeIconFile = R.drawable.ciga;
                title = "Smoke Emergency!";
                content = "Tap to Send Response";
            }


            // For Fire Emergency
            else if (emergenceValue.equals("Fire Alarm")) {
                smallIconFile = R.drawable.firea;
                largeIconFile = R.drawable.firea;
                title = "Fire Emergency!";
                content = "Tap to send response";
            }


             // For Kitchen Smoke
            else if (emergenceValue.equals("Kitchen Smoke")) {
                smallIconFile = R.drawable.steamsa;
                largeIconFile = R.drawable.steamsa;
                title = "Steam Emergency!";
                content = "Tap to send response";
            }


            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

            builder.setSmallIcon(smallIconFile);

            Intent resultIntent = new Intent(this, BlinkLogoActivity.class);
            resultIntent.putExtra("emergencyValue", emergenceValue);
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

            stackBuilder.addParentStack(BlinkLogoActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentIntent(resultPendingIntent);


            builder.setAutoCancel(true);

            builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), largeIconFile));

            builder.setContentTitle(title);
            builder.setContentText(content);


            /*
            *  Create notification with sound IF received value is Fire Alarm
            *  and change the mode of the mobile and and set the sound to Fire sound
            * */
            if (emergenceValue.equals("Fire Alarm")) {

                AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

                // For Normal mode
                int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
                int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

                float percent = 2.0f;
                int seventyVolume = (int) (maxVolume * percent);
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, seventyVolume, 0);
                audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

                Uri sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.smokealarm);
                builder.setSound(sound);

            }


            builder.setVibrate(new long[]{1000, 3000, 1000, 3000, 1000});

            builder.setLights(Color.RED, 1000, 500);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            // Will display the notification in the notification bar
            if (notificationManager != null) {
                notificationManager.notify(1, builder.build());
            }
        }

    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in air plan mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }


}
