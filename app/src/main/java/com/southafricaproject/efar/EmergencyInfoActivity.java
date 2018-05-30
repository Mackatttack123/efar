package com.southafricaproject.efar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.Random;

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

        final FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();

        //check connection
        try {
            ConnectivityManager cm = (ConnectivityManager) this
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            NetworkInfo mWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if ((networkInfo != null && networkInfo.isConnected()) || mWifi.isConnected()) {

            }else{
                new AlertDialog.Builder(EmergencyInfoActivity.this)
                        .setTitle("Connection Error:")
                        .setMessage("Your device is currently unable connect to our services. " +
                                "Please check your connection or try again later.")
                        .show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            new AlertDialog.Builder(EmergencyInfoActivity.this)
                    .setTitle("Connection Error:")
                    .setMessage("Your device is currently unable connect to our services. " +
                            "Please check your connection or try again later.")
                    .show();
        }

        //check if an update is needed
        FirebaseDatabase.getInstance().getReference().child("version").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String current_version = snapshot.child("version_number").getValue().toString();
                if(!current_version.equals(BuildConfig.VERSION_NAME)){
                    AlertDialog.Builder alert = new AlertDialog.Builder(EmergencyInfoActivity.this)
                            .setTitle("Update Needed:")
                            .setMessage("Please updated to the the latest version of our app.").setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                    } catch (android.content.ActivityNotFoundException anfe) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                    }
                                    finish();
                                    startActivity(getIntent());
                                }
                            }).setNegativeButton("Exit App", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finishAndRemoveTask();
                                }
                            }).setCancelable(false);
                    if(!((Activity) EmergencyInfoActivity.this).isFinishing())
                    {
                        alert.show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String id = sharedPreferences.getString("id", "");
        boolean efar_logged_in = sharedPreferences.getBoolean("logged_in", false);
        final String token = FirebaseInstanceId.getInstance().getToken();
        //check if an logged in on another phone
        if(efar_logged_in){
            FirebaseDatabase.getInstance().getReference().child("users/" + id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String current_token= snapshot.child("token").getValue().toString();
                    if(!token.equals(current_token)){
                        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(EmergencyInfoActivity.this)
                                .setTitle("Oops!")
                                .setMessage("Looks liked you're logged in on another device. You will now be logged out but you can log back onto this device if you'd like.").setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //to get rid of stored password and username
                                        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();

                                        // say that user has logged off
                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        DatabaseReference userRef = database.getReference("users");
                                        editor.putString("id", "");
                                        editor.putString("name", "");
                                        editor.putBoolean("logged_in", false);
                                        stopService(new Intent(EmergencyInfoActivity.this, MyService.class));
                                        editor.apply();

                                        //clear the phones token for the database
                                        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                                        DatabaseReference token_ref = database.getReference("tokens/" + refreshedToken);
                                        token_ref.removeValue();

                                        if(mAuth.getCurrentUser() != null){
                                            mAuth.getCurrentUser().delete();
                                        }
                                        finish();
                                        startActivity(getIntent());
                                    }
                                }).setCancelable(false);
                        if(!((Activity) EmergencyInfoActivity.this).isFinishing())
                        {
                            alert.show();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        ListView emergencyInfoListView = (ListView) findViewById(R.id.emergencyInfoListView);
        Adapter emergencyInfoAdapter = new EmergencyInfoActivity.EmergnecyInfoCustomAdapter();
        emergencyInfoListView.setAdapter((ListAdapter) emergencyInfoAdapter);

        Bundle bundle = getIntent().getExtras();
        final String time = bundle.getString("time");
        final String key = bundle.getString("key");
        final String state  = bundle.getString("state");

        setUpButtons(key, time, state);

        //check if the emergency is still in the database
        FirebaseDatabase.getInstance().getReference().child("emergencies/").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                launchEfarMain();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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

        final Button messageButton = (Button) findViewById(R.id.messagesButton);

        final Button endButton = (Button) findViewById(R.id.endButton);

        final Button respondButton = (Button) findViewById(R.id.respondButton);
        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String efar_id = sharedPreferences.getString("id", "");
        Bundle bundle = getIntent().getExtras();
        final String responding_ids = bundle.getString("id");
        if(state.equals("0")|| !responding_ids.contains(efar_id)){
            if(state.equals("1") || state.equals("1.5")){
                messageButton.setVisibility(View.VISIBLE);
                messageButton.setText("Message EFARs");
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
            }else {
                messageButton.setVisibility(View.GONE);
            }
            endButton.setVisibility(View.GONE);
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
                                    if(!state.equals("1.5")) {
                                        DatabaseReference emergency_ref = database.getReference("emergencies/" + keyToUpdate + "/state");
                                        emergency_ref.setValue("1");
                                    }
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
                                            }else{
                                                efar_ref.setValue(sharedPreferences.getString("id", ""));
                                            }
                                            respondButton.setVisibility(View.GONE);
                                            messageButton.setVisibility(View.VISIBLE);
                                            messageButton.setText("Message EFARs");
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
                                            if(state.equals("1.5")){
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
                                            }else{
                                                endButton.setVisibility(View.VISIBLE);
                                                endButton.setText("On Scene");
                                                endButton.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {

                                                        AlertDialog.Builder alert = new AlertDialog.Builder(EmergencyInfoActivity.this);
                                                        final EditText edittext = new EditText(EmergencyInfoActivity.this);
                                                        final ViewGroup.LayoutParams lparams = new ViewGroup.LayoutParams(50, 30);
                                                        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                                                        final String efar_id = sharedPreferences.getString("id", "");
                                                        edittext.setLayoutParams(lparams);
                                                        alert.setMessage("What do you see?");
                                                        alert.setTitle("You are on scene");
                                                        alert.setCancelable(false);
                                                        alert.setView(edittext);

                                                        alert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int whichButton) {

                                                                String comment = edittext.getText().toString();

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
                                                        });

                                                        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                            }
                                                        });
                                                        alert.show();

                                                    }
                                                });
                                            }

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
            });
        }else{
            respondButton.setVisibility(View.GONE);
            messageButton.setVisibility(View.VISIBLE);
            messageButton.setText("Message EFARs");
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
            if(state.equals("1.5")){
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
            }else{
                endButton.setVisibility(View.VISIBLE);
                endButton.setText("On Scene");
                endButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        AlertDialog.Builder alert = new AlertDialog.Builder(EmergencyInfoActivity.this);
                        final EditText edittext = new EditText(EmergencyInfoActivity.this);
                        final ViewGroup.LayoutParams lparams = new ViewGroup.LayoutParams(50, 30);
                        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                        final String efar_id = sharedPreferences.getString("id", "");
                        edittext.setLayoutParams(lparams);
                        alert.setMessage("What do you see?");
                        alert.setTitle("You are on scene");
                        alert.setCancelable(false);
                        alert.setView(edittext);

                        alert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                String comment = edittext.getText().toString();

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
                        });

                        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        });
                        alert.show();

                    }
                });
            }
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
            String phoneNumber = bundle.getString("phoneNumber");
            final String info = bundle.getString("info");
            final String id = bundle.getString("id");

            TextView timeText = (TextView) cell.findViewById(R.id.createdTextView);
            TextView addressText = (TextView) cell.findViewById(R.id.AddressTextView);
            TextView phoneNumberText = (TextView) cell.findViewById(R.id.numberTextView);
            TextView infoText = (TextView) cell.findViewById(R.id.infoTextView);
            TextView idText = (TextView) cell.findViewById(R.id.IdTextView);
            final ImageButton call_button = (ImageButton) cell.findViewById(R.id.call_button);
            final ImageButton to_maps_button = (ImageButton) cell.findViewById(R.id.to_maps_button);

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
            String dipslayTime = displayTimeFormat.format(timeCreated);

            SpannableString infoTextSpan = new SpannableString("<strong>Information Given: </strong><br>" + info);

            SpannableString responderTextSpan = new SpannableString("<strong>Responder ID(s): </strong><br>" + id);

            SpannableString createdTextSpan = new SpannableString("<strong>Created: </strong><br>" + dipslayTime);

            SpannableString addressTextSpan = new SpannableString("<strong>Incident Address: </strong><br>" + address);

            String formattedNumber = phoneNumber;

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


            SpannableString phoneTextSpan = new SpannableString("<strong>Contact Number: </strong><br>" + formattedNumber);

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
            if(!id.equals("") && !id.equals("N/A")) {
                idText.setText(responderTextSpan);
            }else{
                idText.setText("");
            }

            return cell;
        }

    }

    // Goes to patient info tab to send more to EFARs
    private void launchEfarMain() {
        Intent tolaunchEfarMain = new Intent(this, EFARMainActivityTabbed.class);
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
