package it.richmondweb.responsetimetest;

import android.content.ContentValues;

import java.text.SimpleDateFormat;
import java.util.Date;

import static it.richmondweb.responsetimetest.DatabaseHelper.TEST_RESPONSE_TIME_COLUMN_ACCEPTABLE;
import static it.richmondweb.responsetimetest.DatabaseHelper.TEST_RESPONSE_TIME_COLUMN_CREATED;
import static it.richmondweb.responsetimetest.DatabaseHelper.TEST_RESPONSE_TIME_COLUMN_DELAY;
import static it.richmondweb.responsetimetest.DatabaseHelper.TEST_RESPONSE_TIME_COLUMN_NAME;

public class ResponseTimeTestModel {
    int id;
    String created;
    int delay;
    boolean acceptable;
    String name;


    public ResponseTimeTestModel(int id, String created, int delay, boolean acceptable, String name) {
        this.id = id;
        this.created = created;
        this.delay = delay;
        this.acceptable = acceptable;
        this.name = name;
    }
    public ResponseTimeTestModel(int delay, boolean acceptable, String name) {
        this.created = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
        this.delay = delay;
        this.acceptable = acceptable;
        this.name = name;
    }

    public ContentValues getContentValues() {
        ContentValues v = new ContentValues();
        v.put(TEST_RESPONSE_TIME_COLUMN_CREATED, this.created);
        v.put(TEST_RESPONSE_TIME_COLUMN_DELAY, this.delay);
        if(this.acceptable)
            v.put(TEST_RESPONSE_TIME_COLUMN_ACCEPTABLE, 1);
        else
            v.put(TEST_RESPONSE_TIME_COLUMN_ACCEPTABLE, 0);
        v.put(TEST_RESPONSE_TIME_COLUMN_NAME, this.name);

        return v;
    }

}
