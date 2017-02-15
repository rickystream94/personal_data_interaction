package it.richmondweb.cognitivetest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import it.richmondweb.cognitivetest.Models.EriksenFlanker;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper sInstance;

    private static final String DATABASE_NAME = "cognitive_test";
    private static final int DATABASE_VERSION = 1;

    public static synchronized DatabaseHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static method "getInstance()" instead.
     */
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DB", "Setting up new DB");
        db.execSQL(CREATE_DATABASE_TABLE_TEST_ERIKSEN_FLANKER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(DatabaseHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");

        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_TEST_ERIKSEN_FLANKER);

        onCreate(db);
    }

    public void insertEriksenFlankerTest(int correct, int incorrect) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(TEST_ERIKSEN_FLANKER_COLUMN_CORRECT, correct);
        v.put(TEST_ERIKSEN_FLANKER_COLUMN_INCORRECT, incorrect);
        long id = database.insert(DATABASE_TABLE_TEST_ERIKSEN_FLANKER, null, v);
        Log.d("DB", String.format("Saved new Eriksen Flanker Test with ID: %d", id));
    }

    public ArrayList<EriksenFlanker> getAllEriksenFlankerTests() {
        ArrayList<EriksenFlanker> tests = new ArrayList<>();
        String sql = "SELECT * FROM " + DATABASE_TABLE_TEST_ERIKSEN_FLANKER;
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(sql, null);
        cursor.moveToFirst();
        if(cursor.getCount() > 0) {
            while (cursor.isAfterLast() == false) {
                int id = cursor.getInt(cursor.getColumnIndex(TEST_ERIKSEN_FLANKER_COLUMN_ID));
                String created = cursor.getString(cursor.getColumnIndex(TEST_ERIKSEN_FLANKER_COLUMN_CREATED));
                int correct = cursor.getInt(cursor.getColumnIndex(TEST_ERIKSEN_FLANKER_COLUMN_CORRECT));
                int incorrect = cursor.getInt(cursor.getColumnIndex(TEST_ERIKSEN_FLANKER_COLUMN_INCORRECT));
                tests.add(new EriksenFlanker(id, created, correct, incorrect));
                cursor.moveToNext();
            }
        }
        return tests;
    }

    /*
    * Schema
    * TEST_ERIKSEN_FLANKER
    */
    public static final String DATABASE_TABLE_TEST_ERIKSEN_FLANKER = "test_eriksenflanker";
    public static final String TEST_ERIKSEN_FLANKER_COLUMN_ID = "_id";
    public static final String TEST_ERIKSEN_FLANKER_COLUMN_CREATED = "created";
    public static final String TEST_ERIKSEN_FLANKER_COLUMN_CORRECT = "correct";
    public static final String TEST_ERIKSEN_FLANKER_COLUMN_INCORRECT = "incorrect";


    private static final String CREATE_DATABASE_TABLE_TEST_ERIKSEN_FLANKER = "create table "
            + DATABASE_TABLE_TEST_ERIKSEN_FLANKER + "( "
            + TEST_ERIKSEN_FLANKER_COLUMN_ID + " integer primary key autoincrement, "
            + TEST_ERIKSEN_FLANKER_COLUMN_CREATED + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
            + TEST_ERIKSEN_FLANKER_COLUMN_CORRECT + " integer null, "
            + TEST_ERIKSEN_FLANKER_COLUMN_INCORRECT + " integer null"
            + ");";
}