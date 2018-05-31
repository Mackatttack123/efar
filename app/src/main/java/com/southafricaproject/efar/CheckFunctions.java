package com.southafricaproject.efar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Created by mackfitzpatrick on 5/31/18.
 */

public class CheckFunctions {

    //check network connection
    //check if a forced app update is needed
    //check if an logged in on another phone
    public static void runAllChecks(final Context context, final Activity activity){
        checkConnection(context);
        checkForUpdates(context, activity);
        checkOtherDeviceLogin(context, activity);
        //clear all notifications when app is opened
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public static void checkConnection(final Context context){
        //check connection
        try {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            NetworkInfo mWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if ((networkInfo != null && networkInfo.isConnected()) || mWifi.isConnected()) {

            }else{
                new AlertDialog.Builder(context)
                        .setTitle("Connection Error:")
                        .setMessage("Your device is currently unable connect to our services. " +
                                "Please check your connection or try again later.")
                        .setCancelable(false)
                        .setPositiveButton("Try to connect again", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                checkConnection(context);
                            }
                        })
                        .show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            new AlertDialog.Builder(context)
                    .setTitle("Connection Error:")
                    .setMessage("Your device is currently unable connect to our services. " +
                            "Please check your connection or try again later.")
                    .setCancelable(false)
                    .setPositiveButton("Try to connect again", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkConnection(context);
                        }
                    })
                    .show();
        }
    }


    public static void checkForUpdates(final Context context, final Activity activity) {
        //check if an update is needed
        FirebaseDatabase.getInstance().getReference().child("version").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.child("version_number").exists()) {
                    String current_version = snapshot.child("version_number").getValue().toString();
                    if (!current_version.equals(BuildConfig.VERSION_NAME)) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(context)
                                .setTitle("Update Needed:")
                                .setMessage("Please update to the the latest version of EFAR.").setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        final String appPackageName = context.getPackageName(); // getPackageName() from Context or Activity object
                                        try {
                                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                        } catch (android.content.ActivityNotFoundException anfe) {
                                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                        }
                                        activity.finish();
                                        activity.startActivity(activity.getIntent());
                                    }
                                }).setNegativeButton("Exit App", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        activity.finishAndRemoveTask();
                                    }
                                }).setCancelable(false);
                        if (!((Activity) context).isFinishing()) {
                            alert.show();
                        }
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void checkOtherDeviceLogin(final Context context, final Activity activity) {
        final FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();
        final SharedPreferences sharedPreferences = context.getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String id = sharedPreferences.getString("id", "");
        boolean efar_logged_in = sharedPreferences.getBoolean("logged_in", false);
        final String token = FirebaseInstanceId.getInstance().getToken();
        //check if an logged in on another phone
        if(efar_logged_in){
            FirebaseDatabase.getInstance().getReference().child("users/" + id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if(snapshot.child("token").exists()){
                        String current_token = snapshot.child("token").getValue().toString();
                        if(!token.equals(current_token)){
                            android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(context)
                                    .setTitle("Oops!")
                                    .setMessage("Looks liked you're logged in on another device. You will now be logged out but you can log back onto this device if you'd like.").setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //to get rid of stored password and username
                                            SharedPreferences sharedPreferences = context.getSharedPreferences("MyData", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();

                                            // say that user has logged off
                                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                                            DatabaseReference userRef = database.getReference("users");
                                            editor.putString("id", "");
                                            editor.putString("name", "");
                                            editor.putBoolean("logged_in", false);
                                            context.stopService(new Intent(context, GPSTrackingService.class));
                                            editor.apply();

                                            //clear the phones token for the database
                                            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                                            DatabaseReference token_ref = database.getReference("tokens/" + refreshedToken);
                                            token_ref.removeValue();

                                            if(mAuth.getCurrentUser() != null){
                                                mAuth.getCurrentUser().delete();
                                            }
                                            activity.finish();
                                            activity.startActivity(activity.getIntent());
                                        }
                                    }).setCancelable(false);
                            if(!((Activity) context).isFinishing())
                            {
                                alert.show();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }



}
