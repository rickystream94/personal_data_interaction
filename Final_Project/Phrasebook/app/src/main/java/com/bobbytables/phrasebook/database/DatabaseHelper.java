package com.bobbytables.phrasebook.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.bobbytables.phrasebook.utils.CSVUtils;
import com.bobbytables.phrasebook.utils.DateUtil;
import com.bobbytables.phrasebook.utils.SettingsManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by ricky on 18/03/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    //Database info
    private static final String DATABASE_NAME = "phrasebookDatabase";
    private static final int DATABASE_VERSION = 2;
    private static final String TAG = DatabaseHelper.class.getName();
    private Context context;
    private CSVUtils csvUtils;
    private static final String BADGES_CSV = "badges.csv";

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
    public static final String KEY_BADGES_ID = "id";
    public static final String KEY_BADGE_NAME = "badgeName";
    public static final String KEY_BADGE_DESCRIPTION = "badgeDesc";
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
            KEY_BADGE_NAME + " TEXT, " +
            KEY_CREATED_ON + " TEXT, " +
            KEY_BADGE_DESCRIPTION + " TEXT)";

    private static DatabaseHelper instance;
    private static final int CORRECT_COUNT_FOR_ARCHIVE = 3;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        this.csvUtils = CSVUtils.getInstance(context);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Called when the database is created for the FIRST time.
     * If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
     * It will be called ONLY when user installs the app for the first time. If he's upgrading,
     * onUpgrade() will be invoked instead.
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_PHRASES_TABLE);
        sqLiteDatabase.execSQL(CREATE_CHALLENGES_TABLE);
        sqLiteDatabase.execSQL(CREATE_BADGES_TABLE);
        try {
            populateBadgesTable(sqLiteDatabase);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateBadgesTable(SQLiteDatabase sqLiteDatabase) {
        List<String[]> badgesData = csvUtils.readCSV(BADGES_CSV);
        for (String[] data : badgesData) {
            DatabaseModel dataObject = new BadgeModel(data[0], data[1], TABLE_BADGES);
            sqLiteDatabase.insertOrThrow(dataObject.getTableName(), null, dataObject.getContentValues());
        }
    }

    /**
     * Invoked every time the user upgrades the app. It should be implemented according to
     * whether the DB version has changed or not (if no changes were made, it should be transparent)
     *
     * @param db         database
     * @param oldVersion old DB version
     * @param newVersion new DB version
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //We need to update the gamification and the profile pic, since they were not specified
        // in version 1. Besides, level was set by mistake to 1. Must be set to 0!
        if (oldVersion == 1) {
            SettingsManager settingsManager = SettingsManager.getInstance(context);
            settingsManager.updatePrefValue(SettingsManager.KEY_LEVEL, 0);
            settingsManager.updatePrefValue(SettingsManager.KEY_GAMIFICATION, (Math.random() < 0.5));
            settingsManager.updatePrefValue(SettingsManager.KEY_PROFILE_PIC, "DEFAULT");
            settingsManager.updatePrefValue(SettingsManager.KEY_SWITCHED_VERSION, false);
            settingsManager.updatePrefValue(SettingsManager.KEY_FINAL_UPLOAD_PERFORMED, false);
            settingsManager.updatePrefValue(SettingsManager.KEY_CREATED, DateUtil.getCurrentTimestamp());
            settingsManager.updatePrefValue(SettingsManager.KEY_IS_FIRST_TIME, false);
        }

        Log.e(TAG, "Updating table from " + oldVersion + " to " + newVersion);
        // You will not need to modify this unless you need to do some android specific things.
        // When upgrading the database, all you need to do is add a file to the assets folder and name it:
        // from_1_to_2.sql with the version that you are upgrading to as the last version.
        try {
            for (int i = oldVersion; i < newVersion; ++i) {
                String migrationName = String.format("from_%d_to_%d.sql", i, (i + 1));
                Log.d(TAG, "Looking for migration file: " + migrationName);
                readAndExecuteSQLScript(db, context, migrationName);
            }
        } catch (Exception exception) {
            Log.e(TAG, "Exception running upgrade script:", exception);
        }
    }

    /**
     * Invoked on upgrade of the DB, allows to execute all the needed SQL scripts in the assets
     * folder. These must be placed properly and contain all the needed SQL statements to upgrade
     * the DB from an old version to a new version (drops, creates, updates etc.)
     *
     * @param db
     * @param ctx
     * @param fileName
     */
    private void readAndExecuteSQLScript(SQLiteDatabase db, Context ctx, String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            Log.d(TAG, "SQL script file name is empty");
            return;
        }

        Log.d(TAG, "Script found. Executing...");
        AssetManager assetManager = ctx.getAssets();
        BufferedReader reader = null;

        try {
            InputStream is = assetManager.open(fileName);
            InputStreamReader isr = new InputStreamReader(is);
            reader = new BufferedReader(isr);
            executeSQLScript(db, reader);
        } catch (IOException e) {
            Log.e(TAG, "IOException:", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOException:", e);
                }
            }
        }

    }

    /**
     * Executes the SQL script defined by the file (reader) contained in assets folder
     *
     * @param db
     * @param reader
     * @throws IOException
     */
    private void executeSQLScript(SQLiteDatabase db, BufferedReader reader) throws IOException {
        String line;
        StringBuilder statement = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            statement.append(line);
            statement.append("\n");
            if (line.endsWith(";")) {
                db.execSQL(statement.toString());
                statement = new StringBuilder();
            }
        }
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
    public boolean phraseAlreadyExists(DatabaseModel dataObject) {
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

    public int getPhraseCorrectCount(String motherLangString, String foreignLangString) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT " + KEY_CORRECT_COUNT + " FROM " + TABLE_PHRASES
                + " " +
                "WHERE " +
                KEY_MOTHER_LANG_STRING + "=? AND " +
                "" + KEY_FOREIGN_LANG_STRING + "=?", new String[]{motherLangString, foreignLangString});
        cursor.moveToFirst();
        int correctCount = cursor.getInt(0);
        cursor.close();
        return correctCount;

    }

    /**
     * Updates the current correct count of a phrase row in the DB
     * If the correct count has reached the minimum to be archived, return true. False otherwise
     *
     * @param motherLangString
     * @param foreignLangString
     * @param increment
     * @return
     */
    public boolean updateCorrectCount(String motherLangString, String foreignLangString, boolean
            increment) {
        SQLiteDatabase database = this.getWritableDatabase();
        int previousCorrectCount = getPhraseCorrectCount(motherLangString, foreignLangString);
        int newValue = increment ? previousCorrectCount+1 : previousCorrectCount-1;
        /*if (increment)
            newValue = KEY_CORRECT_COUNT + "+1";
        else newValue = KEY_CORRECT_COUNT + "-1";*/
        /*String updateQuery = "UPDATE " + TABLE_PHRASES + " SET " + KEY_CORRECT_COUNT + "=" +
                newValue + "" +
                " WHERE " +
                "" + KEY_MOTHER_LANG_STRING + "=? AND " +
                "" + KEY_FOREIGN_LANG_STRING + "=?";
        database.execSQL(updateQuery, new Object[]{motherLangString, foreignLangString});*/
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_CORRECT_COUNT, newValue);
        int affectedRows = database.update(TABLE_PHRASES, contentValues, KEY_MOTHER_LANG_STRING + "=?" +
                " AND " + KEY_FOREIGN_LANG_STRING + "=?", new String[]{motherLangString,
                foreignLangString});

        //Check if correct count has reached the minimum to be archived
        Cursor cursor = database.rawQuery("SELECT " + KEY_CORRECT_COUNT + " FROM " +
                TABLE_PHRASES +
                " " +
                "WHERE " + KEY_MOTHER_LANG_STRING + "=? AND " +
                "" + KEY_FOREIGN_LANG_STRING + "=?", new String[]{motherLangString, foreignLangString});
        boolean isArchived = false;
        if (cursor.moveToFirst()) {
            do {
                int currentCorrectCount = cursor.getInt(0);
                //Mark as archived if correct count reaches the threshold
                if (currentCorrectCount == CORRECT_COUNT_FOR_ARCHIVE && currentCorrectCount > previousCorrectCount) {
                    updateArchived(database, motherLangString, foreignLangString, true);
                    Log.d("DEBUG ARCHIVED", "Current correct:" + currentCorrectCount + " Previous " +
                            "correct: " + previousCorrectCount);
                    isArchived = true;
                }
                //Mark as no more archived when correct count goes down the threshold
                if (previousCorrectCount == CORRECT_COUNT_FOR_ARCHIVE && currentCorrectCount < previousCorrectCount)
                    updateArchived(database, motherLangString, foreignLangString, false);
            } while (cursor.moveToNext()); //actually useless because there will be only one row
            // in the cursor
        }
        cursor.close();
        return isArchived;
    }

    /**
     * Marks a record as archived or not according to isArchived parameter
     *
     * @param database
     * @param motherLangString
     * @param foreignLangString
     * @param isArchived        if true the record will be archived, otherwise it won't be archived
     */
    public void updateArchived(SQLiteDatabase database, String motherLangString, String
            foreignLangString, boolean isArchived) {
        int isArchivedParam = isArchived ? 1 : 0;
        /*database.execSQL("UPDATE " + TABLE_PHRASES + " SET " + KEY_ARCHIVED + "=? " + "WHERE " +
                KEY_MOTHER_LANG_STRING + "=? AND " +
                "" + KEY_FOREIGN_LANG_STRING + "=?", new Object[]{isArchivedParam, motherLangString,
                foreignLangString});*/
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_ARCHIVED, isArchivedParam);
        int affectedRows = database.update(TABLE_PHRASES, contentValues, KEY_MOTHER_LANG_STRING +
                "=? AND " +
                "" + KEY_FOREIGN_LANG_STRING + "=?", new String[]{motherLangString, foreignLangString});
    }

    public void updatePhrase(String oldLang1, String oldLang2, String newLang1, String newLang2) {
        SQLiteDatabase database = this.getReadableDatabase();
        /*database.execSQL("UPDATE " + TABLE_PHRASES + " SET " + KEY_MOTHER_LANG_STRING + "=? AND
         " +
                "" + KEY_FOREIGN_LANG_STRING + "=? WHERE " + KEY_MOTHER_LANG_STRING + "=? AND " +
                "" + KEY_FOREIGN_LANG_STRING + "=?", new Object[]{newLang1, newLang2, oldLang1,
                oldLang2});*/
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_MOTHER_LANG_STRING, newLang1);
        contentValues.put(KEY_FOREIGN_LANG_STRING, newLang2);
        int affectedRows = database.update(TABLE_PHRASES, contentValues, KEY_MOTHER_LANG_STRING +
                "=? AND " +
                "" + KEY_FOREIGN_LANG_STRING + "=?", new String[]{oldLang1, oldLang2});
    }

    public int deletePhrase(String lang1, String lang2) {
        SQLiteDatabase database = this.getReadableDatabase();
        return database.delete(TABLE_PHRASES,KEY_MOTHER_LANG_STRING+"=? AND " +
                ""+KEY_FOREIGN_LANG_STRING+"=?",new String[]{lang1,lang2});
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
        JSONObject obj = createJsonDump();

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

    public JSONObject createJsonDump() {
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
        return obj;
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
        String query;
        switch (table) {
            case TABLE_PHRASES:
                query = "SELECT ID AS _id,* FROM " + table + " ORDER BY " +
                        "datetime(" + KEY_CREATED_ON + ")" +
                        " DESC LIMIT 30";
                break;
            case TABLE_BADGES:
                query = "SELECT ID AS _id,* FROM " + table;
                break;
            default:
                query = ""; //won't happen ideally
        }
        return database.rawQuery(query, null);
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
        Cursor cursor = database.rawQuery("SELECT count(*) FROM " + TABLE_CHALLENGES, null);
        cursor.moveToFirst();
        int total = cursor.getInt(0);
        cursor.close();

        //Get won challenges
        cursor = database.rawQuery("SELECT count(*) FROM " + TABLE_CHALLENGES + " WHERE " +
                "" + KEY_CHALLENGE_CORRECT + "=1", null);
        cursor.moveToFirst();
        int won = cursor.getInt(0);
        cursor.close();

        //Create content values
        ContentValues values = new ContentValues();
        values.put("total", total);
        values.put("won", won);
        return values;
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

    /**
     * Currently limited to 1 year of data
     *
     * @return
     */
    public Cursor getActivityStats() {
        SQLiteDatabase database = this.getReadableDatabase();
        return database.rawQuery("SELECT date(" + KEY_CREATED_ON + ") AS DATE,count(*) FROM " +
                "" + TABLE_PHRASES + "" +
                " GROUP BY date(" + KEY_CREATED_ON + ") ORDER BY DATE ASC LIMIT 365", null);
    }

    /**
     * Currently limited to 1 year of data
     *
     * @return
     */
    public Cursor getChallengesRatio() {
        SQLiteDatabase database = this.getReadableDatabase();
        String rawQuery = "SELECT date(" + KEY_CREATED_ON + ") as DATE, sum(CASE WHEN " +
                "" + KEY_CHALLENGE_CORRECT + "=1 THEN 1 ELSE 0 END)*1.0/count(*) AS RATIO " +
                "FROM " +
                "" + TABLE_CHALLENGES + " GROUP BY date(" + KEY_CREATED_ON + ") ORDER BY DATE" +
                " ASC LIMIT 365";
        return database.rawQuery(rawQuery, null);
    }

    public void updateAchievedBadgeDate(int badgeId) {
        SQLiteDatabase database = this.getReadableDatabase();
        String timestamp = DateUtil.getCurrentTimestamp();
        /*String updateQuery = "UPDATE " + TABLE_BADGES + " SET " + KEY_CREATED_ON + "=?" +
                " WHERE " +
                "" + KEY_BADGES_ID + "=?";
        database.execSQL(updateQuery, new Object[]{timestamp, badgeId});*/
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_CREATED_ON,timestamp);
        int affectedRows = database.update(TABLE_BADGES,contentValues,KEY_BADGES_ID+"="+badgeId,null);
    }

    public Cursor performRawQuery(String query) {
        SQLiteDatabase database = this.getReadableDatabase();
        return database.rawQuery(query, null);
    }

    public boolean getArchivedStatus(String s1, String s2) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT " + KEY_ARCHIVED + " FROM " + TABLE_PHRASES + " WHERE " +
                "" + KEY_MOTHER_LANG_STRING + "=? AND " + KEY_FOREIGN_LANG_STRING + "=?", new String[]{s1,
                s2});
        cursor.moveToFirst();
        boolean result = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ARCHIVED)) == 1;
        cursor.close();
        return result;
    }
}
