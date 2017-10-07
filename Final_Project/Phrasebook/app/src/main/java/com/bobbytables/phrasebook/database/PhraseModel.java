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
    private int isMastered;
    private int correctCount;
    private int phraseId;

    public PhraseModel(String lang1Value, String lang2Value, int lang1Code, int
            lang2Code, String createdOn, String tableName) {
        this.phraseId = UNSPECIFIED_ID_VALUE;
        this.tableName = tableName;
        this.lang1Value = lang1Value;
        this.lang2Value = lang2Value;
        this.createdOn = createdOn;
        this.lang1Code = lang1Code;
        this.lang2Code = lang2Code;
        this.isMastered = 0;
        this.correctCount = 0;
    }

    public PhraseModel(int phraseId, String lang1Value, String lang2Value, int lang1Code, int
            lang2Code, int isMastered, int correctCount, String createdOn, String tableName) {
        this.phraseId = phraseId;
        this.tableName = tableName;
        this.lang1Value = lang1Value;
        this.lang2Value = lang2Value;
        this.createdOn = createdOn;
        this.lang1Code = lang1Code;
        this.lang2Code = lang2Code;
        this.isMastered = isMastered;
        this.correctCount = correctCount;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        if (this.phraseId != UNSPECIFIED_ID_VALUE)
            contentValues.put(DatabaseHelper.KEY_PHRASE_ID, this.phraseId);
        contentValues.put(DatabaseHelper.KEY_LANG1, this.lang1Code);
        contentValues.put(DatabaseHelper.KEY_LANG2, this.lang2Code);
        contentValues.put(DatabaseHelper.KEY_LANG1_VALUE, this.lang1Value);
        contentValues.put(DatabaseHelper.KEY_LANG2_VALUE, this.lang2Value);
        contentValues.put(DatabaseHelper.KEY_CREATED_ON, this.createdOn);
        contentValues.put(DatabaseHelper.KEY_CORRECT_COUNT, this.correctCount);
        contentValues.put(DatabaseHelper.KEY_IS_MASTERED, this.isMastered);
        return contentValues;
    }

    @Override
    public String getTableName() {
        return this.tableName;
    }
}
