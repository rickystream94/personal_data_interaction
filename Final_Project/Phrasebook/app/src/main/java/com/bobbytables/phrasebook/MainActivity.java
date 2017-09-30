package com.bobbytables.phrasebook;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bobbytables.phrasebook.database.DatabaseHelper;
import com.bobbytables.phrasebook.utils.AlertDialogManager;
import com.bobbytables.phrasebook.utils.SettingsManager;

import java.util.List;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private AlertDialogManager alertDialogManager = new AlertDialogManager();
    private SettingsManager settingsManager;
    private FloatingActionButton fab;
    public static Handler killerHandler;
    private DatabaseHelper databaseHelper;
    private DrawerLayout mDrawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private Fragment fragment;
    private FragmentManager fragmentManager;
    private BottomNavigationView navigation;
    private List<Phrasebook> allPhrasebooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get helper classes
        databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
        settingsManager = SettingsManager.getInstance(getApplicationContext());

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

        //Check always if it's the first time
        //Will invoke automatically NewUserActivity
        settingsManager.createUserProfile();

        //Get fragment manager and add default fragment
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        fragment = new CardsFragment();
        transaction.add(R.id.frame_layout, fragment).commit();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.tab1); //Set app bar title for default fragment

        //Initialize drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, mDrawerLayout, toolbar, R
                .string.navigation_drawer_open, R.string.navigation_drawer_close);

        //Initialize bottom navigation
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);

        //Get list of phrasebooks in drawer list
        refreshPhrasebooks();

        //Initialize fab
        initFloatingActionButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPhrasebooks();
    }

    /**
     * Method used to initialize the default navigation fragment when refreshing main activity
     * content
     */
    private void refreshUi() {
        navigation.setSelectedItemId(R.id.navigation_practice);
        mDrawerLayout.closeDrawers();
    }

    private void refreshPhrasebooks() {
        allPhrasebooks = databaseHelper.getAllPhrasebooks();
        String[] names = new String[allPhrasebooks.size()];
        for (Phrasebook phrasebook : allPhrasebooks)
            names[allPhrasebooks.indexOf(phrasebook)] = phrasebook.toString();
        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, R.id
                .drawer_list_item_id, names));
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Phrasebook phrasebook = allPhrasebooks.get(position);
                switchToPhrasebook(phrasebook);
            }
        });
    }

    private void switchToPhrasebook(Phrasebook phrasebook) {
        settingsManager.updatePrefValue(SettingsManager.KEY_CURRENT_LANG1, phrasebook.getLang1Code());
        settingsManager.updatePrefValue(SettingsManager.KEY_CURRENT_LANG2, phrasebook.getLang2Code
                ());
        refreshUi();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Update Phrase Activity request code
        switch (requestCode) {
            case EditPhraseActivity.REQUEST_CODE:
                if (resultCode == RESULT_CANCELED) {
                    //It means that the database is empty and we need to refresh the layout
                    refreshUi();
                }
                break;
            case EditPhrasebookActivity.REQUEST_CODE:
                switch (resultCode) {
                    case RESULT_OK:
                        refreshUi();
                        break;
                    case RESULT_CANCELED:
                        //The current phrasebook has just been cancelled, therefore we need to
                        // switch to another phrasebook
                        Phrasebook firstPhrasebook = databaseHelper.getAllPhrasebooks().get(0);
                        switchToPhrasebook(firstPhrasebook);
                        break;
                    default:
                        break;
                }
                break;
            case NewPhraseActivity.REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    refreshUi(); //It means the phrasebook is no more empty and we need to refresh
                }
                break;
            default:
                break;
        }
    }

    @Override
    //Remember: this method is invoked just once, exactly when the activity is created!
    //The return value states whether the menu will be active for the activity (true) or not (false)
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        //Developer buttons, enable if needed
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent i;
        switch (id) {
            case R.id.delete_data:
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Are you sure?");
                alertDialog.setMessage("All the data will be permanently deleted and cannot be " +
                        "recovered!");
                alertDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        databaseHelper.reset();
                        Toast.makeText(MainActivity.this, "All data successfully deleted!", Toast
                                .LENGTH_SHORT)
                                .show();
                        refreshUi();
                    }
                });
                alertDialog.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                alertDialog.show();
                break;
            case R.id.export_data:
                checkWritePermissions();
                break;
            case R.id.reset_xp:
                final AlertDialog.Builder resetAlertDialog = new AlertDialog.Builder(MainActivity
                        .this);
                resetAlertDialog.setTitle("Are you sure?");
                resetAlertDialog.setMessage("All the user data (XP points and level) will be " +
                        "permanently deleted and " +
                        "cannot be " +
                        "recovered!");
                resetAlertDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        settingsManager.resetXP();
                        Toast.makeText(MainActivity.this, "Successfully reset user data!", Toast
                                .LENGTH_SHORT).show();
                    }
                });
                resetAlertDialog.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                resetAlertDialog.show();
                break;
            case R.id.profile:
                i = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(i);
                break;
            case R.id.edit_phrasebook:
                ContentValues cv = settingsManager.getCurrentLanguagesIds();
                Intent intent = new Intent(MainActivity.this, EditPhrasebookActivity.class);
                intent.putExtra(SettingsManager.KEY_CURRENT_LANG1, cv.getAsInteger(SettingsManager
                        .KEY_CURRENT_LANG1));
                intent.putExtra(SettingsManager.KEY_CURRENT_LANG2, cv.getAsInteger(SettingsManager.KEY_CURRENT_LANG2));
                startActivityForResult(intent, EditPhrasebookActivity.REQUEST_CODE);
                break;
            case R.id.about:
                i = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(i);
                break;
            case R.id.new_phrasebook:
                i = new Intent(getApplicationContext(), NewPhrasebookActivity.class);
                startActivity(i);
                break;
            default:
                break;
        }
        return false;
    }

    /**
     * Setting floating action button with onClickListener
     */
    private void initFloatingActionButton() {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), NewPhraseActivity.class);
                ContentValues currentLanguagesNames = settingsManager.getCurrentLanguagesNames();
                String lang1Value = currentLanguagesNames.getAsString(SettingsManager
                        .KEY_CURRENT_LANG1_STRING);
                String lang2Value = currentLanguagesNames.getAsString(SettingsManager
                        .KEY_CURRENT_LANG2_STRING);
                ContentValues currentLanguagesCodes = settingsManager.getCurrentLanguagesIds();
                int lang1Code = currentLanguagesCodes.getAsInteger(SettingsManager
                        .KEY_CURRENT_LANG1);
                int lang2Code = currentLanguagesCodes.getAsInteger(SettingsManager
                        .KEY_CURRENT_LANG2);
                i.putExtra(SettingsManager.KEY_CURRENT_LANG1_STRING, lang1Value);
                i.putExtra(SettingsManager.KEY_CURRENT_LANG2_STRING, lang2Value);
                i.putExtra(SettingsManager.KEY_CURRENT_LANG1, lang1Code);
                i.putExtra(SettingsManager.KEY_CURRENT_LANG2, lang2Code);
                startActivityForResult(i, NewPhraseActivity.REQUEST_CODE);
            }
        });
        fab.hide(); //by default
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
                break;
            }
        }
    }

    public void exportData() {
        databaseHelper.exportToJSON(MainActivity.this);
        Toast.makeText(this, "Exported in Downloads/Phrasebook_Exports as JSON file", Toast
                .LENGTH_SHORT)
                .show();
    }

    /**
     * Checks if phone is connected to network
     *
     * @return true if connected, false otherwise
     */
    private boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        String title = item.getTitle().toString();
        getSupportActionBar().setTitle(title);
        switch (item.getItemId()) {
            case R.id.navigation_practice:
                fragment = new CardsFragment();
                fab.hide();
                break;
            case R.id.navigation_phrasebook:
                fragment = new PhrasesFragment();
                fab.show();
                break;
            case R.id.navigation_progress:
                fragment = new ProgressFragment();
                fab.show();
                break;
        }
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in,
                android.R.anim.fade_out);
        transaction.replace(R.id.frame_layout, fragment).commit();
        return true;
    }

    public void closeDrawer(View view) {
        mDrawerLayout.closeDrawers();
    }
}
