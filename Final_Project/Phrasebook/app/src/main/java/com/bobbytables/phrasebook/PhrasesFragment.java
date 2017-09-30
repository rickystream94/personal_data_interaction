package com.bobbytables.phrasebook;


import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

import com.bobbytables.phrasebook.database.DatabaseHelper;
import com.bobbytables.phrasebook.utils.SettingsManager;


/**
 * A simple {@link Fragment} subclass.
 */
public class PhrasesFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    private DatabaseHelper databaseHelper;
    private String lang1Value;
    private String lang2Value;
    private DataRowCursorAdapter rowCursorAdapter;
    private View rootView;
    private Button nextPageButton;
    private Button previousPageButton;
    private int currentOffset = 0;
    private static final int LIMIT = 20;
    private int lang1Code;
    private int lang2Code;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        databaseHelper = DatabaseHelper.getInstance(getContext());
        SettingsManager settingsManager = SettingsManager.getInstance(getContext());
        ContentValues currentLanguagesNames = settingsManager.getCurrentLanguagesNames();
        ContentValues currentLanguagesCodes = settingsManager.getCurrentLanguagesIds();
        lang1Value = currentLanguagesNames.getAsString(SettingsManager.KEY_CURRENT_LANG1_STRING);
        lang2Value = currentLanguagesNames.getAsString(SettingsManager.KEY_CURRENT_LANG2_STRING);
        lang1Code = currentLanguagesCodes.getAsInteger(SettingsManager.KEY_CURRENT_LANG1);
        lang2Code = currentLanguagesCodes.getAsInteger(SettingsManager.KEY_CURRENT_LANG2);

        int layout;
        // Inflate the layout for this fragment
        if (databaseHelper.isDatabaseEmpty(lang1Code, lang2Code)) {
            layout = R.layout.empty_database;
            return inflater.inflate(layout, container, false);
        }
        layout = R.layout.fragment_phrases;
        rootView = inflater.inflate(layout, container, false);

        TextView lang1 = (TextView) rootView.findViewById(R.id.phrases_lang1);
        TextView lang2 = (TextView) rootView.findViewById(R.id.phrases_lang2);
        lang1.setText(this.lang1Value);
        lang2.setText(this.lang2Value);

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
        nextPageButton = (Button) rootView.findViewById(R.id.nextPage);
        previousPageButton = (Button) rootView.findViewById(R.id.previousPage);
        nextPageButton.setOnClickListener(this);
        previousPageButton.setOnClickListener(this);
        previousPageButton.setVisibility((currentOffset == 0 ? View.INVISIBLE : View.VISIBLE));

        //Creating cursor adapter to attach to list view
        initPhrasebookData();

        return rootView;
    }

    private void initPhrasebookData() {
        Cursor dataCursor = databaseHelper.getPhrasesList(lang1Code, lang2Code, LIMIT, currentOffset);
        rowCursorAdapter = new DataRowCursorAdapter(getContext(), dataCursor);
        ExpandableHeightListView dataListView = (ExpandableHeightListView) rootView.findViewById(R.id.dataListView);
        dataListView.setAdapter(rowCursorAdapter);
        dataListView.setOnItemClickListener(this);
        dataListView.setExpanded(true);
    }

    public void searchPhrase(String query) {
        Cursor cursor = databaseHelper.searchPhrase(lang1Code, lang2Code, query);
        // Switch to new cursor and update contents of ListView
        rowCursorAdapter.changeCursor(cursor);
        rowCursorAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        //Perform phrase update if list item is clicked
        Cursor cursor = (Cursor) adapterView.getAdapter().getItem(position);
        cursor.moveToPosition(position);
        String lang1Value = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper
                .KEY_LANG1_VALUE));
        String lang2Value = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper
                .KEY_LANG2_VALUE));
        String createdOn = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper
                .KEY_CREATED_ON));
        int lang1Code = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_LANG1));
        int lang2Code = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_LANG2));
        Intent intent = new Intent(getActivity(), EditPhraseActivity.class);
        intent.putExtra(DatabaseHelper.KEY_LANG1_VALUE, lang1Value);
        intent.putExtra(DatabaseHelper.KEY_LANG2_VALUE, lang2Value);
        intent.putExtra(DatabaseHelper.KEY_LANG1, lang1Code);
        intent.putExtra(DatabaseHelper.KEY_LANG2, lang2Code);
        intent.putExtra(DatabaseHelper.KEY_CREATED_ON, createdOn);
        intent.putExtra(SettingsManager.KEY_CURRENT_LANG1, this.lang1Value);
        intent.putExtra(SettingsManager.KEY_CURRENT_LANG2, this.lang2Value);
        getActivity().startActivityForResult(intent, EditPhraseActivity.REQUEST_CODE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (rowCursorAdapter != null) {
            rowCursorAdapter.changeCursor(databaseHelper.getPhrasesList(lang1Code, lang2Code,
                    LIMIT, currentOffset));
            rowCursorAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.nextPage:
                Cursor cursor = databaseHelper.performRawQuery("SELECT COUNT(*) FROM " + DatabaseHelper
                        .TABLE_PHRASES + " WHERE " + DatabaseHelper.KEY_LANG1 + "=" + lang1Code + " AND " +
                        "" + DatabaseHelper.KEY_LANG2 + "=" + lang2Code);
                cursor.moveToFirst();
                int totalRows = cursor.getInt(0);
                currentOffset += LIMIT;
                if (totalRows - currentOffset < LIMIT)
                    nextPageButton.setVisibility(View.INVISIBLE); //no more rows to display, no need to change page
                previousPageButton.setVisibility(View.VISIBLE);
                break;
            case R.id.previousPage:
                if (currentOffset == 0)
                    return; //can't be negative offset, we're at starting point
                else {
                    currentOffset -= LIMIT;
                    previousPageButton.setVisibility((currentOffset == 0 ? View.INVISIBLE : View.VISIBLE));
                    nextPageButton.setVisibility(View.VISIBLE);
                }
                break;
        }
        rowCursorAdapter.changeCursor(databaseHelper.getPhrasesList(lang1Code, lang2Code, LIMIT,
                currentOffset));
        rowCursorAdapter.notifyDataSetChanged();
    }
}
