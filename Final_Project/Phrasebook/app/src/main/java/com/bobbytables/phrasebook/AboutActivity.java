package com.bobbytables.phrasebook;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bobbytables.phrasebook.utils.SettingsManager;

public class AboutActivity extends AppCompatActivity {

    private SettingsManager settingsManager;
    private int unlockCounter;
    private static final int TAPS_TO_UNLOCK = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        settingsManager = SettingsManager.getInstance(this);
        unlockCounter = 0; //Reset counter
    }

    //TODO: it will not be needed anymore in next release
    public void unlockFullApp(View view) {
        unlockCounter++;
        if (unlockCounter == TAPS_TO_UNLOCK) {
            settingsManager.updatePrefValue(SettingsManager.KEY_GAMIFICATION, true);
            settingsManager.updatePrefValue(SettingsManager.KEY_FINAL_UPLOAD_PERFORMED, true);
            settingsManager.updatePrefValue(SettingsManager.KEY_SWITCHED_VERSION, true);
            Toast.makeText(this, "Full version unlocked!", Toast.LENGTH_SHORT).show();
        }
    }
}
