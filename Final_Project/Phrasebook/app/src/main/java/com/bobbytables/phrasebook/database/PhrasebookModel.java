package com.bobbytables.phrasebook.database; /**
 * Created by ricky on 17/09/2017.
 */

import android.content.ContentValues;
import android.content.Context;

import com.bobbytables.phrasebook.database.DatabaseHelper;

/**
 * This is an object representation of a phrasebook item
 */
public class PhrasebookModel implements DatabaseModel {
    private int lang1Code;
    private int lang2Code;
    private int bookId;
    private Context context;
    private String tableName;

    public PhrasebookModel(int lang1Code, int lang2Code, Context context) {
        this.lang1Code = lang1Code;
        this.lang2Code = lang2Code;
        this.context = context;
    }

    public PhrasebookModel(int bookId, int lang1Code, int lang2Code, String tableName) {
        this.bookId = bookId;
        this.lang1Code = lang1Code;
        this.lang2Code = lang2Code;
        this.tableName = tableName;
    }

    public String toString() {
        DatabaseHelper db = DatabaseHelper.getInstance(context);
        String lang1 = db.getLanguageName(lang1Code);
        String lang2 = db.getLanguageName(lang2Code);
        return lang1 + " - " + lang2;
    }

    public int getLang1Code() {
        return this.lang1Code;
    }

    public int getLang2Code() {
        return this.lang2Code;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.KEY_BOOK_ID, bookId);
        contentValues.put(DatabaseHelper.KEY_BOOK_LANG1, lang1Code);
        contentValues.put(DatabaseHelper.KEY_BOOK_LANG2, lang2Code);
        return contentValues;
    }

    @Override
    public String getTableName() {
        return this.tableName;
    }
}
