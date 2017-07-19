package com.southafricaproject.efar;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//TODO: Make it so that if an EFAR logs in then there password and username is saved
// that way they are automatically logged in when they iopen the app

public class loginScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        //button to get back to patient screen
        Button backButton = (Button) findViewById(R.id.login_back_button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button submitButton = (Button) findViewById(R.id.login_submit_button);

        final EditText user_name = (EditText) findViewById(R.id.login_name_field);
        final EditText user_id = (EditText) findViewById(R.id.login_id_field);

        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String old_id = sharedPreferences.getString("old_id", "");
        String old_name = sharedPreferences.getString("old_name", "");

        if(old_id != "" && old_name != ""){
            user_name.setText(old_name);
            user_id.setText(old_id);
        }

        // logic for the login submit button
        submitButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

                        final String name = user_name.getText().toString();
                        final String id = user_id.getText().toString();
                        // make sure there is some data so app doesn't crash
                        if (name.equals("") || id.equals("")) {
                            Log.wtf("Login", "No data input. Cannot attempt login");
                        } else {
                            // check and validate the user
                            checkUser(name, id);
                        }
                    }
                }
        );

    }

    // Starts up launchEfarScreen screen
    private void launchEfarScreen() {

        Intent toEfarScreen = new Intent(this, EFARMainActivity.class);

        startActivity(toEfarScreen);
    }

    // checks to see if  a user exists in the database
    private void checkUser(String user_name, String user_id) {

        final String name = user_name;
        final String id = user_id;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users");

        userRef.addListenerForSingleValueEvent(new

        ValueEventListener() {
            @Override
            public void onDataChange (DataSnapshot snapshot){
                // Check if id number is in database

                if (snapshot.hasChild(id)) {
                    // check if name matches id in database
                    String check_name = snapshot.child(id + "/name").getValue().toString();

                    if (check_name.equals(name)) {
                        // store password and username for auto login
                        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("id", id);
                        editor.putString("name", name);
                        editor.putString("old_id", id);
                        editor.putString("old_name", name);
                        editor.commit();
                        //if all matches then go onto the efar screen
                        finish();
                        launchEfarScreen();
                    } else {
                        //TODO: tell user they have the wrong name or id
                    }
                } else {
                    Log.wtf("Login", "FAILURE!");
                }
            }

            @Override
            public void onCancelled (DatabaseError firebaseError){
            }
            }

        );
    }

}


