package com.example.android.fillmeinfixed;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

public class MessageDeliveryJobIntentService extends JobIntentService {

    // unique job ID for this service
    static final int JOB_ID = 1000;

    // could be a HashMap<Character, String> but this leaves open potential functionality in the future (emojis?)
    private static HashMap<String, String> textToMorse;
    static String morseCodeFilename = "morse_code.properties";

    /**
     * Convenience method for enqueuing work in to this service.
     */
    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, MessageDeliveryJobIntentService.class, JOB_ID, work);
    }



    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        if (textToMorse == null) {
            populateMorseCodeHashMap(morseCodeFilename);
        }

        toast("Penis Music in morse code is: " + convertTextToMorse("Penis Music"));

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void populateMorseCodeHashMap(String filename) {

        textToMorse = new HashMap<>();

        InputStream inputStream;
        try {
            inputStream = getBaseContext().getAssets().open(filename);
            Properties properties = new Properties();
            properties.load(inputStream);

            Log.d("morse hash map", "Successfully opened " + filename);

            for (String text : properties.stringPropertyNames()) {
                String morseCode = properties.getProperty(text);
                textToMorse.put(text, morseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("morse hash map", "failed to open " + filename);
        }

    }

    private String convertTextToMorse(String str) {

        str = str.toLowerCase();

        StringBuilder output = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            // could use str.charAt if textToMorse was a HashMap<Character, String>
            if (textToMorse.containsKey(str.substring(i, i+1))) {
                output.append(textToMorse.get(str.substring(i, i+1)));
                output.append(" ");
            }
        }

        return output.toString();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        toast("All work complete");
    }

    final Handler mHandler = new Handler();

    // Helper for showing tests
    void toast(final CharSequence text) {
        mHandler.post(new Runnable() {
            @Override public void run() {
                Toast.makeText(MessageDeliveryJobIntentService.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    void sendVibrationIntensity(byte intensity) {
        //jesus fucking christ, how hard is it to send a fucking byte???
        Bundle bundle = new Bundle();
        // TODO: 7/8/2020 make this key a constant somewhere
        bundle.putByte("intensity", intensity);
        Message msg = new Message();
        msg.obj = bundle;
        mHandler.sendMessage(msg);
    }
}
