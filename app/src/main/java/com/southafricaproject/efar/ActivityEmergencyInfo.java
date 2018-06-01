package com.southafricaproject.efar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.content.DialogInterface;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ActivityEmergencyInfo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_info);

        //check network connection
        //check if a forced app update is needed
        //check if an logged in on another phone
        CheckFunctions.runAllAppChecks(ActivityEmergencyInfo.this, this);

        Bundle bundle = getIntent().getExtras();
        final String time = bundle.getString("time");
        final String key = bundle.getString("key");
        final String state  = bundle.getString("state");

        //check if the emergency is still in the database and kick efar out of activity and back to main if it is not
        CheckFunctions.checkIfEmergencyInDatabase(ActivityEmergencyInfo.this, this, key);

        ListView emergencyInfoListView = (ListView) findViewById(R.id.emergencyInfoListView);
        Adapter emergencyInfoAdapter = new ActivityEmergencyInfo.EmergnecyInfoCustomAdapter();
        emergencyInfoListView.setAdapter((ListAdapter) emergencyInfoAdapter);

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
                        launchMessagingScreen(key);
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
                    SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                    boolean responding_to_other = sharedPreferences.getBoolean("responding_to_other", false);
                    if(responding_to_other){
                        new AlertDialog.Builder(ActivityEmergencyInfo.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Already Responding")
                                .setMessage("You are already responding to another emergency!")
                                .setPositiveButton("Okay", null)
                                .show();
                    }else{
                        new AlertDialog.Builder(ActivityEmergencyInfo.this)
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
                                                        launchMessagingScreen(key);
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
                                                            launchEfarWriteUpScreen(key);
                                                        }
                                                    });
                                                }else{
                                                    endButton.setVisibility(View.VISIBLE);
                                                    endButton.setText("On Scene");
                                                    endButton.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {

                                                            AlertDialog.Builder alert = new AlertDialog.Builder(ActivityEmergencyInfo.this);
                                                            SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                                                            final String efar_id = sharedPreferences.getString("id", "");
                                                            LayoutInflater inflater = ActivityEmergencyInfo.this.getLayoutInflater();
                                                            View passwordView = inflater.inflate(R.layout.on_scene_form, null);
                                                            final EditText notesEditText = (EditText) passwordView.findViewById(R.id.notesEditText);
                                                            alert.setView(passwordView);
                                                            alert.setMessage("What do you see?");
                                                            alert.setTitle("You are on scene");
                                                            alert.setCancelable(false);
                                                            alert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int whichButton) {

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
                                                                            launchEfarWriteUpScreen(key);
                                                                        }
                                                                    });
                                                                }
                                                            });

                                                            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                                }
                                                            });
                                                            AlertDialog dialog = alert.create();
                                                            dialog.show();
                                                            dialog.getWindow().setBackgroundDrawableResource(R.color.light_grey);

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
                    launchMessagingScreen(key);
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
                        launchEfarWriteUpScreen(key);
                    }
                });
            }else{
                endButton.setVisibility(View.VISIBLE);
                endButton.setText("On Scene");
                endButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        AlertDialog.Builder alert = new AlertDialog.Builder(ActivityEmergencyInfo.this);
                        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                        final String efar_id = sharedPreferences.getString("id", "");
                        LayoutInflater inflater = ActivityEmergencyInfo.this.getLayoutInflater();
                        View passwordView = inflater.inflate(R.layout.on_scene_form, null);
                        final EditText notesEditText = (EditText) passwordView.findViewById(R.id.notesEditText);
                        alert.setView(passwordView);
                        alert.setMessage("What do you see?");
                        alert.setTitle("You are on scene");
                        alert.setCancelable(false);
                        alert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

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
                                        launchEfarWriteUpScreen(key);
                                    }
                                });
                            }
                        });

                        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        });
                        AlertDialog dialog = alert.create();
                        dialog.show();
                        dialog.getWindow().setBackgroundDrawableResource(R.color.light_grey);

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
            final String efar_ids = bundle.getString("id");
            final String key = bundle.getString("key");

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

            SpannableString responderTextSpan = new SpannableString("<strong>Responder ID(s): </strong><br>" + efar_ids);

            SpannableString createdTextSpan = new SpannableString("<strong>Created: </strong><br>" + dipslayTime);

            SpannableString addressTextSpan = new SpannableString("<strong>Incident Address: </strong><br>" + address);
            if(address.equals("N/A")){
                addressTextSpan = new SpannableString("<strong>Incident Location: </strong><br>(" + latitude + ", " + longitude + ")");
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
                call_button.setEnabled(false);
                call_button.setBackgroundResource(R.drawable.call_disabled);
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
            if(!efar_ids.equals("") && !efar_ids.equals("N/A")) {
                idText.setText(responderTextSpan);
            }else{
                idText.setText("");
            }

            return cell;
        }

    }

    // Goes to patient info tab to send more to EFARs
    private void launchEfarMain() {
        Intent tolaunchEfarMain = new Intent(this, ActivityEFARMainTabbed.class);
        startActivity(tolaunchEfarMain);
        finish();
    }

    // Starts up launchEfarWriteUpScreen screen
    private void launchEfarWriteUpScreen(String key) {
        Intent toLaunchEfarWriteUPScreen = new Intent(this, ActivityEFARInfo.class);
        toLaunchEfarWriteUPScreen.putExtra("key", key);
        startActivity(toLaunchEfarWriteUPScreen);
    }

    // Goes to patient info tab to send more to EFARs
    private void launchMessagingScreen(String key) {
        Intent launchMessagingScreen = new Intent(this, ActivityMessagingScreen.class);
        launchMessagingScreen.putExtra("key", key);
        startActivity(launchMessagingScreen);
    }
}
