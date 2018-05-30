package com.southafricaproject.efar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MessagingScreenActivity extends AppCompatActivity {

    final ArrayList<SpannableString> messageArray = new ArrayList<SpannableString>();
    final ArrayList<SpannableString> HTMLmessageArray = new ArrayList<SpannableString>();

    /* for constant listview updating every few seconds */
    private Handler handler = new Handler();
    public ArrayAdapter adapter;
    public ListView listView;

    public String key = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging_screen);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

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
                new AlertDialog.Builder(MessagingScreenActivity.this)
                        .setTitle("Connection Error:")
                        .setMessage("Your device is currently unable connect to our services. " +
                                "Please check your connection or try again later.")
                        .show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            new AlertDialog.Builder(MessagingScreenActivity.this)
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
                    AlertDialog.Builder alert = new AlertDialog.Builder(MessagingScreenActivity.this)
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
                    if(!((Activity) MessagingScreenActivity.this).isFinishing())
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
                        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(MessagingScreenActivity.this)
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
                                        stopService(new Intent(MessagingScreenActivity.this, MyService.class));
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
                        if(!((Activity) MessagingScreenActivity.this).isFinishing())
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

        key = sharedPreferences.getString("messaging_key", "");

        adapter = new ArrayAdapter<SpannableString>(this, R.layout.activity_listview, messageArray){
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                String name = sharedPreferences.getString("name", "");

                View itemView = super.getView(position, convertView, parent);
                if (messageArray.get(position).toString().startsWith(name)){
                    itemView.setBackgroundColor(Color.argb(100, 0, 200, 0));
                    //itemView.setBackgroundResource(R.drawable.efar_logo_white);
                }else{
                    itemView.setBackgroundColor(Color.argb(100, 0, 80, 250));
                }
                return itemView;
            }
        };

        listView = (ListView) findViewById(R.id.message_list);
        listView.setAdapter(adapter);
        listView.setClickable(true);

        FirebaseDatabase.getInstance().getReference("emergencies/" + key).child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try{
                    displayMessages(dataSnapshot);
                }catch (NullPointerException e){
                    Log.wtf("added", "not yet");
                }

                listView.setVisibility(View.VISIBLE);
                listView.setSelection(adapter.getCount() - 1);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                try{
                    displayMessages(dataSnapshot);
                }catch (NullPointerException e){
                    Log.wtf("added", "not yet");
                }

                listView.setVisibility(View.VISIBLE);
                listView.setSelection(adapter.getCount() - 1);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //button to get back to patient screen
        Button backButton = (Button) findViewById(R.id.back_button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //button to get back to patient screen
        Button sendButton = (Button) findViewById(R.id.send_message_button);
        final EditText message_to_send =  (EditText) findViewById(R.id.message_text_view);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                String name = sharedPreferences.getString("name", "");
                try {
                    if(!message_to_send.getText().toString().equals("")){
                        add_message(name.toString(), message_to_send.getText().toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                message_to_send.setText("");
            }
        });

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

    private void add_message(String name, String message) throws JSONException {
        // Create new emergency in the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference messsage_ref = database.getReference("emergencies/" + key + "/messages");
        DatabaseReference message_key = messsage_ref.push();


        Map<String, String> data = new HashMap<String, String>();
        data.put("user",name);
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String timestamp = simpleDateFormat.format(currentTime);
        data.put("timestamp",timestamp);
        data.put("message",message.trim());
        messsage_ref.child(message_key.getKey()).setValue(data);
    }
    private void displayMessages(DataSnapshot dataSnapshot){
        String user = dataSnapshot.child("user").getValue().toString();
        String message = dataSnapshot.child("message").getValue().toString();
        SpannableString display_message;
        int messageArray_size = messageArray.size() - 1;
        int HTMLmessageArray_size = HTMLmessageArray.size() - 1;
        if(messageArray.size() > 0){
            if(String.valueOf(HTMLmessageArray.get(HTMLmessageArray_size)).startsWith("<i><u>" + user + ":</u></i>")){
                String last_massage = HTMLmessageArray.get(HTMLmessageArray_size).toString().replace("<i><u>" + user + ":</u></i>", "");
                display_message = new SpannableString("<i><u>" + user + ":</u></i>" + last_massage + "<br>" + message);
                messageArray.remove(messageArray_size);
            }else {
                display_message = new SpannableString("<i><u>" + user + ":</u></i><br>" + message);
            }
        }else{
            display_message = new SpannableString("<i><u>" + user + ":</u></i><br>" + message);
        }

        HTMLmessageArray.add(display_message);
        if (Build.VERSION.SDK_INT >= 24) {
            display_message = SpannableString.valueOf(Html.fromHtml(String.valueOf(display_message), 0)); // for 24 api and more
        } else {
            display_message = SpannableString.valueOf(Html.fromHtml(String.valueOf(display_message))); // or for older api
        }

        messageArray.add(display_message);
        adapter.notifyDataSetChanged();

    }

    //disables the werid transition beteen activities
    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    // Goes to patient info tab to send more to EFARs
    private void launchEfarMain() {
        Intent tolaunchEfarMain = new Intent(this, EFARMainActivityTabbed.class);
        startActivity(tolaunchEfarMain);
        finish();
    }
}
