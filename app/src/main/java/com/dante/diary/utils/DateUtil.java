package com.dante.diary.utils;

import android.util.Log;

import com.blankj.utilcode.utils.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * deals with string work like copy, parse.
 */
public class DateUtil {
    public static final String LAST_DATE = "lastDate";
    private static final String TAG = "DateUtil";

    /**
     * Get date String to display
     *
     * @param date Date object
     * @return "yyyy-MM-dd" for display
     */
    public static String getDisplayTime(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
        return formatter.format(date);
    }

    public static String getDisplayDay(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return formatter.format(date);
    }


    public static Date lastDay(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int day = c.get(Calendar.DATE);
        c.set(Calendar.DATE, day - 1);
        return c.getTime();
    }

    public static Date getStartOfDate(Date date) {
        String day = getDisplayDay(date);
        String zeroTime = day.concat(" 00:00:00");
        Log.d(TAG, "getZeroOfDate: " + zeroTime);
        return TimeUtils.string2Date(zeroTime);
    }

    public static Date getEndOfDate(Date date) {
        String day = getDisplayDay(date);
        String zeroTime = day.concat(" 23:59:59");
        Log.d(TAG, "getEndOfDate: " + zeroTime);
        return TimeUtils.string2Date(zeroTime);
    }

    public static Date nextMonthDateOfToday() {
        Date today= new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.MONTH, 1);
        return calendar.getTime();
    }

}
