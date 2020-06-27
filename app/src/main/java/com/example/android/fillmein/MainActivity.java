package com.example.android.fillmein;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements MessageListener{

    private final BroadcastReceiver mSMSBroadcastReceiver = new SMSBroadcastReceiver();
    Switch switchEnable;
    Switch switchVibe;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String permission = Manifest.permission.RECEIVE_SMS;
        int grant = ContextCompat.checkSelfPermission(this, permission);
        if ( grant != PackageManager.PERMISSION_GRANTED) {
            String[] permission_list = new String[1];
            permission_list[0] = permission;

            ActivityCompat.requestPermissions(this, permission_list, 1);
        }

        // init views
        switchEnable = (Switch) findViewById(R.id.switch_enable);
        switchEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    // register the sms broadcast receiver
                    IntentFilter filter = new IntentFilter();
                    filter.addAction("android.provider.Telephony.SMS_RECEIVED");
                    registerReceiver(mSMSBroadcastReceiver, filter);
                } else {
                    // The toggle is disabled
                    unregisterReceiver(mSMSBroadcastReceiver);
                }
            }
        });

        switchVibe = (Switch) findViewById(R.id.switch_vibe);
        switchEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setVibration((byte) 0x64);
                } else {
                    setVibration((byte) 0x00);
                }
            }
        });

//        SMSBroadcastReceiver.bindListener(this);

    }

    // level must be between 0x00 and 0x64, where 0x00 is off and 0x64 is max vibration
    private void setVibration(byte level) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mSMSBroadcastReceiver);
    }

    @Override
    public void messageReceived(String message) {
        Toast.makeText(this, "New Message Received: " + message, Toast.LENGTH_LONG).show();
    }

    public void onClick(View view) {
        Intent intent = new Intent(this, DeviceScanActivity.class);
        startActivity(intent);
    }

}
