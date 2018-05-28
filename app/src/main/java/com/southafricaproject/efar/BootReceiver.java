package com.southafricaproject.efar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Created by mackfitzpatrick on 5/21/18.
 */

public class BootReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        //start tracking activity back up again
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyData", Context.MODE_PRIVATE);
        Boolean logged_in = sharedPreferences.getBoolean("logged_in", false);

        if(logged_in) {
            // start tracking efar
            context.startService(new Intent(context, MyService.class));
        }
    }

}
