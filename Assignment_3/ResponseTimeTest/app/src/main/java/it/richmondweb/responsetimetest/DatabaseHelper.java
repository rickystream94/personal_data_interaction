package it.richmondweb.responsetimetest;
        import android.content.Context;
        import android.database.Cursor;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteOpenHelper;
        import android.os.Environment;
        import android.util.Log;
        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;
        import java.io.File;
        import java.io.FileOutputStream;
        import java.text.SimpleDateFormat;
        import java.util.Date;

public class DatabaseHelper extends SQLiteOpenHelper {

    /*
    * Schema
    * TEST_RESPONSE_TIME
    */
    public static final String DATABASE_TABLE_TEST_RESPONSE_TIME = "test_response_time";
    public static final String TEST_RESPONSE_TIME_COLUMN_ID = "_id";
    public static final String TEST_RESPONSE_TIME_COLUMN_CREATED = "created";
    public static final String TEST_RESPONSE_TIME_COLUMN_DELAY = "delay";
    public static final String TEST_RESPONSE_TIME_COLUMN_ACCEPTABLE = "acceptable";
    public static final String TEST_RESPONSE_TIME_COLUMN_NAME = "name";
    private static final String DATABASE_NAME = "tests";
    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_DATABASE_TABLE_TEST_RESONSE_TIME = "create table "
            + DATABASE_TABLE_TEST_RESPONSE_TIME + "( "
            + TEST_RESPONSE_TIME_COLUMN_ID + " integer primary key autoincrement, "
            + TEST_RESPONSE_TIME_COLUMN_CREATED + " String, "
            + TEST_RESPONSE_TIME_COLUMN_DELAY + " integer null, "
            + TEST_RESPONSE_TIME_COLUMN_ACCEPTABLE + " integer null, "
            + TEST_RESPONSE_TIME_COLUMN_NAME + " String null"
            + ");";
    private static DatabaseHelper sInstance;


    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DB", "Setting up new DB");
        db.execSQL(CREATE_DATABASE_TABLE_TEST_RESONSE_TIME);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(DatabaseHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");

        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_TEST_RESPONSE_TIME);

        onCreate(db);
    }

    public void reset() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_TEST_RESPONSE_TIME);
        onCreate(db);
    }

    public void export() {
        this.exportToJSON();
    }

    public void insertResponseTimeTest(int delay, boolean acceptable, String name) {
        ResponseTimeTestModel obj = new ResponseTimeTestModel(delay, acceptable, name);

        SQLiteDatabase database = this.getWritableDatabase();

        long id = database.insert(DATABASE_TABLE_TEST_RESPONSE_TIME, null, obj.getContentValues());
        Log.d("DB", String.format("Saved new test with ID: %d", id));
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
        JSONArray json_arr = getAllDataFromTable(DATABASE_TABLE_TEST_RESPONSE_TIME);

        JSONObject obj = new JSONObject();

        try {
            obj.put("response_time_tests", json_arr);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        File savePath;
        String fileName = String.format("ResponseTimeTests-%s.json", currentTimeString);
        savePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "ResponseTimeTests");

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
}