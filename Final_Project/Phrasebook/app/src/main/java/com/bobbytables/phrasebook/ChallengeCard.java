package com.bobbytables.phrasebook;

import java.util.Random;

/**
 * Created by ricky on 17/03/2017.
 */

public class ChallengeCard {
    private Random random = new Random();
    private final String randomText;

    public ChallengeCard() {
        randomText = String.valueOf(random.nextInt(100));
    }
    public String getRandomText() {
        return randomText;
    }
}
