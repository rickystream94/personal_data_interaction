package com.bobbytables.phrasebook;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by ricky on 17/03/2017.
 */

public class PagerAdapter extends FragmentPagerAdapter {

    private int tabCount;

    public PagerAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        this.tabCount = tabCount;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new CardsFragment();
            case 1:
                return new PhrasesFragment();
            case 2:
                return new ProgressFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}
