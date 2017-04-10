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
import com.bobbytables.phrasebook.utils.AlertDialogManager;

import java.lang.reflect.Field;

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
        String createdOn = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper
                .KEY_CREATED_ON));
        int resource = R.drawable.badge; //default initialization value, just not to have null
        try {
            int badgeId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_BADGES_ID));
            Class res = R.drawable.class;
            Field field = res.getField("badge" + badgeId);
            int badgeDrawableId = field.getInt(null);
            resource = createdOn != null ? badgeDrawableId : R.drawable.badge;
        } catch (Exception e) {
            Log.e("BadgeIcon", "Failure to get drawable id.", e);
            resource = createdOn != null ? R.drawable.unlocked : R.drawable.badge;
        } finally {
            badgeIcon.setImageResource(resource);
        }
    }
}
