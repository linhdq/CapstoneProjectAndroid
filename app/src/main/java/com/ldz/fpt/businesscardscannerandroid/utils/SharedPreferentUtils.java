package com.ldz.fpt.businesscardscannerandroid.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by linhdq on 5/24/17.
 */

public class SharedPreferentUtils {
    private static final String APP_NAME = "bussiness_card_scanner";
    private static final String PREFERENCES_IS_FIRST_START = "is_first_start";
    private static final String PREFERENCES_IS_FIRST_SCAN = "is_first_scan";
    private final static String PREFERENCES_TRAINING_DATA_DIR = "training_data_dir";
    private final static String PREFERENCES_LANGUAGE_OCR = "language_ocr";

    private SharedPreferences sharedPreferences;

    private static SharedPreferentUtils inst;

    public SharedPreferentUtils(Context context) {
        sharedPreferences = context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
    }

    public static SharedPreferentUtils getInst(Context context) {
        if (inst == null) {
            inst = new SharedPreferentUtils(context);
        }
        return inst;
    }

    public boolean isFirstScan() {
        return sharedPreferences.getBoolean(PREFERENCES_IS_FIRST_SCAN, true);
    }

    public String getTessDir() {
        return sharedPreferences.getString(PREFERENCES_TRAINING_DATA_DIR, null);
    }

    public void saveLanguage(String language) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREFERENCES_LANGUAGE_OCR, language);
        editor.commit();
    }

    public String getLanguage() {
        return sharedPreferences.getString(PREFERENCES_LANGUAGE_OCR, "English");
    }
}
