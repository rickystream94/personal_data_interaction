package com.bobbytables.phrasebook;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.bobbytables.phrasebook.database.DatabaseHelper;

/**
 * Created by ricky on 19/03/2017.
 */

public class DataRowCursorAdapter extends CursorAdapter {


    public DataRowCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.database_row, viewGroup, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView textViewLang1 = (TextView) view.findViewById(R.id.datarow_lang1);
        TextView textViewLang2 = (TextView) view.findViewById(R.id.datarow_lang2);
        // Extract properties from cursor
        String lang1 = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper
                .KEY_MOTHER_LANG_STRING));
        String lang2 = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper
                .KEY_FOREIGN_LANG_STRING));
        // Populate fields with extracted properties
        textViewLang1.setText(lang1);
        textViewLang2.setText(lang2);
    }
}
