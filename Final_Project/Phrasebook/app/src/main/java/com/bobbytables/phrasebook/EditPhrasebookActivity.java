package com.bobbytables.phrasebook;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.bobbytables.phrasebook.database.DatabaseHelper;
import com.bobbytables.phrasebook.utils.AlertDialogManager;
import com.bobbytables.phrasebook.utils.SettingsManager;

public class EditPhrasebookActivity extends AppCompatActivity {

    private static final int ACTIVITY_RESULT_CODE = 2;
    private DatabaseHelper databaseHelper;
    private AlertDialogManager alertDialogManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_phrasebook);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        databaseHelper = DatabaseHelper.getInstance(EditPhrasebookActivity.this);
        alertDialogManager = new AlertDialogManager();
        Intent i = getIntent();
        Bundle bundle = i.getExtras();
        int lang1Id = bundle.getInt(SettingsManager.KEY_CURRENT_LANG1);
        int lang2Id = bundle.getInt(SettingsManager.KEY_CURRENT_LANG2);
        Phrasebook phrasebook = new Phrasebook(lang1Id, lang2Id, EditPhrasebookActivity.this);
        TextView phrasebookName = (TextView) findViewById(R.id.phrasebook_name);
        phrasebookName.setText(phrasebook.toString());

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
            case R.id.updateContent: //// TODO: to implement
                break;
            case R.id.deleteContent: //TODO: to implement
            default:
                break;
        }
        return false;
    }
}
