package com.example.lenovo.angonaa.Activity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.widget.Toast;

import com.example.lenovo.angonaa.Database.DatabaseHandler;
import com.example.lenovo.angonaa.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;
import java.util.Locale;

public class About_usActivity extends AppCompatActivity {

    ActionBar actionBar;
    static int counter = 0;
    PreferenceActivity2 pre_obj = new PreferenceActivity2();
    MainActivity mainActivity;
    DatabaseHandler db1;
    public String fullAddress;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us2);

        actionBar = getSupportActionBar();
        actionBar.hide();

        db1 = new DatabaseHandler(this);
        mainActivity = new MainActivity();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)

            return;
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener( new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            double lastlat = location.getLatitude();
                            double lastlong = location.getLongitude();
                            //Toast.makeText(getApplicationContext(), "The lat : " + lastlat + "The long : " + lastlong + " got it", Toast.LENGTH_SHORT).show();
                            getAddress(getApplicationContext(),lastlat,lastlong);

                        } else {
                            Toast.makeText(getApplicationContext(), "Location not available", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //Use of Volume button
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {

            ++counter;
            if(counter == 3&& pre_obj.volume_flag==1){
                final MediaPlayer mp = MediaPlayer.create(this,R.raw.police_siren2);
                if(pre_obj.siren_flag==1)
                {
                    mp.start();
                    Toast.makeText(getApplicationContext(),"সাইরেন শুরু হবে",Toast.LENGTH_LONG).show();
                    counter = 0;
                    sendSMS();

                }
                else
                {
                    Toast.makeText(getApplicationContext(),"সাইরেন এর প্রেফারেন্স ঠিক করুন ",Toast.LENGTH_LONG).show();
                    counter = 0;

                }

            }

            else if(counter == 3&& pre_obj.volume_flag==0){
                Toast.makeText(getApplicationContext(),"ভলুউম এর প্রেফারেন্স ঠিক করুন ",Toast.LENGTH_LONG).show();
                counter=0;

            }

            return true;
        }

        else {
            return super.onKeyDown(keyCode, event);
        }
    }

    public void getAddress(Context context, double LATITUDE, double LONGITUDE) {

        //Set Address
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null && addresses.size() > 0) {
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

                fullAddress = "" +address+ ", "+city+", "+state+", "+country+", "+postalCode+", "+knownName+" ";
                Toast.makeText(this, "Adress is:  "+fullAddress+" ", Toast.LENGTH_LONG).show();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    //For multiple messaging
    public void sendSMS( ) {
        try{
            List<String> myContactList= db1.getAllContact();
            String[] numbers = myContactList.toArray(new String[0]);
            for(String s:numbers){
                //send sms
                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(String.valueOf(s), null, "Help! This is an emergency at : "+fullAddress  , null, null);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Message NOT sent!",Toast.LENGTH_SHORT).show();
        }

    }

}
