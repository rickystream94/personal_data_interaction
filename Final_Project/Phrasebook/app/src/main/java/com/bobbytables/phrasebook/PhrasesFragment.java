package com.bobbytables.phrasebook;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import com.bobbytables.phrasebook.database.DatabaseHelper;
import com.bobbytables.phrasebook.utils.SettingsManager;


/**
 * A simple {@link Fragment} subclass.
 */
public class PhrasesFragment extends Fragment implements AdapterView.OnItemClickListener {

    private DatabaseHelper databaseHelper;
    private String motherLanguage;
    private String foreignLanguage;
    private DataRowCursorAdapter rowCursorAdapter;
    private View rootView;

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
        return databaseHelper.getDataFromTable(DatabaseHelper.TABLE_PHRASES);
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
        Intent intent = new Intent(getActivity(),UpdatePhraseActivity.class);
        intent.putExtra(DatabaseHelper.KEY_MOTHER_LANG_STRING,motherLangString);
        intent.putExtra(DatabaseHelper.KEY_FOREIGN_LANG_STRING,foreignLangString);
        intent.putExtra(DatabaseHelper.KEY_CREATED_ON,createdOn);
        intent.putExtra(SettingsManager.KEY_MOTHER_LANGUAGE,motherLanguage);
        intent.putExtra(SettingsManager.KEY_FOREIGN_LANGUAGE,foreignLanguage);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (rowCursorAdapter != null) {
            rowCursorAdapter.changeCursor(getAllPhrases());
            rowCursorAdapter.notifyDataSetChanged();
        }
    }
}
