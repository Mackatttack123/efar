package com.southafricaproject.efar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

        //check database connection
        /*DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (!connected) {
                    new AlertDialog.Builder(MessagingScreenActivity.this)
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

        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
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
                launchEfarMain();
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
    }

    // Goes to patient info tab to send more to EFARs
    private void launchEfarMain() {
        Intent tolaunchEfarMain = new Intent(this, EFARMainActivity.class);
        startActivity(tolaunchEfarMain);
        finish();
    }

    private void add_message(String name, String message) throws JSONException {
        // Create new emergency in the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference messsage_ref = database.getReference("emergencies/" + key + "/messages");
        DatabaseReference message_key = messsage_ref.push();


        Map<String, String> data = new HashMap<String, String>();
        data.put("user",name);
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
}
