package com.southafricaproject.efar;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ActivityMessagingScreen extends AppCompatActivity {

    final ArrayList<Message> messageArray = new ArrayList<Message>();

    /* for constant listview updating every few seconds */
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

        adapter = new ArrayAdapter<Message>(this, R.layout.activity_listview, messageArray) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                String name = sharedPreferences.getString("name", "");
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View left_cell = inflater.inflate(R.layout.message_cell_left, parent, false);

                LayoutInflater inflater2 = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View right_cell = inflater2.inflate(R.layout.message_cell_right, parent, false);

                TextView textMessage_left = (TextView)left_cell.findViewById(R.id.txt_msg);
                TextView name_left = (TextView)left_cell.findViewById(R.id.nameTextView_left);
                TextView time_left = (TextView)left_cell.findViewById(R.id.timeTextView_left);
                ConstraintLayout left_message_background = (ConstraintLayout)left_cell.findViewById(R.id.left_message_backgroud);

                TextView textMessage_right = (TextView)right_cell.findViewById(R.id.txt_msg);
                TextView name_right = (TextView)right_cell.findViewById(R.id.nameTextView_right);
                TextView time_right = (TextView)right_cell.findViewById(R.id.timeTextView_right);
                ConstraintLayout right_message_backgroud = (ConstraintLayout)right_cell.findViewById(R.id.right_message_backgroud);

                String timestamp = messageArray.get(position).getTimestamp().toString();
                String user_name = messageArray.get(position).getName().toString();
                String message = messageArray.get(position).getMessage().toString();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date timeCreated = null;
                try {
                    timeCreated = simpleDateFormat.parse(timestamp);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                SimpleDateFormat displayTimeFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
                String dipslayTime = displayTimeFormat.format(timeCreated);

                if (user_name.contains("ALERT")) {
                    textMessage_right.setText(message);
                    name_right.setText(user_name);
                    if(dipslayTime != null){
                        time_right.setText(dipslayTime);
                    }
                    if(message.contains("EMERGENCY ARE IS NOW SAFE.")){
                        right_message_backgroud.setBackgroundColor(Color.argb(255, 0, 170, 0));
                    }else {
                        right_message_backgroud.setBackgroundColor(Color.argb(255, 200, 0, 0));
                    }
                    return right_cell;
                }else if (user_name.contains(name)) {
                    textMessage_right.setText(message);
                    name_right.setText(user_name);
                    if(dipslayTime != null){
                        time_right.setText(dipslayTime);
                    }
                    right_message_backgroud.setBackgroundColor(Color.argb(255, 2, 55, 98));
                    return right_cell;
                } else {
                    name_left.setText(user_name);
                    if(dipslayTime != null){
                        time_left.setText(dipslayTime);
                    }
                    textMessage_left.setText(message);
                    left_message_background.setBackgroundColor(Color.argb(255, 170, 170, 170));
                    return left_cell;
                }
            }
        };

        listView = (ListView) findViewById(R.id.message_list);
        listView.setAdapter(adapter);
        listView.setClickable(true);

        FirebaseDatabase.getInstance().getReference("emergencies/" + key).child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {
                    displayMessages(dataSnapshot);
                } catch (NullPointerException e) {
                    Log.wtf("added", "not yet");
                }

                listView.setVisibility(View.VISIBLE);
                listView.setSelection(adapter.getCount() - 1);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                try {
                    displayMessages(dataSnapshot);
                } catch (NullPointerException e) {
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
        final EditText message_to_send = (EditText) findViewById(R.id.message_text_view);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                String name = sharedPreferences.getString("name", "");
                try {
                    if (!message_to_send.getText().toString().equals("")) {
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
        data.put("user", name);
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String timestamp = simpleDateFormat.format(currentTime);
        data.put("timestamp", timestamp);
        data.put("message", message.trim());
        messsage_ref.child(message_key.getKey()).setValue(data);
    }

    private void displayMessages(DataSnapshot dataSnapshot) {
        String name = dataSnapshot.child("user").getValue().toString();
        String message = dataSnapshot.child("message").getValue().toString();
        String timestamp = dataSnapshot.child("timestamp").getValue().toString();

        Message msg_to_add = new Message(name, timestamp, message);

        messageArray.add(msg_to_add);
        adapter.notifyDataSetChanged();

    }

    //disables the werid transition beteen activities
    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

}
