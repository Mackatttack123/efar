package com.southafricaproject.efar;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EFARInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_efarinfo);

        final EditText efar_writeUp_text = (EditText) findViewById(R.id.efar_writeup_editText);
        efar_writeUp_text.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        Button submitButton = (Button) findViewById(R.id.efar_info_submit_button);

        //TODO: make is so they connot submit an empty report..have a minimum report length?
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                String finished_emergency_key = sharedPreferences.getString("finished_emergency_key", "");
                String finished_emergency_date = sharedPreferences.getString("finished_emergency_date", "");
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference emergency_ref = database.getReference("emergencies/" + finished_emergency_key);
                emergency_ref.child("/state").setValue("2");
                Date currentTime = Calendar.getInstance().getTime();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZZZZZ");
                String timestamp = simpleDateFormat.format(currentTime);
                emergency_ref.child("/ended_date").setValue(timestamp);
                Date e_creation_date = null;
                try {
                    e_creation_date = simpleDateFormat.parse(finished_emergency_date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                emergency_ref.child("/elapsed_time_in_milliseconds").setValue(currentTime.getTime() - e_creation_date.getTime());
                emergency_ref.child("/write_up").setValue(efar_writeUp_text.getText().toString());
                moveFirebaseRecord(emergency_ref, database.getReference("completed/" + finished_emergency_key));
                emergency_ref.removeValue();
                finish();
            }
        });
    }

    //TODO: I don't think this works... so check of fix it
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void moveFirebaseRecord(DatabaseReference fromPath, final DatabaseReference toPath)
    {
        fromPath.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                toPath.setValue(dataSnapshot.getValue(), new DatabaseReference.CompletionListener()
                {
                    @Override
                    public void onComplete(DatabaseError firebaseError, DatabaseReference firebase)
                    {
                        if (firebaseError != null)
                        {
                            System.out.println("Copy failed");
                        }
                        else
                        {
                            System.out.println("Success");
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError firebaseError)
            {
                System.out.println("Copy failed");
            }
        });
    }

}