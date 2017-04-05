package com.bobbytables.phrasebook.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ricky on 04/04/2017.
 */

public class CSVUtils {

    private Context context;
    private static CSVUtils instance;
    private AssetManager assetManager;

    private CSVUtils(Context context) {
        this.context = context;
        this.assetManager = context.getAssets();
    }

    public static CSVUtils getInstance(Context context) {
        if (instance == null)
            instance = new CSVUtils(context);
        return instance;
    }

    public List<String[]> readCSV(String path) {
        List<String[]> badgesData = new ArrayList<>();
        if (TextUtils.isEmpty(path)) {
            Log.d("CSV Reader", "CSV file name is empty");
            return null;
        }
        BufferedReader reader = null;
        try {
            InputStream is = assetManager.open(path);
            InputStreamReader isr = new InputStreamReader(is);
            reader = new BufferedReader(isr);
            String csvLine;
            while ((csvLine = reader.readLine()) != null) {
                String[] rowData = csvLine.split(";");
                String name = rowData[0];
                String description = rowData[1];
                String resource = rowData[2];
                badgesData.add(new String[]{name, description, resource});
            }
        } catch (IOException e) {
            Log.e("CSV Reader", "IOException:", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e("CSV Reader", "IOException:", e);
                }
            }
        }
        return badgesData;
    }
}
