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
    int UNSPECIFIED_ID_VALUE = -1;

    ContentValues getContentValues();

    String getTableName();
}
