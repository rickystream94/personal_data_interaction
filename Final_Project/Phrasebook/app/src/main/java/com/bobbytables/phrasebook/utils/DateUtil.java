package com.bobbytables.phrasebook.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by ricky on 21/03/2017.
 */

public abstract class DateUtil {

    private static final String SQLITE_DATETIME_FORMAT = "y-MM-dd HH:mm:ss";
    private static final String SQLITE_DATE_FORMAT = "y-MM-dd";

    public static String getCurrentTimestamp() {
        return new SimpleDateFormat(SQLITE_DATETIME_FORMAT).format(new Date());
    }

    //1 minute = 60 seconds
    //1 hour = 60 x 60 = 3600
    //1 day = 3600 x 24 = 86400
    public static int daysBetweenDates(String startDateString, String endDateString) throws
            ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SQLITE_DATETIME_FORMAT);
        Date startDate = simpleDateFormat.parse(startDateString);
        Date endDate = simpleDateFormat.parse(endDateString);

        //milliseconds
        long different = endDate.getTime() - startDate.getTime();
        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;
        return (int) (different / daysInMilli);
    }

    public static List<String> getDaysBetweenDates(String startDateString, String endDateString) {
        List<String> dates = new ArrayList<>();
        Calendar calendar = new GregorianCalendar();
        DateFormat dateFormat = new SimpleDateFormat(SQLITE_DATE_FORMAT);
        try {
            Date endDate = dateFormat.parse(endDateString);
            Date startDate = dateFormat.parse(startDateString);
            calendar.setTime(startDate);
            while (calendar.getTime().before(endDate)) {
                Date result = calendar.getTime();
                dates.add(dateFormat.format(result));
                calendar.add(Calendar.DATE, 1);
            }
            dates.add(dateFormat.format(endDate)); //explicitly include endDate, which is skipped
            // in the cycle
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dates;
    }
}
