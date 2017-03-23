package com.bobbytables.phrasebook.database;

import android.content.ContentValues;

/**
 * Created by ricky on 18/03/2017.
 */

public class BadgeModel implements DatabaseModel {
    String tableName;
    String type;
    String createdOn;

    public BadgeModel(String type, String createdOn, String tableName) {
        this.type = type;
        this.createdOn = createdOn;
        this.tableName = tableName;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.KEY_BADGE_TYPE_ID, this.type);
        contentValues.put(DatabaseHelper.KEY_CREATED_ON, this.createdOn);
        return contentValues;
    }

    @Override
    public String getTableName() {
        return this.tableName;
    }
}
