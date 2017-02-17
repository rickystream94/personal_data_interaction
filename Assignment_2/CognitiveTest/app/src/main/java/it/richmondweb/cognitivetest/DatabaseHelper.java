package it.richmondweb.cognitivetest;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import it.richmondweb.cognitivetest.Models.EriksenFlanker;

public class DatabaseHelper extends SQLiteOpenHelper {

    /*
    * Schema
    * TEST_ERIKSEN_FLANKER
    */
    public static final String DATABASE_TABLE_TEST_ERIKSEN_FLANKER = "test_eriksenflanker";
    public static final String TEST_ERIKSEN_FLANKER_COLUMN_ID = "_id";
    public static final String TEST_ERIKSEN_FLANKER_COLUMN_CREATED = "created";
    public static final String TEST_ERIKSEN_FLANKER_COLUMN_CORRECT = "correct";
    public static final String TEST_ERIKSEN_FLANKER_COLUMN_INCORRECT = "incorrect";
    private static final String DATABASE_NAME = "cognitive_test";
    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_DATABASE_TABLE_TEST_ERIKSEN_FLANKER = "create table "
            + DATABASE_TABLE_TEST_ERIKSEN_FLANKER + "( "
            + TEST_ERIKSEN_FLANKER_COLUMN_ID + " integer primary key autoincrement, "
            + TEST_ERIKSEN_FLANKER_COLUMN_CREATED + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
            + TEST_ERIKSEN_FLANKER_COLUMN_CORRECT + " integer null, "
            + TEST_ERIKSEN_FLANKER_COLUMN_INCORRECT + " integer null"
            + ");";
    private static DatabaseHelper sInstance;

    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static method "getInstance()" instead.
     */
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
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

    public void reset() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_TEST_ERIKSEN_FLANKER);
        onCreate(db);
    }

    public void export() {
        this.exportToJSON();
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
            while (!cursor.isAfterLast()) {
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

    private JSONArray getAllDataFromTable(String tablename) {

        SQLiteDatabase database = this.getWritableDatabase();
        String searchQuery = "SELECT  * FROM " + tablename;
        Cursor cursor = database.rawQuery(searchQuery, null);

        JSONArray resultSet = new JSONArray();

        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {

            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();

            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        if (cursor.getString(i) != null) {
                            rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                        } else if (cursor.getInt(i) >= 0) {
                            rowObject.put(cursor.getColumnName(i), cursor.getInt(i));
                        } else {
                            rowObject.put(cursor.getColumnName(i), "");
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

    private void exportToJSON() {
        String currentTimeString = new SimpleDateFormat("yMMddHHmmss").format(new Date());

        JSONArray json_eriksen_flanker = getAllDataFromTable(DATABASE_TABLE_TEST_ERIKSEN_FLANKER);

        JSONObject obj = new JSONObject();

        try {
            obj.put("eriksen_flanker", json_eriksen_flanker);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        File savePath;
        String fileName = String.format("CognitiveTests-%s.json", currentTimeString);
        savePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "CognitiveTests");

        if (!savePath.exists()) {
            if (!savePath.mkdirs()) {
                // show error
                Log.e("EXPORT ERROR", "Could not create directory!");
                //Toast.makeText(this, "Error: could not create directory", Toast.LENGTH_LONG).show();
            }
        }

        File file = new File(savePath, fileName);
        if (file.exists()) {
//            Toast.makeText(this.instanceContext, "File " + fileName + " already exists.", Toast.LENGTH_LONG).show();
            Log.e("EXPORT", "File already exists!");
        }

        Log.d("SAVING", savePath.getAbsolutePath() + "/" + fileName);
        try {
            FileOutputStream out = new FileOutputStream(file);
            out.write(obj.toString().getBytes());
            out.flush();
            out.close();
//            Toast.makeText(this.instanceContext, "File " + fileName + " saved!", Toast.LENGTH_LONG).show();
            Log.i("EXPORT", "File saved!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
        private void checkWritePermissions(){

//        if (ContextCompat.checkSelfPermission(getActivity(),
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // We request the permission.
                Log.e("requesting", "write external permissions");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }
     */

}