package com.bobbytables.phrasebook;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.TabLayoutOnPageChangeListener;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.bobbytables.phrasebook.utils.AlertDialogManager;
import com.bobbytables.phrasebook.utils.SettingsManager;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private AlertDialogManager alertDialogManager = new AlertDialogManager();
    private SettingsManager settingsManager;
    private PagerAdapter pagerAdapter;
    private ViewPager pager;
    public static Handler killerHandler;
    private String motherLanguage;
    private String foreignLanguage;

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

        //Get settings manager
        settingsManager = SettingsManager.getInstance(getApplicationContext());
        //Check always if it's the first time
        //Will invoke automatically NewUserActivity
        settingsManager.createUserProfile();
        motherLanguage = settingsManager.getPrefValue(SettingsManager.KEY_MOTHER_LANGUAGE);
        foreignLanguage = settingsManager.getPrefValue(SettingsManager.KEY_FOREIGN_LANGUAGE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializePager();
        initFloatingActionButton();
    }

    /**
     * Setting floating action button with onClickListener
     */
    private void initFloatingActionButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),NewPhraseActivity.class);
                i.putExtra(SettingsManager.KEY_MOTHER_LANGUAGE,motherLanguage);
                i.putExtra(SettingsManager.KEY_FOREIGN_LANGUAGE,foreignLanguage);
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
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
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
}
