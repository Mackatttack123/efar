package com.southafricaproject.efar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class EFARMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_efarmain);

        ArrayList<String> patientArray = new ArrayList<String>();
        patientArray.add("item1");
        patientArray.add("item2");
        patientArray.add("fugly bob");
        patientArray.add("Nelson");

        //instantiate custom adapter
//        MyCustomAdapter adapter = new MyCustomAdapter(patientArray, this);

        //handle listview and assign adapter
//        ListView lView = (ListView)findViewById(R.id.patient_list);
//        lView.setAdapter(adapter);


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
