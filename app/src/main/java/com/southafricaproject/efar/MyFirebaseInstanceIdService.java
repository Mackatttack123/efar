package com.southafricaproject.efar;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {
    private static final String TAG = "FirebaseIDService";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        Log.wtf("sending: ", refreshedToken);
        sendRegistrationToServer(refreshedToken);
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
        GPSTracker gps = new GPSTracker(this);
        double my_lat = gps.getLatitude(); // latitude
        double my_long = gps.getLongitude(); // longitude

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference tokens_ref = database.getReference("tokens");
        tokens_ref.child(token).child(token).setValue(token);
        tokens_ref.child(token).child("latitude").setValue(my_lat);
        tokens_ref.child(token).child("longitude").setValue(my_long);
        Log.wtf("sent: ", token);
    }
}