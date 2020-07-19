package com.example.android.fillmeinfixed;

// this interface allows the SMSBroadcastReceiver to communicate with DeviceControlActivity
public interface SMSReceivedCallback {
    void receivedSMS(String senderNum, String message);
}
