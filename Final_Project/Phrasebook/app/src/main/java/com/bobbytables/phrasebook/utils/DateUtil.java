package com.bobbytables.phrasebook.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ricky on 21/03/2017.
 */

public abstract class DateUtil {

    public static String getCurrentTimestamp() {
        return new SimpleDateFormat("y/MM/dd HH:mm:ss").format(new Date());
    }
}
