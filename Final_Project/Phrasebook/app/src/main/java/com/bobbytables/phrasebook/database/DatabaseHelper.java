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

import com.bobbytables.phrasebook.EditPhrasebookActivity;
import com.bobbytables.phrasebook.Phrasebook;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ricky on 18/03/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    //Database info
    private static final String DATABASE_NAME = "phrasebookDatabase";
    private static final int DATABASE_VERSION = 4; //Updated to version 3: multi-language support
    private static final String TAG = DatabaseHelper.class.getName();
    private Context context;
    private CSVUtils csvUtils;
    private static final String BADGES_CSV = "badges.csv";

    // Table Names
    public static final String TABLE_PHRASES = "phrases";
    public static final String TABLE_CHALLENGES = "challenges";
    public static final String TABLE_BADGES = "badges";
    public static final String TABLE_LANGUAGES = "languages";
    public static final String TABLE_BOOKS = "books";

    // Phrases Table Columns
    private static final String KEY_PHRASE_ID = "id";
    public static final String KEY_LANG1 = "lang1Code";
    public static final String KEY_LANG2 = "lang2Code";
    public static final String KEY_LANG1_VALUE = "lang1Value";
    public static final String KEY_LANG2_VALUE = "lang2Value";
    public static final String KEY_IS_MASTERED = "isMastered";
    public static final String KEY_CORRECT_COUNT = "correctCount";
    public static final String KEY_LAST_PRACTICED_ON = "lastPracticedOn";

    // Challenges Table Columns
    public static final String KEY_CHALLENGE_ID = "id";
    public static final String KEY_CHALLENGE_PHRASE_ID = "phraseId";
    public static final String KEY_CHALLENGE_CORRECT = "correct";

    //Languages Table Columns
    private static final String KEY_LANG_ID = "id";
    private static final String KEY_LANG_NAME = "languageName";

    //Books table columns
    private static final String KEY_BOOK_ID = "id";
    public static final String KEY_BOOK_LANG1 = "lang1";
    public static final String KEY_BOOK_LANG2 = "lang2";

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
            KEY_LANG1 + " TEXT, " +
            KEY_LANG2 + " TEXT, " +
            KEY_LANG1_VALUE + " TEXT, " +
            KEY_LANG2_VALUE + " TEXT, " +
            KEY_IS_MASTERED + " INTEGER, " +
            KEY_CORRECT_COUNT + " INTEGER, " +
            KEY_LAST_PRACTICED_ON + " TEXT, " +
            KEY_CREATED_ON + " TEXT)";

    private static final String CREATE_CHALLENGES_TABLE = "CREATE TABLE " + TABLE_CHALLENGES +
            "(" +
            KEY_CHALLENGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + // Define a primary key
            KEY_CHALLENGE_PHRASE_ID + " INTEGER REFERENCES " + TABLE_PHRASES + ", " + // Define
            // a foreign key
            KEY_CHALLENGE_CORRECT + " INTEGER T, " +
            KEY_CREATED_ON + " TEXT)";

    private static final String CREATE_LANGUAGES_TABLE = "CREATE TABLE " + TABLE_LANGUAGES +
            " (" + KEY_LANG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            KEY_LANG_NAME + " TEXT)";

    private static final String CREATE_BOOKS_TABLE = "CREATE TABLE " + TABLE_BOOKS +
            " (" + KEY_BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            KEY_BOOK_LANG1 + " INTEGER, " +
            KEY_BOOK_LANG2 + " INTEGER)";

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
        sqLiteDatabase.execSQL(CREATE_LANGUAGES_TABLE);
        sqLiteDatabase.execSQL(CREATE_BOOKS_TABLE);
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
            settingsManager.updatePrefValue(SettingsManager.KEY_PROFILE_PIC, "DEFAULT");
            settingsManager.updatePrefValue(SettingsManager.KEY_CREATED, DateUtil.getCurrentTimestamp());
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

        //Names of language preferences have changed, need to update the names and insert the
        // languages in the proper table!
        SettingsManager settingsManager = SettingsManager.getInstance(context);
        if (newVersion == 3) {
            //Get previous languages
            String currentLang1 = settingsManager.getPrefStringValue("motherLanguage");
            String currentLang2 = settingsManager.getPrefStringValue("foreignLanguage");

            //We need to update the columns in the new phrases table with these two values
            ContentValues cv1 = new ContentValues();
            ContentValues cv2 = new ContentValues();
            cv1.put(KEY_LANG_NAME, currentLang1);
            cv2.put(KEY_LANG_NAME, currentLang2);
            db.insertOrThrow(TABLE_LANGUAGES, null, cv1);
            db.insertOrThrow(TABLE_LANGUAGES, null, cv2);
            ContentValues updateCv = new ContentValues();
            updateCv.put(KEY_LANG1, 1);
            updateCv.put(KEY_LANG2, 2);
            int affectedRows = db.update(TABLE_PHRASES, updateCv, KEY_LANG1 +
                    " IS NULL AND " + KEY_LANG2 + " IS NULL", null);
            if (affectedRows > 0)
                Log.d(TAG, "Successfully updated language codes of " + affectedRows + " rows.");
            else
                Log.e(TAG, affectedRows + " rows have updated language codes!");

            //Set as default current languages the previous ones
            settingsManager.updatePrefValue(SettingsManager.KEY_CURRENT_LANG1, 1);
            settingsManager.updatePrefValue(SettingsManager.KEY_CURRENT_LANG2, 2);

            //Delete old keys
            settingsManager.updatePrefValue("motherLanguage", null);
            settingsManager.updatePrefValue("foreignLanguage", null);
        }
        //We need to insert the current couple lang1-lang2 as the first row in the BOOKS table
        if (newVersion == 4) {
            int currentLang1 = settingsManager.getPrefIntValue(SettingsManager
                    .KEY_CURRENT_LANG1);
            int currentLang2 = settingsManager.getPrefIntValue(SettingsManager
                    .KEY_CURRENT_LANG2);
            ContentValues cv = new ContentValues();
            cv.put(KEY_BOOK_LANG1, currentLang1);
            cv.put(KEY_BOOK_LANG2, currentLang2);
            db.insertOrThrow(TABLE_BOOKS, null, cv);
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
        String lang1Value = contentValues.getAsString(KEY_LANG1_VALUE);
        String lang2Value = contentValues.getAsString(KEY_LANG2_VALUE);
        int lang1Code = contentValues.getAsInteger(KEY_LANG1);
        int lang2Code = contentValues.getAsInteger(KEY_LANG2);
        SQLiteDatabase database = this.getReadableDatabase();
        String languageWhereCondition = KEY_LANG1 + "=" + lang1Code + " AND " +
                "" + KEY_LANG2 + "=" + lang2Code;
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_PHRASES + " WHERE " +
                        "" + KEY_LANG1_VALUE + "=? AND " +
                        "" + KEY_LANG2_VALUE + "=? AND " + languageWhereCondition,
                new String[]{lang1Value, lang2Value});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    /**
     * Checks if there is a match between the two inputs
     *
     * @param lang1Value
     * @param lang2Value
     * @param correctTranslation
     * @return true if found (at least one!), false otherwise
     */
    public boolean checkIfCorrect(int lang1Code, int lang2Code, String lang1Value, String
            lang2Value, String correctTranslation) {
        String languageWhereCondition = KEY_LANG1 + "=" + lang1Code + " AND " +
                "" + KEY_LANG2 + "=" + lang2Code;
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT count(*) FROM " + TABLE_PHRASES + " WHERE " +
                "" + KEY_LANG1_VALUE + " =? AND " +
                "" + KEY_LANG2_VALUE + " =? AND " + languageWhereCondition, new String[]{lang1Value,
                lang2Value});
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
     * @param lang1Value text in mother language
     * @return translation
     */
    public String getTranslation(int lang1Code, int lang2Code, String lang1Value) {
        SQLiteDatabase database = this.getReadableDatabase();
        String languageWhereCondition = KEY_LANG1 + "=" + lang1Code + " AND " +
                "" + KEY_LANG2 + "=" + lang2Code;
        Cursor cursor = database.rawQuery("SELECT " + KEY_LANG2_VALUE + " FROM " +
                "" + TABLE_PHRASES + " WHERE " + KEY_LANG1_VALUE + "=? AND " + languageWhereCondition +
                " LIMIT 1", new String[]{lang1Value});
        cursor.moveToFirst();
        String translation = cursor.getString(0);
        cursor.close();
        return translation;
    }

    public int getPhraseId(int lang1Code, int lang2Code, String lang1Value, String lang2Value) {
        SQLiteDatabase database = this.getReadableDatabase();
        String languageWhereCondition = KEY_LANG1 + "=" + lang1Code + " AND " +
                "" + KEY_LANG2 + "=" + lang2Code;
        Cursor cursor = database.rawQuery("SELECT " + KEY_PHRASE_ID + " FROM " +
                "" + TABLE_PHRASES + " WHERE " + KEY_LANG1_VALUE + "=? AND " +
                "" + KEY_LANG2_VALUE + "=? AND " + languageWhereCondition + " LIMIT 1", new String[]{lang1Value,
                lang2Value});
        cursor.moveToFirst();
        int phraseId = cursor.getInt(0);
        cursor.close();
        return phraseId;
    }

    public int getPhraseCorrectCount(int lang1Code, int lang2Code, String lang1Value, String
            lang2Value) {
        String languageWhereCondition = KEY_LANG1 + "=" + lang1Code + " AND " +
                "" + KEY_LANG2 + "=" + lang2Code;
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT " + KEY_CORRECT_COUNT + " FROM " +
                TABLE_PHRASES + " WHERE " + KEY_LANG1_VALUE + "=? AND " +
                "" + KEY_LANG2_VALUE + "=? AND " + languageWhereCondition, new String[]{lang1Value, lang2Value});
        cursor.moveToFirst();
        int correctCount = cursor.getInt(0);
        cursor.close();
        return correctCount;

    }

    /**
     * Updates the current correct count of a phrase row in the DB
     * If the correct count has reached the minimum to be archived, return true. False otherwise
     *
     * @param lang1Value
     * @param lang2Value
     * @param increment
     * @return
     */
    public boolean updateCorrectCount(int lang1Code, int lang2Code, String lang1Value, String
            lang2Value, boolean increment) {
        String languageWhereCondition = KEY_LANG1 + "=" + lang1Code + " AND " +
                "" + KEY_LANG2 + "=" + lang2Code;
        SQLiteDatabase database = this.getWritableDatabase();
        int previousCorrectCount = getPhraseCorrectCount(lang1Code, lang2Code, lang1Value,
                lang2Value);
        int newValue = increment ? previousCorrectCount + 1 : previousCorrectCount - 1;
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_CORRECT_COUNT, newValue);
        int affectedRows = database.update(TABLE_PHRASES, contentValues, KEY_LANG1_VALUE + "=?" +
                " AND " + KEY_LANG2_VALUE + "=?", new String[]{lang1Value,
                lang2Value});

        //Check if correct count has reached the minimum to be archived
        Cursor cursor = database.rawQuery("SELECT " + KEY_CORRECT_COUNT + " FROM " +
                TABLE_PHRASES +
                " " +
                "WHERE " + KEY_LANG1_VALUE + "=? AND " +
                "" + KEY_LANG2_VALUE + "=? AND " + languageWhereCondition, new String[]{lang1Value, lang2Value});
        boolean isArchived = false;
        if (cursor.moveToFirst()) {
            do {
                int currentCorrectCount = cursor.getInt(0);
                //Mark as archived if correct count reaches the threshold
                if (currentCorrectCount == CORRECT_COUNT_FOR_ARCHIVE && currentCorrectCount > previousCorrectCount) {
                    updateArchived(database, lang1Code, lang2Code, lang1Value, lang2Value, true);
                    Log.d("DEBUG ARCHIVED", "Current correct:" + currentCorrectCount + " Previous " +
                            "correct: " + previousCorrectCount);
                    isArchived = true;
                }
                //Mark as no more archived when correct count goes down the threshold
                if (previousCorrectCount == CORRECT_COUNT_FOR_ARCHIVE && currentCorrectCount < previousCorrectCount)
                    updateArchived(database, lang1Code, lang2Code, lang1Value, lang2Value, false);
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
     * @param lang1Value
     * @param lang2Value
     * @param isArchived if true the record will be archived, otherwise it won't be archived
     */
    public void updateArchived(SQLiteDatabase database, int lang1Code, int lang2Code, String
            lang1Value, String lang2Value, boolean isArchived) {
        String languageWhereCondition = KEY_LANG1 + "=" + lang1Code + " AND " +
                "" + KEY_LANG2 + "=" + lang2Code;
        int isArchivedParam = isArchived ? 1 : 0;
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_IS_MASTERED, isArchivedParam);
        int affectedRows = database.update(TABLE_PHRASES, contentValues, KEY_LANG1_VALUE +
                "=? AND " +
                "" + KEY_LANG2_VALUE + "=? AND " + languageWhereCondition, new String[]{lang1Value, lang2Value});
    }

    public int updatePhrase(int lang1Code, int lang2Code, String oldLang1Value, String
            oldLang2Value, String newLang1Value, String newLang2Value) {
        String languageWhereCondition = KEY_LANG1 + "=" + lang1Code + " AND " +
                "" + KEY_LANG2 + "=" + lang2Code;
        SQLiteDatabase database = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_LANG1_VALUE, newLang1Value);
        contentValues.put(KEY_LANG2_VALUE, newLang2Value);
        return database.update(TABLE_PHRASES, contentValues, KEY_LANG1_VALUE +
                        "=? AND " + "" + KEY_LANG2_VALUE + "=? AND " + languageWhereCondition,
                new String[]{oldLang1Value,
                        oldLang2Value});
    }

    public int deletePhrase(int lang1Code, int lang2Code, String lang1Value, String lang2Value) {
        SQLiteDatabase database = this.getReadableDatabase();
        String languageWhereCondition = KEY_LANG1 + "=" + lang1Code + " AND " +
                "" + KEY_LANG2 + "=" + lang2Code;
        return database.delete(TABLE_PHRASES, KEY_LANG1_VALUE + "=? AND " + "" + KEY_LANG2_VALUE
                        + "=? AND " + languageWhereCondition,
                new String[]{lang1Value, lang2Value});
    }

    /**
     * Checks if the database is currently empty (no phrases added)
     *
     * @return true if it's empty, false otherwise
     */
    public boolean isDatabaseEmpty(int lang1Code, int lang2Code) {
        SQLiteDatabase db = this.getWritableDatabase();
        String languageWhereCondition = KEY_LANG1 + "=" + lang1Code + " AND " +
                "" + KEY_LANG2 + "=" + lang2Code;
        String countQuery = "SELECT count(*) FROM " + TABLE_PHRASES + " WHERE " +
                languageWhereCondition;
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
            JSONArray json_phrasebooks = getAllDataFromTable(TABLE_BOOKS);
            JSONArray json_languages = getAllDataFromTable(TABLE_LANGUAGES);
            JSONArray json_user = getUserData(context);
            obj.put("phrases", json_phrases);
            obj.put("phrasebooks", json_phrasebooks);
            obj.put("languages", json_languages);
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

    public String getRandomChallenge(int lang1Code, int lang2Code) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT " + KEY_LANG1_VALUE + " FROM " +
                "" + TABLE_PHRASES + " WHERE " + KEY_LANG1 + "=" + lang1Code + " AND " +
                "" + KEY_LANG2 + "=" + lang2Code + " ORDER BY " +
                "RANDOM() LIMIT 1", null);
        cursor.moveToFirst();
        String result = cursor.getString(0);
        cursor.close();
        return result;
    }

    /**
     * Invoked by the PhrasesFragment, returns a subset of the phrases stored by the user.
     */
    public Cursor getPhrasesList(int lang1Code, int lang2Code, int limit, int offset) {
        String languageWhereCondition = KEY_LANG1 + "=" + lang1Code + " AND " +
                "" + KEY_LANG2 + "=" + lang2Code;
        SQLiteDatabase database = this.getReadableDatabase();
        String query = "SELECT ID AS _id,* FROM " + TABLE_PHRASES + " WHERE ID NOT IN (SELECT ID FROM " +
                "" + TABLE_PHRASES + " WHERE " + languageWhereCondition + " ORDER BY ID DESC " +
                "LIMIT " + offset + ") " +
                " AND " + languageWhereCondition + " ORDER BY " +
                "datetime(" + KEY_CREATED_ON + ")" +
                " DESC LIMIT " + limit;
        return database.rawQuery(query, null);
    }

    public Cursor getAllBadges() {
        SQLiteDatabase database = this.getReadableDatabase();
        String query = "SELECT ID AS _id,* FROM " + TABLE_BADGES;
        return database.rawQuery(query, null);
    }

    public Cursor searchPhrase(int lang1Code, int lang2Code, String query) {
        SQLiteDatabase database = this.getReadableDatabase();
        return database.rawQuery("SELECT ID AS _id,* FROM " + TABLE_PHRASES + " WHERE " + KEY_LANG1 + "=" + lang1Code + " AND " +
                "" + KEY_LANG2 + "=" + lang2Code + " AND " +
                "(" + KEY_LANG1_VALUE + " LIKE ? OR " + KEY_LANG2_VALUE + " " +
                "LIKE ?)", new String[]{"%" + query + "%", "%" + query + "%"});
    }

    public ContentValues getChallengesStats(int lang1Code, int lang2Code) {
        SQLiteDatabase database = this.getReadableDatabase();
        String languageWhereConditions = KEY_LANG1 + "=" + lang1Code + " AND " +
                "" + KEY_LANG2 + "=" + lang2Code;

        //Get total challenges
        Cursor cursor = database.rawQuery("SELECT count(*) FROM " + TABLE_CHALLENGES + " WHERE " +
                KEY_CHALLENGE_PHRASE_ID + " IN (SELECT " + KEY_PHRASE_ID + " FROM " +
                "" + TABLE_PHRASES + " WHERE " + languageWhereConditions + ")", null);
        cursor.moveToFirst();
        int total = cursor.getInt(0);
        cursor.close();

        //Get won challenges
        cursor = database.rawQuery("SELECT count(*) FROM " + TABLE_CHALLENGES + " WHERE " +
                "" + KEY_CHALLENGE_CORRECT + "=1 AND " +
                KEY_CHALLENGE_PHRASE_ID + " IN (SELECT " + KEY_PHRASE_ID +
                " FROM " + TABLE_PHRASES + " WHERE " + languageWhereConditions + ")", null);
        cursor.moveToFirst();
        int won = cursor.getInt(0);
        cursor.close();

        //Create content values
        ContentValues values = new ContentValues();
        values.put("total", total);
        values.put("won", won);
        return values;
    }

    public ContentValues getPhrasesStats(int lang1Code, int lang2Code) {
        String languageWhereConditions = KEY_LANG1 + "=" + lang1Code + " AND " +
                "" + KEY_LANG2 + "=" + lang2Code;
        SQLiteDatabase database = this.getReadableDatabase();

        //Get total phrases
        Cursor cursor = database.rawQuery("SELECT count(*) FROM " + TABLE_PHRASES + " WHERE " + languageWhereConditions,
                null);
        cursor.moveToFirst();
        int total = cursor.getInt(0);
        cursor.close();

        //Get archived phrases
        cursor = database.rawQuery("SELECT count(*) FROM " + TABLE_PHRASES + " WHERE " +
                "" + KEY_IS_MASTERED + "=1 AND " + languageWhereConditions, null);
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
    public Cursor getActivityStats(int lang1Code, int lang2Code) {
        String languageWhereConditions = KEY_LANG1 + "=" + lang1Code + " AND " +
                "" + KEY_LANG2 + "=" + lang2Code;
        SQLiteDatabase database = this.getReadableDatabase();
        return database.rawQuery("SELECT date(" + KEY_CREATED_ON + ") AS DATE,count(*) FROM " +
                "" + TABLE_PHRASES + " WHERE " + languageWhereConditions +
                " GROUP BY date(" + KEY_CREATED_ON + ") ORDER BY DATE ASC LIMIT 365", null);
    }

    /**
     * Currently limited to 1 year of data
     *
     * @return
     */
    public Cursor getChallengesRatio(int lang1Code, int lang2Code) {
        String languageWhereConditions = KEY_LANG1 + "=" + lang1Code + " AND " +
                "" + KEY_LANG2 + "=" + lang2Code;
        SQLiteDatabase database = this.getReadableDatabase();
        String rawQuery = "SELECT date(" + KEY_CREATED_ON + ") as DATE, sum(CASE WHEN " +
                KEY_CHALLENGE_CORRECT + "=1 THEN 1 ELSE 0 END)*1.0/count(*) AS RATIO " +
                "FROM " + TABLE_CHALLENGES + " WHERE " +
                KEY_CHALLENGE_PHRASE_ID + " IN (SELECT " + KEY_PHRASE_ID + " FROM "
                + TABLE_PHRASES + " WHERE " + languageWhereConditions + ")" + " GROUP BY date("
                + KEY_CREATED_ON + ") ORDER BY DATE" +
                " ASC LIMIT 365";
        return database.rawQuery(rawQuery, null);
    }

    public void updateAchievedBadgeDate(int badgeId) {
        SQLiteDatabase database = this.getReadableDatabase();
        String timestamp = DateUtil.getCurrentTimestamp();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_CREATED_ON, timestamp);
        int affectedRows = database.update(TABLE_BADGES, contentValues, KEY_BADGES_ID + "=" + badgeId, null);
    }

    public Cursor performRawQuery(String query) {
        SQLiteDatabase database = this.getReadableDatabase();
        return database.rawQuery(query, null);
    }

    public boolean getArchivedStatus(int lang1Code, int lang2Code, String s1, String s2) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT " + KEY_IS_MASTERED + " FROM " + TABLE_PHRASES + " WHERE " +
                "" + KEY_LANG1_VALUE + "=? AND " + KEY_LANG2_VALUE + "=? AND " + KEY_LANG1 + "=" +
                lang1Code + " AND " +
                "" + KEY_LANG2 + "=" + lang2Code, new String[]{s1,
                s2});
        cursor.moveToFirst();
        boolean result = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_MASTERED)) == 1;
        cursor.close();
        return result;
    }

    public String getLanguageName(int langCode) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT " + KEY_LANG_NAME + " FROM " + TABLE_LANGUAGES + " " +
                "WHERE " + KEY_LANG_ID + "=" + langCode, null);
        cursor.moveToFirst();
        String result = cursor.getString(cursor.getColumnIndexOrThrow(KEY_LANG_NAME));
        cursor.close();
        return result;
    }

    public int getLanguageId(String langName) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT " + KEY_LANG_ID + " FROM " + TABLE_LANGUAGES + " " +
                "WHERE " + KEY_LANG_NAME + "=?", new String[]{langName});
        cursor.moveToFirst();
        int result = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_LANG_ID));
        cursor.close();
        return result;
    }

    /**
     * When creating a new phrasebook or editing an existing one, we need to check if the
     * specified languages already exist or not in the TABLE_LANGUAGES, and eventually include
     * them in order to get their language codes.
     *
     * @param language1
     * @param language2
     * @return hash map with key-value pairs of type language_name - language_code
     */
    public HashMap<String, Integer> addNewLanguages(String language1, String language2) {
        ContentValues cv;
        SQLiteDatabase db = this.getReadableDatabase();
        HashMap<String, Integer> langCodes = new HashMap<>();
        Cursor cursor;
        String getLangIdQuery = "SELECT " + KEY_LANG_ID + " FROM " + TABLE_LANGUAGES + " WHERE " +
                "" + KEY_LANG_NAME + "=?";
        for (String lang : new String[]{language1, language2}) {
            cv = new ContentValues();
            boolean gotId = false;
            while (!gotId) {
                cursor = db.rawQuery(getLangIdQuery, new String[]{lang});
                if (!cursor.moveToFirst()) {
                    cv.put(KEY_LANG_NAME, lang);
                    db.insertOrThrow(TABLE_LANGUAGES, null, cv);
                } else {
                    gotId = true;
                    langCodes.put(lang, cursor.getInt(cursor.getColumnIndexOrThrow(KEY_LANG_ID)));
                }
                cursor.close();
            }
        }
        return langCodes;
    }

    /**
     * This method creates a new phrasebook row in the proper table, and checks if the languages
     * already exist, otherwise it creates new rows in the languages table.
     *
     * @param language1
     * @param language2
     * @throws Exception If trying to add an already existing phrasebook
     */
    public void createPhrasebook(String language1, String language2) throws Exception {
        SQLiteDatabase db = this.getReadableDatabase();
        HashMap<String, Integer> langCodes = addNewLanguages(language1, language2);

        ContentValues cv = new ContentValues();
        cv.put(KEY_BOOK_LANG1, langCodes.get(language1));
        cv.put(KEY_BOOK_LANG2, langCodes.get(language2));

        if (!phrasebookAlreadyExists(cv)) {
            db.insertOrThrow(TABLE_BOOKS, null, cv);
        } else {
            throw new Exception("A phrasebook for " + language1 + " - " + language2 + " is already " +
                    "existing!");
        }
    }

    /**
     * Checks if a phrasebook specified by the languages in the ContentValues object is already
     * existing in the DB. Keys refer to column names
     *
     * @param cv
     * @return
     */
    public boolean phrasebookAlreadyExists(ContentValues cv) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_BOOKS + " WHERE " + KEY_BOOK_LANG1 +
                "=" + cv.getAsInteger(KEY_BOOK_LANG1) +
                " AND " + KEY_BOOK_LANG2 + "=" + cv.getAsInteger(KEY_BOOK_LANG2), null);
        boolean result;
        result = cursor.moveToFirst();
        cursor.close();
        return result;
    }

    /**
     * Retrieves all phrasebook names with a specific string format (e.g. "ITA - ENG")
     *
     * @return
     */
    public List<Phrasebook> getAllPhrasebooks() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_BOOKS, null);
        List<Phrasebook> allPhrasebooks = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int langId1 = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_BOOK_LANG1));
                int langId2 = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_BOOK_LANG2));
                allPhrasebooks.add(new Phrasebook(langId1, langId2, context));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return allPhrasebooks;
    }

    /**
     * Updates the current phrasebook with the new languages. Automatically creates new rows for
     * new languages in the corresponding table
     *
     * @param oldLang1
     * @param oldLang2
     * @param newLanguage1
     * @param newLanguage2
     * @return
     * @throws Exception
     */
    public int updatePhrasebook(int oldLang1, int oldLang2, String newLanguage1, String
            newLanguage2) throws Exception {
        SQLiteDatabase db = this.getReadableDatabase();
        HashMap<String, Integer> langCodes = addNewLanguages(newLanguage1, newLanguage2);

        ContentValues cvPhrasebookUpdate = new ContentValues();
        cvPhrasebookUpdate.put(KEY_BOOK_LANG1, langCodes.get(newLanguage1));
        cvPhrasebookUpdate.put(KEY_BOOK_LANG2, langCodes.get(newLanguage2));

        ContentValues cvPhrasesUpdate = new ContentValues();
        cvPhrasesUpdate.put(KEY_LANG1, langCodes.get(newLanguage1));
        cvPhrasesUpdate.put(KEY_LANG2, langCodes.get(newLanguage2));

        if (!phrasebookAlreadyExists(cvPhrasebookUpdate)) {
            int affectedPhrasebooks = db.update(TABLE_BOOKS, cvPhrasebookUpdate, KEY_BOOK_LANG1 + "=" + oldLang1 + " AND" +
                    " " +
                    "" + KEY_BOOK_LANG2 + "=" + oldLang2, null);
            int affectedPhrases = db.update(TABLE_PHRASES, cvPhrasesUpdate,
                    KEY_LANG1 + "=" + oldLang1 + " AND " + KEY_LANG2 + "=" + oldLang2, null);
            //Update current languages before returning
            SettingsManager settingsManager = SettingsManager.getInstance(context);
            settingsManager.updatePrefValue(SettingsManager.KEY_CURRENT_LANG1, langCodes.get(newLanguage1));
            settingsManager.updatePrefValue(SettingsManager.KEY_CURRENT_LANG2, langCodes.get
                    (newLanguage2));
            return affectedPhrasebooks;
        } else {
            throw new Exception("A phrasebook for " + newLanguage1 + " - " + newLanguage2 + " is already " +
                    "existing!");
        }
    }

    public void deletePhrasebook(int lang1, int oldLang2) {

    }
}
