package com.southafricaproject.efar;

/**
 * Created by mackfitzpatrick on 4/17/18.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.telephony.PhoneNumberUtils;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EFARMainTabAll extends Fragment{

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
    AlertDialog infoDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.efar_main_all_tab, container, false);

        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyData", Context.MODE_PRIVATE);
        final String id = sharedPreferences.getString("id", "");

        final TextView alertText = (TextView)rootView.findViewById(R.id.alert_text);
        alertText.setText("Loading . . .");

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
                SimpleDateFormat displayTimeFormat = new SimpleDateFormat("HH:mm");
                String dipslayTime = displayTimeFormat.format(timeCreated);
                TextView timeText = (TextView) cell.findViewById(R.id.timeTextView_right);
                timeText.setText(dipslayTime);
                TextView distanceText =  (TextView) cell.findViewById(R.id.distanceTextView);
                TextView addressText =  (TextView) cell.findViewById(R.id.addressTextView);
                addressText.setText(emergenecyArray.get(position).getAddress());
                distanceText.setText(distanceArray.get(position).toString() + " away");

                GPSTracker gps = new GPSTracker(getActivity());
                my_lat = gps.getLatitude(); // latitude
                my_long = gps.getLongitude(); // longitude

                ProgressBar distance_progress = (ProgressBar) cell.findViewById(R.id.distance_progress_bar);
                distance_progress.setMax(100);
                int Total_progress = (int) Math.round((distance(emergenecyArray.get(position).getLatitude(), emergenecyArray.get(position).getLongitude(), my_lat, my_long) / 2.0) * 100);
                if(Total_progress >= 100){
                    distance_progress.setProgress(0);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        distance_progress.setProgressTintList(ColorStateList.valueOf(Color.rgb(200, 0, 0)));
                    }
                }else {
                    distance_progress.setProgress(100 - Total_progress);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        distance_progress.setProgressTintList(ColorStateList.valueOf(Color.rgb(2, 55, 98)));
                    }
                }

                TextView activeStateText =  (TextView) cell.findViewById(R.id.stateTextView);
                if (emergenecyArray.get(position).getState().equals("0")){
                    //cell.setBackgroundColor(Color.argb(150, 255, 0, 0));
                    activeStateText.setText("Awaiting Response!");
                    activeStateText.setTextColor(Color.rgb(200, 0, 0));
                }else if(emergenecyArray.get(position).getRespondingEfar().contains(id)){
                    //cell.setBackgroundColor(Color.argb(150, 0, 255, 0));
                    activeStateText.setText("Responded to by you");
                    activeStateText.setTextColor(Color.rgb(2, 55, 98));
                }else{
                    String[] responders = emergenecyArray.get(position).getRespondingEfar().split(",");
                    int num = responders.length;
                    //cell.setBackgroundColor(Color.argb(150, 255, 255, 0));
                    activeStateText.setText("Responded to by " + num);
                    activeStateText.setTextColor(Color.rgb(81, 150, 80));
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
        final SwipeRefreshLayout pullToRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.pullToRefresh);
        listView.setAdapter(adapter);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                refreshContent(pullToRefresh);
            }
        });

        listView.setClickable(true);
        listView.setBackgroundColor(Color.TRANSPARENT);

        GPSTracker gps = new GPSTracker(getActivity());
        my_lat = gps.getLatitude(); // latitude
        my_long = gps.getLongitude(); // longitude

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("responding_to_other", false);
        editor.commit();

        FirebaseDatabase.getInstance().getReference().child("emergencies").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try{
                    String e_key = dataSnapshot.getKey();
                    String e_phone_number = dataSnapshot.child("phone_number").getValue().toString();
                    String e_info = dataSnapshot.child("other_info").getValue().toString();
                    Double e_lat = Double.parseDouble(dataSnapshot.child("latitude").getValue().toString());
                    Double e_long = Double.parseDouble(dataSnapshot.child("longitude").getValue().toString());
                    String e_address;
                    try {
                        e_address = getCompleteAddressString(e_lat, e_long);
                    }catch (Exception e) {
                        e_address = "N/A";
                    }
                    String e_creationDate = dataSnapshot.child("creation_date").getValue().toString();
                    String e_respondingEfar;
                    try {
                        e_respondingEfar = dataSnapshot.child("responding_efar").getValue().toString();
                        String id = sharedPreferences.getString("id", "");
                        if (e_respondingEfar.contains(id)) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("responding_to_other", true);
                            editor.commit();
                        }
                    }catch (Exception e){
                        e_respondingEfar = "N/A";
                    }
                    String e_state = dataSnapshot.child("state").getValue().toString();
                    // only show local emergencies within 10km
                    if(distance(e_lat, e_long, my_lat, my_long) < 10.0){
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
                    String e_respondingEfar;
                    try{
                        e_respondingEfar = dataSnapshot.child("responding_efar").getValue().toString();
                    }catch (NullPointerException e) {
                        e_respondingEfar = "";
                    }
                    String e_state = dataSnapshot.child("state").getValue().toString();
                    // only show local emergencies within 10km
                    if(distance(e_lat, e_long, my_lat, my_long) < 10.0){
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

        FirebaseDatabase.getInstance().getReference().child("emergencies").addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Handler handler = new Handler();
                handler.postDelayed( new Runnable() {

                    @Override
                    public void run() {
                        updateDistances();
                        adapter.notifyDataSetChanged();
                        alertText.setText("Emergencies within 10km of you will appear here");
                        handler.postDelayed( this, 30 * 1000 );
                    }
                }, 0);
            }
            public void onCancelled(DatabaseError databaseError) {
                alertText.setText("Error: Could not load data. Pull down to try and refresh.");
                // TESTING THE CODE BELOW, MAY CAUSE ERROR //
                refreshContent(pullToRefresh);
                // TESTING THE CODE ABOVE, MAY CAUSE ERROR //
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyData", Context.MODE_PRIVATE);

                String efar_id = sharedPreferences.getString("id", "");
                if(emergenecyArray.get(position).getRespondingEfar().contains(efar_id)){
                    TabLayout tabs = (TabLayout)getActivity().findViewById(R.id.tabs);
                    tabs.getTabAt(1).select();
                }else{
                    if(getActivity() != null){
                        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View cell = inflater.inflate(R.layout.emergecy_info_cell, null);

                        String time = emergenecyArray.get(position).getCreationDate();
                        final String latitude = emergenecyArray.get(position).getLatitude().toString();
                        final String longitude = emergenecyArray.get(position).getLongitude().toString();
                        String address = emergenecyArray.get(position).getAddress();
                        String phoneNumber = emergenecyArray.get(position).getPhone();
                        String info = emergenecyArray.get(position).getInfo();
                        String efar_ids = emergenecyArray.get(position).getRespondingEfar();
                        String key = emergenecyArray.get(position).getKey();
                        String state = emergenecyArray.get(position).getState();

                        TextView timeText = (TextView) cell.findViewById(R.id.createdTextView);
                        TextView addressText = (TextView) cell.findViewById(R.id.AddressTextView);
                        TextView phoneNumberText = (TextView) cell.findViewById(R.id.numberTextView);
                        TextView infoText = (TextView) cell.findViewById(R.id.infoTextView);
                        TextView idText = (TextView) cell.findViewById(R.id.IdTextView);
                        final Button call_button = (Button) cell.findViewById(R.id.call_button);
                        final Button to_maps_button = (Button) cell.findViewById(R.id.to_maps_button);
                        final ImageButton doneButton = (ImageButton) cell.findViewById(R.id.doneButton);

                        final String phoneLink = "tel:" + phoneNumber.replaceAll("[^\\d.]", "");

                        call_button.setOnClickListener(
                                new Button.OnClickListener() {
                                    public void onClick(View v) {
                                        Intent intent = new Intent();
                                        intent.setAction(Intent.ACTION_VIEW);
                                        intent.addCategory(Intent.CATEGORY_BROWSABLE);
                                        intent.setData(Uri.parse(phoneLink));
                                        startActivity(intent);
                                    }
                                });

                        doneButton.setOnClickListener(
                                new Button.OnClickListener() {
                                    public void onClick(View v) {
                                        infoDialog.dismiss();
                                    }
                                });

                        to_maps_button.setOnClickListener(
                                new Button.OnClickListener() {
                                    public void onClick(View v) {
                                        Intent intent = new Intent();
                                        intent.setAction(Intent.ACTION_VIEW);
                                        intent.addCategory(Intent.CATEGORY_BROWSABLE);
                                        intent.setData(Uri.parse("http://maps.google.com/?q=" + latitude + "," + longitude));
                                        startActivity(intent);
                                    }
                                });

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        Date timeCreated = null;
                        try {
                            timeCreated = simpleDateFormat.parse(time);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        SimpleDateFormat displayTimeFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
                        String displayTime = "";
                        try {
                            displayTime = displayTimeFormat.format(timeCreated);
                        } catch (Exception e) {
                            displayTime = "N/A";
                        }

                        if(info.equals("")){
                            info = "N/A";
                        }
                        SpannableString infoTextSpan = new SpannableString("<strong>Information Given: </strong><br>" + info);

                        SpannableString responderTextSpan = new SpannableString("<strong>Responder ID(s): </strong> " + efar_ids);

                        SpannableString createdTextSpan = new SpannableString("<strong>Created: </strong> " + displayTime);

                        SpannableString addressTextSpan = new SpannableString("<strong>Incident Address: </strong><br>" + address);
                        if(address.equals("N/A")){
                            addressTextSpan = new SpannableString("<strong>Incident Location: </strong> (" + latitude + ", " + longitude + ")");
                        }


                        String formattedNumber = phoneNumber;

                        if(!phoneNumber.equals("N/A")){
                            if(phoneNumber.startsWith("27")){
                                phoneNumber = phoneNumber.substring(2, phoneNumber.length());
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    formattedNumber = "+27 " + PhoneNumberUtils.formatNumber(phoneNumber,Locale.getDefault().getCountry());
                                } else {
                                    //Deprecated method
                                    formattedNumber = "+27 " + PhoneNumberUtils.formatNumber(phoneNumber);
                                }
                            }else{
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    formattedNumber = PhoneNumberUtils.formatNumber(phoneNumber,Locale.getDefault().getCountry());
                                } else {
                                    //Deprecated method
                                    formattedNumber = PhoneNumberUtils.formatNumber(phoneNumber);
                                }
                            }
                        }else{
                            call_button.setVisibility(View.GONE);
                        }


                        SpannableString phoneTextSpan = new SpannableString("<strong>Contact Number: </strong> " + formattedNumber);

                        if (Build.VERSION.SDK_INT >= 24) {
                            // for 24 api and more
                            addressTextSpan = SpannableString.valueOf(Html.fromHtml(String.valueOf(addressTextSpan), 0));
                            phoneTextSpan = SpannableString.valueOf(Html.fromHtml(String.valueOf(phoneTextSpan), 0));
                            createdTextSpan = SpannableString.valueOf(Html.fromHtml(String.valueOf(createdTextSpan), 0));
                            responderTextSpan = SpannableString.valueOf(Html.fromHtml(String.valueOf(responderTextSpan), 0));
                            infoTextSpan = SpannableString.valueOf(Html.fromHtml(String.valueOf(infoTextSpan), 0));
                        } else {
                            // or for older api
                            addressTextSpan = SpannableString.valueOf(Html.fromHtml(String.valueOf(addressTextSpan)));
                            phoneTextSpan = SpannableString.valueOf(Html.fromHtml(String.valueOf(phoneTextSpan)));
                            createdTextSpan = SpannableString.valueOf(Html.fromHtml(String.valueOf(createdTextSpan)));
                            responderTextSpan = SpannableString.valueOf(Html.fromHtml(String.valueOf(responderTextSpan)));
                            infoTextSpan = SpannableString.valueOf(Html.fromHtml(String.valueOf(infoTextSpan)));
                        }

                        timeText.setText(createdTextSpan);
                        //to make the link clickable in the textview
                        addressText.setText(addressTextSpan);
                        phoneNumberText.setText(phoneTextSpan);
                        infoText.setText(infoTextSpan);
                        if(!efar_ids.equals("") && !efar_ids.equals("N/A")) {
                            idText.setText(responderTextSpan);
                        }else{
                            idText.setText("");
                        }
                        if(getActivity() != null){
                            setUpButtons(key, time, state, cell);
                        }
                        alert.setView(cell);
                        infoDialog = alert.create();
                        infoDialog.show();
                        infoDialog.getWindow().setBackgroundDrawableResource(R.color.light_grey);
                    }
                }
            }


        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client2 = new GoogleApiClient.Builder(getActivity()).addApi(AppIndex.API).build();

        return rootView;
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
        if(getContext() != null){
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
                return addresses.get(0).getAddressLine(0);
            } catch (IOException e) {
                e.printStackTrace();
                return "N/A";
            }
        }else{
            return "N/A";
        }
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

    private void refreshContent(final SwipeRefreshLayout pullToRefresh){
        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                //updateDistances();
                //adapter.notifyDataSetChanged();
                if(getActivity() != null){
                    getActivity().finish();
                    startActivity(getActivity().getIntent());
                }
                pullToRefresh.setRefreshing(false);
            }
        }, 3000);

    }

    private void setUpButtons(final String key, final String time, final String state, View view) {

        final Button messageButton = (Button) view.findViewById(R.id.messagesButton);
        final Button respondButton = (Button) view.findViewById(R.id.respondButton);

        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyData", Context.MODE_PRIVATE);

        if(state.equals("1") || state.equals("1.5")){
            messageButton.setVisibility(View.VISIBLE);
            messageButton.setText("Message EFARs");
            messageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // store emergency key to be passed to messages
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("messaging_key", key);
                    editor.commit();
                    launchMessagingScreen(key);
                }
            });
        }else {
            messageButton.setVisibility(View.GONE);
        }
        respondButton.setVisibility(View.VISIBLE);
        respondButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean responding_to_other = sharedPreferences.getBoolean("responding_to_other", false);
                if(responding_to_other){
                    new AlertDialog.Builder(getContext())
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Already Responding")
                            .setMessage("You are already responding to another emergency!")
                            .setPositiveButton("Okay", null)
                            .show();
                }else{
                    new AlertDialog.Builder(getContext())
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Respond to Emergency:")
                            .setMessage("Are you able to respond to this emergency?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                            {
                                final String keyToUpdate = key;
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean("responding_to_other", true);
                                    editor.commit();
                                    final FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    if(!state.equals("1.5")) {
                                        DatabaseReference emergency_ref = database.getReference("emergencies/" + keyToUpdate + "/state");
                                        emergency_ref.setValue("1");
                                    }
                                    DatabaseReference ref = database.getReference("emergencies/" + keyToUpdate);
                                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            DatabaseReference efar_ref = database.getReference("emergencies/" + keyToUpdate + "/responding_efar");
                                            if(dataSnapshot.hasChild("responding_efar")){
                                                String other_efars = dataSnapshot.child("responding_efar").getValue().toString();
                                                String new_id_set = other_efars + ", " + sharedPreferences.getString("id", "");
                                                efar_ref.setValue(new_id_set);
                                            }else{
                                                efar_ref.setValue(sharedPreferences.getString("id", ""));
                                            }
                                            TabLayout tabs = (TabLayout)getActivity().findViewById(R.id.tabs);
                                            tabs.getTabAt(1).select();
                                            infoDialog.dismiss();

                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            // None
                                        }
                                    });
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            }
        });
    }

    // Goes to patient info tab to send more to EFARs
    private void launchMessagingScreen(String key) {
        Intent launchMessagingScreen = new Intent(getContext(), ActivityMessagingScreen.class);
        launchMessagingScreen.putExtra("key", key);
        startActivity(launchMessagingScreen);
    }

}
