package com.southafricaproject.efar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class EFARMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_efarmain);

        //TODO: make and emergency array that store an emergancy struct (which you need to make)
        //TODO: When you click on the emergency it will show you it in detail
        //TODO: display distance using cordinates in the arrayview for the EFARS
        final ArrayList<String> patientArray = new ArrayList<String>();

        // go through all the emergencies and put there data in the array
        FirebaseDatabase.getInstance().getReference().child("emergencies")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            patientArray.add("Phone: " + snapshot.child("phone_number").getValue().toString());
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });


        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_listview, patientArray);

        ListView listView = (ListView) findViewById(R.id.patient_list);
        listView.setAdapter(adapter);

        //button to get back to patient screen
        Button logoutButton = (Button) findViewById(R.id.logout_button);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
            }
        });
    }
}
