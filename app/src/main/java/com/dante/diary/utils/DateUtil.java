package com.dante.diary.utils;

import android.support.annotation.NonNull;
import android.util.Log;

import com.blankj.utilcode.utils.TimeUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * deals with string work like copy, parse.
 */
public class DateUtil {
    public static final String LAST_DATE = "lastDate";
    public final static long MINUTE = 60 * 1000;// 1分钟
    public final static long HOUR = 60 * MINUTE;// 1小时
    public final static long DAY = 24 * HOUR;// 1天
    public final static long MONTH = 31 * DAY;// 月
    public final static long YEAR = 12 * MONTH;// 年
    private static final String TAG = "DateUtil";

    public static Date parseStandardDate(String date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        formatter.setLenient(false);
        try {
            return formatter.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getDisplayTime(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return formatter.format(date);
    }

    public static String getDisplayDayAndTime(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return formatter.format(date);
    }

    public static String getDisplayDay(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return formatter.format(date);
    }

    public static String getDisplayDayOfMonth(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM", Locale.getDefault());
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
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.MONTH, 1);
        return calendar.getTime();
    }

    public static Date nextWeekDateOfToday() {
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DAY_OF_WEEK_IN_MONTH, 1);
        return calendar.getTime();
    }

    public static Date tomorrowDate() {
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DAY_OF_WEEK, 1);
        return calendar.getTime();
    }

    public static String getDisplayYear(Date created) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy", Locale.getDefault());
        return formatter.format(created);
    }

    /**
     * 把Date转换为 "几分钟前"、"几小时前"
     *
     * @param date 需要格式化的时间
     */
    public static String getTimeText(@NonNull Date date) {
        long diff = new Date().getTime() - date.getTime();
        long r = 0;
        if (diff > YEAR) {
            r = (diff / YEAR);
            return r + "年前";
        }
        if (diff > MONTH) {
            r = (diff / MONTH);
            return r + "个月前";
        }
        if (diff > DAY) {
            r = (diff / DAY);
            return r + "天前";
        }
        if (diff > HOUR) {
            r = (diff / HOUR);
            return r + "个小时前";
        }
        if (diff > MINUTE) {
            r = (diff / MINUTE);
            return r + "分钟前";
        }
        return "刚刚";
    }
}
