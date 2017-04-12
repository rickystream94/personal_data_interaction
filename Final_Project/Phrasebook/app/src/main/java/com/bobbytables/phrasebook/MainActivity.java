package com.bobbytables.phrasebook;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.TabLayoutOnPageChangeListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bobbytables.phrasebook.database.DatabaseHelper;
import com.bobbytables.phrasebook.utils.AlertDialogManager;
import com.bobbytables.phrasebook.utils.DateUtil;
import com.bobbytables.phrasebook.utils.SettingsManager;

import org.json.JSONObject;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener,
        ViewPager.OnPageChangeListener {

    private AlertDialogManager alertDialogManager = new AlertDialogManager();
    private SettingsManager settingsManager;
    private PagerAdapter pagerAdapter;
    private ViewPager pager;
    private FloatingActionButton fab;
    public static Handler killerHandler;
    private String motherLanguage;
    private String foreignLanguage;
    private DatabaseHelper databaseHelper;
    private TabLayout tabLayout;
    private String[] pagesTitles;
    private RequestQueue requestQueue;
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
        pagesTitles = new String[]{getString(R.string.tab1), getString(R.string
                .tab2), getString(R.string.tab3)};

        //Initialize ViewPager
        initializePager();
        initFloatingActionButton();

        //Initialize request queue for Volley
        requestQueue = Volley.newRequestQueue(this);

        //TODO: to be removed after experiment!
        //Perform check of gamification, to be changed after 10 days
        if (settingsManager.getPrefStringValue(SettingsManager.KEY_CREATED).equals(""))
            return;
        String currentDate = DateUtil.getCurrentTimestamp();
        String userCreationDate = settingsManager.getPrefStringValue(SettingsManager.KEY_CREATED);
        try {
            int daysDiff = DateUtil.daysBetweenDates(userCreationDate, currentDate);
            boolean hasAlreadySwitchedVersion = settingsManager.getPrefBoolValue(SettingsManager
                    .KEY_SWITCHED_VERSION);
            boolean hasPerformedLastUpload = settingsManager.getPrefBoolValue(SettingsManager
                    .KEY_FINAL_UPLOAD_PERFORMED);
            if (daysDiff >= 10 && !hasAlreadySwitchedVersion) {
                try {
                    //Try to upload data before switching to new version
                    executeUpload();
                    boolean gamificationStatus = settingsManager.getPrefBoolValue(SettingsManager
                            .KEY_GAMIFICATION);
                    settingsManager.updatePrefValue(SettingsManager.KEY_GAMIFICATION, !gamificationStatus);
                    settingsManager.updatePrefValue(SettingsManager.KEY_SWITCHED_VERSION, true);
                    String message = getString(R.string.uiSwitchRootMessage);
                    if (!gamificationStatus)
                        message += getString(R.string.uiSwitchGamification);
                    else
                        message += getString(R.string.uiSwitchNoGamification);
                    alertDialogManager.showAlertDialog(MainActivity.this, "Important update!", message,
                            !gamificationStatus);
                    return;
                } catch (Exception e) {
                    alertDialogManager.showAlertDialog(MainActivity.this, "Error", getString(R.string.uiSwitchErrorConnection), false);
                }
            }
            //perform latest automatic data upload
            if (daysDiff >= 20 && hasAlreadySwitchedVersion && !hasPerformedLastUpload) {
                try {
                    //Try to upload data before switching to new version
                    executeUpload();
                    settingsManager.updatePrefValue(SettingsManager.KEY_FINAL_UPLOAD_PERFORMED, true);
                    alertDialogManager.showAlertDialog(MainActivity.this, "Important update!",
                            getString(R.string.finalMessage),
                            true);
                } catch (Exception e) {
                    alertDialogManager.showAlertDialog(MainActivity.this, "Error", getString(R.string.uiSwitchErrorConnection), false);
                }
            }
        } catch (ParseException e) {
            alertDialogManager.showAlertDialog(MainActivity.this, "Error", e.getMessage(), false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (settingsManager.getPrefBoolValue(SettingsManager.KEY_IS_FIRST_TIME)) {
            Cursor cursor = databaseHelper.performRawQuery("SELECT * FROM " + DatabaseHelper
                    .TABLE_PHRASES + " LIMIT 2");
            if (cursor.getCount() == 1) {
                settingsManager.updatePrefValue(SettingsManager.KEY_IS_FIRST_TIME, false);
                initializePager();
            }
        }
    }

    @Override
    //Remember: this method is invoked just once, exactly when the activity is created!
    //The return value states whether the menu will be active for the activity (true) or not (false)
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        //TODO: to be changed after experiment, delete the 2 lines below!
        MenuItem profileItem = menu.findItem(R.id.profile);
        profileItem.setVisible(settingsManager.getPrefBoolValue(SettingsManager.KEY_GAMIFICATION));
        //Developer buttons, enable if needed
        MenuItem resetXpItem = menu.findItem(R.id.reset_xp);
        resetXpItem.setVisible(false);
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
                        initializePager();
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
                settingsManager.resetXP();
                Toast.makeText(this, "Successfully reset!", Toast
                        .LENGTH_SHORT)
                        .show();
                break;
            case R.id.profile:
                Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(i);
                break;
            case R.id.upload_data:
                try {
                    executeUpload();
                } catch (Exception e) {
                    alertDialogManager.showAlertDialog(MainActivity.this, "Error", e.getMessage(), false);
                }
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
                i.putExtra(SettingsManager.KEY_MOTHER_LANGUAGE, motherLanguage);
                i.putExtra(SettingsManager.KEY_FOREIGN_LANGUAGE, foreignLanguage);
                startActivity(i);
            }
        });
        fab.hide(); //by default
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
        pager.addOnPageChangeListener(this);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        pager.setCurrentItem(tab.getPosition());
        switch (tab.getPosition()) {
            case 0:
                fab.hide();
                break;
            default:
                fab.show();
        }
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

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        //Not necessary
    }

    @Override
    public void onPageSelected(int position) {
        getSupportActionBar().setTitle(pagesTitles[position]);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        //Not necessary
    }

    private void executeUpload() throws Exception {
        if (isConnected())
            uploadDataToServer();
        else throw new Exception("You're not " +
                "connected to any network! Please try again when you have internet " +
                "connection");
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

    public void uploadDataToServer() {
        final JSONObject jsonObject = databaseHelper.createJsonDump();
        Request request = new StringRequest(Request.Method.POST, SERVER_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Request Response", response);
                Toast.makeText(MainActivity.this, "Data successfully uploaded!", Toast.LENGTH_SHORT)
                        .show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ERROR RESPONSE", error.toString());
                Toast.makeText(MainActivity.this, "An error occurred! Please try again...", Toast
                        .LENGTH_SHORT)
                        .show();
            }
        }) {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("data", jsonObject.toString());
                return params;
            }
        };

        // Adding request to request queue
        requestQueue.add(request);
    }
}
