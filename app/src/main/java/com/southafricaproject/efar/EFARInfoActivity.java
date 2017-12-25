package com.southafricaproject.efar;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        ListView writeUpListView = (ListView) findViewById(R.id.writeUpListView);
        Adapter writeUpAdapter = new WriteUpCustomAdapter();
        writeUpListView.setAdapter((ListAdapter) writeUpAdapter);

        /*final EditText efar_writeUp_text = (EditText) findViewById(R.id.efar_writeup_editText);
        efar_writeUp_text.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        Button submitButton = (Button) findViewById(R.id.efar_info_submit_button);

        //TODO: make is so they connot submit an empty report...have a minimum report length?
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
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                String timestamp = simpleDateFormat.format(currentTime);
                emergency_ref.child("/ended_date").setValue(timestamp);
                Date e_creation_date = null;
                try {
                    e_creation_date = simpleDateFormat.parse(finished_emergency_date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                emergency_ref.child("/elapsed_time").setValue(currentTime.getTime() - e_creation_date.getTime());
                emergency_ref.child("/write_up").setValue(efar_writeUp_text.getText().toString());
                moveFirebaseRecord(emergency_ref, database.getReference("completed/" + finished_emergency_key));
                emergency_ref.removeValue();
                finish();
            }
        });*/
    }

    private class WriteUpCustomAdapter extends BaseAdapter {

        @Override //responsible for the number of rows in the list
        public int getCount() {

            return 1;
        }

        @Override
        public long getItemId(int position) {
            return  position;
        }

        @Override
        public Object getItem(int position) {
            return "test String";
        }

        @Override //renders out each row
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View cell = inflater.inflate(R.layout.writeup_full, parent, false);

            //TODO: make it so you can either select a radio button or type other.

            Button submitReportButton = (Button) cell.findViewById(R.id.comments_writeup).findViewById(R.id.submit_report_button);

            submitReportButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                    String finished_emergency_key = sharedPreferences.getString("finished_emergency_key", "");
                    String finished_emergency_date = sharedPreferences.getString("finished_emergency_date", "");
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference emergency_ref = database.getReference("emergencies/" + finished_emergency_key);
                    emergency_ref.child("/state").setValue("2");
                    Date currentTime = Calendar.getInstance().getTime();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    String timestamp = simpleDateFormat.format(currentTime);
                    emergency_ref.child("/ended_date").setValue(timestamp);
                    Date e_creation_date = null;
                    try {
                        e_creation_date = simpleDateFormat.parse(finished_emergency_date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    emergency_ref.child("/elapsed_time").setValue(currentTime.getTime() - e_creation_date.getTime());

                    TextView commentTextView = (TextView) cell.findViewById(R.id.comments_writeup).findViewById(R.id.commentEditText);
                    emergency_ref.child("/comments").setValue(commentTextView.getText().toString());

                    moveFirebaseRecord(emergency_ref, database.getReference("completed/" + finished_emergency_key));
                    emergency_ref.removeValue();
                    finish();

                    Toast.makeText(EFARInfoActivity.this,
                            "Report Sent", Toast.LENGTH_LONG).show();
                }
            });
            /*if(position == 0){
                cell = inflater.inflate(R.layout.title_writeup, parent, false);
            } else if(position == 1){
                 cell = inflater.inflate(R.layout.patient_detail_writeup, parent, false);
            } else if(position == 2){
                cell = inflater.inflate(R.layout.incident_details_writeup, parent, false);
            } else if(position == 3){
                cell = inflater.inflate(R.layout.injury_details_writeup, parent, false);
            } else if(position == 4){
                cell = inflater.inflate(R.layout.treatment_details_writeup, parent, false);
            } else if(position == 5){
                cell = inflater.inflate(R.layout.comments_writeup, parent, false);
            }*/
            return cell;
        }
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
