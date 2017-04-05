package com.bobbytables.phrasebook;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bobbytables.phrasebook.database.DatabaseHelper;
import com.bobbytables.phrasebook.utils.DateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * This class handles all the logic for badges, how to achieve them etc.
 * It assumes that there is consistency between the badge ID and the order of insertion in the
 * SQLite DB, so we always must be sure that in the switch statement, the cases of the ID are
 * corresponding to the ones in the DB
 * Created by ricky on 29/03/2017.
 */

public class BadgeManager {

    private static BadgeManager instance;
    private DatabaseHelper databaseHelper;
    public static final String TABLE_CHALLENGES = DatabaseHelper.TABLE_CHALLENGES;
    public static final String TABLE_PHRASES = DatabaseHelper.TABLE_PHRASES;
    private static final String CREATED_ON = DatabaseHelper.KEY_CREATED_ON;
    private static final String CHALLENGE_CORRECT = DatabaseHelper.KEY_CHALLENGE_CORRECT;

    private BadgeManager(Context context) {
        databaseHelper = DatabaseHelper.getInstance(context);
    }

    public static BadgeManager getInstance(Context context) {
        if (instance == null)
            instance = new BadgeManager(context);
        return instance;
    }

    /**
     * TODO: Right now badges IDs are hardcoded when added to list; in the future, find a better way
     *
     * @param table either "phrases" or "challenges"
     * @return List of newly achieved badges IDs
     */
    public List<String> checkNewBadges(String table) {
        List<Integer> badgesToCheck;
        switch (table) {
            case TABLE_PHRASES:
                badgesToCheck = checkPhrasesBadgeAchieved();
                break;
            case TABLE_CHALLENGES:
                badgesToCheck = checkChallengesBadgeAchieved();
                break;
            default:
                badgesToCheck = new ArrayList<>();
        }
        List<Integer> achievedBadges = getAchievedBadges();
        List<Integer> newAchievedBadges = new ArrayList<>();

        //Remove already unlocked badges
        for (Integer i : badgesToCheck) {
            if (!achievedBadges.contains(i))
                newAchievedBadges.add(i);
        }

        //Update created on key for the newly achieved badges
        for (Integer i : newAchievedBadges)
            databaseHelper.updateAchievedBadgeDate(i);

        List<String> newAchievedBadgesNames = new ArrayList<>();
        for (Integer id : newAchievedBadges)
            newAchievedBadgesNames.add(getBadgeName(id));

        return newAchievedBadgesNames;
    }

