package com.example.lenovo.angonaa.Service;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lenovo.angonaa.Activity.EmergencyListActivity;
import com.example.lenovo.angonaa.Activity.MainActivity;
import com.example.lenovo.angonaa.Activity.PreferenceActivity2;
import com.example.lenovo.angonaa.Activity.StopSMSActivity;
import com.example.lenovo.angonaa.HelperClass.ShakeDetector;

import com.example.lenovo.angonaa.Database.DatabaseHandler;
import com.example.lenovo.angonaa.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.List;
import java.util.Locale;

public class ShakeSensorService extends Service implements LocationListener {

    private final String TAG = this.getClass().getSimpleName();
    private SensorManager mSensorManager;
    private ShakeDetector mShakeDetector;
    PreferenceActivity2 pref_obj;
    MainActivity mainActivity_obj;
    DatabaseHandler db1;
    StopSMSActivity smsActivity_obj;

    private LocationManager locationManager;
    private String provider;
    public int lat, lng;
    public String adrss;
    public String fullAddress;
    private FusedLocationProviderClient mFusedLocationClient;
    public int smsCount;
    //public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 5469;
    public ShakeSensorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        pref_obj = new PreferenceActivity2();
        db1 = new DatabaseHandler(this);
        mainActivity_obj = new MainActivity();
        smsActivity_obj = new StopSMSActivity();
        boolean value2 = false;
        //get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        // check if enabled and if not send user to the GSP settings
        // Better solution would be to display a dialog and suggesting to
        // go to the settings
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)

            return;
        }
        android.location.Location location = locationManager.getLastKnownLocation(provider);

        // Initialize the location fields
        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
        } else {
            Toast.makeText(getApplicationContext(), "Location not available", Toast.LENGTH_SHORT).show();

        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            double lastlat = location.getLatitude();
                            double lastlong = location.getLongitude();
                            //Toast.makeText(getApplicationContext(), "The lat : " + lastlat + "The long : " + lastlong + " got it", Toast.LENGTH_SHORT).show();
                            getAddress(getApplicationContext(), lastlat, lastlong);

                        } else {
                            Toast.makeText(getApplicationContext(), "Location not available", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        final SharedPreferences sharedPreferences2 = getSharedPreferences("isChecked2", 0);
        value2 = sharedPreferences2.getBoolean("isChecked2", value2);
        if (value2 == true) {
            pref_obj.shake_flag = 1;
        }


        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        final Sensor mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                //Checking for sensitivity and shake count
                if ((count == 1 && pref_obj.progress_value == 0) || (count == 1 && pref_obj.progress_value == 1) || (count == 2 && pref_obj.progress_value == 2)
                        || (count == 3 && pref_obj.progress_value == 3) || (count == 4 && pref_obj.progress_value == 4)
                        || (count == 5 && pref_obj.progress_value == 5) || (count == 6 && pref_obj.progress_value == 6)
                        || (count == 7 && pref_obj.progress_value == 7) || (count == 8 && pref_obj.progress_value == 8)
                        || (count == 9 && pref_obj.progress_value == 9) || (count == 10 && pref_obj.progress_value == 10)) {
                    Log.d(TAG, "Shake Count:" + count);

                    if (pref_obj.shake_flag == 1) {

                        Toast.makeText(getApplicationContext(), "সেকিং", Toast.LENGTH_LONG).show();
                        final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.police_siren2);
                        // mp.setLooping(true);
                        mp.start();
                        sendSMS();
                        Intent smsActivity = new Intent(ShakeSensorService.this, StopSMSActivity.class);
                        startActivity(smsActivity);

                        //continuous msging
                       /* while (smsActivity_obj.safeButtonPressed == false)
                        {
                            //sendSMS();
                           /* new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    sendSMS();
                                    smsCount++;

                                }
                            }, 30000);
                            if (smsCount == 5)
                            {
                                break;
                            }
                        }*/

                        //startCall();
                        // popUpAlert();
                        //CallAlert();

                    } else {
                        Toast.makeText(getApplicationContext(), " Shake এর প্রেফারেন্স ঠিক করুন ", Toast.LENGTH_LONG).show();
                    }

                }
            }
        });
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);


       // askPermission();


    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mSensorManager.unregisterListener(mShakeDetector);
        super.onDestroy();
    }

  /*  public void askPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                mainActivity_obj.startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            }
        }
    }
*/
    //method for multiple messaging

    public void sendSMS() {
        try {
            List<String> myContactList = db1.getAllContact();
            String[] numbers = myContactList.toArray(new String[0]);
            //for calling
            /*Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + numbers[0]));
            if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            startActivity(callIntent);
            Toast.makeText(this, "Callingggggg:  ", Toast.LENGTH_LONG).show();*/
            //for messaging
            for (String s : numbers) {
                //send sms
                SmsManager sms = SmsManager.getDefault();
                Toast.makeText(this, "Adress is:  "+fullAddress+" ", Toast.LENGTH_LONG).show();
                sms.sendTextMessage(String.valueOf(s), null, "Help! This is an emergency at : " + fullAddress, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Message NOT sent!", Toast.LENGTH_SHORT).show();
        }

    }
    //for calling
   /* public  void startCall(){
        List<String> myContactList = db1.getAllContact();
        String[] numbers = myContactList.toArray(new String[0]);
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + numbers[0]));
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
           Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

        startActivity(callIntent);
        Toast.makeText(this, "Callingggggg:  ", Toast.LENGTH_LONG).show();
    }*/

    //popuptry
    /*
    public void popUpAlert(){
        AlertDialog alertDialog = new AlertDialog.Builder(this,R.style.Theme_AppCompat)

                //set title
                .setTitle("Are you sure to Exit")
                //set message
                .setMessage("Exiting will call finish() method")
                //set positive button
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //set what would happen when positive button is clicked

                    }
                })
                //set negative button
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //set what should happen when negative button is clicked
                        Toast.makeText(getApplicationContext(),"Nothing Happened",Toast.LENGTH_LONG).show();
                    }
                })
                .show();
    }
*/
    //alert for calling timer

 /*   public void CallAlert() {
        List<String> myContactList = db1.getAllContact();
        String[] numbers = myContactList.toArray(new String[0]);
        Toast.makeText(this, "Alert will come:  ", Toast.LENGTH_LONG).show();
        AlertDialog.Builder alert = new AlertDialog.Builder(ShakeSensorService.this,R.style.Theme_AppCompat);

        alert.setTitle("");
        alert.setMessage("Calling in: ");
        final TextView text = new TextView(this);
                alert.setView(text);
                alert.setPositiveButton("Don't call",new DialogInterface.OnClickListener()

        {
            public void onClick (DialogInterface dialog,int whichButton){
            dialog.cancel();
        }
        });


        new CountDownTimer(300000,1000) {

            public void onTick ( long millisUntilFinished){
                text.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
            //text.setText("done!");
            //calling
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + numbers[0]));
            if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            startActivity(callIntent);
            //Toast.makeText(this, "Callingggggg:  ", Toast.LENGTH_LONG).show();

        }
    }.start();
    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                dialog.cancel();
            }
        });
   // AlertDialog dialog=alert.create();
    //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
     /*   final AlertDialog dialog = alert.create();
        final Window dialogWindow = dialog.getWindow();
        final WindowManager.LayoutParams dialogWindowAttributes = dialogWindow.getAttributes();

// Set fixed width (280dp) and WRAP_CONTENT height
        final WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialogWindowAttributes);
        lp.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 280, getResources().getDisplayMetrics());
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogWindow.setAttributes(lp);

// Set to TYPE_SYSTEM_ALERT so that the Service can display it
        dialogWindow.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialogWindowAttributes.windowAnimations = R.style.Theme_AppCompat_DayNight_Dialog;
    dialog.show();
     alert.show();


    }*/
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


    @Override
    public void onLocationChanged(android.location.Location location) {
        lat = (int) (location.getLatitude());
        lng = (int) (location.getLongitude());
        adrss = db1.chkgps(String.valueOf(lng),String.valueOf(lat));
        Toast.makeText(getApplicationContext(),adrss,Toast.LENGTH_SHORT).show();

    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


}