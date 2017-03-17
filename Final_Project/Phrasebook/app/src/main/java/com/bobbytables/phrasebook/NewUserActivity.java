package com.bobbytables.phrasebook;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.bobbytables.phrasebook.utils.AlertDialogManager;
import com.bobbytables.phrasebook.utils.SettingsManager;

public class NewUserActivity extends AppCompatActivity {

    private AlertDialogManager alertDialogManager;
    private static final int NICKNAME_MIN_LENGTH = 4;
    private static final int LANG_MIN_LENGTH = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);
        alertDialogManager = new AlertDialogManager();
    }

    public void createUser(View view) {
        String nickname = ((TextView) findViewById(R.id.nicknameText)).getText()
                .toString();
        String motherLanguage = ((TextView) findViewById(R.id.motherLanguage)).getText()
                .toString();
        String foreignLanguage = ((TextView) findViewById(R.id.foreignLanguage)).getText()
                .toString();
        String errorMessage = "";
        if (nickname.length() < NICKNAME_MIN_LENGTH)
            errorMessage += "Nickname too short, it must be long at least " + NICKNAME_MIN_LENGTH + " " +
                    "characters\n";
        if (motherLanguage.length() < LANG_MIN_LENGTH)
            errorMessage += "Mother language name too short, it must be long at least " +
                    "" + LANG_MIN_LENGTH + " characters\n";
        if (foreignLanguage.length() < LANG_MIN_LENGTH)
            errorMessage += "Foreign language name too short, it must be long at least " +
                    "" + LANG_MIN_LENGTH + " characters\n";
        if (errorMessage.length() > 0) {
            alertDialogManager.showAlertDialog(NewUserActivity.this, "Error!", errorMessage, false);
            return;
        }
        //Otherwise, if everything is fine, proceed
        SettingsManager settingsManager = new SettingsManager(getApplicationContext());
        settingsManager.createUser(nickname, motherLanguage, foreignLanguage);
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        finish();
    }
}
