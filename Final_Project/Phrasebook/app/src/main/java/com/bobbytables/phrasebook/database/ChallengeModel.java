package com.bobbytables.phrasebook.database;

import android.content.ContentValues;

/**
 * Created by ricky on 18/03/2017.
 */

public class ChallengeModel implements DatabaseModel {
    String id;
    String phraseId;
    String createdOn;
    String tableName;

    public ChallengeModel(String id, String phraseId, String createdOn, String tableName) {
        this.id = id;
        this.phraseId = phraseId;
        this.createdOn = createdOn;
        this.tableName = tableName;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.KEY_CHALLENGE_ID, this.id);
        contentValues.put(DatabaseHelper.KEY_CHALLENGE_PHRASE_ID, this.phraseId);
        contentValues.put(DatabaseHelper.KEY_CREATED_ON, this.createdOn);
        return contentValues;
    }

    @Override
    public String getTableName() {
        return this.tableName;
    }
}
