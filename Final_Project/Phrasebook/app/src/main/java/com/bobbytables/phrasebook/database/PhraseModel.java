package com.bobbytables.phrasebook.database;

import android.content.ContentValues;

/**
 * Created by ricky on 18/03/2017.
 */

public class PhraseModel implements DatabaseModel {
    private String lang1Value;
    private String lang2Value;
    private String createdOn;
    private String tableName;
    private int lang1Code;
    private int lang2Code;
    private static final int DEFAULT_CORRECT_COUNT = 0;
    private static final int DEFAULT_IS_MASTERED = 0;

    public PhraseModel(String lang1Value, String lang2Value, int lang1Code, int
            lang2Code, String createdOn, String tableName) {
        this.tableName = tableName;
        this.lang1Value = lang1Value;
        this.lang2Value = lang2Value;
        this.createdOn = createdOn;
        this.lang1Code = lang1Code;
        this.lang2Code = lang2Code;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.KEY_LANG1, this.lang1Code);
        contentValues.put(DatabaseHelper.KEY_LANG2, this.lang2Code);
        contentValues.put(DatabaseHelper.KEY_LANG1_VALUE, this.lang1Value);
        contentValues.put(DatabaseHelper.KEY_LANG2_VALUE, this.lang2Value);
        contentValues.put(DatabaseHelper.KEY_CREATED_ON, this.createdOn);
        contentValues.put(DatabaseHelper.KEY_CORRECT_COUNT, DEFAULT_CORRECT_COUNT);
        contentValues.put(DatabaseHelper.KEY_IS_MASTERED, DEFAULT_IS_MASTERED);
        return contentValues;
    }

    @Override
    public String getTableName() {
        return this.tableName;
    }
}
