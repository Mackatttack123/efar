package com.southafricaproject.efar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.google.android.gms.vision.text.Text;
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


public class EFARMainActivity extends AppCompatActivity {

    final ArrayList<String> distanceArray = new ArrayList<String>();
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

    String alertButton_respond_end_option = "";
    String alertButton_message_option = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_efarmain);

        //check database connection
        /*DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (!connected) {
                    new AlertDialog.Builder(EFARMainActivity.this)
                            .setTitle("Connection Error:")
                            .setMessage("Your device is currently unable connect to our services. " +
                                    "Please check your connection or try again later.")
                            .show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Listener was cancelled");
            }
        });*/

        // start tracking efar
        startService(new Intent(this, MyService.class));

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users");
        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        final String id = sharedPreferences.getString("id", "");
        userRef.child(id + "/token").setValue(refreshedToken);


        adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, distanceArray){
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View cell = inflater.inflate(R.layout.emergency_cell, parent, false);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date timeCreated = null;
                try {
                    timeCreated = simpleDateFormat.parse(emergenecyArray.get(position).getCreationDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                SimpleDateFormat displayTimeFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
                String dipslayTime = displayTimeFormat.format(timeCreated);
                TextView timeText = (TextView) cell.findViewById(R.id.timeTextView);
                timeText.setText(dipslayTime);
                TextView distanceText =  (TextView) cell.findViewById(R.id.distanceTextView);
                distanceText.setText(distanceArray.get(position).toString());

                TextView activeStateText =  (TextView) cell.findViewById(R.id.stateTextView);
                if (emergenecyArray.get(position).getState().equals("0")){
                    //cell.setBackgroundColor(Color.argb(150, 255, 0, 0));
                    activeStateText.setText("Awaiting Response!");
                    activeStateText.setTextColor(Color.argb(255, 200, 0, 0));
                }else if(emergenecyArray.get(position).getRespondingEfar().equals(id)){
                    //cell.setBackgroundColor(Color.argb(150, 0, 255, 0));
                    activeStateText.setText("Responded To (Me)");
                    activeStateText.setTextColor(Color.argb(255, 0, 153, 0));
                }else{
                    //cell.setBackgroundColor(Color.argb(150, 255, 255, 0));
                    activeStateText.setText("Responded To");
                    activeStateText.setTextColor(Color.argb(255, 0, 153, 0));
                }

                if(position % 2 == 0){
                    cell.setBackgroundColor(Color.argb(150, 224, 224, 224));
                }else{
                    cell.setBackgroundColor(Color.argb(150, 255, 255, 255));
                }
                return cell;
            }
        };

        listView = (ListView) findViewById(R.id.patient_list);
        listView.setAdapter(adapter);
        listView.setClickable(true);

        listView.setBackgroundColor(Color.TRANSPARENT);

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
                    String e_respondingEfar;
                    try {
                        e_respondingEfar = dataSnapshot.child("responding_efar").getValue().toString();
                    }catch (Exception e){
                        e_respondingEfar = "N/A";
                    }
                    String e_state = dataSnapshot.child("state").getValue().toString();
                    emergenecyArray.add(new Emergency(e_key, e_address, e_lat, e_long, e_phone_number, e_info, e_creationDate, e_respondingEfar, e_state));
                    distanceArray.add(String.format("%.2f", distance(e_lat, e_long, my_lat, my_long)) + " km");
                    adapter.notifyDataSetChanged();
                }catch (NullPointerException e){
                    Log.wtf("added", "not yet");
                }

                listView.setVisibility(View.VISIBLE);
                listView.setBackgroundColor(Color.WHITE);

            }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    String key = dataSnapshot.getKey();
                    for (int j = 0; j < emergenecyArray.size(); j++){
                        Emergency e = emergenecyArray.get(j);
                        if(e.getKey().equals(key)){
                            //found, delete.
                            emergenecyArray.remove(j);
                            distanceArray.remove(j);
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
                        String e_respondingEfar = dataSnapshot.child("responding_efar").getValue().toString();
                        String e_state = dataSnapshot.child("state").getValue().toString();
                        emergenecyArray.add(new Emergency(e_key, e_address, e_lat, e_long, e_phone_number, e_info, e_creationDate, e_respondingEfar, e_state));
                        distanceArray.add(String.format("%.2f", distance(e_lat, e_long, my_lat, my_long)) + " km");
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
                            distanceArray.remove(j);
                            adapter.notifyDataSetChanged();
                            break;
                        }
                    }

                    if(emergenecyArray.size() == 0){
                        listView.setVisibility(View.GONE);
                        Log.d("SIZE:", String.valueOf(emergenecyArray.size()));
                    }else{
                        listView.setVisibility(View.VISIBLE);
                        Log.d("SIZE:", String.valueOf(emergenecyArray.size()));
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

                String mapLink = "http://maps.google.com/?q=" + emergenecyArray.get(position).getLatitude() + ","  + emergenecyArray.get(position).getLongitude();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date timeCreated = null;
                try {
                    timeCreated = simpleDateFormat.parse(emergenecyArray.get(position).getCreationDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                SimpleDateFormat displayTimeFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
                String dipslayTime = displayTimeFormat.format(timeCreated);
                launchEmergencyInfoScreen(emergenecyArray.get(position).getCreationDate(),
                                            emergenecyArray.get(position).getLatitude().toString(),
                                            emergenecyArray.get(position).getLongitude().toString(),
                                            emergenecyArray.get(position).getAddress(),
                                            emergenecyArray.get(position).getPhone(),
                                            emergenecyArray.get(position).getInfo(),
                                            emergenecyArray.get(position).getRespondingEfar(),
                                            emergenecyArray.get(position).getKey(),
                                            emergenecyArray.get(position).getState());
            }


        });

        //button to get back to patient screen
        Button logoutButton = (Button) findViewById(R.id.logout_button);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //to get rid of stored password and username
                SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                // say that user has logged off
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference userRef = database.getReference("users");
                userRef.child(sharedPreferences.getString("id", "") + "/logged_in").setValue(false);
                userRef.child(sharedPreferences.getString("id", "") + "/token").setValue("null");
                editor.putString("id", "");
                editor.putString("name", "");
                editor.putBoolean("logged_in", false);
                stopService(new Intent(EFARMainActivity.this, MyService.class));
                editor.apply();

                //clear the phones token for the database
                String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                DatabaseReference token_ref = database.getReference("tokens/" + refreshedToken);
                token_ref.removeValue();

                launchPatientMainScreen();
                finish();
            }
        });

        Button backButton = (Button) findViewById(R.id.back_button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchPatientMainScreen();
            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client2 = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        final Handler handler = new Handler();
        handler.postDelayed( new Runnable() {

            @Override
            public void run() {
                updateDistances();
                adapter.notifyDataSetChanged();
                handler.postDelayed( this, 30 * 1000 );
            }
        }, 30 * 1000 );
    }

    // Goes to patient info tab to send more to EFARs
    private void launchPatientMainScreen() {
        Intent toPatientMainScreen = new Intent(this, PatientMainActivity.class);
        startActivity(toPatientMainScreen);
        finish();
    }

    // Goes to emergency info tab to send more to EFARs
    private void launchEmergencyInfoScreen(String time, String latitude, String longitude, String address, String phoneNumber, String info, String id, String key, String state) {
        Intent toEmergnecyInfoScreen = new Intent(this, EmergencyInfoActivity.class);
        toEmergnecyInfoScreen.putExtra("time", time);
        toEmergnecyInfoScreen.putExtra("lat", latitude);
        toEmergnecyInfoScreen.putExtra("long", longitude);
        toEmergnecyInfoScreen.putExtra("address", address);
        toEmergnecyInfoScreen.putExtra("phoneNumber", phoneNumber);
        toEmergnecyInfoScreen.putExtra("info", info);
        toEmergnecyInfoScreen.putExtra("id", id);
        toEmergnecyInfoScreen.putExtra("key", key);
        toEmergnecyInfoScreen.putExtra("state", state);
        startActivity(toEmergnecyInfoScreen);
        finish();
    }

    // Goes to patient info tab to send more to EFARs
    private void launchMessagingScreen() {
        Intent launchMessagingScreen = new Intent(this, MessagingScreenActivity.class);
        startActivity(launchMessagingScreen);
    }

    //distance functions via: http://www.geodatasource.com/developers/java
    private static double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;
        return (dist);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts decimal degrees to radians						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts radians to decimal degrees						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
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

    // Starts up launchEfarWriteUpScreen screen
    private void launchEfarWriteUpScreen() {
        Intent toLaunchEfarWriteUPScreen = new Intent(this, EFARInfoActivity.class);
        startActivity(toLaunchEfarWriteUPScreen);
    }

    private void updateDistances(){
        GPSTracker gps = new GPSTracker(this);
        my_lat = gps.getLatitude(); // latitude
        my_long = gps.getLongitude(); // longitude
        distanceArray.clear();
        for (int i = 0; i < emergenecyArray.size(); i++) {
            distanceArray.add(String.format("%.2f", distance(emergenecyArray.get(i).getLatitude(), emergenecyArray.get(i).getLongitude(), my_lat, my_long)) + " km");
        }
    }

    //disables the werid transition beteen activities
    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

}
