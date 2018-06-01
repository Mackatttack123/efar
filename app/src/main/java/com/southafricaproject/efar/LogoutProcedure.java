package com.southafricaproject.efar;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Created by mackfitzpatrick on 6/1/18.
 */

public class LogoutProcedure {

    public static  void logout(final Context context, final Activity activity){
        new android.app.AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Logging Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //to get rid of stored name
                        SharedPreferences sharedPreferences = context.getSharedPreferences("MyData", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        //say that user has logged off on database
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference userRef = database.getReference("users");
                        userRef.child(sharedPreferences.getString("id", "") + "/logged_in").setValue(false);
                        userRef.child(sharedPreferences.getString("id", "") + "/token").setValue("null");
                        editor.putString("id", "");
                        editor.putString("name", "");
                        editor.putBoolean("logged_in", false);
                        context.stopService(new Intent(context, GPSTrackingService.class));
                        editor.apply();

                        //clear the phones token for the database
                        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                        DatabaseReference token_ref = database.getReference("tokens/" + refreshedToken);
                        token_ref.removeValue();

                        FirebaseAuth mAuth;
                        mAuth = FirebaseAuth.getInstance();
                        if(mAuth.getCurrentUser() != null){
                            mAuth.getCurrentUser().delete();
                        }

                        //launch patient main screen
                        editor.putBoolean("screen_bypass", false);
                        editor.apply();
                        Intent toPatientMainScreen = new Intent(context, ActivityPatientMain.class);
                        context.startActivity(toPatientMainScreen);
                        activity.finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

}
