package com.southafricaproject.efar;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

            //Box 1 radio/ other box control
            /*final EditText ageTextView = (EditText) cell.findViewById(R.id.patient_detail_writeup).findViewById(R.id.editTextAge);
            final RadioGroup ageRadioGroup = (RadioGroup) cell.findViewById(R.id.patient_detail_writeup).findViewById(R.id.radioGroupAge);
            ageRadioGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ageTextView.setText("");
                }
            });
            ageTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ageRadioGroup.clearCheck();
                }
            });*/

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

                    //Box 1: Patient Details
                    EditText nameTextView = (EditText) cell.findViewById(R.id.patient_detail_writeup).findViewById(R.id.patientNameText);
                    emergency_ref.child("/patient_details/name").setValue(nameTextView.getText().toString());
                    RadioGroup genderRadioGroup = (RadioGroup) cell.findViewById(R.id.patient_detail_writeup).findViewById(R.id.radioGroupGender);
                    // get selected radio button from radioGroup
                    int selectedId = genderRadioGroup.getCheckedRadioButtonId();
                    // find the radiobutton by returned id
                    RadioButton radioButtonGender = (RadioButton) findViewById(selectedId);
                    emergency_ref.child("/patient_details/gender").setValue(radioButtonGender.getText().toString());

                    EditText ageTextView = (EditText) cell.findViewById(R.id.patient_detail_writeup).findViewById(R.id.editTextAge);
                    if(ageTextView.getText().toString().equals("")){
                        RadioGroup ageRadioGroup = (RadioGroup) cell.findViewById(R.id.patient_detail_writeup).findViewById(R.id.radioGroupAge);
                        // get selected radio button from radioGroup
                        selectedId = ageRadioGroup.getCheckedRadioButtonId();
                        // find the radiobutton by returned id
                        RadioButton ageRadioButton = (RadioButton) findViewById(selectedId);
                        emergency_ref.child("/patient_details/age").setValue(ageRadioButton.getText().toString());
                    }else{
                        emergency_ref.child("/patient_details/age").setValue(ageTextView.getText().toString());
                    }

                    //Box 2: Incident Details
                    EditText emsTimeTextView = (EditText) cell.findViewById(R.id.incident_details_writeup).findViewById(R.id.editTextEMSTime);
                    emergency_ref.child("/incident_details/ems_or_ambulance_time").setValue(emsTimeTextView.getText().toString());
                    EditText locationTextView = (EditText) cell.findViewById(R.id.incident_details_writeup).findViewById(R.id.locationEditText);
                    emergency_ref.child("/incident_details/incident_location").setValue(locationTextView.getText().toString());
                    EditText communityTextView = (EditText) cell.findViewById(R.id.incident_details_writeup).findViewById(R.id.editTextCommunity);
                    emergency_ref.child("/incident_details/community").setValue(communityTextView.getText().toString());

                    //Box 3: Injury Details
                    EditText weaponTextView = (EditText) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.editTextWeponOther);
                    if(weaponTextView.getText().toString().equals("")){
                        RadioGroup weaponRadioGroup = (RadioGroup) cell.findViewById(R.id.patient_detail_writeup).findViewById(R.id.radioGroupWepons);
                        // get selected radio button from radioGroup
                        selectedId = weaponRadioGroup.getCheckedRadioButtonId();
                        // find the radiobutton by returned id
                        RadioButton weaponRadioButton = (RadioButton) findViewById(selectedId);
                        emergency_ref.child("/injury_details/Weapon_used").setValue(weaponRadioButton.getText().toString());
                    }else{
                        emergency_ref.child("/injury_details/Weapon_used").setValue(weaponTextView.getText().toString());
                    }
                    EditText motorVehicleTextView = (EditText) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.editTextMotorVehicleOther);
                    if(motorVehicleTextView.getText().toString().equals("")){
                        RadioGroup motorVehicleRadioGroup = (RadioGroup) cell.findViewById(R.id.patient_detail_writeup).findViewById(R.id.radioGroupMotorVehicle);
                        // get selected radio button from radioGroup
                        selectedId = motorVehicleRadioGroup.getCheckedRadioButtonId();
                        // find the radiobutton by returned id
                        RadioButton motorVehicleRadioButton = (RadioButton) findViewById(selectedId);
                        emergency_ref.child("/injury_details/motor_vehicle_accident").setValue(motorVehicleRadioButton.getText().toString());
                    }else{
                        emergency_ref.child("/injury_details/motor_vehicle_accident").setValue(motorVehicleTextView.getText().toString());
                    }
                    CheckBox poisoningCheckBox = (CheckBox) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.checkBoxPoisoning);
                    emergency_ref.child("/injury_details/medical_emergency/poisoning").setValue(poisoningCheckBox.isChecked());
                    CheckBox chestPainCheckBox = (CheckBox) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.checkBoxChestPain);
                    emergency_ref.child("/injury_details/medical_emergency/chest_pain").setValue(chestPainCheckBox.isChecked());
                    CheckBox diabetesPainCheckBox = (CheckBox) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.checkBoxDiabetes);
                    emergency_ref.child("/injury_details/medical_emergency/diabetes").setValue(diabetesPainCheckBox.isChecked());
                    CheckBox strokePainCheckBox = (CheckBox) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.checkBoxStroke);
                    emergency_ref.child("/injury_details/medical_emergency/stroke").setValue(strokePainCheckBox.isChecked());
                    CheckBox epilepsyPainCheckBox = (CheckBox) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.checkBoxEpilepsy);
                    emergency_ref.child("/injury_details/medical_emergency/epilepsy").setValue(epilepsyPainCheckBox.isChecked());
                    CheckBox dehydratedPainCheckBox = (CheckBox) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.checkBoxDehydrated);
                    emergency_ref.child("/injury_details/medical_emergency/dehydrated").setValue(dehydratedPainCheckBox.isChecked());
                    CheckBox difficultyBreathingCheckBox = (CheckBox) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.checkBoxDifficultyBreathing);
                    emergency_ref.child("/injury_details/medical_emergency/difficulty_breathing").setValue(difficultyBreathingCheckBox.isChecked());
                    CheckBox abdominalPainCheckBox = (CheckBox) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.checkBoxAbdominalPain);
                    emergency_ref.child("/injury_details/medical_emergency/abdominal_pain").setValue(abdominalPainCheckBox.isChecked());
                    EditText medicalOtherTextView = (EditText) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.editTextMedicalOther);
                    emergency_ref.child("/injury_details/medical_emergency/other").setValue(medicalOtherTextView.getText().toString());
                    CheckBox awakeCheckBox = (CheckBox) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.checkBoxAwake);
                    emergency_ref.child("/injury_details/injuries_and_illness/awake").setValue(awakeCheckBox.isChecked());
                    CheckBox unconsciousCheckBox = (CheckBox) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.checkBoxUnconscious);
                    emergency_ref.child("/injury_details/injuries_and_illness/unconscious").setValue(unconsciousCheckBox.isChecked());
                    CheckBox chokingCheckBox = (CheckBox) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.checkBoxChoking);
                    emergency_ref.child("/injury_details/injuries_and_illness/choking").setValue(chokingCheckBox.isChecked());
                    CheckBox bleedingCheckBox = (CheckBox) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.checkBoxBleeding);
                    emergency_ref.child("/injury_details/injuries_and_illness/bleeding").setValue(bleedingCheckBox.isChecked());
                    CheckBox fractureCheckBox = (CheckBox) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.checkBoxFracture);
                    emergency_ref.child("/injury_details/injuries_and_illness/fracture").setValue(fractureCheckBox.isChecked());
                    CheckBox burnsCheckBox = (CheckBox) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.checkBoxBurns);
                    emergency_ref.child("/injury_details/injuries_and_illness/burns").setValue(burnsCheckBox.isChecked());
                    EditText illnessOtherTextView = (EditText) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.editTextInjuriesOther);
                    emergency_ref.child("/injury_details/injuries_and_illness/other").setValue(illnessOtherTextView.getText().toString());



                    //Box 5: Comments
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
