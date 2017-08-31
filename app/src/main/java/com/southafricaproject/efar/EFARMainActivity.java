package com.southafricaproject.efar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.ViewGroup;
import android.widget.TextView;

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
import android.content.DialogInterface;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import java.text.SimpleDateFormat;
import java.util.Date;

class Emergency {
    private String key;
    private String address;
    private Double latitude;
    private Double longitude;
    private String phone_number;
    private String info;
    private String creationDate;
    private String state;

    // constructor
    public Emergency(String key, String address, Double latitude, Double longitude,
                     String phone_number, String info, String creationDate, String state) {
        this.key = key;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone_number = phone_number;
        this.info = info;
        this.creationDate = creationDate;
        this.state = state;
    }

    // getter
    public String getKey() { return key; }
    public String getAddress() { return address; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getPhone() { return phone_number; }
    public String getInfo() { return info; }
    public String getCreationDate() { return creationDate; }
    public String getState() { return state; }
}


public class EFARMainActivity extends AppCompatActivity {

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

    String alertButtonOption = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_efarmain);

        // start tracking efar
        startService(new Intent(this, MyService.class));

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users");
        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        userRef.child(sharedPreferences.getString("id", "") + "/token").setValue(refreshedToken);


        adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, disctanceArray){
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                View itemView = super.getView(position, convertView, parent);
                if (emergenecyArray.get(position).getState().equals("0")){
                    itemView.setBackgroundColor(Color.argb(100, 255, 0, 0));
                }else{
                    itemView.setBackgroundColor(Color.argb(100, 0, 0, 255));
                }
                return itemView;
            }
        };

        listView = (ListView) findViewById(R.id.patient_list);
        listView.setAdapter(adapter);
        listView.setClickable(true);

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
                    String e_creationDate = dataSnapshot.child("creation_date").getValue().toString();
                    String e_state = dataSnapshot.child("state").getValue().toString();
                    emergenecyArray.add(new Emergency(e_key, e_address, e_lat, e_long, e_phone_number, e_info, e_creationDate, e_state));
                    disctanceArray.add("Emergancy: " + String.format("%.2f", distance(e_lat, e_long, my_lat, my_long)) + " km away");
                    adapter.notifyDataSetChanged();
                }catch (NullPointerException e){
                    Log.wtf("added", "not yet");
                }

            }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
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
                    try{
                        String e_key = dataSnapshot.getKey();
                        String e_phone_number = dataSnapshot.child("phone_number").getValue().toString();
                        String e_info = dataSnapshot.child("other_info").getValue().toString();
                        Double e_lat = Double.parseDouble(dataSnapshot.child("latitude").getValue().toString());
                        Double e_long = Double.parseDouble(dataSnapshot.child("longitude").getValue().toString());
                        String e_address = getCompleteAddressString(e_lat, e_long);
                        String e_creationDate = dataSnapshot.child("creation_date").getValue().toString();
                        String e_state = dataSnapshot.child("state").getValue().toString();
                        emergenecyArray.add(new Emergency(e_key, e_address, e_lat, e_long, e_phone_number, e_info, e_creationDate, e_state));
                        disctanceArray.add("Emergancy: " + String.format("%.2f", distance(e_lat, e_long, my_lat, my_long)) + " km away");
                        adapter.notifyDataSetChanged();
                    }catch (NullPointerException e){
                        Log.wtf("added", "not yet");
                    }
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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                final int pos = position;
                Object o = listView.getItemAtPosition(position);
                String phoneLink = "tel:" + emergenecyArray.get(position).getPhone().replaceAll("[^\\d.]", "");
                String mapLink = "http://maps.google.com/?q=" + emergenecyArray.get(position).getLatitude() + ","  + emergenecyArray.get(position).getLongitude();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZZZZZ");
                Date timeCreated = null;
                try {
                    timeCreated = simpleDateFormat.parse(emergenecyArray.get(position).getCreationDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                SimpleDateFormat displayTimeFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
                String dipslayTime = displayTimeFormat.format(timeCreated);

                final SpannableString message = new SpannableString(Html.fromHtml("<p><b>Created: </b>" + dipslayTime + "</p><p><b>Location:</b> <a href=" + mapLink + ">(" + String.format("%.2f", emergenecyArray.get(position).getLatitude())
                        + ", " + String.format("%.2f", emergenecyArray.get(position).getLongitude()) + ")</a></p><p><b>Address:</b> <a href=" + mapLink + ">" + emergenecyArray.get(position).getAddress()
                        + "</a></p><p><b>Senders #:</b> <a href=" + phoneLink + ">" + emergenecyArray.get(position).getPhone() + "</a></p><p><b>Other Info:</b> " + emergenecyArray.get(position).getInfo(), 0));

                if(emergenecyArray.get(position).getState().equals("0")){
                    alertButtonOption = "Respond";
                }else{
                    alertButtonOption = "End Emergency";
                }

                final AlertDialog d = new AlertDialog.Builder(EFARMainActivity.this)
                        .setIcon(0)
                        .setTitle(Html.fromHtml("<h3><u>Emergency Information</u></h3>", 0))
                        .setMessage(message)
                        .setPositiveButton("Exit", null)
                        .setNeutralButton(alertButtonOption, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(alertButtonOption.equals("Respond")){
                                    new AlertDialog.Builder(EFARMainActivity.this)
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setTitle("Respond to Emergency:")
                                            .setMessage("Are you able to respond to this emergency?")
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                            {
                                                final String keyToUpdate = emergenecyArray.get(pos).getKey();
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                    DatabaseReference emergency_ref = database.getReference("emergencies/" + keyToUpdate + "/state");
                                                    emergency_ref.setValue("1");
                                                }

                                            })
                                            .setNegativeButton("No", null)
                                            .show();
                                }else if(alertButtonOption.equals("End Emergency")) {
                                    new AlertDialog.Builder(EFARMainActivity.this)
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setTitle("End Emergency")
                                            .setMessage("Are you sure you want to end this emergency?\n\n" +
                                                    "Ending it will remove it from the emergency stream for good.")
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                // to delete the emergency
                                                final String keyToMove = emergenecyArray.get(pos).getKey();

                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                    DatabaseReference emergency_ref = database.getReference("emergencies/" + keyToMove);
                                                    emergency_ref.child("/state").setValue("2");
                                                    Date currentTime = Calendar.getInstance().getTime();
                                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZZZZZ");
                                                    String timestamp = simpleDateFormat.format(currentTime);
                                                    emergency_ref.child("/ended_date").setValue(timestamp);
                                                    Date e_creation_date = null;
                                                    try {
                                                        e_creation_date = simpleDateFormat.parse(emergenecyArray.get(pos).getCreationDate());
                                                    } catch (ParseException e) {
                                                        e.printStackTrace();
                                                    }
                                                    emergency_ref.child("/elapsed_time_in_milliseconds").setValue(e_creation_date.getTime() - currentTime.getTime());
                                                    moveFirebaseRecord(emergency_ref, database.getReference("completed/" + keyToMove));
                                                    emergency_ref.removeValue();
                                                }

                                            })
                                            .setNegativeButton("No", null)
                                            .show();
                                }

                            }
                        })
                        .setCancelable(false)
                        .create();
                d.show();
                ((TextView)d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
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

                // say that user has logged off
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference userRef = database.getReference("users");
                userRef.child(sharedPreferences.getString("id", "") + "/logged_in").setValue(false);

                editor.putString("id", "");
                editor.putString("name", "");
                editor.putBoolean("logged_in", false);
                editor.commit();


                finish();
                launchPatientMainScreen();
            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client2 = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

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

    public void moveFirebaseRecord(DatabaseReference fromPath, final DatabaseReference toPath)
    {
        fromPath.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                toPath.setValue(dataSnapshot.getValue(), new DatabaseReference.CompletionListener()
                {
                    @Override
                    public void onComplete(DatabaseError firebaseError, DatabaseReference firebase)
                    {
                        if (firebaseError != null)
                        {
                            System.out.println("Copy failed");
                        }
                        else
                        {
                            System.out.println("Success");
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError firebaseError)
            {
                System.out.println("Copy failed");
            }
        });
    }

}
