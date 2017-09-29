package com.bobbytables.phrasebook;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bobbytables.phrasebook.database.DatabaseHelper;
import com.bobbytables.phrasebook.database.DatabaseModel;
import com.bobbytables.phrasebook.database.PhraseModel;
import com.bobbytables.phrasebook.utils.AlertDialogManager;
import com.bobbytables.phrasebook.utils.SettingsManager;

public class EditPhraseActivity extends AppCompatActivity {

    private EditText lang1EditText;
    private EditText lang2EditText;
    private String oldLang1Value;
    private String oldLang2Value;
    private int lang1Code;
    private int lang2Code;
    private static String TAG = EditPhraseActivity.class.getName();

    private DatabaseHelper databaseHelper;
    private AlertDialogManager alertDialogManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_phrase);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
        alertDialogManager = new AlertDialogManager();
        Intent i = getIntent();
        lang1Code = i.getExtras().getInt(DatabaseHelper.KEY_LANG1);
        lang2Code = i.getExtras().getInt(DatabaseHelper.KEY_LANG2);
        TextView lang1Label = (TextView) findViewById(R.id.updateLang1Label);
        TextView lang2Label = (TextView) findViewById(R.id.updateLang2Label);
        lang1EditText = (EditText) findViewById(R.id.updateLang1EditText);
        lang2EditText = (EditText) findViewById(R.id.updateLang2EditText);
        String motherLang = "Phrase in " + i.getExtras().getString(SettingsManager.KEY_CURRENT_LANG1);
        String foreignLang = "Phrase in " + i.getExtras().getString(SettingsManager.KEY_CURRENT_LANG2);
        lang1Label.setText(motherLang);
        lang2Label.setText(foreignLang);
        oldLang1Value = i.getExtras().getString(DatabaseHelper.KEY_LANG1_VALUE);
        oldLang2Value = i.getExtras().getString(DatabaseHelper.KEY_LANG2_VALUE);
        lang1EditText.setText(oldLang1Value);
        lang2EditText.setText(oldLang2Value);
    }

    @Override
    //Remember: this method is invoked just once, exactly when the activity is created!
    //The return value states whether the menu will be active for the activity (true) or not (false)
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_content_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.updateContent:
                updatePhrase();
                break;
            case R.id.deleteContent:
                deletePhrase();
            default:
                break;
        }
        return false;
    }

    private void deletePhrase() {
        String lang1Value = lang1EditText.getText().toString().trim().toLowerCase();
        String lang2Value = lang2EditText.getText().toString().trim().toLowerCase();
        int affectedRows = databaseHelper.deletePhrase(lang1Code, lang2Code, lang1Value, lang2Value);
        if (affectedRows == 0) {
            alertDialogManager.showAlertDialog(EditPhraseActivity.this, "Error", "You tried to " +
                    "delete a phrase that doesn't exist in your Phrasebook! No changes were " +
                    "applied.", false);
        } else {
            Toast.makeText(EditPhraseActivity.this, "Phrase successfully deleted!", Toast
                    .LENGTH_LONG).show();
            int resultCode;
            if (databaseHelper.isDatabaseEmpty()) {
                resultCode = RESULT_CANCELED;
            } else
                resultCode = RESULT_OK;
            setResult(resultCode);
            finish();
        }
    }

    public void updatePhrase() {
        String newLang1Value = lang1EditText.getText().toString().trim().toLowerCase();
        String newLang2Value = lang2EditText.getText().toString().trim().toLowerCase();

        //If no changes were applied, show alert dialog and return
        if (newLang1Value.equals(oldLang1Value) && newLang2Value.equals(oldLang2Value)) {
            alertDialogManager.showAlertDialog(EditPhraseActivity.this, "Error", "No changes were" +
                    " made!", false);
            return;
        }

        //If the phrase the user just edited already exists in the phrasebook, show dialog and
        // return
        DatabaseModel databaseModel = new PhraseModel(newLang1Value, newLang2Value, lang1Code, lang2Code, null,
                DatabaseHelper
                        .TABLE_PHRASES);
        if (databaseHelper.phraseAlreadyExists(databaseModel)) {
            alertDialogManager.showAlertDialog(EditPhraseActivity.this, "Error", "This phrase " +
                    "already exists in your Phrasebook!", false);
            return;
        }
        int affectedPhrases = databaseHelper.updatePhrase(lang1Code, lang2Code, oldLang1Value,
                oldLang2Value, newLang1Value,
                newLang2Value);
        if (affectedPhrases != 1) {
            Log.e(TAG, affectedPhrases + " phrases affected!");
        }
        Toast.makeText(EditPhraseActivity.this, "Phrase successfully updated!", Toast
                .LENGTH_LONG).show();
        finish();
    }
}
