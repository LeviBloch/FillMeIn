package com.example.android.fillmein;

import java.util.HashMap;

public class GattAttributes {
    private static HashMap<String, String> attributes = new HashMap<>();
    public static String VIBRATION_INTENSITY_SERVICE = "78667579-7b48-43db-b8c5-7928a6b0a335";
    public static String VIBRATION_INTENSITY_CHARACTERISTIC = "78667579-a914-49a4-8333-aa3c0cd8fedc";

    static {
        // services
        attributes.put(VIBRATION_INTENSITY_SERVICE, "Vibration Intensity Service");

        //characteristics
        attributes.put(VIBRATION_INTENSITY_CHARACTERISTIC, "Vibration Intensity Characteristic");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
