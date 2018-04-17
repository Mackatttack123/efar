package com.southafricaproject.efar;

/**
 * Created by mackfitzpatrick on 4/17/18.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EFARMainTabYou extends Fragment{

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.efar_main_you_tab, container, false);

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users");
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        final String id = sharedPreferences.getString("id", "");
        userRef.child(id + "/token").setValue(refreshedToken);

        adapter = new ArrayAdapter<String>(getActivity(), R.layout.activity_listview, distanceArray){
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                if(emergenecyArray.get(position).getRespondingEfar().contains(id)){
                    //cell.setBackgroundColor(Color.argb(150, 0, 255, 0));
                    activeStateText.setText("Responded to by you");
                    activeStateText.setTextColor(Color.argb(255, 0, 150, 0));

                }

                if(position % 2 == 0){
                    cell.setBackgroundColor(Color.argb(150, 224, 224, 224));
                }else{
                    cell.setBackgroundColor(Color.argb(150, 255, 255, 255));
                }
                return cell;
            }
        };

        listView = (ListView) rootView.findViewById(R.id.patient_list_view);
        listView.setAdapter(adapter);
        listView.setClickable(true);

        listView.setBackgroundColor(Color.TRANSPARENT);

        GPSTracker gps = new GPSTracker(getActivity());
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
                    if(e_respondingEfar.toString().contains(id)) {
                        emergenecyArray.add(new Emergency(e_key, e_address, e_lat, e_long, e_phone_number, e_info, e_creationDate, e_respondingEfar, e_state));
                        distanceArray.add(String.format("%.2f", distance(e_lat, e_long, my_lat, my_long)) + " km");
                        adapter.notifyDataSetChanged();
                        listView.setVisibility(View.VISIBLE);
                        listView.setBackgroundColor(Color.WHITE);
                    }
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
                    if(e_respondingEfar.toString().contains(id)) {
                        emergenecyArray.add(new Emergency(e_key, e_address, e_lat, e_long, e_phone_number, e_info, e_creationDate, e_respondingEfar, e_state));
                        distanceArray.add(String.format("%.2f", distance(e_lat, e_long, my_lat, my_long)) + " km");
                        adapter.notifyDataSetChanged();
                    }
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

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client2 = new GoogleApiClient.Builder(getActivity()).addApi(AppIndex.API).build();

        final Handler handler = new Handler();
        handler.postDelayed( new Runnable() {

            @Override
            public void run() {
                updateDistances();
                adapter.notifyDataSetChanged();
                handler.postDelayed( this, 30 * 1000 );
            }
        }, 30 * 1000 );

        return rootView;
    }

    // Goes to emergency info tab to send more to EFARs
    private void launchEmergencyInfoScreen(String time, String latitude, String longitude, String address, String phoneNumber, String info, String id, String key, String state) {
        Intent toEmergnecyInfoScreen = new Intent(getActivity(), EmergencyInfoActivity.class);
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
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
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

    private void updateDistances(){
        GPSTracker gps = new GPSTracker(getActivity());
        my_lat = gps.getLatitude(); // latitude
        my_long = gps.getLongitude(); // longitude
        distanceArray.clear();
        for (int i = 0; i < emergenecyArray.size(); i++) {
            distanceArray.add(String.format("%.2f", distance(emergenecyArray.get(i).getLatitude(), emergenecyArray.get(i).getLongitude(), my_lat, my_long)) + " km");
        }
    }
}
