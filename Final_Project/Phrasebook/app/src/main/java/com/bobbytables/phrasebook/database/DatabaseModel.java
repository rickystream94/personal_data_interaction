package com.bobbytables.phrasebook.database;

import android.content.ContentValues;

/**
 * Created by ricky on 18/03/2017.
 */

/**
 * This interface represents a common database model, therefore all the classes that implement
 * this interface represent a single record in a Database table.
 */
public interface DatabaseModel {
    public ContentValues getContentValues();
    public String getTableName();
}
