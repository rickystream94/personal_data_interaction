package com.bobbytables.phrasebook;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;

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
    private int[] badgesIds;
    private int badgesCount;

    private BadgeManager(Context context) {
        databaseHelper = DatabaseHelper.getInstance(context);
        badgesCount = databaseHelper.getBadgesCount();
        badgesIds = new int[badgesCount];
        for (int i = 0; i < badgesCount; i++)
            badgesIds[i] = i;
    }

    public static BadgeManager getInstance(Context context) {
        if (instance == null)
            instance = new BadgeManager(context);
        return instance;
    }

    public List<Integer> getBadgesAchieved() {
        List<Integer> achievedBadges = getAchievedBadges();
        List<Integer> phrasesBadges = checkPhrasesBadgeAchieved();
        List<Integer> challengesBadges = checkChallengesBadgeAchieved();
        List<Integer> newAchievedBadges = new ArrayList<>();

        //Remove already unlocked badges
        for (Integer i : phrasesBadges) {
            if (!achievedBadges.contains(i))
                newAchievedBadges.add(i);
        }
        for (Integer i : challengesBadges) {
            if (!achievedBadges.contains(i))
                newAchievedBadges.add(i);
        }
        return newAchievedBadges;
    }

    /**
     * Main method that asks for the specific queries to lookup for unlocked badges related to
     * phrases
     * TODO: ADAPT BADGE IDS!
     *
     * @return an array containing the IDs of unlocked badges
     */
    private List<Integer> checkPhrasesBadgeAchieved() {
        List<Integer> achievedBadgesIds = new ArrayList<>();
        Cursor cursor;
        String queryCount = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_PHRASES;
        String timestamp = DateUtil.getCurrentTimestamp();
        String today = timestamp.split("\\s+")[0];
        String queryOneDay = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_PHRASES + " WHERE DATE" +
                "(" + DatabaseHelper.KEY_CREATED_ON + ")='" + today + "'";
        String queryLongPhrase = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_PHRASES + " WHERE " +
                "LENGTH(" + DatabaseHelper.KEY_FOREIGN_LANG_STRING + ")>=25";
        String queryNight = queryOneDay +
                " AND strftime('%H'," + DatabaseHelper.KEY_CREATED_ON + ") BETWEEN '00' AND '06'";
        String query15Mins = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_PHRASES + " WHERE " +
                "strftime('%M','" + timestamp + "' - " + DatabaseHelper.KEY_CREATED_ON + ")<='15'";


        //Check Beginner (10 added), Novice (100 added), Expert (250 added)
        cursor = databaseHelper.performRawQuery(queryCount);
        cursor.moveToFirst();
        if (cursor.getInt(0) >= 10)
            achievedBadgesIds.add(0);
        else if (cursor.getInt(0) > 100)
            achievedBadgesIds.add(2);
        else if (cursor.getInt(0) > 250)
            achievedBadgesIds.add(4);

        //Check added 10 in 1 day
        cursor = databaseHelper.performRawQuery(queryOneDay);
        cursor.moveToFirst();
        if (cursor.getInt(0) >= 10)
            achievedBadgesIds.add(13);

        //Check "I like it difficult", "Get on my level"
        cursor = databaseHelper.performRawQuery(queryLongPhrase);
        cursor.moveToFirst();
        if (cursor.getInt(0) == 1)
            achievedBadgesIds.add(16);
        else if (cursor.getInt(0) >= 5)
            achievedBadgesIds.add(17);

        //Check "No sleep"
        cursor = databaseHelper.performRawQuery(queryNight);
        cursor.moveToFirst();
        if (cursor.getInt(0) >= 5)
            achievedBadgesIds.add(22);

        //Check "Sudden inspiration"
        cursor = databaseHelper.performRawQuery(query15Mins);
        cursor.moveToFirst();
        if (cursor.getInt(0) >= 10)
            achievedBadgesIds.add(23);

        cursor.close();
        return achievedBadgesIds;
    }

    /**
     * TODO: ADAPT BADGE IDS!
     *
     * @return
     */
    private List<Integer> checkChallengesBadgeAchieved() {
        List<Integer> achievedBadgesIds = new ArrayList<>();
        Cursor cursor;
        String timestamp = DateUtil.getCurrentTimestamp();
        String today = timestamp.split("\\s+")[0];
        String queryOneDay = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CHALLENGES + " WHERE DATE" +
                "(" + DatabaseHelper.KEY_CREATED_ON + ")='" + today + "'";

        //Check Doing Good (10 correct), Novice (100 correct)
        String queryCount = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CHALLENGES + " WHERE " +
                DatabaseHelper
                        .KEY_CHALLENGE_CORRECT + "=1";
        cursor = databaseHelper.performRawQuery(queryCount);
        cursor.moveToFirst();
        if (cursor.getInt(0) >= 10)
            achievedBadgesIds.add(1);
        else if (cursor.getInt(0) >= 100)
            achievedBadgesIds.add(3);
        else if (cursor.getInt(0) >= 250)
            achievedBadgesIds.add(5);

        //Check 10 guessed in one day
        cursor = databaseHelper.performRawQuery(queryOneDay + " AND " + DatabaseHelper
                .KEY_CHALLENGE_CORRECT + "=1");
        cursor.moveToFirst();
        if (cursor.getInt(0) >= 10)
            achievedBadgesIds.add(12);

        //Check "High fidelity" and "Not too shabby"
        String queryInRow = "SELECT COUNT(*) FROM (SELECT * FROM " + DatabaseHelper
                .TABLE_CHALLENGES + " ORDER BY " +
                "" + DatabaseHelper.KEY_CREATED_ON + " DESC LIMIT 10) AS A WHERE A." + DatabaseHelper
                .KEY_CHALLENGE_CORRECT + "=";
        String queryCorrectInRow = queryInRow + "1";
        String queryIncorrectInRow = queryInRow + "0";
        cursor = databaseHelper.performRawQuery(queryCorrectInRow);
        cursor.moveToFirst();
        if (cursor.getInt(0) == 10)
            achievedBadgesIds.add(14);
        cursor = databaseHelper.performRawQuery(queryIncorrectInRow);
        cursor.moveToFirst();
        if (cursor.getInt(0) == 10)
            achievedBadgesIds.add(15);

        //TODO: Continue from ID=18


        cursor.close();
        return achievedBadgesIds;
    }

    public List<Integer> getAchievedBadges() {
        List<Integer> achievedBadges = new ArrayList<>();
        String query = "SELECT " + DatabaseHelper.KEY_BADGES_ID + " FROM " + DatabaseHelper
                .TABLE_BADGES + " WHERE " + DatabaseHelper.KEY_BADGES_ID + " IS NOT NULL";
        Cursor cursor = databaseHelper.performRawQuery(query);
        if (cursor.moveToFirst()) {
            do {
                achievedBadges.add(cursor.getInt(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return achievedBadges;
    }
}
