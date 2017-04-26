package com.bobbytables.phrasebook.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;

import com.bobbytables.phrasebook.MainActivity;
import com.bobbytables.phrasebook.NewUserActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * Created by ricky on 15/03/2017.
 */

public class SettingsManager {

    private static SettingsManager instance;
    private SharedPreferences preferences;
    private Editor editor;
    private Context context;
    // Shared preferences file name
    private static final String PREF_NAME = "MyPref";
    // All Shared Preferences Keys
    private static final String KEY_USER_EXISTS = "userExists";
    public static final String KEY_IS_FIRST_TIME = "isFirstTime";
    // User name
    public static final String KEY_NICKNAME = "nickname";
    public static final String KEY_MOTHER_LANGUAGE = "motherLanguage";
    public static final String KEY_FOREIGN_LANGUAGE = "foreignLanguage";
    public static final String KEY_TOTAL_XP = "totalXP";
    public static final String KEY_LEVEL = "level";
    public static final String KEY_CREATED = "created";
    //GAMIFICATION INCLUDED OR NOT
    public static final String KEY_GAMIFICATION = "Gamification";
    //TODO: remove unnecessary keys in future releases
    public static final String KEY_SWITCHED_VERSION = "hasSwitchedVersion";
    public static final String KEY_FINAL_UPLOAD_PERFORMED = "lastUploadPerformed";
    public static final String KEY_PROFILE_PIC = "profilePic";

    private SettingsManager(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.apply();
        instance = this;
    }

    /**
     * Singleton method to retrieve settings manager
     *
     * @param context
     * @return
     */
    public static SettingsManager getInstance(Context context) {
        if (instance == null)
            return new SettingsManager(context);
        return instance;
    }

    public void createUserProfile() {
        boolean userProfileExists = preferences.getBoolean(KEY_USER_EXISTS, false);
        if (userProfileExists)
            return;
        //User must be redirected to first activity
        Intent i = new Intent(context, NewUserActivity.class);
        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Starting Login Activity
        context.startActivity(i);
        //Kill main activity
        MainActivity.killerHandler.sendEmptyMessage(0);
    }

    public void createUser(String nickname, String motherLanguage, String foreignLanguage,
                           boolean gamification) {
        String currentTimeString = DateUtil.getCurrentTimestamp();
        editor.putString(KEY_NICKNAME, nickname);
        editor.putString(KEY_MOTHER_LANGUAGE, motherLanguage.toUpperCase());
        editor.putString(KEY_FOREIGN_LANGUAGE, foreignLanguage.toUpperCase());
        editor.putBoolean(KEY_USER_EXISTS, true);
        editor.putInt(KEY_TOTAL_XP, 0);
        editor.putInt(KEY_LEVEL, 0);
        editor.putString(KEY_CREATED, currentTimeString);
        editor.putString(KEY_PROFILE_PIC, "DEFAULT"); //is updated in version 2!
        //TODO: now is performed randomly, but it will be changed to true after experiment
        editor.putBoolean(KEY_GAMIFICATION, gamification);
        editor.putBoolean(KEY_SWITCHED_VERSION, false); //TODO: TO BE REMOVED after experiment
        editor.putBoolean(KEY_FINAL_UPLOAD_PERFORMED, false);
        editor.putBoolean(KEY_IS_FIRST_TIME, true);
        editor.commit();
    }

    /**
     * Get a generic shared preference string value given a specific key
     *
     * @param key
     * @return
     */
    public String getPrefStringValue(String key) {
        return preferences.getString(key, "");
    }

    /**
     * Get a generic shared preference integer value given a specific key
     *
     * @param key
     * @return
     */
    public int getPrefIntValue(String key) {
        return preferences.getInt(key, -1);
    }

    /**
     * Get a generic shared preference boolean value given a specific key
     *
     * @param key
     * @return
     */
    public boolean getPrefBoolValue(String key) {
        return preferences.getBoolean(key, false);
    }

    /**
     * It will be used to update the current level of the user
     *
     * @param key
     * @param value
     */
    public void updatePrefValue(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    /**
     * Used to update profile pic
     *
     * @param key
     * @param value
     */
    public void updatePrefValue(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public void updatePrefValue(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    /**
     * It will be used to add experience points
     *
     * @param key
     * @param newValue
     */
    public void addValue(String key, int newValue) {
        int currentValue = preferences.getInt(key, 0);
        editor.putInt(key, currentValue + newValue);
        editor.commit();
    }

    /**
     * Returns all user's data in a JSONObject, used to export data
     *
     * @return
     */
    public JSONObject getUserData() throws JSONException {
        JSONObject userData = new JSONObject();
        userData.put(KEY_NICKNAME, getPrefStringValue(KEY_NICKNAME));
        userData.put(KEY_MOTHER_LANGUAGE, getPrefStringValue(KEY_MOTHER_LANGUAGE));
        userData.put(KEY_FOREIGN_LANGUAGE, getPrefStringValue(KEY_FOREIGN_LANGUAGE));
        userData.put(KEY_CREATED, getPrefStringValue(KEY_CREATED));
        userData.put(KEY_LEVEL, getPrefIntValue(KEY_LEVEL));
        userData.put(KEY_TOTAL_XP, getPrefIntValue(KEY_TOTAL_XP));
        userData.put(KEY_GAMIFICATION, getPrefBoolValue(KEY_GAMIFICATION) ? 1 : 0);
        userData.put(KEY_SWITCHED_VERSION, getPrefBoolValue(KEY_SWITCHED_VERSION) ? 1 : 0);
        return userData;
    }

    public void resetXP() {
        editor.putInt(KEY_TOTAL_XP, 0);
        editor.putInt(KEY_LEVEL, 0);
        editor.commit();
    }
}
