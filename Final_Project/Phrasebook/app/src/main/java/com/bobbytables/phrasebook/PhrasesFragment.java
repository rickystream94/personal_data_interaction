package com.bobbytables.phrasebook;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bobbytables.phrasebook.database.DatabaseHelper;


/**
 * A simple {@link Fragment} subclass.
 */
public class PhrasesFragment extends Fragment {

    private DatabaseHelper databaseHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        databaseHelper = DatabaseHelper.getInstance(getContext());
        int layout;
        if (databaseHelper.isDatabaseEmpty())
            layout = R.layout.empty_database;
        else layout = R.layout.fragment_phrases;
        // Inflate the layout for this fragment
        return inflater.inflate(layout, container, false);
    }

}
