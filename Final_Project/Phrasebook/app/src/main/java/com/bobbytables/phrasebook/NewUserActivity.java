package com.bobbytables.phrasebook;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.bobbytables.phrasebook.database.DatabaseHelper;
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
        String lang1Value = ((TextView) findViewById(R.id.lang1Value)).getText()
                .toString();
        String lang2Value = ((TextView) findViewById(R.id.lang2Value)).getText()
                .toString();
        String errorMessage = "";
        if (nickname.length() < NICKNAME_MIN_LENGTH)
            errorMessage += "Nickname too short, it must be long at least " + NICKNAME_MIN_LENGTH + " " +
                    "characters\n";
        if (lang1Value.length() < LANG_MIN_LENGTH)
            errorMessage += "Mother language name too short, it must be long at least " +
                    "" + LANG_MIN_LENGTH + " characters\n";
        if (lang2Value.length() < LANG_MIN_LENGTH)
            errorMessage += "Foreign language name too short, it must be long at least " +
                    "" + LANG_MIN_LENGTH + " characters\n";
        if (errorMessage.length() > 0) {
            alertDialogManager.showAlertDialog(NewUserActivity.this, "Error!", errorMessage, false);
            return;
        }
        //Otherwise, if everything is fine, proceed
        SettingsManager settingsManager = SettingsManager.getInstance(getApplicationContext());
        DatabaseHelper db = DatabaseHelper.getInstance(getApplicationContext());
        try {
            db.createPhrasebook(lang1Value, lang2Value);
            int lang1Code = db.getLanguageId(lang1Value);
            int lang2Code = db.getLanguageId(lang2Value);
            settingsManager.createUser(nickname, lang1Code, lang2Code);
        } catch (Exception ex) {
            //Raised if a phrasebook with the specified languages already exists
            //(Of course it will never happen when user is created, but we have to catch the
            // exception anyway)
            alertDialogManager.showAlertDialog(NewUserActivity.this, "Error!", ex.getMessage(), false);
            return;
        }
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        finish();
    }
}
