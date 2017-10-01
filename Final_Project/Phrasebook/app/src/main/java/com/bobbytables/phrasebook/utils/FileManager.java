package com.bobbytables.phrasebook.utils;

import android.content.Context;
import android.os.Environment;
import android.util.JsonReader;
import android.util.Log;
import android.widget.Toast;

import com.bobbytables.phrasebook.database.BadgeModel;
import com.bobbytables.phrasebook.database.ChallengeModel;
import com.bobbytables.phrasebook.database.DatabaseHelper;
import com.bobbytables.phrasebook.database.DatabaseModel;
import com.bobbytables.phrasebook.database.LanguageModel;
import com.bobbytables.phrasebook.database.PhraseModel;
import com.bobbytables.phrasebook.database.PhrasebookModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by ricky on 01/10/2017.
 */

public class FileManager {
    private static FileManager instance;
    private Context context;
    private DatabaseHelper databaseHelper;
    private SettingsManager settingsManager;
    private static final String TAG = FileManager.class.getSimpleName();
    private static final String EXPORT_PATH = Environment.DIRECTORY_DOWNLOADS;
    private static final String EXPORT_FOLDER_NAME = "Phrasebook_Exports";
    private static final String EXPORT_FILENAME_FORMAT = "PhrasebookDump_%s.json";
    private static final String CHARSET = "UTF-8";

    private FileManager(Context context) {
        this.context = context;
        databaseHelper = DatabaseHelper.getInstance(context);
        settingsManager = SettingsManager.getInstance(context);
    }

    public static synchronized FileManager getInstance(Context context) {
        if (instance == null) {
            instance = new FileManager(context.getApplicationContext());
        }
        return instance;
    }

    public void exportDataToJSON() throws IOException {
        String currentTimeString = new SimpleDateFormat("yMMddHHmmss").format(new Date());
        JSONObject obj = createJsonDump();

        String fileName = String.format(EXPORT_FILENAME_FORMAT, currentTimeString);
        File savePath = new File(Environment.getExternalStoragePublicDirectory(EXPORT_PATH),
                EXPORT_FOLDER_NAME);

        if (!savePath.exists()) {
            if (!savePath.mkdirs()) {
                String error = "Could not create export folder!";
                Log.e(TAG, error);
                throw new IOException(error);
            }
        }

        File file = new File(savePath, fileName);
        if (file.exists()) {
            Log.d(TAG, "File already exists!");
        }

        Log.d(TAG, savePath.getAbsolutePath() + "/" + fileName);
        FileOutputStream out = new FileOutputStream(file);
        out.write(obj.toString().getBytes());
        out.flush();
        out.close();
        Log.i(TAG, "File saved!");
        Toast.makeText(context, "Data exported in " + EXPORT_PATH + "/" + EXPORT_FOLDER_NAME + "/" +
                fileName + " as " + "JSON file", Toast.LENGTH_LONG).show();
    }

