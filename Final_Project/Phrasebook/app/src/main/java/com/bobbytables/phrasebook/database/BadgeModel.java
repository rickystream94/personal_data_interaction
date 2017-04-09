package com.bobbytables.phrasebook.database;

import android.content.ContentValues;

/**
 * Created by ricky on 18/03/2017.
 */

public class BadgeModel implements DatabaseModel {
    private String tableName;
    private String badgeName;
    private String description;

    public BadgeModel(String badgeName, String description, String tableName) {
        this.badgeName = badgeName;
        this.tableName = tableName;
        this.description = description;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.KEY_BADGE_NAME, this.badgeName);
        contentValues.put(DatabaseHelper.KEY_BADGE_DESCRIPTION, this.description);
        return contentValues;
    }

    @Override
    public String getTableName() {
        return this.tableName;
    }
}
