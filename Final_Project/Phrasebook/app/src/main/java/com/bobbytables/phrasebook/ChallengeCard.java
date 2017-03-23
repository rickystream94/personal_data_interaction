package com.bobbytables.phrasebook;

import java.util.Random;

/**
 * Created by ricky on 17/03/2017.
 */

class ChallengeCard {
    private final String randomText;
    private String foreignLanguage;
    private String motherLanguage;

    ChallengeCard(String motherLanguage,String foreignLanguage) {
        this.motherLanguage = motherLanguage;
        this.foreignLanguage = foreignLanguage;
        Random random = new Random();
        randomText = String.valueOf(random.nextInt(100));
    }

    String getRandomText() {
        return randomText;
    }

    public String getForeignLanguage() {
        return foreignLanguage;
    }

    public String getMotherLanguage() {
        return motherLanguage;
    }
}
