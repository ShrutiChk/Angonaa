package com.example.lenovo.angonaa.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lenovo.angonaa.Database.DatabaseHandler;
import com.example.lenovo.angonaa.Manifest;
import com.example.lenovo.angonaa.R;
import com.example.lenovo.angonaa.Service.ShakeSensorService;

import java.util.List;

public class StopSMSActivity extends AppCompatActivity {
    public  Button stopSMSButton;
    public  Button stopCallButton;
    public static  Boolean safeButtonPressed = false;
    public TextView time ;
    DatabaseHandler db1;
    CountDownTimer countDownTimer;
    ActionBar actionBar;
    public int smsCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_sms);
        actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#BF7FBF")));
        stopSMSButton = (Button) findViewById(R.id.safeButton);
        stopSMSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                safeButtonPressed = true;
            }
        });

        time = (TextView) findViewById(R.id.callTime);
        db1 = new DatabaseHandler(this);
        countDownTimer = new CountDownTimer(10000,1000) {

            public void onTick ( long millisUntilFinished){
                time.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                //text.setText("done!");
                //calling
                List<String> myContactList = db1.getAllContact();
                String[] numbers = myContactList.toArray(new String[0]);
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + numbers[0]));
                if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                        android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                startActivity(callIntent);
                Toast.makeText(getApplicationContext(), "Callinggg", Toast.LENGTH_SHORT).show();

            }
        }.start();
        stopCallButton = (Button) findViewById(R.id.cancleCallButton);
        stopCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.cancel();
                Toast.makeText(getApplicationContext(), "Call Canceled", Toast.LENGTH_SHORT).show();
            }
        });

      /*  while (safeButtonPressed == false)
        {
            //sendSMS();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ShakeSensorService.sendSMS();
                    smsCount++;

                }
            }, 30000);
            if (smsCount == 5)
            {
                break;
            }
        }*/


    }

    //method for multiple messaging

  /*  public void sendSMS() {
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
            Toast.makeText(this, "Callingggggg:  ", Toast.LENGTH_LONG).show();
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

    }*/
}
