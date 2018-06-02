package com.southafricaproject.efar;

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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ActivityMessagingScreen extends AppCompatActivity {

    final ArrayList<SpannableString> messageArray = new ArrayList<SpannableString>();
    final ArrayList<SpannableString> HTMLmessageArray = new ArrayList<SpannableString>();

    /* for constant listview updating every few seconds */
    private Handler handler = new Handler();
    public ArrayAdapter adapter;
    public ListView listView;

    public String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging_screen);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        //check network connection
        //check if a forced app update is needed
        //check if an logged in on another phone
        CheckFunctions.runAllAppChecks(ActivityMessagingScreen.this, this);

        //check if the emergency is still in the database and kick efar out of activity and back to main if it is not
        Bundle bundle = getIntent().getExtras();
        key = bundle.getString("key");
        CheckFunctions.checkIfEmergencyInDatabase(ActivityMessagingScreen.this, this, key);

        adapter = new ArrayAdapter<SpannableString>(this, R.layout.activity_listview, messageArray){
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                String name = sharedPreferences.getString("name", "");

                View itemView = super.getView(position, convertView, parent);
                if (messageArray.get(position).toString().startsWith(name)){
                    itemView.setBackgroundColor(Color.argb(100, 0, 200, 0));
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
}
