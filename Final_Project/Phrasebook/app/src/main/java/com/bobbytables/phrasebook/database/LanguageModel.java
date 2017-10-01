package com.bobbytables.phrasebook.database;

import android.content.ContentValues;

/**
 * Created by ricky on 01/10/2017.
 */

public class LanguageModel implements DatabaseModel {
    private int langId;
    private String langName;
    private String tableName;

    public LanguageModel(int langId, String langName, String tableName) {
        this.langId = langId;
        this.langName = langName;
        this.tableName = tableName;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.KEY_LANG_ID, this.langId);
        contentValues.put(DatabaseHelper.KEY_LANG_NAME, this.langName);
        return contentValues;
    }

    @Override
    public String getTableName() {
        return tableName;
    }
}
