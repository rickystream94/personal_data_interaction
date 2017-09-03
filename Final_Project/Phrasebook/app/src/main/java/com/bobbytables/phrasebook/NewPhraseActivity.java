package com.bobbytables.phrasebook;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bobbytables.phrasebook.database.DatabaseHelper;
import com.bobbytables.phrasebook.database.PhraseModel;
import com.bobbytables.phrasebook.utils.AlertDialogManager;
import com.bobbytables.phrasebook.utils.DateUtil;
import com.bobbytables.phrasebook.utils.SettingsManager;

import java.util.List;

public class NewPhraseActivity extends AppCompatActivity {

    private String lang1;
    private String lang2;
    private EditText addNewMotherLangPhrase;
    private EditText addNewForeignLangPhrase;
    private DatabaseHelper databaseHelper;
    private AlertDialogManager alertDialogManager;
    private BadgeManager badgeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_phrase);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        lang1 = i.getExtras().getString(SettingsManager.KEY_CURRENT_LANG1);
        lang2 = i.getExtras().getString(SettingsManager.KEY_CURRENT_LANG2);

        TextView foreignLangTextView = (TextView) findViewById(R.id.textView_new_phrase_language);
        foreignLangTextView.setText(lang1 + " - " + lang2);
        Button saveAddMore = (Button) findViewById(R.id.save_and_add_more);
        addNewMotherLangPhrase = (EditText) findViewById(R.id.add_new_mother_lang);
        addNewForeignLangPhrase = (EditText) findViewById(R.id.add_new_foreign_lang);
        saveAddMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNewPhrase();
            }
        });

        databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
        badgeManager = BadgeManager.getInstance(getApplicationContext());
        alertDialogManager = new AlertDialogManager();
    }

    @Override
    //Remember: this method is invoked just once, exactly when the activity is created!
    //The return value states whether the menu will be active for the activity (true) or not (false)
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.new_phrase_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_save_and_close:
                if (saveNewPhrase())
                    finish();
                break;
            default:
                break;
        }
        return false;
    }

    /**
     * Saves to database
     *
     * @return true if successful, false otherwise
     */
    public boolean saveNewPhrase() {
        if (addNewMotherLangPhrase.getText().toString().equals("") || addNewForeignLangPhrase
                .getText().toString().equals("")) {
            alertDialogManager.showAlertDialog(NewPhraseActivity.this, "Error", "Please fill" +
                    " all the fields!", false);
            return false;
        }
        String currentTimeString = DateUtil.getCurrentTimestamp();
        try {
            databaseHelper.insertRecord(new PhraseModel(addNewMotherLangPhrase.getText().toString
                    ().trim().toLowerCase(),
                    addNewForeignLangPhrase.getText().toString().trim().toLowerCase(), currentTimeString,
                    DatabaseHelper.TABLE_PHRASES));
            addNewForeignLangPhrase.setText("");
            addNewMotherLangPhrase.setText("");
            Toast.makeText(getApplicationContext(), "New phrase saved!", Toast.LENGTH_SHORT)
                    .show();
            checkNewBadges();
            return true;
        } catch (Exception e) {
            alertDialogManager.showAlertDialog(NewPhraseActivity.this, "Error!", e.getMessage(), false);
            return false;
        }
    }

    private void checkNewBadges() {
        List<String> achievedBadges = badgeManager.checkNewBadges(BadgeManager.TABLE_PHRASES);
        if (achievedBadges.size() > 0) {
            badgeManager.showDialogAchievedBadges(NewPhraseActivity.this, achievedBadges);
        }
    }
}
