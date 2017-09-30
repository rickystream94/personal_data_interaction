package com.bobbytables.phrasebook;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.bobbytables.phrasebook.database.DatabaseHelper;
import com.bobbytables.phrasebook.utils.AlertDialogManager;
import com.bobbytables.phrasebook.utils.SettingsManager;

import java.util.List;

public class EditPhrasebookActivity extends AppCompatActivity {

    private static String TAG = EditPhraseActivity.class.getName();
    private DatabaseHelper databaseHelper;
    private AlertDialogManager alertDialogManager;
    private int oldLang1Code;
    private int oldLang2Code;
    private String oldLang1;
    private String oldLang2;
    private EditText lang1ValueEditText;
    private EditText lang2ValueEditText;
    public static final int REQUEST_CODE = 2;
    public static final int NO_ACTION_RESULT_CODE = -2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_phrasebook);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        databaseHelper = DatabaseHelper.getInstance(EditPhrasebookActivity.this);
        alertDialogManager = new AlertDialogManager();
        Intent i = getIntent();
        Bundle bundle = i.getExtras();
        oldLang1Code = bundle.getInt(SettingsManager.KEY_CURRENT_LANG1);
        oldLang2Code = bundle.getInt(SettingsManager.KEY_CURRENT_LANG2);
        oldLang1 = databaseHelper.getLanguageName(oldLang1Code);
        oldLang2 = databaseHelper.getLanguageName(oldLang2Code);
        lang1ValueEditText = (EditText) findViewById(R.id.edit_phrasebook_lang1_value);
        lang2ValueEditText = (EditText) findViewById(R.id.edit_phrasebook_lang2_value);
        lang1ValueEditText.setText(oldLang1);
        lang2ValueEditText.setText(oldLang2);
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
                updatePhrasebook();
                break;
            case R.id.deleteContent: //TODO: to implement
                deletePhrasebook();
                break;
            case android.R.id.home:
                noAction();
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        noAction();
        super.onBackPressed();
    }

    private void noAction() {
        setResult(NO_ACTION_RESULT_CODE);
    }

    /**
     * Deletes the current phrasebook, with all data related to it (phrases and challenges). It
     * doesn't delete the languages from the lang tables. It sets the current phrasebook to the
     * first phrasebook in the list
     */
    private void deletePhrasebook() {
        List<Phrasebook> phrasebookList = databaseHelper.getAllPhrasebooks();
        if (phrasebookList.size() == 1) {
            //It means that the current phrasebook is the only one and can't be deleted
            alertDialogManager.showAlertDialog(EditPhrasebookActivity.this, "Error!", "You need " +
                            "to have at least one existing Phrasebook.",
                    false);
            return;
        }

        //Inform with confirmation dialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(EditPhrasebookActivity.this);
        builder.setMessage("Are you sure? All data related to this Phrasebook will be " +
                "irreversibly lost, including phrases and challenges!")
                .setTitle("Confirm?")
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            databaseHelper.deletePhrasebook(oldLang1Code, oldLang2Code);
                            Toast.makeText(EditPhrasebookActivity.this, "Phrasebook successfully " +
                                    "deleted!", Toast.LENGTH_LONG).show();
                            setResult(RESULT_CANCELED);
                            finish();
                        } catch (Exception e) {
                            alertDialogManager.showAlertDialog(EditPhrasebookActivity.this,
                                    "Error!", e.getMessage(), false);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Do nothing...
                    }
                }).show();
    }

    /***
     * Performs update of phrasebook when languages are changed. It automatically will also
     * create the new languages and check if a phrasebook is already existing for them. If
     * successful, finish activity and send ok result code to mainActivity to refresh ui.
     */
    private void updatePhrasebook() {
        String newLang1Value = lang1ValueEditText.getText().toString().trim().toUpperCase();
        String newLang2Value = lang2ValueEditText.getText().toString().trim().toUpperCase();

        //If no changes were applied, show alert dialog and return
        if (newLang1Value.equals(oldLang1) && newLang2Value.equals(oldLang2)) {
            alertDialogManager.showAlertDialog(EditPhrasebookActivity.this, "Error", "No changes were" +
                    " made!", false);
            return;
        }

        try {
            databaseHelper.updatePhrasebook(oldLang1Code, oldLang2Code,
                    newLang1Value,
                    newLang2Value);
            Toast.makeText(EditPhrasebookActivity.this, "Phrasebook successfully updated!", Toast
                    .LENGTH_LONG).show();
            setResult(RESULT_OK); //MainActivity should always refresh if changes were applied
            finish();
        } catch (Exception e) {
            alertDialogManager.showAlertDialog(EditPhrasebookActivity.this, "Error!", e.getMessage
                    (), false);
        }
    }
}
