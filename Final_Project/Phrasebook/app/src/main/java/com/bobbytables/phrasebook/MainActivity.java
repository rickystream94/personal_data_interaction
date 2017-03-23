package com.bobbytables.phrasebook;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.TabLayoutOnPageChangeListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bobbytables.phrasebook.database.DatabaseHelper;
import com.bobbytables.phrasebook.utils.AlertDialogManager;
import com.bobbytables.phrasebook.utils.SettingsManager;

import static com.bobbytables.phrasebook.R.id.tabLayout;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private AlertDialogManager alertDialogManager = new AlertDialogManager();
    private SettingsManager settingsManager;
    private PagerAdapter pagerAdapter;
    private ViewPager pager;
    public static Handler killerHandler;
    private String motherLanguage;
    private String foreignLanguage;
    private DatabaseHelper databaseHelper;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize the killer handler, such that another activity can kill the current one
        killerHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        finish();
                        break;
                }
            }
        };

        databaseHelper = DatabaseHelper.getInstance(getApplicationContext());

        //Get settings manager
        settingsManager = SettingsManager.getInstance(getApplicationContext());
        //Check always if it's the first time
        //Will invoke automatically NewUserActivity
        settingsManager.createUserProfile();
        motherLanguage = settingsManager.getPrefStringValue(SettingsManager.KEY_MOTHER_LANGUAGE);
        foreignLanguage = settingsManager.getPrefStringValue(SettingsManager.KEY_FOREIGN_LANGUAGE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializePager();
        initFloatingActionButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Needed to refresh the layout of the fragments and always display the
        // most up-to-date content
        initializePager();
    }

    @Override
    //Remember: this method is invoked just once, exactly when the activity is created!
    //The return value states whether the menu will be active for the activity (true) or not (false)
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.delete_data:
                //ADD CONFIRMATION ALERT DIALOG: Are you sure This will delete all the data!
                databaseHelper.reset();
                Toast.makeText(this, "All data successfully deleted!", Toast
                        .LENGTH_SHORT)
                        .show();
                initializePager();
                break;
            case R.id.export_data:
                checkWritePermissions();
            default:
                break;
        }
        return false;
    }

    /**
     * Setting floating action button with onClickListener
     */
    private void initFloatingActionButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), NewPhraseActivity.class);
                i.putExtra(SettingsManager.KEY_MOTHER_LANGUAGE, motherLanguage);
                i.putExtra(SettingsManager.KEY_FOREIGN_LANGUAGE, foreignLanguage);
                startActivity(i);
            }
        });
    }

    /**
     * Initializes the ViewPager and its adapter
     */
    private void initializePager() {
        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        //used to change tab when selected
        tabLayout.addOnTabSelectedListener(this);
        pagerAdapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(pagerAdapter);
        //Used for changing selected tab when swiping right/left
        pager.addOnPageChangeListener(new TabLayoutOnPageChangeListener(tabLayout));
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        pager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        //Not necessary
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        //Not necessary
    }

    private void checkWritePermissions() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // We request the permission.
                Log.e("requesting", "write external permissions");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        } else
            exportData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    exportData();
                } else {
                    Toast.makeText(this, "Error, permission not granted!", Toast
                            .LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void exportData() {
        databaseHelper.exportToJSON(getApplicationContext());
        Toast.makeText(this, "Exported in Downloads/Phrasebook_Exports as JSON file", Toast
                .LENGTH_SHORT)
                .show();
    }
}
