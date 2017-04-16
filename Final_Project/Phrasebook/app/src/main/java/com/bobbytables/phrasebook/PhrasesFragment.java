package com.bobbytables.phrasebook;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.bobbytables.phrasebook.database.DatabaseHelper;
import com.bobbytables.phrasebook.utils.SettingsManager;


/**
 * A simple {@link Fragment} subclass.
 */
public class PhrasesFragment extends Fragment implements AdapterView.OnItemClickListener,View.OnClickListener {

    private DatabaseHelper databaseHelper;
    private String motherLanguage;
    private String foreignLanguage;
    private DataRowCursorAdapter rowCursorAdapter;
    private View rootView;
    private int currentOffset = 0;
    private static final int OFFSET = 10;

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
        rootView = inflater.inflate(layout, container, false);

        motherLanguage = SettingsManager.getInstance(getContext()).getPrefStringValue(SettingsManager
                .KEY_MOTHER_LANGUAGE);
        foreignLanguage = SettingsManager.getInstance(getContext()).getPrefStringValue(SettingsManager
                .KEY_FOREIGN_LANGUAGE);
        TextView lang1 = (TextView) rootView.findViewById(R.id.phrases_lang1);
        TextView lang2 = (TextView) rootView.findViewById(R.id.phrases_lang2);
        lang1.setText(motherLanguage);
        lang2.setText(foreignLanguage);

        // Get the SearchView and set it properly
        SearchView searchView = (SearchView) rootView.findViewById(R.id.search_phrase);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchPhrase(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (query.equals(""))
                    initPhrasebookData();
                else
                    searchPhrase(query);
                return false;
            }
        });
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        Button nextPageButton = (Button)rootView.findViewById(R.id.nextPage);
        Button previousPageButton = (Button)rootView.findViewById(R.id.previousPage);
        nextPageButton.setOnClickListener(this);
        previousPageButton.setOnClickListener(this);

        //Creating cursor adapter to attach to list view
        initPhrasebookData();

        return rootView;
    }

    private void initPhrasebookData() {
        Cursor dataCursor = getAllPhrases();
        rowCursorAdapter = new DataRowCursorAdapter(getContext(), dataCursor);
        ListView dataListView = (ListView) rootView.findViewById(R.id.dataListView);
        dataListView.setAdapter(rowCursorAdapter);
        dataListView.setOnItemClickListener(this);
    }

    public Cursor getAllPhrases() {
        return databaseHelper.getDataFromTable(DatabaseHelper.TABLE_PHRASES, currentOffset);
    }

    public void searchPhrase(String query) {
        Cursor cursor = databaseHelper.searchPhrase(query);
        // Switch to new cursor and update contents of ListView
        rowCursorAdapter.changeCursor(cursor);
        rowCursorAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        //Perform phrase update if list item is clicked
        Cursor cursor = (Cursor) adapterView.getAdapter().getItem(position);
        cursor.moveToPosition(position);
        String motherLangString = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper
                .KEY_MOTHER_LANG_STRING));
        String foreignLangString = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper
                .KEY_FOREIGN_LANG_STRING));
        String createdOn = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper
                .KEY_CREATED_ON));
        Intent intent = new Intent(getActivity(), UpdatePhraseActivity.class);
        intent.putExtra(DatabaseHelper.KEY_MOTHER_LANG_STRING, motherLangString);
        intent.putExtra(DatabaseHelper.KEY_FOREIGN_LANG_STRING, foreignLangString);
        intent.putExtra(DatabaseHelper.KEY_CREATED_ON, createdOn);
        intent.putExtra(SettingsManager.KEY_MOTHER_LANGUAGE, motherLanguage);
        intent.putExtra(SettingsManager.KEY_FOREIGN_LANGUAGE, foreignLanguage);
        getActivity().startActivityForResult(intent, 1);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (rowCursorAdapter != null) {
            rowCursorAdapter.changeCursor(getAllPhrases());
            rowCursorAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.nextPage:
                Cursor cursor = databaseHelper.performRawQuery("SELECT COUNT(*) FROM " + DatabaseHelper
                        .TABLE_PHRASES);
                cursor.moveToFirst();
                int totalRows = cursor.getInt(0);
                if (totalRows - currentOffset < OFFSET)
                    return; //no more rows to display, no need to change page
                else
                    currentOffset += OFFSET;
                break;
            case R.id.previousPage:
                if (currentOffset==0)
                    return; //can't be negative offset, we're at starting point
                else
                    currentOffset-=OFFSET;
                break;
        }
        rowCursorAdapter.changeCursor(databaseHelper.getDataFromTable(DatabaseHelper
                .TABLE_PHRASES, currentOffset));
        rowCursorAdapter.notifyDataSetChanged();
    }
}
