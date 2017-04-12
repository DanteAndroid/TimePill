package com.dante.diary.utils;

import android.support.annotation.NonNull;
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
    public final static long minute = 60 * 1000;// 1分钟
    public final static long hour = 60 * minute;// 1小时
    public final static long day = 24 * hour;// 1天
    public final static long month = 31 * day;// 月
    public final static long year = 12 * month;// 年
    private static final String TAG = "DateUtil";

    public static String getDisplayTime(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
        return formatter.format(date);
    }

    public static String getDisplayDayAndTime(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
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
        Date today= new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.MONTH, 1);
        return calendar.getTime();
    }

    public static Date nextWeekDateOfToday() {
        Date today= new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DAY_OF_WEEK_IN_MONTH, 1);
        return calendar.getTime();
    }

    public static String getDisplayYear(Date created) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy", Locale.getDefault());
        return formatter.format(created);
    }

    /**
     * 把Date转换为 "几分钟前"、"几小时前"
     * @param date 需要格式化的时间
     */
    public static String getTimeText(@NonNull Date date) {
        long diff = new Date().getTime() - date.getTime();
        long r = 0;
        if (diff > year) {
            r = (diff / year);
            return r + "年前";
        }
        if (diff > month) {
            r = (diff / month);
            return r + "个月前";
        }
        if (diff > day) {
            r = (diff / day);
            return r + "天前";
        }
        if (diff > hour) {
            r = (diff / hour);
            return r + "个小时前";
        }
        if (diff > minute) {
            r = (diff / minute);
            return r + "分钟前";
        }
        return "刚刚";
    }
}