    private JSONObject createJsonDump() {
        JSONObject obj = new JSONObject();
        try {
            JSONArray json_phrases = databaseHelper.getAllDataFromTable(DatabaseHelper
                    .TABLE_PHRASES);
            JSONArray json_challenges = databaseHelper.getAllDataFromTable(DatabaseHelper.TABLE_CHALLENGES);
            JSONArray json_badges = databaseHelper.getAllDataFromTable(DatabaseHelper.TABLE_BADGES);
            JSONArray json_phrasebooks = databaseHelper.getAllDataFromTable(DatabaseHelper.TABLE_BOOKS);
            JSONArray json_languages = databaseHelper.getAllDataFromTable(DatabaseHelper.TABLE_LANGUAGES);
            JSONArray json_user = settingsManager.getUserData();
            obj.put(DatabaseHelper.TABLE_PHRASES, json_phrases);
            obj.put(DatabaseHelper.TABLE_BOOKS, json_phrasebooks);
            obj.put(DatabaseHelper.TABLE_LANGUAGES, json_languages);
            obj.put(DatabaseHelper.TABLE_CHALLENGES, json_challenges);
            obj.put(DatabaseHelper.TABLE_BADGES, json_badges);
            obj.put("user", json_user);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    /**
     * Reads the latest created JSON backup in the export folder and restores all the data in the
     * DB, including user data
     */
    public void importDataFromBackup() throws Exception {
        databaseHelper.reset();
        File importPath = new File(Environment.getExternalStoragePublicDirectory(EXPORT_PATH),
                EXPORT_FOLDER_NAME);

        //Check if folders exist
        if (!importPath.exists()) {
            String error = "Import folder doesn't exist!";
            Log.e(TAG, error);
            throw new IOException(error);
        }

        File lastModifiedBackup = lastFileModified(importPath);
        FileInputStream in = new FileInputStream(lastModifiedBackup);
        int size = in.available();
        byte[] buffer = new byte[size];
        in.read(buffer);
        in.close();
        JSONObject jsonBackup = new JSONObject(new String(buffer, CHARSET));
        restoreData(jsonBackup);
    }

    private void restoreData(JSONObject jsonBackup) throws Exception {
        //Restore data in DB tables
        JSONArray json_phrases = jsonBackup.getJSONArray(DatabaseHelper.TABLE_PHRASES);
        JSONArray json_challenges = jsonBackup.getJSONArray(DatabaseHelper.TABLE_CHALLENGES);
        JSONArray json_badges = jsonBackup.getJSONArray(DatabaseHelper.TABLE_BADGES);
        JSONArray json_languages = jsonBackup.getJSONArray(DatabaseHelper.TABLE_LANGUAGES);
        JSONArray json_phrasebooks = jsonBackup.getJSONArray(DatabaseHelper.TABLE_BOOKS);

        //Restore Phrases
        for (int i = 0; i < json_phrases.length(); i++) {
            JSONObject obj = json_phrases.getJSONObject(i);
            String lang1Value = obj.getString(DatabaseHelper.KEY_LANG1_VALUE);
            String lang2Value = obj.getString(DatabaseHelper.KEY_LANG2_VALUE);
            int lang1Code = obj.getInt(DatabaseHelper.KEY_LANG1);
            int lang2Code = obj.getInt(DatabaseHelper.KEY_LANG2);
            String createdOn = obj.getString(DatabaseHelper.KEY_CREATED_ON);
            int phraseId = obj.getInt(DatabaseHelper.KEY_PHRASE_ID);
            int isMastered = obj.getInt(DatabaseHelper.KEY_IS_MASTERED);
            int correctCount = obj.getInt(DatabaseHelper.KEY_CORRECT_COUNT);
            DatabaseModel databaseModel = new PhraseModel(phraseId, lang1Value, lang2Value, lang1Code,
                    lang2Code, isMastered, correctCount, createdOn, DatabaseHelper.TABLE_PHRASES);
            databaseHelper.insertRow(databaseModel);
        }

        //Restore challenges
        for (int i = 0; i < json_challenges.length(); i++) {
            JSONObject obj = json_challenges.getJSONObject(i);
            int phraseId = obj.getInt(DatabaseHelper.KEY_CHALLENGE_PHRASE_ID);
            String createdOn = obj.getString(DatabaseHelper.KEY_CREATED_ON);
            int challengeId = obj.getInt(DatabaseHelper.KEY_CHALLENGE_ID);
            int challengeCorrect = obj.getInt(DatabaseHelper.KEY_CHALLENGE_CORRECT);
            DatabaseModel databaseModel = new ChallengeModel(challengeId, phraseId, createdOn,
                    DatabaseHelper.TABLE_CHALLENGES, challengeCorrect);
            databaseHelper.insertRow(databaseModel);
        }

        //Restore badges
        for (int i = 0; i < json_badges.length(); i++) {
            JSONObject obj = json_badges.getJSONObject(i);
            int badgeId = obj.getInt(DatabaseHelper.KEY_BADGES_ID);
            String badgeName = obj.getString(DatabaseHelper.KEY_BADGE_NAME);
            String badgeDesc = obj.getString(DatabaseHelper.KEY_BADGE_DESCRIPTION);
            String createdOn = obj.has(DatabaseHelper.KEY_CREATED_ON) ? obj.getString
                    (DatabaseHelper.KEY_CREATED_ON) : null;
            DatabaseModel databaseModel;
            if (createdOn != null)
                databaseModel = new BadgeModel(badgeId, badgeName, badgeDesc, createdOn, DatabaseHelper.TABLE_BADGES);
            else
                databaseModel = new BadgeModel(badgeId, badgeName, badgeDesc, DatabaseHelper
                        .TABLE_BADGES);
            databaseHelper.insertRow(databaseModel);
        }

        //Restore languages
        for (int i = 0; i < json_languages.length(); i++) {
            JSONObject obj = json_languages.getJSONObject(i);
            int langId = obj.getInt(DatabaseHelper.KEY_LANG_ID);
            String langName = obj.getString(DatabaseHelper.KEY_LANG_NAME);
            DatabaseModel databaseModel = new LanguageModel(langId, langName, DatabaseHelper
                    .TABLE_LANGUAGES);
            databaseHelper.insertRow(databaseModel);
        }

        //Restore languages
        for (int i = 0; i < json_phrasebooks.length(); i++) {
            JSONObject obj = json_phrasebooks.getJSONObject(i);
            int bookId = obj.getInt(DatabaseHelper.KEY_BOOK_ID);
            int lang1Code = obj.getInt(DatabaseHelper.KEY_BOOK_LANG1);
            int lang2Code = obj.getInt(DatabaseHelper.KEY_BOOK_LANG2);
            DatabaseModel databaseModel = new PhrasebookModel(bookId, lang1Code, lang2Code, DatabaseHelper
                    .TABLE_BOOKS);
            databaseHelper.insertRow(databaseModel);
        }
    }

    private static File lastFileModified(File directory) {
        File[] files = directory.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isFile();
            }
        });
        long lastMod = Long.MIN_VALUE;
        File choice = null;
        for (File file : files) {
            if (file.lastModified() > lastMod) {
                choice = file;
                lastMod = file.lastModified();
            }
        }
        return choice;
    }
}
