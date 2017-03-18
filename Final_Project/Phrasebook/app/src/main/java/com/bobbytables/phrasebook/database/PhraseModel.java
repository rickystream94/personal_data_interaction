package com.bobbytables.phrasebook.database;

import android.content.ContentValues;

/**
 * Created by ricky on 18/03/2017.
 */

public class PhraseModel implements DatabaseModel {
    String id;
    String motherLanguage;
    String foreignLanguage;
    String createdOn;
    int correctCount = 0; //default 0 when created
    int archived = 0; //represents boolean: 1 = true, 0 = false, default 0 when created
    String tableName;

    public PhraseModel(String id, String motherLanguage, String foreignLanguage, String
            createdOn, String tableName) {
        this.id = id;
        this.motherLanguage = motherLanguage;
        this.foreignLanguage = foreignLanguage;
        this.createdOn = createdOn;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.KEY_PHRASE_ID, this.id);
        contentValues.put(DatabaseHelper.KEY_MOTHER_LANG_STRING, this.motherLanguage);
        contentValues.put(DatabaseHelper.KEY_FOREIGN_LANG_STRING, this.foreignLanguage);
        contentValues.put(DatabaseHelper.KEY_CREATED_ON, this.createdOn);
        contentValues.put(DatabaseHelper.KEY_CORRECT_COUNT, this.correctCount);
        contentValues.put(DatabaseHelper.KEY_ARCHIVED, this.archived);
        return contentValues;
    }

    @Override
    public String getTableName() {
        return this.tableName;
    }
}
