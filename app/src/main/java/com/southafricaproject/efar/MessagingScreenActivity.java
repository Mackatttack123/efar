package com.southafricaproject.efar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MessagingScreenActivity extends AppCompatActivity {

    final ArrayList<String> messageArray = new ArrayList<String>();

    /* for constant listview updating every few seconds */
    private Handler handler = new Handler();
    public ArrayAdapter adapter;
    public ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging_screen);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, messageArray){
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                String name = sharedPreferences.getString("name", "");

                View itemView = super.getView(position, convertView, parent);
                if (messageArray.get(position).startsWith(name)){
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

        listView.setBackgroundColor(Color.TRANSPARENT);

        FirebaseDatabase.getInstance().getReference().child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try{
                    String user = dataSnapshot.child("user").getValue().toString();
                    String message = dataSnapshot.child("message").getValue().toString();
                    messageArray.add(user + ": " + message);
                    adapter.notifyDataSetChanged();
                }catch (NullPointerException e){
                    Log.wtf("added", "not yet");
                }

                listView.setVisibility(View.VISIBLE);
                listView.setBackgroundColor(Color.WHITE);
                listView.setSelection(adapter.getCount() - 1);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                try{
                    String user = dataSnapshot.child("user").getValue().toString();
                    String message = dataSnapshot.child("message").getValue().toString();
                    messageArray.add(user + ": " + message);
                    listView.setSelection(adapter.getCount() - 1);
                    adapter.notifyDataSetChanged();
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
                    add_message(name.toString(), message_to_send.getText().toString());
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
    }

    private void add_message(String name, String message) throws JSONException {
        // Create new emergency in the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference messsage_ref = database.getReference("messages");
        DatabaseReference message_key = messsage_ref.push();


        Map<String, String> data = new HashMap<String, String>();
        data.put("user",name);
        data.put("message",message);
        messsage_ref.child(message_key.getKey()).setValue(data);
    }
}
