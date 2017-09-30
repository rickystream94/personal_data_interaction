package com.bobbytables.phrasebook; /**
 * Created by ricky on 17/09/2017.
 */

import android.content.Context;

import com.bobbytables.phrasebook.database.DatabaseHelper;

/**
 * This is an object representation of a phrasebook item
 */
public class Phrasebook {
    private int lang1Code;
    private int lang2Code;
    private Context context;

    public Phrasebook(int lang1Code, int lang2Code, Context context) {
        this.lang1Code = lang1Code;
        this.lang2Code = lang2Code;
        this.context = context;
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

}
