package com.bobbytables.phrasebook.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.bobbytables.phrasebook.utils.SettingsManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ricky on 18/03/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    //Database info
    private static final String DATABASE_NAME = "phrasebookDatabase";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    public static final String TABLE_PHRASES = "phrases";
    public static final String TABLE_CHALLENGES = "challenges";
    public static final String TABLE_BADGES = "badges";

    // Phrases Table Columns
    private static final String KEY_PHRASE_ID = "id";
    public static final String KEY_MOTHER_LANG_STRING = "motherLangString";
    public static final String KEY_FOREIGN_LANG_STRING = "foreignLangString";
    public static final String KEY_ARCHIVED = "archived";
    public static final String KEY_CORRECT_COUNT = "correctCount";

    // Challenges Table Columns
    public static final String KEY_CHALLENGE_ID = "id";
    public static final String KEY_CHALLENGE_PHRASE_ID = "phraseId";
    public static final String KEY_CHALLENGE_CORRECT = "correct";

    // Badges Table Columns
    private static final String KEY_BADGES_ID = "id";
    public static final String KEY_BADGE_TYPE_ID = "badgeId";

    //Common columns
    public static final String KEY_CREATED_ON = "createdOn";

    //Create statements
    private static final String CREATE_PHRASES_TABLE = "CREATE TABLE " + TABLE_PHRASES +
            "(" +
            KEY_PHRASE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + // Define a primary key
            KEY_MOTHER_LANG_STRING + " TEXT, " +
            KEY_FOREIGN_LANG_STRING + " TEXT, " +
            KEY_ARCHIVED + " INTEGER, " +
            KEY_CORRECT_COUNT + " INTEGER, " +
            KEY_CREATED_ON + " TEXT)";

    private static final String CREATE_CHALLENGES_TABLE = "CREATE TABLE " + TABLE_CHALLENGES +
            "(" +
            KEY_CHALLENGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + // Define a primary key
            KEY_CHALLENGE_PHRASE_ID + " INTEGER REFERENCES " + TABLE_PHRASES + ", " + // Define
            // a foreign key
            KEY_CHALLENGE_CORRECT + " INTEGER T, " +
            KEY_CREATED_ON + " TEXT)";

    private static final String CREATE_BADGES_TABLE = "CREATE TABLE " + TABLE_BADGES +
            "(" +
            KEY_BADGES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + // Define a primary key
            KEY_BADGE_TYPE_ID + " INTEGER, " +
            KEY_CREATED_ON + " TEXT)";

    private static DatabaseHelper instance;
    private static final int CORRECT_COUNT_FOR_ARCHIVE = 3;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_PHRASES_TABLE);
        sqLiteDatabase.execSQL(CREATE_CHALLENGES_TABLE);
        sqLiteDatabase.execSQL(CREATE_BADGES_TABLE);
        //Badges table must be filled here
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    /**
     * Inserts a new data object in a specified table
     *
     * @param dataObject either a new phrase or challenge data row
     * @throws Exception if the data row already exists in the DB
     */
    public void insertRecord(DatabaseModel dataObject) throws Exception {
        if (dataObject.getTableName().equals(TABLE_PHRASES))
            if (phraseAlreadyExists(dataObject))
                throw new Exception("Error! Record already existing!");
        SQLiteDatabase database = this.getWritableDatabase();
        long id = database.insertOrThrow(dataObject.getTableName(), null, dataObject.getContentValues());
        Log.d("DB", String.format("Saved new record in table " + dataObject.getTableName() + " with ID: %d", id));
    }

    /**
     * A phrase already exists if both mother and foreign language strings are already existing
     * in the DB in the same record (this allows synonyms)
     *
     * @param dataObject the data object to insert in the DB
     * @return true if a record is found, false otherwise
     */
    private boolean phraseAlreadyExists(DatabaseModel dataObject) {
        ContentValues contentValues = dataObject.getContentValues();
        String motherLanguageString = contentValues.getAsString(KEY_MOTHER_LANG_STRING);
        String foreignLanguageString = contentValues.getAsString(KEY_FOREIGN_LANG_STRING);
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_PHRASES + " WHERE " +
                        "" + KEY_MOTHER_LANG_STRING + "=? AND " +
                        "" + KEY_FOREIGN_LANG_STRING + "=?",
                new String[]{motherLanguageString, foreignLanguageString});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    /**
     * Checks if there is a match between the two inputs
     *
     * @param motherLanguageString
     * @param foreignLangString
     * @param correctTranslation
     * @return true if found (at least one!), false otherwise
     */
    public boolean checkIfCorrect(String motherLanguageString, String foreignLangString, String correctTranslation) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT count(*) FROM " + TABLE_PHRASES + " WHERE " +
                "" + KEY_MOTHER_LANG_STRING + " =? AND " +
                "" + KEY_FOREIGN_LANG_STRING + " =?", new String[]{motherLanguageString,
                foreignLangString});
        cursor.moveToFirst();
        boolean result = cursor.getInt(0) > 0;
        cursor.close();
        return result;
    }

    /**
     * Returns the correct translation of the meaning passed as parameter.
     * IMPORTANT: Returns just the first matching result, if multiple meanings are found, they're
     * ignored
     *
     * @param motherLanguageString text in mother language
     * @return translation
     */
    public String getTranslation(String motherLanguageString) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT " + KEY_FOREIGN_LANG_STRING + " FROM " +
                "" + TABLE_PHRASES + " WHERE " + KEY_MOTHER_LANG_STRING + "=? " +
                "LIMIT 1", new String[]{motherLanguageString});
        cursor.moveToFirst();
        String translation = cursor.getString(0);
        cursor.close();
        return translation;
    }

    public int getPhraseId(String motherLangString, String foreignLangString) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT " + KEY_PHRASE_ID + " FROM " +
                "" + TABLE_PHRASES + " WHERE " + KEY_MOTHER_LANG_STRING + "=? AND " +
                "" + KEY_FOREIGN_LANG_STRING + "=? LIMIT 1", new String[]{motherLangString,
                foreignLangString});
        cursor.moveToFirst();
        int phraseId = cursor.getInt(0);
        cursor.close();
        return phraseId;
    }

    /**
     * Updates the current correct count of a phrase row in the DB
     * If the correct count has reached the minimum to be archived, return true. False otherwise
     * @param motherLangString
     * @param foreignLangString
     * @param increment
     * @return
     */
    public boolean updateCorrectCount(String motherLangString, String foreignLangString, boolean
            increment) {
        String newValue;
        if (increment)
            newValue = KEY_CORRECT_COUNT + "+1";
        else newValue = KEY_CORRECT_COUNT + "-1";
        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "UPDATE " + TABLE_PHRASES + " SET " + KEY_CORRECT_COUNT + "=" +
                newValue + "" +
                " WHERE " +
                "" + KEY_MOTHER_LANG_STRING + "=? AND " +
                "" + KEY_FOREIGN_LANG_STRING + "=?";
        database.execSQL(updateQuery, new Object[]{motherLangString, foreignLangString}); //Always use execSQL with
        // update statements!

        //Check if correct count has reached the minimum to be archived
        Cursor cursor = database.rawQuery("SELECT " + KEY_CORRECT_COUNT + " FROM " +
                TABLE_PHRASES +
                " " +
                "WHERE " + KEY_MOTHER_LANG_STRING + "=? AND " +
                "" + KEY_FOREIGN_LANG_STRING + "=?", new String[]{motherLangString, foreignLangString});
        boolean result = false;
        if (cursor.moveToFirst()) {
            do {
                int currentCorrect = cursor.getInt(0);
                if (currentCorrect == CORRECT_COUNT_FOR_ARCHIVE) {
                    updateArchived(database, motherLangString, foreignLangString);
                    result = true;
                }
            } while (cursor.moveToNext()); //actually useless because there will be only one row
            // in the cursor
        }
        cursor.close();
        return result;
    }

    public void updateArchived(SQLiteDatabase database, String motherLangString, String foreignLangString) {
        database.execSQL("UPDATE " + TABLE_PHRASES + " SET " + KEY_ARCHIVED + "=1 " + "WHERE " +
                KEY_MOTHER_LANG_STRING + "=? AND " +
                "" + KEY_FOREIGN_LANG_STRING + "=?", new Object[]{motherLangString, foreignLangString});
    }

    //Future implementation, allows to edit a currently existing record in the DB
    public void updatePhrase() {
    }

    /**
     * Checks if the database is currently empty (no phrases added)
     *
     * @return true if it's empty, false otherwise
     */
    public boolean isDatabaseEmpty() {
        SQLiteDatabase db = this.getWritableDatabase();
        String countQuery = "SELECT count(*) FROM " + TABLE_PHRASES;
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count == 0;
    }

    public void reset() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHRASES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BADGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHALLENGES);
        onCreate(db);
    }

    public void exportToJSON(Context context) {
        String currentTimeString = new SimpleDateFormat("yMMddHHmmss").format(new Date());
        JSONObject obj = new JSONObject();
        try {
            JSONArray json_phrases = getAllDataFromTable(TABLE_PHRASES);
            JSONArray json_challenges = getAllDataFromTable(TABLE_CHALLENGES);
            JSONArray json_badges = getAllDataFromTable(TABLE_BADGES);
            JSONArray json_user = getUserData(context);
            obj.put("phrasebook", json_phrases);
            obj.put("challenges", json_challenges);
            obj.put("badges", json_badges);
            obj.put("user", json_user);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        File savePath;
        String fileName = String.format("PhrasebookDump_%s.json", currentTimeString);
        savePath = new File(Environment.getExternalStoragePublicDirectory(Environment
                .DIRECTORY_DOWNLOADS), "Phrasebook_Exports");

        if (!savePath.exists()) {
            if (!savePath.mkdirs()) {
                Log.d("EXPORT ERROR", "Could not create directory!");
            }
        }

        File file = new File(savePath, fileName);
        if (file.exists()) {
            Log.d("EXPORT", "File already exists!");
        }

        Log.d("SAVING", savePath.getAbsolutePath() + "/" + fileName);
        try {
            FileOutputStream out = new FileOutputStream(file);
            out.write(obj.toString().getBytes());
            out.flush();
            out.close();
            Log.i("EXPORT", "File saved!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JSONArray getUserData(Context context) throws JSONException {
        JSONArray resultSet = new JSONArray();
        SettingsManager settingsManager = SettingsManager.getInstance(context);
        JSONObject rowObject = settingsManager.getUserData();
        resultSet.put(rowObject);
        Log.d("USER DATA DUMP:", resultSet.toString());
        return resultSet;
    }

    private JSONArray getAllDataFromTable(String tablename) {

        SQLiteDatabase database = this.getWritableDatabase();
        String searchQuery = "SELECT  * FROM " + tablename;
        Cursor cursor = database.rawQuery(searchQuery, null);

        JSONArray resultSet = new JSONArray();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();

            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        int columnType = cursor.getType(i);
                        switch (columnType) {
                            case Cursor.FIELD_TYPE_INTEGER:
                                rowObject.put(cursor.getColumnName(i), cursor.getInt(i));
                                break;
                            case Cursor.FIELD_TYPE_STRING:
                                rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                                break;
                            case Cursor.FIELD_TYPE_NULL:
                                throw new Exception("Unhandled data type stored in the DB!");
                            default:
                                throw new Exception("Unhandled data type stored in the DB!");
                        }
                    } catch (Exception e) {
                        Log.e("Column export error!", e.getMessage());
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();
        Log.d(String.format("DB DUMP: %s", tablename), resultSet.toString());
        return resultSet;
    }

    public String getRandomChallenge() {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT " + KEY_MOTHER_LANG_STRING + " FROM " +
                "" + TABLE_PHRASES + " ORDER BY RANDOM() LIMIT 1", null);
        cursor.moveToFirst();
        String result = cursor.getString(0);
        cursor.close();
        return result;
    }

    /**
     * Invoked by the PhrasesFragment, returns a subset of the phrases stored by the user.
     *
     * @param table
     * @return
     */
    public Cursor getDataFromTable(String table) {
        SQLiteDatabase database = this.getReadableDatabase();
        return database.rawQuery("SELECT ID AS _id,* FROM " + table + " ORDER BY " +
                        "datetime(" + KEY_CREATED_ON + ")" +
                        " DESC LIMIT 30",
                null);
    }

    public Cursor searchPhrase(String query) {
        SQLiteDatabase database = this.getReadableDatabase();
        return database.rawQuery("SELECT ID AS _id,* FROM " + TABLE_PHRASES + " WHERE " +
                "" + KEY_MOTHER_LANG_STRING + " LIKE ? OR " + KEY_FOREIGN_LANG_STRING + " " +
                "LIKE ?", new String[]{"%" + query + "%", "%" + query + "%"});
    }

    public ContentValues getChallengesStats() {
        SQLiteDatabase database = this.getReadableDatabase();

        //Get total challenges
        Cursor cursor = database.rawQuery("SELECT count(*) FROM "+TABLE_CHALLENGES,null);
        cursor.moveToFirst();
        int total = cursor.getInt(0);
        cursor.close();

        //Get won challenges
        cursor = database.rawQuery("SELECT count(*) FROM "+TABLE_CHALLENGES +" WHERE " +
                ""+KEY_CHALLENGE_CORRECT+"=1",null);
        cursor.moveToFirst();
        int won = cursor.getInt(0);
        cursor.close();

        //Create content values
        ContentValues values = new ContentValues();
        values.put("total",total);
        values.put("won",won);
        return  values;
    }

    public ContentValues getPhrasesStats() {
        SQLiteDatabase database = this.getReadableDatabase();

        //Get total phrases
        Cursor cursor = database.rawQuery("SELECT count(*) FROM " + TABLE_PHRASES, null);
        cursor.moveToFirst();
        int total = cursor.getInt(0);
        cursor.close();

        //Get archived phrases
        cursor = database.rawQuery("SELECT count(*) FROM " + TABLE_PHRASES + " WHERE " +
                "" + KEY_ARCHIVED + "=1", null);
        cursor.moveToFirst();
        int archived = cursor.getInt(0);
        cursor.close();

        ContentValues values = new ContentValues();
        values.put("total", total);
        values.put("archived", archived);
        return values;
    }
}
