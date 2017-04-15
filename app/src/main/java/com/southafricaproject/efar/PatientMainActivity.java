package com.southafricaproject.efar;

import android.support.v7.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

public class PatientMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_main);

        final Button cancelButton = (Button)findViewById(R.id.canel_efar_button);

        Button helpMeButton = (Button)findViewById(R.id.help_me_button);

        //I NEED HELP button logic
        helpMeButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

                        //send and alert asking if they are sure they want to call
                        new AlertDialog.Builder(PatientMainActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Call EFAR")
                                .setMessage("Are you sure you want to call an EFAR?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        TextView userUpdate = (TextView) findViewById(R.id.user_update);
                                        userUpdate.animate().alpha(1.0f).setDuration(1);
                                        userUpdate.setText("EFARs in your area are being contacted...");
                                        cancelButton.setVisibility(View.VISIBLE);
                                        blinkText();
                                        launchPatientInfoScreen();
                                    }

                                })
                                .setNegativeButton("No", null)
                                .show();

                    }
                }
        );

        Button toLoginButton = (Button)findViewById(R.id.to_login_button);

        toLoginButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        // go to login screen
                        launchLoginScreen();
                    }
                }
        );

        cancelButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        //send and alert asking if they are sure they want to cancel the efar
                        new AlertDialog.Builder(PatientMainActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Cancel EFAR")
                                .setMessage("Are you sure you want to cancel your EFAR?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // cancel EFAR here
                                        TextView userUpdate = (TextView) findViewById(R.id.user_update);
                                        userUpdate.setText("EFAR Cancled!");
                                        // fade out text
                                        userUpdate.animate().alpha(0.0f).setDuration(3000);
                                        // take away cancel button
                                        cancelButton.setVisibility(View.INVISIBLE);
                                    }

                                })
                                .setNegativeButton("No", null)
                                .show();

                    }
                }
        );
    }

    // Starts up login screen
    private void launchLoginScreen() {

        Intent toLogin = new Intent(this, loginScreen.class);
        startActivity(toLogin);
    }

    // Goes to patient info tab to send more to EFARs
    private void launchPatientInfoScreen() {

        Intent toPatientInfoScreen = new Intent(this, PatientInfoActivity.class);
        startActivity(toPatientInfoScreen);
    }

    // blinking text animation
    public void blinkText(){
        TextView userUpdate = (TextView) findViewById(R.id.user_update );

        Animation anim = new AlphaAnimation(1.0f, 0.3f);
        anim.setDuration(800); //manage the time of the blink with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        userUpdate.startAnimation(anim);
    }


}
