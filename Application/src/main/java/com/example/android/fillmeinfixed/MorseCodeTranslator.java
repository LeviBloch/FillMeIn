package com.example.android.fillmeinfixed;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

public class MorseCodeTranslator {

    // could be a HashMap<Character, String> but this leaves open potential functionality in the future (emojis?)
    private HashMap<String, String> textToMorse;

    static String morseCodeFilename = "morse_code.properties";

    private Context context;


    public MorseCodeTranslator(Context context) {
        this.context = context;
        populateMorseCodeHashMap(morseCodeFilename);
    }



    private void populateMorseCodeHashMap(String filename) {

        textToMorse = new HashMap<>();

        InputStream inputStream;
        try {
            inputStream = context.getAssets().open(filename);
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

    public String convertTextToReadableMorse(String str) {

        str = str.toLowerCase();

        StringBuilder output = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            // could use str.charAt if textToMorse was a HashMap<Character, String>
            if (textToMorse.containsKey(str.substring(i, i+1))) {
                output.append(textToMorse.get(str.substring(i, i+1)));

                // fence post problem; get rid of final " "
                if (i < str.length()-1) {
                    output.append(" ");
                }
            }
        }


        return output.toString();
    }

    /**
     * morse converter that's easier for the code
     * @param str text to be changed
     * @return One unit of "on" signal is represented by a 1, and one unit of "off" is represented by a 0. For convenience, the output is a string.
     */
    public String convertTextToMorse(String str) {
        str = str.toLowerCase();
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            char currentChar = str.charAt(i);

            //space between words, 7 units of off
            if (currentChar == ' ') {
                output.append("0000000");
            }

            else if (textToMorse.containsKey(str.substring(i, i+1))) {
                String currentMorse = textToMorse.get(str.substring(i, i+1));
                assert currentMorse != null;
                for (int j = 0; j < currentMorse.length(); j++) {
                    if (currentMorse.charAt(j) == '.') {
                        output.append("1");
                    }
                    else if (currentMorse.charAt(j) == '_') {
                        output.append("111");
                    }
                    else {
                        Log.w("convertTextToMorse", "Unknown character in textToMorse HashMap. The value for " + currentChar + " contains unknown character " + currentMorse.charAt(j));
                    }

                    // inter-element gap
                    // fence post problem
                    if (j < currentMorse.length()-1) {
                        output.append("0");
                    }
                }

                // gap between letters
                if (i < str.length()-1) {
                    output.append("000");
                }
            }
        }

        // make sure there's a gap if outputting multiple messages back to back
        output.append("0000000");

        return output.toString();
    }
}
