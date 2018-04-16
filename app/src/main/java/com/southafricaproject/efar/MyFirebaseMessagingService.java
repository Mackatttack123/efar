package com.southafricaproject.efar;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.app.NotificationManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Locale;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM Service";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {



        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        boolean efar_logged_in = sharedPreferences.getBoolean("logged_in", false);

        // only send if an efar is logged in
        if(efar_logged_in){
            // If the application is in the foreground handle both data and notification messages here.
            // Also if you intend on generating your own notifications as a result of a received FCM
            // message, here is where that should be initiated.
            Log.d(TAG, "From: " + remoteMessage.getFrom());
            Log.d(TAG, "Notification Message Body: " + remoteMessage.getData().get("body"));
            Intent intent = new Intent(this, PatientMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
            notificationBuilder.setContentTitle(remoteMessage.getData().get("title"));

            //make the phone vibrate when notification is received
            if(remoteMessage.getData().get("title").equals("NEW EMERGANCY!")){
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                long[] pattern = {0, 1000, 300, 1000, 300, 1000, 300, 1000, 300};
                v.vibrate(pattern, -1);

                notificationBuilder.setVibrate(new long[] {0, 1000, 300, 1000, 300 });
                notificationBuilder.setLights(0xff00ff00, 3000, 3000);
                notificationBuilder.setSound(Uri.parse(""));
            }else if(remoteMessage.getData().get("title").equals("Emergency Canceled:")){
                vibrate(100);
            }else if(remoteMessage.getData().get("title").equals("Emergency Over:")){
                vibrate(100);
            }
            notificationBuilder.setContentText(remoteMessage.getData().get("body"));
            notificationBuilder.setAutoCancel(true);
            notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
            notificationBuilder.setContentIntent(pendingIntent);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Notification note = notificationBuilder.build();
            note.flags = Notification.FLAG_INSISTENT;
            // clear the notification after its selected
            note.flags |= Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(0, note);
        }
    }

    public void vibrate(int duration)
    {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(duration);
    }
}

