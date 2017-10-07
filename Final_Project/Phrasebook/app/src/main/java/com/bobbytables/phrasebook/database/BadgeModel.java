package com.bobbytables.phrasebook.database;

import android.content.ContentValues;

/**
 * Created by ricky on 18/03/2017.
 */

public class BadgeModel implements DatabaseModel {
    private String tableName;
    private String badgeName;
    private String description;
    private int badgeId;
    private String createdOn;

    public BadgeModel(String badgeName, String description, String tableName) {
        this.badgeId = UNSPECIFIED_ID_VALUE;
        this.badgeName = badgeName;
        this.tableName = tableName;
        this.description = description;
        this.createdOn = "";
    }

    public BadgeModel(int badgeId, String createdOn, String tableName) {
        this.badgeId = badgeId;
        this.createdOn = createdOn;
        this.tableName = tableName;
        this.badgeName = "";
        this.description = "";
    }

    public int getBadgeId() {
        return this.badgeId;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        if (this.badgeId != UNSPECIFIED_ID_VALUE)
            contentValues.put(DatabaseHelper.KEY_BADGES_ID, this.badgeId);
        if (!this.createdOn.equals(""))
            contentValues.put(DatabaseHelper.KEY_CREATED_ON, this.createdOn);
        if (!this.badgeName.equals(""))
            contentValues.put(DatabaseHelper.KEY_BADGE_NAME, this.badgeName);
        if (!this.description.equals(""))
            contentValues.put(DatabaseHelper.KEY_BADGE_DESCRIPTION, this.description);
        return contentValues;
    }

    @Override
    public String getTableName() {
        return this.tableName;
    }
}
