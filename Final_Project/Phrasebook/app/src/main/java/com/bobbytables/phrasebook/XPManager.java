package com.bobbytables.phrasebook;

import android.content.Context;
import android.util.Log;
import android.util.SparseIntArray;

import com.bobbytables.phrasebook.utils.SettingsManager;

/**
 * Created by ricky on 26/03/2017.
 */

/**
 * This class is responsible for handling the XP points of the user, the levelsXP and the badges.
 */
public class XPManager {

    private SettingsManager settingsManager;
    private SparseIntArray levelsXP;
    public static final int XP_CHALLENGE_WON = 10;
    public static final int XP_BONUS_ARCHIVED = 20;
    private static final int MIN_LEVEL = 0;
    private static final int MAX_LEVEL = 10;
    private static final int LEVEL_1_XP = 50;
    private static final int LEVEL_2_XP = LEVEL_1_XP * 2;
    private static final int LEVEL_3_XP = LEVEL_2_XP * 2;
    private static final int LEVEL_4_XP = LEVEL_3_XP * 2;
    private static final int LEVEL_5_XP = LEVEL_4_XP * 2;
    private static final int LEVEL_6_XP = LEVEL_5_XP * 2;
    private static final int LEVEL_7_XP = LEVEL_6_XP * 2;
    private static final int LEVEL_8_XP = LEVEL_7_XP * 2;
    private static final int LEVEL_9_XP = LEVEL_8_XP * 2;
    private static final int LEVEL_10_XP = LEVEL_9_XP * 2;
    private static XPManager instance;

    private XPManager(Context context) {
        settingsManager = SettingsManager.getInstance(context);
        levelsXP = new SparseIntArray();
        levelsXP.put(1, LEVEL_1_XP);
        levelsXP.put(2, LEVEL_2_XP);
        levelsXP.put(3, LEVEL_3_XP);
        levelsXP.put(4, LEVEL_4_XP);
        levelsXP.put(5, LEVEL_5_XP);
        levelsXP.put(6, LEVEL_6_XP);
        levelsXP.put(7, LEVEL_7_XP);
        levelsXP.put(8, LEVEL_8_XP);
        levelsXP.put(9, LEVEL_9_XP);
        levelsXP.put(10, LEVEL_10_XP);
    }

    public static XPManager getInstance(Context context) {
        if (instance == null)
            instance = new XPManager(context);
        return instance;
    }

    /**
     * Add experience points by adding the specified value to the current XP
     *
     * @param value
     */
    public void addExperience(int value) {
        settingsManager.addValue(SettingsManager.KEY_TOTAL_XP, value);
    }

    public boolean checkLevelUp() {
        int currentXp = getCurrentXp();
        int currentLevel = getCurrentLevel();
        if (currentLevel == MAX_LEVEL)
            return false;
        int XpToLevelUp = levelsXP.get(currentLevel + 1) - currentXp;
        Log.d("DEBUG XP POINTS","Missing "+XpToLevelUp+" XP points to level up");
        return XpToLevelUp <= 0;
    }

    public int getCurrentXp() {
        return settingsManager.getPrefIntValue(SettingsManager.KEY_TOTAL_XP);
    }

    public int getCurrentLevel() {
        return settingsManager.getPrefIntValue(SettingsManager.KEY_LEVEL);
    }

    /**
     * Levels up and returns the new level
     *
     * @return
     */
    public int levelUp() {
        int currentLevel = getCurrentLevel();
        settingsManager.updatePrefValue(SettingsManager.KEY_LEVEL, currentLevel + 1);
        return currentLevel + 1;
    }
}
