package com.bobbytables.phrasebook.database;

import android.content.ContentValues;

/**
 * Created by ricky on 18/03/2017.
 */

public class BadgeModel implements DatabaseModel {
    String tableName;
    String badgeName;
    int badgeResource;

    public BadgeModel(String badgeName, int badgeResource, String tableName) {
        this.badgeName = badgeName;
        this.tableName = tableName;
        this.badgeResource = badgeResource;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.KEY_BADGE_NAME, this.badgeName);
        contentValues.put(DatabaseHelper.KEY_BADGE_ICON_RESOURCE, this.badgeResource);
        return contentValues;
    }

    @Override
    public String getTableName() {
        return this.tableName;
    }
}
