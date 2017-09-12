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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bobbytables.phrasebook.database.DatabaseHelper;
import com.bobbytables.phrasebook.utils.AlertDialogManager;
import com.bobbytables.phrasebook.utils.SettingsManager;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private AlertDialogManager alertDialogManager = new AlertDialogManager();
    private SettingsManager settingsManager;
    private FloatingActionButton fab;
    public static Handler killerHandler;
    private String lang1;
    private String lang2;
    private DatabaseHelper databaseHelper;
    private DrawerLayout mDrawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private RequestQueue requestQueue;
    private Fragment fragment;
    private FragmentManager fragmentManager;
    private BottomNavigationView navigation;
    private static final String SERVER_URL = "http://www.richmondweb.it/phrasebook/upload_data.php";

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

        //Get fragment manager and add default fragment
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        fragment = new CardsFragment();
        transaction.add(R.id.frame_layout, fragment).commit();

        databaseHelper = DatabaseHelper.getInstance(getApplicationContext());

        //Get settings manager
        settingsManager = SettingsManager.getInstance(getApplicationContext());
        //Check always if it's the first time
        //Will invoke automatically NewUserActivity
        settingsManager.createUserProfile();
        ContentValues currentLanguages = SettingsManager.getInstance(getApplicationContext()).getCurrentLanguages();
        lang1 = currentLanguages.getAsString(SettingsManager.KEY_CURRENT_LANG1);
        lang2 = currentLanguages.getAsString(SettingsManager.KEY_CURRENT_LANG2);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initialize drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, mDrawerLayout, toolbar, R
                .string.navigation_drawer_open, R.string.navigation_drawer_close);

        //Initialize bottom navigation
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);


        String[] allPhrasebooks = databaseHelper.getAllPhrasebooks();
        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item,
                allPhrasebooks));


        //Initialize fab
        initFloatingActionButton();

        //Initialize request queue for Volley
        requestQueue = Volley.newRequestQueue(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (settingsManager.getPrefBoolValue(SettingsManager.KEY_IS_FIRST_TIME)) {
            Cursor cursor = databaseHelper.performRawQuery("SELECT COUNT(*) FROM " + DatabaseHelper
                    .TABLE_PHRASES);
            cursor.moveToFirst();
            if (cursor.getInt(0) > 0) {
                settingsManager.updatePrefValue(SettingsManager.KEY_IS_FIRST_TIME, false);
                initializeNavigation();
            }
        }
    }

    /**
     * Method used to initialize the default navigation fragment when refreshing main activity
     * content
     */
    private void initializeNavigation() {
        navigation.setSelectedItemId(R.id.navigation_practice);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Update Phrase Activity request code
        if (requestCode == 1) {
            if (resultCode == RESULT_CANCELED) {
                //It means that the database is empty and we need to refresh the view pager
                settingsManager.updatePrefValue(SettingsManager.KEY_IS_FIRST_TIME, true);
                initializeNavigation();
            }
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
                        settingsManager.updatePrefValue(SettingsManager.KEY_IS_FIRST_TIME, true);
                        Toast.makeText(MainActivity.this, "All data successfully deleted!", Toast
                                .LENGTH_SHORT)
                                .show();
                        initializeNavigation();
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
                Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(i);
                break;
            case R.id.about:
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
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
                i.putExtra(SettingsManager.KEY_CURRENT_LANG1, lang1);
                i.putExtra(SettingsManager.KEY_CURRENT_LANG2, lang2);
                startActivity(i);
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
        databaseHelper.exportToJSON(getApplicationContext());
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
