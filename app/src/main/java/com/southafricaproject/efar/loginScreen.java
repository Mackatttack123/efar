package com.southafricaproject.efar;

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

        submitButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference userRef = database.getReference("users");

                        final String name = user_name.getText().toString();
                        final String id = user_id.getText().toString();
                        if (name.equals("") || id.equals("")) {
                            Log.wtf("Login", "No data input. Cannot attempt login");
                        }
                        else{
                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    // Check if id number match

                                    if (snapshot.hasChild(id)) {
                                        // check if name matches id
                                        String check_name = snapshot.child(id + "/name").getValue().toString();

                                        if (check_name.equals(name)) {
                                            //if all matches then go onto the efar screen
                                            finish();
                                            launchEfarScreen();
                                        }
                                    } else {
                                        Log.wtf("Login", "FAILURE!");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError firebaseError) {
                                }
                            });
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

}

