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
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

class Emergency {
    private String key;
    private String address;
    private Double latitude;
    private Double longitude;
    private String phone_number;
    private String info;
    private String creationDate;
    private String respondingEfar;
    private String state;

    // constructor
    public Emergency(String key, String address, Double latitude, Double longitude,
                     String phone_number, String info, String creationDate, String respondingEfar, String state) {
        this.key = key;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone_number = phone_number;
        this.info = info;
        this.creationDate = creationDate;
        this.respondingEfar = respondingEfar;
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
    public String getRespondingEfar() { return respondingEfar; }
    public String getState() { return state; }
}

public class EmergencyInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_info);

        ListView emergencyInfoListView = (ListView) findViewById(R.id.emergencyInfoListView);
        Adapter emergencyInfoAdapter = new EmergencyInfoActivity.EmergnecyInfoCustomAdapter();
        emergencyInfoListView.setAdapter((ListAdapter) emergencyInfoAdapter);

        Bundle bundle = getIntent().getExtras();
        final String time = bundle.getString("time");
        final String key = bundle.getString("key");
        final String state  = bundle.getString("state");

        setUpButtons(key, time, state);
    }

    private void setUpButtons(final String key, final String time, final String state) {
        //button to get back to patient screen
        Button backButton = (Button) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchEfarMain();
            }
        });

        Button messageButton = (Button) findViewById(R.id.messagesButton);

        Button endButton = (Button) findViewById(R.id.endButton);

        final Button respondButton = (Button) findViewById(R.id.respondButton);

        if(state.equals("0")){
            messageButton.setText("Respond");
            messageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(EmergencyInfoActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Respond to Emergency:")
                            .setMessage("Are you able to respond to this emergency?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                            {
                                final String keyToUpdate = key;
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference emergency_ref = database.getReference("emergencies/" + keyToUpdate + "/state");
                                    emergency_ref.setValue("1");
                                    DatabaseReference ref = database.getReference("emergencies/" + keyToUpdate);
                                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                                            DatabaseReference efar_ref = database.getReference("emergencies/" + keyToUpdate + "/responding_efar");
                                            if(dataSnapshot.hasChild("responding_efar")){
                                                String other_efars = dataSnapshot.child("responding_efar").getValue().toString();
                                                String new_id_set = other_efars + ", " + sharedPreferences.getString("id", "");
                                                efar_ref.setValue(new_id_set);
                                                respondButton.setVisibility(View.GONE);
                                            }else{
                                                efar_ref.setValue(sharedPreferences.getString("id", ""));
                                                respondButton.setVisibility(View.GONE);
                                            }
                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            // None
                                        }
                                    });

                                    setUpButtons(key, time, "1");
                                }

                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            });
            respondButton.setVisibility(View.GONE);
        }else{
            messageButton.setText("Message");
            messageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // store emergency key to be passed to messages
                    SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("messaging_key", key);
                    editor.commit();
                    launchMessagingScreen();
                }
            });
            //check to see if efar is already responding
            SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
            String efar_id = sharedPreferences.getString("id", "");
            Bundle bundle = getIntent().getExtras();
            final String responding_ids = bundle.getString("id");
            if(!responding_ids.contains(efar_id)){
                respondButton.setVisibility(View.VISIBLE);
                respondButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AlertDialog.Builder(EmergencyInfoActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Respond to Emergency:")
                                .setMessage("Are you able to respond to this emergency?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                {
                                    final String keyToUpdate = key;
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        final FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        DatabaseReference emergency_ref = database.getReference("emergencies/" + keyToUpdate + "/state");
                                        emergency_ref.setValue("1");

                                        DatabaseReference ref = database.getReference("emergencies/" + keyToUpdate);
                                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                                                DatabaseReference efar_ref = database.getReference("emergencies/" + keyToUpdate + "/responding_efar");
                                                String other_efars = dataSnapshot.child("responding_efar").getValue().toString();
                                                String new_id_set = other_efars + ", " + sharedPreferences.getString("id", "");
                                                efar_ref.setValue(new_id_set);
                                                respondButton.setVisibility(View.GONE);
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                // None
                                            }
                                        });

                                        setUpButtons(key, time, "1");
                                    }

                                })
                                .setNegativeButton("No", null)
                                .show();
                    }
                });
            }else{
                respondButton.setVisibility(View.GONE);
            }
        }

        if(state.equals("0")){
            endButton.setText("");
            endButton.setVisibility(View.GONE);
        }else{
            endButton.setVisibility(View.VISIBLE);
            endButton.setText("End Emergency");
            endButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("finished_emergency_key", key);
                    editor.putString("finished_emergency_date", time);
                    editor.commit();
                    launchEfarWriteUpScreen();
                }
            });
        }
    }

    private class EmergnecyInfoCustomAdapter extends BaseAdapter {

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
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View cell = inflater.inflate(R.layout.emergecy_info_cell, parent, false);

            Bundle bundle = getIntent().getExtras();
            final String time = bundle.getString("time");
            final String latitude = bundle.getString("lat");
            final String longitude = bundle.getString("long");
            final String address = bundle.getString("address");
            final String phoneNumber = bundle.getString("phoneNumber");
            final String info = bundle.getString("info");
            final String id = bundle.getString("id");

            TextView timeText = (TextView) cell.findViewById(R.id.createdTextView);
            TextView locationText = (TextView) cell.findViewById(R.id.locationTextView);
            TextView addressText = (TextView) cell.findViewById(R.id.AddressTextView);
            TextView phoneNumberText = (TextView) cell.findViewById(R.id.numberTextView);
            TextView infoText = (TextView) cell.findViewById(R.id.infoTextView);
            TextView idText = (TextView) cell.findViewById(R.id.IdTextView);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date timeCreated = null;
            try {
                timeCreated = simpleDateFormat.parse(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            SimpleDateFormat displayTimeFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
            String dipslayTime = displayTimeFormat.format(timeCreated);

            String phoneLink = "tel:" + phoneNumber.replaceAll("[^\\d.]", "");

            SpannableString locationTextSpan = new SpannableString("Location: <br><a href=" + "http://maps.google.com/?q=" + latitude + "," + longitude + ">("
                    + String.format("%.2f", Float.parseFloat(latitude)) + ", " + String.format("%.2f", Float.parseFloat(longitude)) + ")</a>");

            SpannableString addressTextSpan = new SpannableString("Address: <br><a href=" + "http://maps.google.com/?q=" + latitude + "," + longitude + ">" + address + "</a>");

            SpannableString phoneTextSpan = new SpannableString("Patient's #: <br><a href=" + phoneLink + ">" + phoneNumber + "</a>");

            if (Build.VERSION.SDK_INT >= 24) {
                // for 24 api and more
                locationTextSpan = SpannableString.valueOf(Html.fromHtml(String.valueOf(locationTextSpan), 0));
                addressTextSpan = SpannableString.valueOf(Html.fromHtml(String.valueOf(addressTextSpan), 0));
                phoneTextSpan = SpannableString.valueOf(Html.fromHtml(String.valueOf(phoneTextSpan), 0));
            } else {
                // or for older api
                locationTextSpan = SpannableString.valueOf(Html.fromHtml(String.valueOf(locationTextSpan)));
                addressTextSpan = SpannableString.valueOf(Html.fromHtml(String.valueOf(addressTextSpan)));
                phoneTextSpan = SpannableString.valueOf(Html.fromHtml(String.valueOf(phoneTextSpan)));
            }

            timeText.setText("Created: " + dipslayTime);
            locationText.setText(locationTextSpan);
            //to make the link clickable in the textview
            locationText.setMovementMethod(LinkMovementMethod.getInstance());
            addressText.setText(addressTextSpan);
            addressText.setMovementMethod(LinkMovementMethod.getInstance());
            phoneNumberText.setText(phoneTextSpan);
            phoneNumberText.setMovementMethod(LinkMovementMethod.getInstance());
            infoText.setText("Info Given: \n" + info);
            if(!id.equals("") && !id.equals("N/A")) {
                idText.setText("Responder ID(s): " + id);
            }else{
                idText.setText("");
            }

            return cell;
        }
    }

    // Goes to patient info tab to send more to EFARs
    private void launchEfarMain() {
        Intent tolaunchEfarMain = new Intent(this, EFARMainActivity.class);
        startActivity(tolaunchEfarMain);
        finish();
    }

    // Starts up launchEfarWriteUpScreen screen
    private void launchEfarWriteUpScreen() {
        Intent toLaunchEfarWriteUPScreen = new Intent(this, EFARInfoActivity.class);
        startActivity(toLaunchEfarWriteUPScreen);
    }

    // Goes to patient info tab to send more to EFARs
    private void launchMessagingScreen() {
        Intent launchMessagingScreen = new Intent(this, MessagingScreenActivity.class);
        startActivity(launchMessagingScreen);
    }
}
