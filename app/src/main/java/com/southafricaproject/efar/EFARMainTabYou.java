package com.southafricaproject.efar;

/**
 * Created by mackfitzpatrick on 4/17/18.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.Html;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EFARMainTabYou extends Fragment{

    String responding_ids;
    String time;
    String latitude;
    String longitude;
    String address = "N/A";
    String phoneNumber = "N/A";
    String info;
    String key;
    String state;
    Boolean area_unsafe;
    Boolean severe_trauma;
    Boolean heart_attack;
    Button notSafeButton;
    Button severeTraumaButton;
    Button heartAttackButton;
    Button endButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.efar_main_you_tab, container, false);
        final ListView emergencyInfoListView = (ListView) rootView.findViewById(R.id.emergencyInfoListView);
        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyData", Context.MODE_PRIVATE);

        final TextView messageTextView = (TextView)rootView.findViewById(R.id.messageTextView);
        messageTextView.setText("Loading . . .");

        FirebaseDatabase.getInstance().getReference().child("emergencies").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.exists() && dataSnapshot.hasChild("responding_efar") && dataSnapshot.hasChild("creation_date")){
                    String id = sharedPreferences.getString("id", "");
                    if(dataSnapshot.child("responding_efar").getValue().toString().contains(id)){
                        key = dataSnapshot.getKey();
                        responding_ids  = dataSnapshot.child("responding_efar").getValue().toString();
                        time = dataSnapshot.child("creation_date").getValue().toString();
                        latitude = dataSnapshot.child("latitude").getValue().toString();
                        longitude = dataSnapshot.child("longitude").getValue().toString();
                        address = getCompleteAddressString(Double.parseDouble(latitude), Double.parseDouble(longitude));
                        phoneNumber = dataSnapshot.child("phone_number").getValue().toString();
                        info = dataSnapshot.child("other_info").getValue().toString();
                        state = dataSnapshot.child("state").getValue().toString();
                        if(dataSnapshot.child("area_unsafe").exists()) {
                            String area_safe_string = dataSnapshot.child("area_unsafe").getValue().toString();
                            if(area_safe_string.equals("true")){
                                area_unsafe = true;
                            }else{
                                area_unsafe = false;
                            }
                        }else{
                            area_unsafe = false;
                        }
                        if(dataSnapshot.child("severe_trauma").exists()) {
                            String area_safe_string = dataSnapshot.child("severe_trauma").getValue().toString();
                            if(area_safe_string.equals("true")){
                                severe_trauma = true;
                            }else{
                                severe_trauma = false;
                            }
                        }else{
                            severe_trauma = false;
                        }
                        if(dataSnapshot.child("heart_attack").exists()) {
                            String area_safe_string = dataSnapshot.child("heart_attack").getValue().toString();
                            if(area_safe_string.equals("true")){
                                heart_attack = true;
                            }else{
                                heart_attack = false;
                            }
                        }else{
                            heart_attack = false;
                        }

                        emergencyInfoListView.setVisibility(View.VISIBLE);
                        Adapter emergencyInfoAdapter = new EmergencyInfoCustomAdapter();
                        emergencyInfoListView.setAdapter((ListAdapter) emergencyInfoAdapter);
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.exists() && dataSnapshot.hasChild("responding_efar")){
                    String id = sharedPreferences.getString("id", "");
                    if(dataSnapshot.child("responding_efar").getValue().toString().contains(id)){
                        key = dataSnapshot.getKey();
                        responding_ids  = dataSnapshot.child("responding_efar").getValue().toString();
                        time = dataSnapshot.child("creation_date").getValue().toString();
                        latitude = dataSnapshot.child("latitude").getValue().toString();
                        longitude = dataSnapshot.child("longitude").getValue().toString();
                        address = getCompleteAddressString(Double.parseDouble(latitude), Double.parseDouble(longitude));
                        phoneNumber = dataSnapshot.child("phone_number").getValue().toString();
                        info = dataSnapshot.child("other_info").getValue().toString();
                        state = dataSnapshot.child("state").getValue().toString();
                        if(dataSnapshot.child("area_unsafe").exists()) {
                            String area_safe_string = dataSnapshot.child("area_unsafe").getValue().toString();
                            if(area_safe_string.equals("true")){
                                area_unsafe = true;
                            }else{
                                area_unsafe = false;
                            }
                        }else{
                            area_unsafe = false;
                        }
                        if(dataSnapshot.child("severe_trauma").exists()) {
                            String area_safe_string = dataSnapshot.child("severe_trauma").getValue().toString();
                            if(area_safe_string.equals("true")){
                                severe_trauma = true;
                            }else{
                                severe_trauma = false;
                            }
                        }else{
                            severe_trauma = false;
                        }
                        if(dataSnapshot.child("heart_attack").exists()) {
                            String area_safe_string = dataSnapshot.child("heart_attack").getValue().toString();
                            if(area_safe_string.equals("true")){
                                heart_attack = true;
                            }else{
                                heart_attack = false;
                            }
                        }else{
                            heart_attack = false;
                        }

                        emergencyInfoListView.setVisibility(View.VISIBLE);
                        Adapter emergencyInfoAdapter = new EmergencyInfoCustomAdapter();
                        emergencyInfoListView.setAdapter((ListAdapter) emergencyInfoAdapter);
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.hasChild("responding_efar")){
                    String id = sharedPreferences.getString("id", "");
                    if(dataSnapshot.child("responding_efar").getValue().toString().contains(id)){
                        emergencyInfoListView.setVisibility(View.GONE);
                        messageTextView.setText("Information about an emergency you are responding to will appear here");
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

        messageTextView.setText("Information about an emergency you are responding to will appear here");
        return rootView;
    }

    // Starts up launchEfarWriteUpScreen screen
    private void launchEfarWriteUpScreen(String key) {
        Intent toLaunchEfarWriteUPScreen = new Intent(getContext(), ActivityEFARInfo.class);
        toLaunchEfarWriteUPScreen.putExtra("key", key);
        startActivity(toLaunchEfarWriteUPScreen);
    }

    // Goes to patient info tab to send more to EFARs
    private void launchMessagingScreen(String key) {
        Intent launchMessagingScreen = new Intent(getContext(), ActivityMessagingScreen.class);
        launchMessagingScreen.putExtra("key", key);
        startActivity(launchMessagingScreen);
    }

    View cell = null;

    private class EmergencyInfoCustomAdapter extends BaseAdapter {

        @Override //responsible for the number of rows in the list
        public int getCount() {

            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return "test String";
        }

        @Override //renders out each row
        public View getView(int position, View convertView, ViewGroup parent) {

            if(cell == null){
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                cell = inflater.inflate(R.layout.emergecy_info_cell_responded_to, parent, false);
            }

            if(getActivity() != null) {
                final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyData", Context.MODE_PRIVATE);

                final Button messageButton = (Button) cell.findViewById(R.id.messagesButton);
                endButton = (Button) cell.findViewById(R.id.endButton);
                notSafeButton = (Button) cell.findViewById(R.id.notSafeButton);
                severeTraumaButton = (Button) cell.findViewById(R.id.severeTraumaButton);
                heartAttackButton = (Button) cell.findViewById(R.id.heartAttackButton);
                notSafeButton.setVisibility(View.GONE);
                severeTraumaButton.setVisibility(View.GONE);
                heartAttackButton.setVisibility(View.GONE);

                if(!area_unsafe){
                    notSafeButton.setText("Area\nUnsafe");
                }else{
                    notSafeButton.setText("Area\nSafe");
                }
                notSafeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!area_unsafe){
                            areaUnsafeAlert();
                        }else{
                            areaSafeAlert();
                        }
                    }
                });

                severeTraumaButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        severeTraumaAlert();
                    }
                });
                if(severe_trauma){
                    severeTraumaButton.setEnabled(false);
                }

                heartAttackButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        heartAttackAlert();
                    }
                });
                if(heart_attack){
                    heartAttackButton.setEnabled(false);
                }

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
                if(state.equals("1.5")){
                    setEndEmergency();
                }else if(state.equals("1.75")){
                    endButton.setVisibility(View.VISIBLE);
                    endButton.setEnabled(false);
                    endButton.setText("Waiting for dispatch");
                }else{
                    endButton.setVisibility(View.VISIBLE);
                    endButton.setText("On Scene");
                    endButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            endButton.setEnabled(false);
                            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                            final String efar_id = sharedPreferences.getString("id", "");
                            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                            View passwordView = inflater.inflate(R.layout.on_scene_form, null);
                            final EditText notesEditText = (EditText) passwordView.findViewById(R.id.notesEditText);
                            alert.setView(passwordView);
                            alert.setMessage("What do you see?");
                            alert.setTitle("You are on scene");
                            alert.setCancelable(false);
                            alert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                    final FirebaseUser currentUser = mAuth.getCurrentUser();
                                    if(currentUser == null && getActivity() != null){
                                        mAuth.signInAnonymously()
                                                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                                        if (task.isSuccessful()) {
                                                            // Sign in success, update UI with the signed-in user's information
                                                            String comment = notesEditText.getText().toString();
                                                            Date currentTime = Calendar.getInstance().getTime();
                                                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                                            String timestamp = simpleDateFormat.format(currentTime);
                                                            final FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                            final String keyToUpdate = key;
                                                            DatabaseReference emergency_ref_comment = database.getReference("emergencies/" + keyToUpdate + "/on_scene_first_impression/" + efar_id);
                                                            emergency_ref_comment.setValue(comment);
                                                            DatabaseReference emergency_ref_time = database.getReference("emergencies/" + keyToUpdate + "/on_scene_time/" + efar_id);
                                                            emergency_ref_time.setValue(timestamp);
                                                            DatabaseReference emergency_ref_state = database.getReference("emergencies/" + keyToUpdate + "/state");
                                                            emergency_ref_state.setValue("1.5");
                                                            setEndEmergency();
                                                        } else {
                                                            new AlertDialog.Builder(getActivity())
                                                                    .setTitle("ERROR:")
                                                                    .setMessage("Could not send on Scene message!")
                                                                    .setNegativeButton("No", null)
                                                                    .show();
                                                        }
                                                    }
                                                });
                                    }else{
                                        String comment = notesEditText.getText().toString();
                                        Date currentTime = Calendar.getInstance().getTime();
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                        String timestamp = simpleDateFormat.format(currentTime);
                                        final FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        final String keyToUpdate = key;
                                        DatabaseReference emergency_ref_comment = database.getReference("emergencies/" + keyToUpdate + "/on_scene_first_impression/" + efar_id);
                                        emergency_ref_comment.setValue(comment);
                                        DatabaseReference emergency_ref_time = database.getReference("emergencies/" + keyToUpdate + "/on_scene_time/" + efar_id);
                                        emergency_ref_time.setValue(timestamp);
                                        DatabaseReference emergency_ref_state = database.getReference("emergencies/" + keyToUpdate + "/state");
                                        emergency_ref_state.setValue("1.5");
                                        setEndEmergency();
                                    }
                                }
                            });
                            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            });
                            AlertDialog dialog = alert.create();
                            dialog.show();
                            dialog.getWindow().setBackgroundDrawableResource(R.color.light_grey);
                            endButton.setEnabled(true);

                        }
                    });
                }
            }

            TextView timeText = (TextView) cell.findViewById(R.id.createdTextView);
            TextView addressText = (TextView) cell.findViewById(R.id.AddressTextView);
            TextView phoneNumberText = (TextView) cell.findViewById(R.id.numberTextView);
            TextView infoText = (TextView) cell.findViewById(R.id.infoTextView);
            TextView idText = (TextView) cell.findViewById(R.id.IdTextView);
            final Button call_button = (Button) cell.findViewById(R.id.call_button);
            final Button to_maps_button = (Button) cell.findViewById(R.id.to_maps_button);

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

            SpannableString responderTextSpan = new SpannableString("<strong>Responder ID(s): </strong> " + responding_ids);

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
            if(!responding_ids.equals("") && !responding_ids.equals("N/A")) {
                idText.setText(responderTextSpan);
            }else{
                idText.setText("");
            }

            return cell;
        }

    }

    public void setEndEmergency(){
        if(getActivity() != null) {
            final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyData", Context.MODE_PRIVATE);
            endButton.setVisibility(View.VISIBLE);
            endButton.setText("End Emergency");
            endButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("finished_emergency_key", key);
                    editor.putString("finished_emergency_date", time);
                    editor.commit();
                    launchEfarWriteUpScreen(key);
                }
            });
            notSafeButton.setVisibility(View.VISIBLE);
            severeTraumaButton.setVisibility(View.VISIBLE);
            heartAttackButton.setVisibility(View.VISIBLE);
        }
    }

    public String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
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

    public void areaUnsafeAlert() {
        if (getActivity() != null) {
            final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyData", Context.MODE_PRIVATE);
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("AREA UNSAFE:");
            alert.setMessage("Are you sure you want to notify other EFARs that the emergency scene is unsafe?");
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Create new emergency in the database
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference messsage_ref = database.getReference("emergencies/" + key + "/messages");
                    DatabaseReference message_key = messsage_ref.push();
                    Map<String, String> data = new HashMap<String, String>();
                    String name = sharedPreferences.getString("name", "");
                    data.put("user", "ALERT FROM: " + name);
                    Date currentTime = Calendar.getInstance().getTime();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    String timestamp = simpleDateFormat.format(currentTime);
                    data.put("timestamp", timestamp);
                    data.put("message", "AREA UNSAFE AT EMERGENCY SCENE!");
                    messsage_ref.child(message_key.getKey()).setValue(data);
                    database.getReference("emergencies/" + key + "/area_unsafe").setValue("true");
                    area_unsafe = true;
                    notSafeButton.setText("Area\nSafe");
                    notSafeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            areaSafeAlert();
                        }
                    });
                }
            });
            alert.setNegativeButton("No", null);
            alert.show();
        }
    }

    public void areaSafeAlert() {
        if (getActivity() != null) {
            final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyData", Context.MODE_PRIVATE);
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("AREA NOW SAFE:");
            alert.setMessage("Are you sure you want to notify other EFARs that the emergency scene is now safe?");
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Create new emergency in the database
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference messsage_ref = database.getReference("emergencies/" + key + "/messages");
                    DatabaseReference message_key = messsage_ref.push();
                    Map<String, String> data = new HashMap<String, String>();
                    String name = sharedPreferences.getString("name", "");
                    data.put("user", "ALERT FROM: " + name);
                    data.put("area_unsafe", "false");
                    Date currentTime = Calendar.getInstance().getTime();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    String timestamp = simpleDateFormat.format(currentTime);
                    data.put("timestamp", timestamp);
                    data.put("message", "EMERGENCY AREA IS NOW SAFE.");
                    messsage_ref.child(message_key.getKey()).setValue(data);
                    database.getReference("emergencies/" + key + "/area_unsafe").setValue("false");
                    area_unsafe = false;
                    notSafeButton.setText("Area\nUnsafe");
                    notSafeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            areaUnsafeAlert();
                        }
                    });
                }
            });
            alert.setNegativeButton("No", null);
            alert.show();
        }
    }

    public void severeTraumaAlert(){
        if(getActivity() != null) {
            final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyData", Context.MODE_PRIVATE);
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("SEVERE TRAUMA:");
            alert.setMessage("Are you sure you want to notify other EFARs that this emergency involves a severe trauma?");
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Create new emergency in the database
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference messsage_ref = database.getReference("emergencies/" + key + "/messages");
                    DatabaseReference message_key = messsage_ref.push();
                    Map<String, String> data = new HashMap<String, String>();
                    String name = sharedPreferences.getString("name", "");
                    data.put("user", "ALERT FROM: " + name);
                    Date currentTime = Calendar.getInstance().getTime();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    String timestamp = simpleDateFormat.format(currentTime);
                    data.put("timestamp", timestamp);
                    data.put("message", "AT LEAST ONE SEVERE TRAUMA PATIENT AT THE EMERGENCY SCENE!");
                    messsage_ref.child(message_key.getKey()).setValue(data);
                    database.getReference("emergencies/" + key + "/severe_trauma").setValue("true");
                }
            });
            alert.setNegativeButton("No", null);
            alert.show();
        }
    }

    public void heartAttackAlert(){
        if(getActivity() != null) {
            final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyData", Context.MODE_PRIVATE);
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("HEART ATTACK:");
            alert.setMessage("Are you sure you want to notify other EFARs that this emergency involves a heart attack?");
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Create new emergency in the database
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference messsage_ref = database.getReference("emergencies/" + key + "/messages");
                    DatabaseReference message_key = messsage_ref.push();
                    Map<String, String> data = new HashMap<String, String>();
                    String name = sharedPreferences.getString("name", "");
                    data.put("user", "ALERT FROM: " + name);
                    Date currentTime = Calendar.getInstance().getTime();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    String timestamp = simpleDateFormat.format(currentTime);
                    data.put("timestamp", timestamp);
                    data.put("message", "HEART ATTACK PATIENT AT THE EMERGENCY SCENE!");
                    messsage_ref.child(message_key.getKey()).setValue(data);
                    database.getReference("emergencies/" + key + "/heart_attack").setValue("true");
                }
            });
            alert.setNegativeButton("No", null);
            alert.show();
        }
    }
}

