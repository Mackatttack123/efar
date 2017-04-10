package com.southafricaproject.efar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PatientInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_info);

        TextView userInfoScreenUpdate = (TextView) findViewById(R.id.user_update_info_screen);
        userInfoScreenUpdate.setText("EFARs in your area are being contacted!\n\nThis survey will " +
                "let us get you help sooner.");

        Button infoSumbitButton = (Button)findViewById(R.id.patient_info_sumbmit_button);

        infoSumbitButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        // go to main screen
                        finish();
                    }
                }
        );
    }

}
