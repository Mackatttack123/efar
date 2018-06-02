package com.southafricaproject.efar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.os.*;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.content.Intent;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class GPSTrackingService extends Service {

    public Context context = this;
    public Handler handler = null;
    public static Runnable runnable = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        //Toast.makeText(this, "Service created!", Toast.LENGTH_LONG).show();

        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                Log.wtf("location updater:", "so far so good...");
                GPSTracker gps = new GPSTracker(GPSTrackingService.this);
                double my_lat = gps.getLatitude(); // latitude
                double my_long = gps.getLongitude(); // longitude

                String token = FirebaseInstanceId.getInstance().getToken();

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference tokens_ref = database.getReference("tokens");
                SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                String id = sharedPreferences.getString("id", "");
                Boolean logged_in = sharedPreferences.getBoolean("logged_in", false);

                if(logged_in){
                    tokens_ref.child(token).child(token).setValue(token);
                    tokens_ref.child(token).child("latitude").setValue(my_lat);
                    tokens_ref.child(token).child("longitude").setValue(my_long);
                    Date currentTime = Calendar.getInstance().getTime();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    String timestamp = simpleDateFormat.format(currentTime);
                    tokens_ref.child(token).child("last_location_update").setValue(timestamp);

                    if(!id.equals("")){
                        DatabaseReference users_ref = database.getReference("users/" + id);
                        users_ref.child("latitude").setValue(my_lat);
                        users_ref.child("longitude").setValue(my_long);
                        users_ref.child("last_location_update").setValue(timestamp);
                    }

                    Log.wtf("location update:", "(" + my_lat + ", " + my_long + ") ---> token: " + token);
                }else{
                    Log.wtf("location update:", "no location update!");
                }

                //Toast.makeText(context, "Service is still running", Toast.LENGTH_LONG).show();
                handler.postDelayed(runnable, 15000);
            }
        };

        handler.postDelayed(runnable, 0);
    }

    @Override
    public void onDestroy() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        boolean logged_in = sharedPreferences.getBoolean("logged_in", false);
        if(logged_in = false){
            handler.removeCallbacks(runnable);
        }
        //Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart(Intent intent, int startid) {
        //Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags,
                              final int startId) {
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent){
        Intent restartServiceTask = new Intent(getApplicationContext(),this.getClass());
        restartServiceTask.setPackage(getPackageName());
        PendingIntent restartPendingIntent =PendingIntent.getService(getApplicationContext(), 1,restartServiceTask, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        myAlarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartPendingIntent);
        super.onTaskRemoved(rootIntent);
    }
}


