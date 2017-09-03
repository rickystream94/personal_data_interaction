package com.bobbytables.phrasebook.database;

import android.content.ContentValues;

/**
 * Created by ricky on 18/03/2017.
 */

public class PhraseModel implements DatabaseModel {
    private String motherLanguage;
    private String foreignLanguage;
    private String createdOn;
    private String tableName;

    public PhraseModel(String motherLanguage, String foreignLanguage, String
            createdOn, String tableName) {
        this.tableName = tableName;
        this.motherLanguage = motherLanguage;
        this.foreignLanguage = foreignLanguage;
        this.createdOn = createdOn;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.KEY_LANG1_VALUE, this.motherLanguage);
        contentValues.put(DatabaseHelper.KEY_LANG2_VALUE, this.foreignLanguage);
        contentValues.put(DatabaseHelper.KEY_CREATED_ON, this.createdOn);
        int correctCount = 0;
        contentValues.put(DatabaseHelper.KEY_CORRECT_COUNT, correctCount);
        int archived = 0;
        contentValues.put(DatabaseHelper.KEY_IS_MASTERED, archived);
        return contentValues;
    }

    @Override
    public String getTableName() {
        return this.tableName;
    }
}
