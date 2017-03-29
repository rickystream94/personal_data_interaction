package com.bobbytables.phrasebook;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bobbytables.phrasebook.database.DatabaseHelper;

/**
 * Created by ricky on 29/03/2017.
 */

public class BadgeAdapter extends CursorAdapter {

    public BadgeAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.badge, viewGroup, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView badgeIcon = (ImageView) view.findViewById(R.id.badgeIcon);
        TextView badgeName = (TextView) view.findViewById(R.id.badgeName);
        String text = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper
                .KEY_BADGE_NAME));
        badgeName.setText(text);

        int resource = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper
                .KEY_BADGE_ICON_RESOURCE));
        badgeIcon.setImageResource(resource);
    }

    /**
     * Needed to make all the child views of the current view group non-selectable
     *
     * @return
     */
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    /**
     * Needed to make all the child views of the current view group non-selectable
     *
     * @return
     */
    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}
