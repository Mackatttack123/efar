package com.southafricaproject.efar;

import android.support.v7.app.AppCompatActivity;

import android.app.AlertDialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class Emergency {
    private String key;
    private String address;
    private Double latitude;
    private Double longitude;
    private String phone_number;
    private String info;

    // constructor
    public Emergency(String key, String address, Double latitude, Double longitude, String phone_number, String info) {
        this.key = key;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone_number = phone_number;
        this.info = info;
    }

    // getter
    public String getKey() { return key; }
    public String getAddress() { return address; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getPhone() { return phone_number; }
    public String getInfo() { return info; }
}


public class EFARMainActivity extends AppCompatActivity {

    //TODO: make and emergency array that store an emergancy struct (which you need to make)
    //TODO: When you click on the emergency it will show you it in detail
    //TODO: display distance using cordinates in the arrayview for the EFARS
    //TODO: sort emergencics by distance away
    final ArrayList<String> disctanceArray = new ArrayList<String>();
    final ArrayList<Emergency> emergenecyArray = new ArrayList<Emergency>();
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_efarmain);

        GPSTracker gps = new GPSTracker(this);
        final double my_lat = gps.getLatitude(); // latitude
        final double my_long = gps.getLongitude(); // longitude

        //TODO: add manual update button to check for data
        // go through all the emergencies and put there data in the array
        FirebaseDatabase.getInstance().getReference().child("emergencies")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String e_key = snapshot.getKey();
                            Double e_lat = Double.parseDouble(snapshot.child("latitude").getValue().toString());
                            Double e_long = Double.parseDouble(snapshot.child("longitude").getValue().toString());
                            String e_phone_number = snapshot.child("phone_number").getValue().toString();
                            String e_info = snapshot.child("other_info").getValue().toString();

                            //TODO: THIS ADDRESS THING ISN't working any more!??!?!?! WTF...
                            // to get address
                            /*Geocoder geocoder = new Geocoder(EFARMainActivity.this, Locale.getDefault());
                            List<Address> addressList = null;
                            try {
                                addressList = geocoder.getFromLocation(e_lat, e_long, 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            String address, city, state, country;

                            if(addressList.size() > 0){
                                Address emergency_address = addressList.get(0);

                                address = emergency_address.getAddressLine(0);
                                city = emergency_address.getLocality();
                                state = emergency_address.getAdminArea();
                                country = emergency_address.getCountryName();
                            }else{
                                address = "";
                                city = "";
                                state = "";
                                country = "";
                            }*/

                            /*
                            String postalCode = emergency_address.getPostalCode();
                            String knownName = emergency_address.getFeatureName(); // Only if available else return NULL
                            if(postalCode == null){
                                postalCode = "";
                            }
                            if(knownName == null){
                                knownName = "";
                            }
                            */
                            //String e_address = address + " " + city + " " + state + " " + country; //+ " " + postalCode + " " + knownName;
                            String e_address = getCompleteAddressString(e_lat, e_long);
                            emergenecyArray.add(new Emergency(e_key, e_address, e_lat, e_long, e_phone_number, e_info));
                            disctanceArray.add("Emergancy: " + String.format("%.2f", distance(e_lat, e_long, my_lat, my_long)) + " km away");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_listview, disctanceArray);

        final ListView listView = (ListView) findViewById(R.id.patient_list);
        listView.setAdapter(adapter);

        listView.setClickable(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                Object o = listView.getItemAtPosition(position);
                final SpannableString message = new SpannableString(Html.fromHtml("<b>Location:</b> (" + String.format("%.2f", emergenecyArray.get(position).getLatitude())
                        + ", " + String.format("%.2f", emergenecyArray.get(position).getLongitude()) + ")<p><b>Address:</b> " + emergenecyArray.get(position).getAddress()
                        + "</p><p><b>Senders #:</b> " + emergenecyArray.get(position).getPhone() + "</p><p><b>Other Info:</b> " + emergenecyArray.get(position).getInfo(), 0));
                Linkify.addLinks(message, Linkify.ALL);
                new AlertDialog.Builder(EFARMainActivity.this)
                        .setIcon(0)
                        .setTitle(Html.fromHtml("<h3>Emergency Information</h3>", 0))
                        .setMessage(message)
                        .setPositiveButton("Done", null)
                        .setCancelable(false)
                        .show();
            }
        });

        //button to get back to patient screen
        Button logoutButton = (Button) findViewById(R.id.logout_button);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client2 = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.wtf("My Current loction address", "" + strReturnedAddress.toString());
            } else {
                Log.wtf("My Current loction address", "No Address returned!");
                strAdd = "N/A";
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.wtf("My Current loction address", "Canont get Address!");
            strAdd = "N/A";
        }
        return strAdd;
    }

    /*public JSONObject getLocationInfo() {

        HttpGet httpGet = new HttpGet("http://maps.google.com/maps/api/geocode/json?latlng=" + lat + "," + lng + "&sensor=true");
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }*/
}
