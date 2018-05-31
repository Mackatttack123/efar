package com.southafricaproject.efar;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class ActivityEFARMainTabbed extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_efarmain_tabbed);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            launchPatientMainScreen();
        }

        //check network connection
        //check if a forced app update is needed
        //check if an logged in on another phone
        CheckFunctions.runAllChecks(ActivityEFARMainTabbed.this, this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setSelectedTabIndicatorHeight((int) (4 * getResources().getDisplayMetrics().density));

        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String id = sharedPreferences.getString("id", "");
        final String token = FirebaseInstanceId.getInstance().getToken();
        //check if an logged in on another phone
        FirebaseDatabase.getInstance().getReference().child("users/" + id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String current_token= snapshot.child("token").getValue().toString();
                if(!token.equals(current_token)){
                    android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(ActivityEFARMainTabbed.this)
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
                                    editor.putString("id", "");
                                    editor.putString("name", "");
                                    editor.putBoolean("logged_in", false);
                                    stopService(new Intent(ActivityEFARMainTabbed.this, GPSTrackingService.class));
                                    editor.apply();

                                    //clear the phones token for the database
                                    String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                                    DatabaseReference token_ref = database.getReference("tokens/" + refreshedToken);
                                    token_ref.removeValue();

                                    if(mAuth.getCurrentUser() != null){
                                        mAuth.getCurrentUser().delete();
                                    }
                                    launchPatientMainScreen();
                                    finish();
                                }
                            }).setCancelable(false);
                    if(!((Activity) ActivityEFARMainTabbed.this).isFinishing())
                    {
                        alert.show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // start tracking efar
        startService(new Intent(this, GPSTrackingService.class));

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //userRef.child(id + "/token").setValue(refreshedToken);

        //button to get back to patient screen
        Button logoutButton = (Button) findViewById(R.id.logout_button);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new android.app.AlertDialog.Builder(ActivityEFARMainTabbed.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Logging Out")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //to get rid of stored password and username
                                SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();

                                // say that user has logged off
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference userRef = database.getReference("users");
                                userRef.child(sharedPreferences.getString("id", "") + "/logged_in").setValue(false);
                                userRef.child(sharedPreferences.getString("id", "") + "/token").setValue("null");
                                editor.putString("id", "");
                                editor.putString("name", "");
                                editor.putBoolean("logged_in", false);
                                stopService(new Intent(ActivityEFARMainTabbed.this, GPSTrackingService.class));
                                editor.apply();

                                //clear the phones token for the database
                                String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                                DatabaseReference token_ref = database.getReference("tokens/" + refreshedToken);
                                token_ref.removeValue();

                                if(mAuth.getCurrentUser() != null){
                                    mAuth.getCurrentUser().delete();
                                }
                                launchPatientMainScreen();
                                finish();
                            }

                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        Button backButton = (Button) findViewById(R.id.back_button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchPatientMainScreen();
                finish();
            }
        });

    }

    // Goes to patient info tab to send more to EFARs
    private void launchPatientMainScreen() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("screen_bypass", false);
        editor.apply();
        Intent toPatientMainScreen = new Intent(this, ActivityPatientMain.class);
        startActivity(toPatientMainScreen);
        finish();
    }

    //disables the werid transition beteen activities
    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_efarmain_activity_tabbed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    EFARMainTabAll alltab = new EFARMainTabAll();
                    return alltab;
                case 1:
                    EFARMainTabYou youtab = new EFARMainTabYou();
                    return youtab;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "All Emergencies";
                case 1:
                    return "Your Emergencies";
            }
            return null;
        }
    }
}
