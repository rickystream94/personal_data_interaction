package it.richmondweb.cognitivetest.Models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by rasmus on 15/02/17.
 */

public class EriksenFlanker {
    private int id;
    private String created;
    private int correct;
    private int incorrect;

    public EriksenFlanker(int correct, int incorrect) {
        this.correct = correct;
        this.incorrect = incorrect;
    }

    /*
    * When retrieving/creating an object from
    * the database, the timestamp needs to be
    * converted from UTC to the local timezone.
    */
    static SimpleDateFormat timeFormatUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static SimpleDateFormat timeFormatLocal = new SimpleDateFormat("dd/MM HH:mm");

    public EriksenFlanker(int id, String created, int correct, int incorrect) {
        timeFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
        timeFormatLocal.setTimeZone(TimeZone.getDefault());

        this.id = id;
        try {
            this.created = timeFormatLocal.format(timeFormatUTC.parse(created));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.correct = correct;
        this.incorrect = incorrect;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public int getCorrect() {
        return correct;
    }

    public void setCorrect(int correct) {
        this.correct = correct;
    }

    public int getIncorrect() {
        return incorrect;
    }

    public void setIncorrect(int incorrect) {
        this.incorrect = incorrect;
    }
}
