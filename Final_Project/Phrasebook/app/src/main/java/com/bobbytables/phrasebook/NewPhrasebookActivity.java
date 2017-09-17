package com.bobbytables.phrasebook;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bobbytables.phrasebook.database.DatabaseHelper;
import com.bobbytables.phrasebook.utils.AlertDialogManager;
import com.bobbytables.phrasebook.utils.SettingsManager;

public class NewPhrasebookActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private AlertDialogManager alertDialogManager = new AlertDialogManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_phrasebook);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        databaseHelper = DatabaseHelper.getInstance(NewPhrasebookActivity.this);
    }

    /**
     * Creates a new phrasebook with the specified languages that are inserted by the user in the
     * edittext fields. It additionally checks for invalid input.
     *
     * @param view
     */
    public void createPhrasebook(View view) {
        EditText lang1EditText = (EditText) findViewById(R.id.new_phrasebook_lang1);
        EditText lang2EditText = (EditText) findViewById(R.id.new_phrasebook_lang2);
        String lang1 = lang1EditText.getText().toString();
        String lang2 = lang2EditText.getText().toString();
        if (!lang1.equals("") && !lang2.equals("")) {
            try {
                databaseHelper.createPhrasebook(lang1.toUpperCase(), lang2.toUpperCase());
                Toast.makeText(NewPhrasebookActivity.this, "New com.bobbytables.phrasebook.Phrasebook created!", Toast
                        .LENGTH_LONG).show();
                finish();
            } catch (Exception e) {
                alertDialogManager.showAlertDialog(NewPhrasebookActivity.this, "Error!", e
                        .getMessage(), false);
            }
        } else
            alertDialogManager.showAlertDialog(NewPhrasebookActivity.this, "Invalid input!",
                    "No empty fields are allowed.", false);
    }
}
