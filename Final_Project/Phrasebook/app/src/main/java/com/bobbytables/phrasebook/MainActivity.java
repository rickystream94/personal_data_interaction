package com.bobbytables.phrasebook;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.TabLayoutOnPageChangeListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private AlertDialogManager alertDialogManager = new AlertDialogManager();
    private SettingsManager settingsManager;
    private PagerAdapter pagerAdapter;
    private ViewPager pager;
    public static Handler killerHandler;

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
        settingsManager = new SettingsManager(getApplicationContext());
        //Check always if it's the first time
        //Will invoke automatically NewUserActivity
        settingsManager.userProfileExists();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        //Setting floating action button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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

    public static class PagerAdapter extends FragmentPagerAdapter {

        private int tabCount;

        public PagerAdapter(FragmentManager fm, int tabCount) {
            super(fm);
            this.tabCount = tabCount;
        }

        @Override
        public Fragment getItem(int position) {
            //Should change the fragment according to the position
            Fragment fragment = new PageFragment();
            return fragment;
        }

        @Override
        public int getCount() {
            return tabCount;
        }
    }

    // Instances of this class are fragments representing a single
// object in our collection.
    public static class PageFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            // The last two arguments ensure LayoutParams are inflated
            // properly.
            return inflater.inflate(
                    R.layout.page_fragment, container, false);
        }
    }
}
