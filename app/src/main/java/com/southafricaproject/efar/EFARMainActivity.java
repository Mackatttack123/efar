package com.southafricaproject.efar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class EFARMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_efarmain);

        //TODO: make and emergency array that store an emergancy struct (which you need to make)
        //TODO: When you click on the emergency it will show you it in detail
        //TODO: display distance using cordinates in the arrayview for the EFARS
        final ArrayList<String> patientArray = new ArrayList<String>();

        GPSTracker gps = new GPSTracker(this);
        final double my_lat = gps.getLatitude(); // latitude
        final double my_long = gps.getLongitude(); // longitude

        //TODO: add manual update button to check for data
        // go through all the emergencies and put there data in the array
        FirebaseDatabase.getInstance().getReference().child("emergencies")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            double patient_lat = Double.parseDouble(snapshot.child("latitude").getValue().toString());
                            double patient_long = Double.parseDouble(snapshot.child("longitude").getValue().toString());
                            patientArray.add("Emergancy: " + Double.toString(distance(patient_lat, patient_long, my_lat, my_long)) + " km away");
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });


        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_listview, patientArray);

        ListView listView = (ListView) findViewById(R.id.patient_list);
        listView.setAdapter(adapter);

        //button to get back to patient screen
        Button logoutButton = (Button) findViewById(R.id.logout_button);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
            }
        });
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}
