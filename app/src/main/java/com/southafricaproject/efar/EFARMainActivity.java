package com.southafricaproject.efar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import android.app.AlertDialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class Emergency {
    private String key;
    private String address;
    private Double latitude;
    private Double longitude;
    private String phone_number;
    private String info;

    // constructor
    public Emergency(String key, String address, Double latitude, Double longitude, String phone_number, String info) {
        this.key = key;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone_number = phone_number;
        this.info = info;
    }

    // getter
    public String getKey() { return key; }
    public String getAddress() { return address; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getPhone() { return phone_number; }
    public String getInfo() { return info; }
}


public class EFARMainActivity extends AppCompatActivity {

    //TODO: make and emergency array that store an emergancy struct (which you need to make)
    //TODO: When you click on the emergency it will show you it in detail
    //TODO: display distance using cordinates in the arrayview for the EFARS
    //TODO: sort emergencics by distance away
    final ArrayList<String> disctanceArray = new ArrayList<String>();
    final ArrayList<Emergency> emergenecyArray = new ArrayList<Emergency>();
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client2;

    /* for constant listview updating every few seconds */
    private Handler handler = new Handler();
    public ArrayAdapter adapter;
    public ListView listView;
    private double my_lat;
    private double my_long;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_efarmain);

        adapter = new ArrayAdapter<String>(this,
                R.layout.activity_listview, disctanceArray);
        listView = (ListView) findViewById(R.id.patient_list);
        listView.setAdapter(adapter);
        listView.setClickable(true);

        // TODO: get listView to auto update when emergencies are addded or deleted to the database
        GPSTracker gps = new GPSTracker(this);
        my_lat = gps.getLatitude(); // latitude
        my_long = gps.getLongitude(); // longitude
        FirebaseDatabase.getInstance().getReference().child("emergencies").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try{
                    String e_key = dataSnapshot.getKey();
                    String e_phone_number = dataSnapshot.child("phone_number").getValue().toString();
                    String e_info = dataSnapshot.child("other_info").getValue().toString();
                    Double e_lat = Double.parseDouble(dataSnapshot.child("latitude").getValue().toString());
                    Double e_long = Double.parseDouble(dataSnapshot.child("longitude").getValue().toString());
                    String e_address = getCompleteAddressString(e_lat, e_long);
                    emergenecyArray.add(new Emergency(e_key, e_address, e_lat, e_long, e_phone_number, e_info));
                    disctanceArray.add("Emergancy: " + String.format("%.2f", distance(e_lat, e_long, my_lat, my_long)) + " km away");
                    adapter.notifyDataSetChanged();
                }catch (NullPointerException e){
                    Log.wtf("added", "not yet");
                }

            }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    String key = dataSnapshot.getKey();
                    for (int j = 0; j < emergenecyArray.size(); j++){
                        Emergency e = emergenecyArray.get(j);
                        if(e.getKey().equals(key)){
                            //found, delete.
                            emergenecyArray.remove(j);
                            disctanceArray.remove(j);
                            adapter.notifyDataSetChanged();
                            break;
                        }
                    }

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        /*checkForEmergencies();
        handler.postDelayed(update_runnable, 5000);*/

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                Object o = listView.getItemAtPosition(position);
                final SpannableString message = new SpannableString(Html.fromHtml("<b>Location:</b> (" + String.format("%.2f", emergenecyArray.get(position).getLatitude())
                        + ", " + String.format("%.2f", emergenecyArray.get(position).getLongitude()) + ")<p><b>Address:</b> " + emergenecyArray.get(position).getAddress()
                        + "</p><p><b>Senders #:</b> " + emergenecyArray.get(position).getPhone() + "</p><p><b>Other Info:</b> " + emergenecyArray.get(position).getInfo(), 0));
                Linkify.addLinks(message, Linkify.ALL);
                new AlertDialog.Builder(EFARMainActivity.this)
                        .setIcon(0)
                        .setTitle(Html.fromHtml("<h3>Emergency Information</h3>", 0))
                        .setMessage(message)
                        .setPositiveButton("Done", null)
                        .setCancelable(false)
                        .show();
            }
        });

        //button to get back to patient screen
        Button logoutButton = (Button) findViewById(R.id.logout_button);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get rid of stored password and username
                SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("id", "");
                editor.putString("name", "");
                editor.commit();
                finish();
                launchPatientMainScreen();
            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client2 = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /*private void checkForEmergencies() {

        disctanceArray.clear();
        emergenecyArray.clear();
        GPSTracker gps = new GPSTracker(this);
        final double my_lat = gps.getLatitude(); // latitude
        final double my_long = gps.getLongitude(); // longitude

        // go through all the emergencies and put there data in the array
        FirebaseDatabase.getInstance().getReference().child("emergencies")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String e_key = snapshot.getKey();
                            Double e_lat = Double.parseDouble(snapshot.child("latitude").getValue().toString());
                            Double e_long = Double.parseDouble(snapshot.child("longitude").getValue().toString());
                            String e_phone_number = snapshot.child("phone_number").getValue().toString();
                            String e_info = snapshot.child("other_info").getValue().toString();

                            //TODO: THIS ADDRESS THING ISN't working any more!??!?!?! WTF...
                            // to get address
                            Geocoder geocoder = new Geocoder(EFARMainActivity.this, Locale.getDefault());
                            List<Address> addressList = null;
                            try {
                                addressList = geocoder.getFromLocation(e_lat, e_long, 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            String address, city, state, country;

                            if(addressList.size() > 0){
                                Address emergency_address = addressList.get(0);

                                address = emergency_address.getAddressLine(0);
                                city = emergency_address.getLocality();
                                state = emergency_address.getAdminArea();
                                country = emergency_address.getCountryName();
                            }else{
                                address = "";
                                city = "";
                                state = "";
                                country = "";
                            }


                            String postalCode = emergency_address.getPostalCode();
                            String knownName = emergency_address.getFeatureName(); // Only if available else return NULL
                            if(postalCode == null){
                                postalCode = "";
                            }
                            if(knownName == null){
                                knownName = "";
                            }

                            //String e_address = address + " " + city + " " + state + " " + country; //+ " " + postalCode + " " + knownName;
                            String e_address = getCompleteAddressString(e_lat, e_long);
                            emergenecyArray.add(new Emergency(e_key, e_address, e_lat, e_long, e_phone_number, e_info));
                            disctanceArray.add("Emergancy: " + String.format("%.2f", distance(e_lat, e_long, my_lat, my_long)) + " km away");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
        Log.wtf("emergenecyArray", Integer.toString(emergenecyArray.size()));
        Log.wtf("disctanceArray", Integer.toString(disctanceArray.size()));
        adapter.clear();
        adapter.addAll(disctanceArray);
        Log.wtf("disctanceArray", Integer.toString(adapter.getCount()));
        adapter.notifyDataSetChanged();
    }*/

    /* updates the listview*/
    /*private Runnable update_runnable = new Runnable() {
        @Override
        public void run() {
            checkForEmergencies();

            handler.postDelayed(this, 5000);
        }
    };*/

    // Goes to patient info tab to send more to EFARs
    private void launchPatientMainScreen() {

        Intent toPatientMainScreen = new Intent(this, PatientMainActivity.class);
        startActivity(toPatientMainScreen);
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

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.wtf("My Current loction address", "" + strReturnedAddress.toString());
            } else {
                Log.wtf("My Current loction address", "No Address returned!");
                strAdd = "N/A";
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.wtf("My Current loction address", "Canont get Address!");
            strAdd = "N/A";
        }
        return strAdd;
    }
}
