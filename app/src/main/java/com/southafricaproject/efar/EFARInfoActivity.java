package com.southafricaproject.efar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

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

        final FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();

        //check connection
        try {
            ConnectivityManager cm = (ConnectivityManager) this
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            NetworkInfo mWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if ((networkInfo != null && networkInfo.isConnected()) || mWifi.isConnected()) {

            }else{
                new AlertDialog.Builder(EFARInfoActivity.this)
                        .setTitle("Connection Error:")
                        .setMessage("Your device is currently unable connect to our services. " +
                                "Please check your connection or try again later.")
                        .show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            new AlertDialog.Builder(EFARInfoActivity.this)
                    .setTitle("Connection Error:")
                    .setMessage("Your device is currently unable connect to our services. " +
                            "Please check your connection or try again later.")
                    .show();
        }

        //check if an update is needed
        FirebaseDatabase.getInstance().getReference().child("version").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String current_version = snapshot.child("version_number").getValue().toString();
                if(!current_version.equals(BuildConfig.VERSION_NAME)){
                    AlertDialog.Builder alert = new AlertDialog.Builder(EFARInfoActivity.this)
                            .setTitle("Update Needed:")
                            .setMessage("Please updated to the the latest version of our app.").setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                    } catch (android.content.ActivityNotFoundException anfe) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                    }
                                    finish();
                                    startActivity(getIntent());
                                }
                            }).setNegativeButton("Exit App", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finishAndRemoveTask();
                                }
                            }).setCancelable(false);
                    if(!((Activity) EFARInfoActivity.this).isFinishing())
                    {
                        alert.show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String id = sharedPreferences.getString("id", "");
        boolean efar_logged_in = sharedPreferences.getBoolean("logged_in", false);
        final String token = FirebaseInstanceId.getInstance().getToken();
        //check if an logged in on another phone
        if(efar_logged_in){
            FirebaseDatabase.getInstance().getReference().child("users/" + id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String current_token= snapshot.child("token").getValue().toString();
                    if(!token.equals(current_token)){
                        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(EFARInfoActivity.this)
                                .setTitle("Oops!")
                                .setMessage("Looks liked you're logged in on another device. You will now be logged out but you can log back onto this device if you'd like.").setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //to get rid of stored password and username
                                        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();

                                        // say that user has logged off
                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        DatabaseReference userRef = database.getReference("users");
                                        userRef.child(sharedPreferences.getString("id", "") + "/logged_in").setValue(false);
                                        editor.putString("id", "");
                                        editor.putString("name", "");
                                        editor.putBoolean("logged_in", false);
                                        stopService(new Intent(EFARInfoActivity.this, MyService.class));
                                        editor.apply();

                                        //clear the phones token for the database
                                        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                                        DatabaseReference token_ref = database.getReference("tokens/" + refreshedToken);
                                        token_ref.removeValue();

                                        if(mAuth.getCurrentUser() != null){
                                            mAuth.getCurrentUser().delete();
                                        }
                                        finish();
                                        startActivity(getIntent());
                                    }
                                }).setCancelable(false);
                        if(!((Activity) EFARInfoActivity.this).isFinishing())
                        {
                            alert.show();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        Button backButton = (Button) findViewById(R.id.back_button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //check if the emergency is still in the database
        FirebaseDatabase.getInstance().getReference().child("emergencies/").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                launchEfarMainScreen();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


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

            //for Violent Injury section in Box 3
            final EditText weaponTextView = (EditText) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.editTextWeponOther);
            final RadioGroup weaponRadioGroup = (RadioGroup) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.radioGroupWepons);
            weaponTextView.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    if(!weaponTextView.getText().toString().equals("")){
                        // get selected radio button from radioGroup
                        int selectedId = weaponRadioGroup.getCheckedRadioButtonId();
                        if(selectedId != -1) {
                            weaponRadioGroup.clearCheck();
                        }
                    }
                }
            });
            weaponRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    // checkedId is the RadioButton selected
                    weaponTextView.setText("");
                }
            });

            //for motor vehicle acident section in Box 3
            final EditText motorVehicleTextView = (EditText) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.editTextMotorVehicleOther);
            final RadioGroup motorVehicleRadioGroup = (RadioGroup) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.radioGroupMotorVehicle);
            motorVehicleTextView.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    if(!motorVehicleTextView.getText().toString().equals("")){
                        // get selected radio button from radioGroup
                        int selectedId = motorVehicleRadioGroup.getCheckedRadioButtonId();
                        if(selectedId != -1) {
                            motorVehicleRadioGroup.clearCheck();
                        }
                    }
                }
            });
            motorVehicleRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    // checkedId is the RadioButton selected
                    motorVehicleTextView.setText("");
                }
            });

            Button submitReportButton = (Button) cell.findViewById(R.id.comments_writeup).findViewById(R.id.submit_report_button);

            submitReportButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(EFARInfoActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Submit Report")
                            .setMessage("Are you sure you want to submit this report? Sending all the information to database may take a few minutes.")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                                    String finished_emergency_key = sharedPreferences.getString("finished_emergency_key", "");
                                    String finished_emergency_date = sharedPreferences.getString("finished_emergency_date", "");
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference emergency_ref = database.getReference("emergencies/" + finished_emergency_key);
                                    emergency_ref.child("/state").setValue("2");
                                    //give slight delay so patients phone can be updated before the data is moved
                                    SystemClock.sleep(100); //ms
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
                                    //EditText nameTextView = (EditText) cell.findViewById(R.id.patient_detail_writeup).findViewById(R.id.patientNameText);
                                    //emergency_ref.child("/patient_care_report_form/patient_details/name").setValue(nameTextView.getText().toString());
                                    RadioGroup genderRadioGroup = (RadioGroup) cell.findViewById(R.id.patient_detail_writeup).findViewById(R.id.radioGroupGender);
                                    // get selected radio button from radioGroup
                                    int selectedId = genderRadioGroup.getCheckedRadioButtonId();
                                    // find the radiobutton by returned id
                                    if(selectedId != -1){
                                        RadioButton radioButtonGender = (RadioButton) findViewById(selectedId);
                                        emergency_ref.child("/patient_care_report_form/patient_details/gender").setValue(radioButtonGender.getText().toString());
                                    }
                                    EditText ageTextView = (EditText) cell.findViewById(R.id.patient_detail_writeup).findViewById(R.id.editTextAge);
                                    if(ageTextView.getText().toString().equals("")){
                                        RadioGroup ageRadioGroup = (RadioGroup) cell.findViewById(R.id.patient_detail_writeup).findViewById(R.id.radioGroupAge);
                                        // get selected radio button from radioGroup
                                        selectedId = ageRadioGroup.getCheckedRadioButtonId();
                                        // find the radiobutton by returned id
                                        if(selectedId != -1) {
                                            RadioButton ageRadioButton = (RadioButton) findViewById(selectedId);
                                            emergency_ref.child("/patient_care_report_form/patient_details/age").setValue(ageRadioButton.getText().toString());
                                        }
                                    }else{
                                        emergency_ref.child("/patient_care_report_form/patient_details/age").setValue(ageTextView.getText().toString());
                                    }

                                    //Box 2: Incident Details
                                    EditText emsTimeTextView = (EditText) cell.findViewById(R.id.incident_details_writeup).findViewById(R.id.editTextEMSTime);
                                    emergency_ref.child("/patient_care_report_form/incident_details/ems_or_ambulance_time").setValue(emsTimeTextView.getText().toString());
                                    EditText locationTextView = (EditText) cell.findViewById(R.id.incident_details_writeup).findViewById(R.id.locationEditText);
                                    emergency_ref.child("/patient_care_report_form/incident_details/incident_location").setValue(locationTextView.getText().toString());
                                    EditText communityTextView = (EditText) cell.findViewById(R.id.incident_details_writeup).findViewById(R.id.editTextCommunity);
                                    emergency_ref.child("/patient_care_report_form/incident_details/community").setValue(communityTextView.getText().toString());

                                    //Box 3: Injury Details
                                    EditText weaponTextView = (EditText) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.editTextWeponOther);
                                    if(weaponTextView.getText().toString().equals("")){
                                        RadioGroup weaponRadioGroup = (RadioGroup) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.radioGroupWepons);
                                        // get selected radio button from radioGroup
                                        selectedId = weaponRadioGroup.getCheckedRadioButtonId();
                                        // find the radiobutton by returned id
                                        if(selectedId != -1) {
                                            RadioButton weaponRadioButton = (RadioButton) findViewById(selectedId);
                                            emergency_ref.child("/patient_care_report_form/injury_details/Weapon_used").setValue(weaponRadioButton.getText().toString());
                                        }
                                    }else{
                                        if(!weaponTextView.getText().toString().equals("Other...")) {
                                            emergency_ref.child("/patient_care_report_form/injury_details/Weapon_used").setValue(weaponTextView.getText().toString());
                                        }
                                    }
                                    EditText motorVehicleTextView = (EditText) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.editTextMotorVehicleOther);
                                    if(motorVehicleTextView.getText().toString().equals("")){
                                        RadioGroup motorVehicleRadioGroup = (RadioGroup) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.radioGroupMotorVehicle);
                                        // get selected radio button from radioGroup
                                        selectedId = motorVehicleRadioGroup.getCheckedRadioButtonId();
                                        // find the radiobutton by returned id
                                        if(selectedId != -1) {
                                            RadioButton motorVehicleRadioButton = (RadioButton) findViewById(selectedId);
                                            emergency_ref.child("/patient_care_report_form/injury_details/motor_vehicle_accident").setValue(motorVehicleRadioButton.getText().toString());
                                        }
                                    }else{
                                        if(!motorVehicleTextView.getText().toString().equals("Other...")) {
                                            emergency_ref.child("/patient_care_report_form/injury_details/motor_vehicle_accident").setValue(motorVehicleTextView.getText().toString());
                                        }
                                    }
                                    CheckBox poisoningCheckBox = (CheckBox) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.checkBoxPoisoning);
                                    emergency_ref.child("/patient_care_report_form/injury_details/medical_emergency/poisoning").setValue(poisoningCheckBox.isChecked());
                                    CheckBox chestPainCheckBox = (CheckBox) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.checkBoxChestPain);
                                    emergency_ref.child("/patient_care_report_form/injury_details/medical_emergency/chest_pain").setValue(chestPainCheckBox.isChecked());
                                    CheckBox diabetesPainCheckBox = (CheckBox) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.checkBoxDiabetes);
                                    emergency_ref.child("/patient_care_report_form/injury_details/medical_emergency/diabetes").setValue(diabetesPainCheckBox.isChecked());
                                    CheckBox strokePainCheckBox = (CheckBox) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.checkBoxStroke);
                                    emergency_ref.child("/patient_care_report_form/injury_details/medical_emergency/stroke").setValue(strokePainCheckBox.isChecked());
                                    CheckBox epilepsyPainCheckBox = (CheckBox) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.checkBoxEpilepsy);
                                    emergency_ref.child("/patient_care_report_form/injury_details/medical_emergency/epilepsy").setValue(epilepsyPainCheckBox.isChecked());
                                    CheckBox dehydratedPainCheckBox = (CheckBox) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.checkBoxDehydrated);
                                    emergency_ref.child("/patient_care_report_form/injury_details/medical_emergency/dehydrated").setValue(dehydratedPainCheckBox.isChecked());
                                    CheckBox difficultyBreathingCheckBox = (CheckBox) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.checkBoxDifficultyBreathing);
                                    emergency_ref.child("/patient_care_report_form/injury_details/medical_emergency/difficulty_breathing").setValue(difficultyBreathingCheckBox.isChecked());
                                    CheckBox abdominalPainCheckBox = (CheckBox) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.checkBoxAbdominalPain);
                                    emergency_ref.child("/patient_care_report_form/injury_details/medical_emergency/abdominal_pain").setValue(abdominalPainCheckBox.isChecked());
                                    EditText medicalOtherTextView = (EditText) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.editTextMedicalOther);
                                    if(!medicalOtherTextView.getText().toString().equals("Other...")) {
                                        emergency_ref.child("/patient_care_report_form/injury_details/medical_emergency/other").setValue(medicalOtherTextView.getText().toString());
                                    }
                                    CheckBox awakeCheckBox = (CheckBox) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.checkBoxAwake);
                                    emergency_ref.child("/patient_care_report_form/injury_details/injuries_and_illness/awake").setValue(awakeCheckBox.isChecked());
                                    CheckBox unconsciousCheckBox = (CheckBox) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.checkBoxUnconscious);
                                    emergency_ref.child("/patient_care_report_form/injury_details/injuries_and_illness/unconscious").setValue(unconsciousCheckBox.isChecked());
                                    CheckBox chokingCheckBox = (CheckBox) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.checkBoxChoking);
                                    emergency_ref.child("/patient_care_report_form/injury_details/injuries_and_illness/choking").setValue(chokingCheckBox.isChecked());
                                    CheckBox bleedingCheckBox = (CheckBox) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.checkBoxBleeding);
                                    emergency_ref.child("/patient_care_report_form/injury_details/injuries_and_illness/bleeding").setValue(bleedingCheckBox.isChecked());
                                    CheckBox fractureCheckBox = (CheckBox) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.checkBoxFracture);
                                    emergency_ref.child("/patient_care_report_form/injury_details/injuries_and_illness/fracture").setValue(fractureCheckBox.isChecked());
                                    CheckBox burnsCheckBox = (CheckBox) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.checkBoxBurns);
                                    emergency_ref.child("/patient_care_report_form/injury_details/injuries_and_illness/burns").setValue(burnsCheckBox.isChecked());
                                    EditText illnessOtherTextView = (EditText) cell.findViewById(R.id.injury_details_writeup).findViewById(R.id.editTextInjuriesOther);
                                    if(!illnessOtherTextView.getText().toString().equals("Other...")){
                                        emergency_ref.child("/patient_care_report_form/injury_details/injuries_and_illness/other").setValue(illnessOtherTextView.getText().toString());
                                    }

                                    //Box 4: Treatment Details
                                    CheckBox cprCheckBox = (CheckBox) cell.findViewById(R.id.treatment_details_writeup).findViewById(R.id.checkBoxCPR);
                                    emergency_ref.child("/patient_care_report_form/treatment_details/care_provided/cpr").setValue(cprCheckBox.isChecked());
                                    CheckBox recoveryPositionCheckBox = (CheckBox) cell.findViewById(R.id.treatment_details_writeup).findViewById(R.id.checkBoxRecoveryPosition);
                                    emergency_ref.child("/patient_care_report_form/treatment_details/care_provided/recovery_position").setValue(recoveryPositionCheckBox.isChecked());
                                    CheckBox stopBleedingCheckBox = (CheckBox) cell.findViewById(R.id.treatment_details_writeup).findViewById(R.id.checkBoxStopBleeding);
                                    emergency_ref.child("/patient_care_report_form/treatment_details/care_provided/stop_bleeding").setValue(stopBleedingCheckBox.isChecked());
                                    CheckBox applyBandageCheckBox = (CheckBox) cell.findViewById(R.id.treatment_details_writeup).findViewById(R.id.checkBoxApplyBandage);
                                    emergency_ref.child("/patient_care_report_form/treatment_details/care_provided/apply_bandage").setValue(applyBandageCheckBox.isChecked());
                                    CheckBox splintingCheckBox = (CheckBox) cell.findViewById(R.id.treatment_details_writeup).findViewById(R.id.checkBoxSplinting);
                                    emergency_ref.child("/patient_care_report_form/treatment_details/care_provided/splinting").setValue(splintingCheckBox.isChecked());
                                    CheckBox assistInTakingMedsCheckBox = (CheckBox) cell.findViewById(R.id.treatment_details_writeup).findViewById(R.id.checkBoxAssistInTakingMeds);
                                    emergency_ref.child("/patient_care_report_form/treatment_details/care_provided/assist_patient_in_taking_own_medication").setValue(assistInTakingMedsCheckBox.isChecked());
                                    EditText careOtherTextView = (EditText) cell.findViewById(R.id.treatment_details_writeup).findViewById(R.id.editTextCareProvidedOther);
                                    if(!careOtherTextView.getText().toString().equals("Other...")){
                                        emergency_ref.child("/patient_care_report_form/treatment_details/care_provided/other").setValue(careOtherTextView.getText().toString());
                                    }
                                    CheckBox glovesCheckBox = (CheckBox) cell.findViewById(R.id.treatment_details_writeup).findViewById(R.id.checkBoxGloves);
                                    emergency_ref.child("/patient_care_report_form/treatment_details/equipment_used/gloves").setValue(glovesCheckBox.isChecked());
                                    CheckBox threeSidedDressingCheckBox = (CheckBox) cell.findViewById(R.id.treatment_details_writeup).findViewById(R.id.checkBox3SidedDressing);
                                    emergency_ref.child("/patient_care_report_form/treatment_details/equipment_used/three_sided_dressing").setValue(threeSidedDressingCheckBox.isChecked());
                                    CheckBox bandageCheckBox = (CheckBox) cell.findViewById(R.id.treatment_details_writeup).findViewById(R.id.checkBoxBandage);
                                    emergency_ref.child("/patient_care_report_form/treatment_details/equipment_used/bandage").setValue(bandageCheckBox.isChecked());
                                    CheckBox aedCheckBox = (CheckBox) cell.findViewById(R.id.treatment_details_writeup).findViewById(R.id.checkBoxAED);
                                    emergency_ref.child("/patient_care_report_form/treatment_details/equipment_used/aed").setValue(aedCheckBox.isChecked());
                                    EditText equipmentOtherTextView = (EditText) cell.findViewById(R.id.treatment_details_writeup).findViewById(R.id.editTextEquipmentOther);
                                    if(!careOtherTextView.getText().toString().equals("Other...")){
                                        emergency_ref.child("/patient_care_report_form/treatment_details/equipment_used/other").setValue(equipmentOtherTextView.getText().toString());
                                    }
                                    RadioGroup takenToHospitalRadioGroup = (RadioGroup) cell.findViewById(R.id.treatment_details_writeup).findViewById(R.id.radioGroupHospital);
                                    // get selected radio button from radioGroup
                                    selectedId = takenToHospitalRadioGroup.getCheckedRadioButtonId();
                                    // find the radiobutton by returned id
                                    if(selectedId != -1){
                                        RadioButton takenToHospitalRadioButton = (RadioButton) findViewById(selectedId);
                                        emergency_ref.child("/patient_care_report_form/treatment_details/hospital/patient_taken_to_hospital").setValue(takenToHospitalRadioButton.getText().toString());
                                    }
                                    CheckBox efarCheckBox = (CheckBox) cell.findViewById(R.id.treatment_details_writeup).findViewById(R.id.checkBoxEFAR);
                                    emergency_ref.child("/patient_care_report_form/treatment_details/hospital/transport/EFAR").setValue(efarCheckBox.isChecked());
                                    CheckBox privateCarCheckBox = (CheckBox) cell.findViewById(R.id.treatment_details_writeup).findViewById(R.id.checkBoxPrivateCar);
                                    emergency_ref.child("/patient_care_report_form/treatment_details/hospital/transport/private_car").setValue(privateCarCheckBox.isChecked());
                                    CheckBox ambulaceCheckBox = (CheckBox) cell.findViewById(R.id.treatment_details_writeup).findViewById(R.id.checkBoxAmbulance);
                                    emergency_ref.child("/patient_care_report_form/treatment_details/hospital/transport/ambulace").setValue(ambulaceCheckBox.isChecked());
                                    EditText hospitalTextView = (EditText) cell.findViewById(R.id.treatment_details_writeup).findViewById(R.id.editTextHospitalName);
                                    emergency_ref.child("/patient_care_report_form/treatment_details/hospital/hospital_taken_to").setValue(hospitalTextView.getText().toString());

                                    //Box 5: Comments
                                    TextView commentTextView = (TextView) cell.findViewById(R.id.comments_writeup).findViewById(R.id.commentEditText);
                                    emergency_ref.child("/patient_care_report_form/comments").setValue(commentTextView.getText().toString());

                                    moveFirebaseRecord(emergency_ref, database.getReference("completed/" + finished_emergency_key));
                                    emergency_ref.removeValue();
                                    finish();
                                    launchEfarMainScreen();
                                }

                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            });
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

    private void launchEfarMainScreen() {
        Intent toEfarMainScreen = new Intent(this, EFARMainActivityTabbed.class);
        startActivity(toEfarMainScreen);
        finish();
    }

}
