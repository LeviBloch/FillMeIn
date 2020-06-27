package com.example.android.fillmein;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.util.Objects;

public class SMSBroadcastReceiver extends BroadcastReceiver {


//    private static MessageListener mListener;
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        Bundle data = intent.getExtras();
//        assert data != null;
//        Object[] pdus = (Object[]) data.get("pdus");
//        assert pdus != null;
//        for (Object o : pdus) {
//            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) o);
//            String message = "Sender : " + smsMessage.getDisplayOriginatingAddress()
//                    + "Email From: " + smsMessage.getEmailFrom()
//                    + "Email Body: " + smsMessage.getEmailBody()
//                    + "Display message body: " + smsMessage.getDisplayMessageBody()
//                    + "Time in millisecond: " + smsMessage.getTimestampMillis()
//                    + "Message: " + smsMessage.getMessageBody();
//            mListener.messageReceived(message);
//        }
//    }
//
//    public static void bindListener(MessageListener listener){
//        mListener = listener;
//    }

    private Bundle bundle;
    private SmsMessage currentSMS;
    private String message;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Objects.equals(intent.getAction(), "android.provider.Telephony.SMS_RECEIVED")) {
            bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdu_Objects = (Object[]) bundle.get("pdus");
                if (pdu_Objects != null) {

                    for (Object aObject : pdu_Objects) {

                        currentSMS = getIncomingMessage(aObject, bundle);

                        String senderNo = currentSMS.getDisplayOriginatingAddress();

                        message = currentSMS.getDisplayMessageBody();
                        Toast.makeText(context, "senderNum: " + senderNo + " :\n message: " + message, Toast.LENGTH_LONG).show();
                    }
                    this.abortBroadcast();
                    // End of loop
                }
            }
        } // bundle null
    }

    private SmsMessage getIncomingMessage(Object aObject, Bundle bundle) {
        SmsMessage currentSMS;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String format = bundle.getString("format");
            currentSMS = SmsMessage.createFromPdu((byte[]) aObject, format);
        } else {
            currentSMS = SmsMessage.createFromPdu((byte[]) aObject);
        }
        return currentSMS;

    }
}
