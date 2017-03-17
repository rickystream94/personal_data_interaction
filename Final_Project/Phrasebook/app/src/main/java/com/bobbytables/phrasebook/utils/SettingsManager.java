package com.bobbytables.phrasebook.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.bobbytables.phrasebook.MainActivity;
import com.bobbytables.phrasebook.NewUserActivity;

/**
 * Created by ricky on 15/03/2017.
 */

public class SettingsManager {

    private SharedPreferences preferences;
    private Editor editor;
    private Context context;
    // Shared preferences file name
    private static final String PREF_NAME = "MyPref";
    // All Shared Preferences Keys
    private static final String KEY_USER_EXISTS = "userExists";
    private static final String KEY_IS_FIRST_TIME = "isFirstTime"; //might be used for launch
    // tutorial
    // User name (make variable public to access from outside)
    public static final String KEY_NICKNAME = "nickname";
    public static final String KEY_MOTHER_LANGUAGE = "nickname";
    public static final String KEY_FOREIGN_LANGUAGE = "nickname";
    //GAMIFICATION INCLUDED OR NOT
    private static final String KEY_GAMIFICATION = "Gamification";

    public SettingsManager(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putBoolean(KEY_GAMIFICATION, false); //Change this for future release!!!
        editor.apply();
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

    public void createUser(String nickname, String motherLanguage, String foreignLanguage) {
        editor.putString(KEY_NICKNAME, nickname);
        editor.putString(KEY_MOTHER_LANGUAGE, motherLanguage.toUpperCase());
        editor.putString(KEY_FOREIGN_LANGUAGE, foreignLanguage.toUpperCase());
        editor.putBoolean(KEY_USER_EXISTS, true);
        editor.commit();
    }
}