    private String getBadgeName(Integer id) {
        String query = "SELECT " + DatabaseHelper.KEY_BADGE_NAME + " FROM " + DatabaseHelper
                .TABLE_BADGES + " WHERE " + DatabaseHelper.KEY_BADGES_ID + "=" + id + "";
        Cursor cursor = databaseHelper.performRawQuery(query);
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_BADGE_NAME));
    }

    private List<Integer> checkPhrasesBadgeAchieved() {
        List<Integer> achievedBadgesIds = new ArrayList<>();
        Cursor cursor;
        String queryCount = "SELECT COUNT(*) FROM " + TABLE_PHRASES;
        String timestamp = DateUtil.getCurrentTimestamp();
        String today = timestamp.split("\\s+")[0];
        String queryOneDay = "SELECT COUNT(*) FROM " + TABLE_PHRASES + " WHERE DATE" +
                "(" + DatabaseHelper.KEY_CREATED_ON + ")='" + today + "'";
        String queryLongPhrase = "SELECT COUNT(*) FROM " + TABLE_PHRASES + " WHERE " +
                "LENGTH(" + DatabaseHelper.KEY_FOREIGN_LANG_STRING + ")>=25";
        String queryNight = queryOneDay +
                " AND strftime('%H'," + CREATED_ON + ") BETWEEN '00' AND '06'";
        String query15Mins = "SELECT COUNT(*) FROM " + TABLE_PHRASES + " WHERE " +
                "strftime('%M','" + timestamp + "' - " + CREATED_ON + ")<='15'";


        //Check Beginner (10 added), Novice (100 added), Expert (250 added)
        cursor = databaseHelper.performRawQuery(queryCount);
        if (cursor.moveToFirst()) {
            if (cursor.getInt(0) == 10)
                achievedBadgesIds.add(1);
            else if (cursor.getInt(0) == 100)
                achievedBadgesIds.add(3);
            else if (cursor.getInt(0) == 250)
                achievedBadgesIds.add(5);
        }

        //Check added 10 in 1 day
        cursor = databaseHelper.performRawQuery(queryOneDay);
        if (cursor.moveToFirst()) {
            if (cursor.getInt(0) == 10)
                achievedBadgesIds.add(8);
        }

        //Check "I like it difficult", "Get on my level"
        cursor = databaseHelper.performRawQuery(queryLongPhrase);
        if (cursor.moveToFirst()) {
            if (cursor.getInt(0) == 1)
                achievedBadgesIds.add(11);
            else if (cursor.getInt(0) == 5)
                achievedBadgesIds.add(12);
        }

        //Check "Inspiring dreams"
        cursor = databaseHelper.performRawQuery(queryNight);
        if (cursor.moveToFirst()) {
            if (cursor.getInt(0) == 5)
                achievedBadgesIds.add(16);
        }

        //Check "Sudden inspiration"
        cursor = databaseHelper.performRawQuery(query15Mins);
        if (cursor.moveToFirst()) {
            if (cursor.getInt(0) == 10)
                achievedBadgesIds.add(17);
        }

        cursor.close();
        return achievedBadgesIds;
    }

    private List<Integer> checkChallengesBadgeAchieved() {
        List<Integer> achievedBadgesIds = new ArrayList<>();
        Cursor cursor;
        String timestamp = DateUtil.getCurrentTimestamp();
        String today = timestamp.split("\\s+")[0];
        String queryOneDay = "SELECT COUNT(*) FROM " + TABLE_CHALLENGES + " WHERE DATE" +
                "(" + CREATED_ON + ")='" + today + "'";
        String queryMorning = queryOneDay + " AND" +
                " strftime('%H'," + CREATED_ON + ") BETWEEN '04' AND '10' AND " +
                "" + CHALLENGE_CORRECT + "=1";
        String queryNight = queryOneDay + " AND" +
                " strftime('%H'," + CREATED_ON + ")>= '22' AND strftime('%H',"
                + DatabaseHelper.KEY_CREATED_ON + ")<='02' AND " + CHALLENGE_CORRECT + "=1";
        String queryNoSleep = queryOneDay + " AND" +
                " strftime('%H'," + CREATED_ON + ") BETWEEN '00' AND '06' AND " + CHALLENGE_CORRECT + "=1";
        String query15Mins = "SELECT COUNT(*) FROM " + TABLE_CHALLENGES + " WHERE " +
                "strftime('%M','" + timestamp + "' - " + CREATED_ON + ")<='15'";

        //Check Doing Good (30 correct), Novice (150 correct), Beacon of light (300 correct)
        String queryCount = "SELECT COUNT(*) FROM " + TABLE_CHALLENGES + " WHERE " +
                CHALLENGE_CORRECT + "=1";
        cursor = databaseHelper.performRawQuery(queryCount);
        if (cursor.moveToFirst()) {
            if (cursor.getInt(0) == 30)
                achievedBadgesIds.add(2);
            else if (cursor.getInt(0) == 150)
                achievedBadgesIds.add(4);
            else if (cursor.getInt(0) == 300)
                achievedBadgesIds.add(6);
        }

        //Check 10 guessed in one day
        cursor = databaseHelper.performRawQuery(queryOneDay + " AND " + DatabaseHelper
                .KEY_CHALLENGE_CORRECT + "=1");
        if (cursor.moveToFirst()) {
            if (cursor.getInt(0) == 10)
                achievedBadgesIds.add(7);
        }

        //Check "High fidelity" and "Not too shabby"
        String queryInRow = "SELECT COUNT(*) FROM (SELECT * FROM " + TABLE_CHALLENGES + " ORDER BY " +
                "" + CREATED_ON + " DESC LIMIT 10) AS A WHERE A." + CHALLENGE_CORRECT + "=";
        String queryCorrectInRow = queryInRow + "1";
        String queryIncorrectInRow = queryInRow + "0";
        cursor = databaseHelper.performRawQuery(queryCorrectInRow);
        if (cursor.moveToFirst()) {
            if (cursor.getInt(0) == 20)
                achievedBadgesIds.add(9);
        }
        cursor = databaseHelper.performRawQuery(queryIncorrectInRow);
        if (cursor.moveToFirst()) {
            if (cursor.getInt(0) == 10)
                achievedBadgesIds.add(10);
        }

        //Check "Rise and Shine", "Night owl" and "No sleep"
        cursor = databaseHelper.performRawQuery(queryMorning);
        if (cursor.moveToFirst()) {
            if (cursor.getInt(0) == 5)
                achievedBadgesIds.add(13);
        }

        cursor = databaseHelper.performRawQuery(queryNight);
        if (cursor.moveToFirst()) {
            if (cursor.getInt(0) == 5)
                achievedBadgesIds.add(14);
        }

        cursor = databaseHelper.performRawQuery(queryNoSleep);
        if (cursor.moveToFirst()) {
            if (cursor.getInt(0) == 5)
                achievedBadgesIds.add(15);
        }

        //Check "Extreme stamina"
        cursor = databaseHelper.performRawQuery(query15Mins);
        if (cursor.moveToFirst()) {
            if (cursor.getInt(0) == 15)
                achievedBadgesIds.add(18);
        }

        cursor.close();
        return achievedBadgesIds;
    }

    private List<Integer> getAchievedBadges() {
        List<Integer> achievedBadges = new ArrayList<>();
        String query = "SELECT " + DatabaseHelper.KEY_BADGES_ID + " FROM " + DatabaseHelper
                .TABLE_BADGES + " WHERE " + CREATED_ON + " IS NOT NULL";
        Cursor cursor = databaseHelper.performRawQuery(query);
        if (cursor.moveToFirst()) {
            do {
                achievedBadges.add(cursor.getInt(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return achievedBadges;
    }

    public void showDialogAchievedBadges(Context context, List<String> achievedBadges) {
        final Dialog d = new Dialog(context, android.R.style.Theme_DeviceDefault_Dialog);
        d.setTitle("New Badges Unlocked!");
        d.setContentView(R.layout.badges_dialog);
        TextView badgeText = (TextView) d.findViewById(R.id.newBadgeText);
        Button closeButton = (Button) d.findViewById(R.id.closeDialog);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                d.dismiss();
            }
        });
        String text = "";
        for (String badgeName : achievedBadges)
            text += badgeName + "\n";
        badgeText.setText(text);
        d.show();
    }
}
