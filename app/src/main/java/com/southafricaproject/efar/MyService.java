package com.southafricaproject.efar;

import android.app.Service;
import android.content.*;
import android.graphics.Color;
import android.os.*;
import android.widget.Toast;
import android.app.IntentService;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.widget.EditText;
import android.preference.PreferenceManager;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class MyService extends Service {

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
                GPSTracker gps = new GPSTracker(MyService.this);
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

                    if(!id.equals("")){
                        DatabaseReference users_ref = database.getReference("users/" + id);
                        users_ref.child("latitude").setValue(my_lat);
                        users_ref.child("longitude").setValue(my_long);
                    }

                    Log.wtf("location update:", "(" + my_lat + ", " + my_long + ") ---> token: " + token);
                }else{
                    Log.wtf("location update:", "no location update!");
                }

                //Toast.makeText(context, "Service is still running", Toast.LENGTH_LONG).show();
                handler.postDelayed(runnable, 10000);
            }
        };

        handler.postDelayed(runnable, 15000);
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
}
