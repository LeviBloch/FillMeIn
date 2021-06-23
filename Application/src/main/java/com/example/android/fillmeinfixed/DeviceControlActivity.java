/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.fillmeinfixed;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends AppCompatActivity implements SMSReceivedCallback {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";


    private Switch switchIncludeSenderNum;
    boolean includeSenderNum;

    private SeekBar vibrationIntensitySeekBar;
    byte vibrationIntensityOn = 0x64;

    private SeekBar morseTimeUnitSeekBar;
    private TextView morseTimeUnitTextView;

    // length of time, in milliseconds, of one dot in morse code
    // wikipedia uses the example of 50ms
    // does not seem to work on anything much lower than 100ms.
    private int morseTimeUnit = 200;


    private String mDeviceName;
    private String mDeviceAddress;
    private ArrayList<BluetoothGattService> gattServices;
    private BluetoothLeService mBluetoothLeService;

    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final int SMS_REQUEST_CODE = 2;

    BroadcastReceiver mSMSBroadcastReceiver = new SMSBroadcastReceiver(this);
    Queue<String> messageQueue = new PriorityQueue<>();
    boolean isSendingMessage = false;


    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // make sure to turn off vibration when the device disconnects
            mBluetoothLeService.setVibration(gattServices, (byte) 0x00);
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

                gattServices = (ArrayList<BluetoothGattService>) mBluetoothLeService.getSupportedGattServices();

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
            }
        }
    };





    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);

        // set bars at top and bottom to transparent
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        // init views
        initSwitchIncludeSenderNum();
        initVibrationIntensitySeekBar();
        initMorseTimeUnitSeekBar();


        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);


//        getSupportActionBar().setTitle(mDeviceName);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);


        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, SMS_REQUEST_CODE);
        }

        // start listening for new sms broadcasts
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(mSMSBroadcastReceiver, filter);

    }

    private void initMorseTimeUnitSeekBar() {
        morseTimeUnitSeekBar = findViewById(R.id.morseTimeUnitSeekBar);
        morseTimeUnitTextView = findViewById(R.id.morseTimeUnitTextView);

        // physical values in milliseconds
        final int min = 100;
        final int max = 600;
        final int initialProgress = 200;
        // difference between selectable values
        // i.e. if resolution were 50, then the values might go 100, 150, 200, 250, etc.
        final int resolution = 50;

        // the tick marks don't work properly when using setMin
        // Maybe there's a fix, but this works just as well
        morseTimeUnitSeekBar.setMax((max-min)/resolution);

        morseTimeUnitSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                morseTimeUnit = progress*resolution + min;
                morseTimeUnitTextView.setText(getResources().getString(R.string.morseTimeUnit, morseTimeUnit));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        
        morseTimeUnitSeekBar.setProgress((initialProgress-min)/resolution);
    }

    private void initSwitchIncludeSenderNum() {
        switchIncludeSenderNum = findViewById(R.id.switchIncludeSenderNum);
        switchIncludeSenderNum.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                includeSenderNum = isChecked;
            }
        });
    }

    // helper method to avoid cluttering up onCreate()
    private void initVibrationIntensitySeekBar() {
        vibrationIntensitySeekBar = findViewById(R.id.vibrationIntensitySeekBar);
        vibrationIntensitySeekBar.setMax(64);
        vibrationIntensitySeekBar.setProgress(32);
        vibrationIntensitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                vibrationIntensityOn = (byte) progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;

        unregisterReceiver(mSMSBroadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }




    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());

    private void updateVibrationFromMorse(final String morse) {

        //base case
        if (mBluetoothLeService == null) {
            Log.w("DeviceControlActivity", "In updateVibrationFromMorse, mBluetoothLeService is null");
            return;
        }
        if (morse.length() == 0) {
            if (messageQueue.size() == 0) { // all out of text messages
                // turn off vibration in case it is still on
                mBluetoothLeService.setVibration(gattServices, (byte) 0x00);

                isSendingMessage = false;
            } else { // get the next message in the queue
                updateVibrationFromMorse(messageQueue.poll());
            }
            return;
        }
        //recursive case
        if (morse.charAt(0) == '0') {
            mBluetoothLeService.setVibration(gattServices, (byte) 0x00);
        }
        else if (morse.charAt(0) == '1') {
            mBluetoothLeService.setVibration(gattServices, (byte) vibrationIntensityOn);
        }
        else {
            Log.wtf("updateVibrationFromMorse", "Unexpected char in morse string: " + morse.charAt(0) + ", should only be 1 or 0");
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateVibrationFromMorse(morse.substring(1));
            }
        }, morseTimeUnit);

    }

    @Override
    public void receivedSMS(String senderNum, String body) {
        String message = "";
        if (includeSenderNum) {
            message = "From " + senderNum + ": ";
        }
        message += body;

        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        String morseMessage = new MorseCodeTranslator(getApplicationContext()).convertTextToMorse(message);

        if (!isSendingMessage) {
            isSendingMessage = true;
            updateVibrationFromMorse(morseMessage);
        } else {
            messageQueue.offer(morseMessage);
        }
    }
}
