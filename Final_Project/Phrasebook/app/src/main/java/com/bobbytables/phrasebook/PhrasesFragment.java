package com.bobbytables.phrasebook;


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

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
        // Inflate the layout for this fragment
        if (databaseHelper.isDatabaseEmpty()) {
            layout = R.layout.empty_database;
            return inflater.inflate(layout, container, false);
        }
        layout = R.layout.fragment_phrases;
        View rootView = inflater.inflate(layout, container, false);

        //Creating cursor adapter to attach to list view
        Cursor dataCursor = getAllPhrases();
        DataRowCursorAdapter rowCursorAdapter = new DataRowCursorAdapter(getContext(), dataCursor);
        ListView dataListView = (ListView) rootView.findViewById(R.id.dataListView);
        dataListView.setAdapter(rowCursorAdapter);

        // Switch to new cursor and update contents of ListView
        //rowCursorAdapter.changeCursor(newCursor);
        return rootView;
    }

    public Cursor getAllPhrases() {
        return databaseHelper.getDataFromTable(DatabaseHelper.TABLE_PHRASES);
    }

}
