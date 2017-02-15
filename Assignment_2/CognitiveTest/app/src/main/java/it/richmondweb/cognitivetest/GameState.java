package it.richmondweb.cognitivetest;

/**
 * Created by ricky on 15/02/2017.
 */
public class GameState {

    public static String gameState = "STOP";

    public static void startPlaying() {
        gameState = "PLAY";
    }

    public static void stopPlaying() {
        gameState = "STOP";
    }

    public static boolean isPlayMode() {
        return gameState.equals("PLAY");
    }

}
