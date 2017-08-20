package com.mdg.droiders.samagra.shush;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by rohan on 12/8/17.
 * <br>
 * This class schedules an alarm which changes ringer mode of the phone when it is triggered.
 */
public class AlarmScheduler {

    private static final int ONE_WEEK_IN_MILLIS = 7 * 24 * 60 * 60 * 1000;
    private static final String LOG_TAG = "Samagra/AS/";

    private AlarmManager alarmManager;
    private Context context;

    public AlarmScheduler(Context context) {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.context = context;
    }

    /**
     * Sets a weekly alarm to silence your phone.
     * <br><br>
     * <b>Uses</b> : {@link #setAlarm(long, long, Integer)} to set each alarm one by one.
     *
     * @param startTime The time of day at which phone is to be shushed
     * @param endTime   The time of day at which phone is to be un-shushed
     * @param days      A boolean array that is true if the alarm is to be
     *                  scheduled for the corresponding day. i.e. days[0] is set to be true
     *                  then a weekly alarm will be set such that it will shush the phone every Monday.
     * @param rowID     Unique primary key of the shush alarm row.
     */
    public void setWeeklyAlarm(Calendar startTime, Calendar endTime, boolean[] days, Integer rowID) {
        if (rowID == null || days.length != 7) {
            return;
        }
        Calendar rightNow = Calendar.getInstance();

        startTime.set(Calendar.YEAR, rightNow.get(Calendar.YEAR));
        startTime.set(Calendar.MONTH, rightNow.get(Calendar.MONTH));
        startTime.set(Calendar.DAY_OF_MONTH, rightNow.get(Calendar.DAY_OF_MONTH));
        endTime.set(Calendar.YEAR, rightNow.get(Calendar.YEAR));
        endTime.set(Calendar.MONTH, rightNow.get(Calendar.MONTH));
        endTime.set(Calendar.DAY_OF_MONTH, rightNow.get(Calendar.DAY_OF_MONTH));
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a, EE ,dd MMM yyyy", Locale.ENGLISH);
        Log.d(LOG_TAG + "now", sdf.format(rightNow.getTime()));

        if (startTime.get(Calendar.HOUR_OF_DAY)
                > endTime.get(Calendar.HOUR_OF_DAY)) {
            endTime.add(Calendar.DAY_OF_MONTH, 1);
        }
        int i = getDay(startTime);
        for (int j = i; j < 7; j++) {
            if (j != i) {
                startTime.add(Calendar.DAY_OF_MONTH, 1);
                endTime.add(Calendar.DAY_OF_MONTH, 1);
            }
            Log.d(LOG_TAG + "I-Val", String.valueOf(j));
            if (days[j]) {
                int dayID = getDayID(j, rowID);
                setAlarm(startTime.getTimeInMillis(),
                        endTime.getTimeInMillis(),
                        dayID);
            }
        }
        for (int j = 0; j < i; j++) {
            startTime.add(Calendar.DAY_OF_MONTH, 1);
            endTime.add(Calendar.DAY_OF_MONTH, 1);
            Log.d(LOG_TAG + "I-Val", String.valueOf(j));
            if (days[j]) {
                int dayID = getDayID(j, rowID);
                setAlarm(startTime.getTimeInMillis(),
                        endTime.getTimeInMillis(),
                        dayID);
            }
        }
    }

    /**
     * Sets a weekly alarm to silence your phone.
     * <br><br>
     * <b>Uses</b> : {@link #setWeeklyAlarm(Calendar, Calendar, boolean[], Integer)} to set weekly alarms.
     *
     * @param startHour    The hour at which phone is to be shushed in 24 hour format
     * @param startMinutes The minute at which phone is to be shushed
     * @param endHour      The hour at which phone is to be un-shushed in 24 hour format
     * @param endMinutes   The minute at which phone is to be un-shushed
     * @param days         A boolean array that is true if the alarm is to be
     *                     scheduled for the corresponding day. i.e. days[0] is set to be true
     *                     then a weekly alarm will be set such that it will shush the phone every Monday.
     * @param rowID        Unique primary key of the shush alarm row.
     */
    public void setWeeklyAlarm(int startHour, int startMinutes, int endHour,
                               int endMinutes, boolean[] days, Integer rowID) {
        if (rowID == null || days.length != 7) {
            return;
        }
        Calendar startTime = Calendar.getInstance();
        Calendar endTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, startHour);
        startTime.set(Calendar.MINUTE, startMinutes);
        endTime.set(Calendar.HOUR_OF_DAY, endHour);
        endTime.set(Calendar.MINUTE, endMinutes);
        setWeeklyAlarm(startTime, endTime, days, rowID);
    }

    /**
     * Set an alarm to silence the phone.
     *
     * @param startTimeInMillis The time at which phone is to be shushed
     * @param endTimeInMillis   The time at which phone is to be un-shushed
     */
    public void setAlarm(long startTimeInMillis, long endTimeInMillis, Integer alarmId) {
        if (startTimeInMillis >= endTimeInMillis) {
            return;
        }
        if (alarmId == null) {
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a, EE ,dd MMM yyyy", Locale.ENGLISH);
        Log.d(LOG_TAG + "start", sdf.format(new Date(startTimeInMillis)));
        Log.d(LOG_TAG + "end", sdf.format(new Date(endTimeInMillis)));
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                startTimeInMillis,
                ONE_WEEK_IN_MILLIS,
                getDefaultPendingIntent(true, alarmId)
        );
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                endTimeInMillis,
                ONE_WEEK_IN_MILLIS,
                getDefaultPendingIntent(false, alarmId)
        );
    }

    /**
     * Sets multiple alarms at once.
     * <br><br>
     * <b>Uses</b> : {@link #setAlarm(long, long, Integer)} to set each alarm one by one.
     *
     * @param startTimesInMillis The list of start times at which phone is to be shushed
     * @param endTimesInMillis   The list of end times at which phone is to be un-shushed
     */
    public void setAlarms(List<Long> startTimesInMillis, List<Long> endTimesInMillis, List<Integer> alarmIds) {
        if (startTimesInMillis.size() != endTimesInMillis.size()) {
            return;
        }
        for (int i = 0; i < startTimesInMillis.size(); i++) {
            setAlarm(startTimesInMillis.get(i), endTimesInMillis.get(i), alarmIds.get(i));
        }
    }

    private PendingIntent getDefaultPendingIntent(boolean shouldShush, int alarmId) {
        PendingIntent pendingIntent;

        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        intent.putExtra(context.getString(R.string.alarm_intent_extra_key), shouldShush);
        pendingIntent = PendingIntent.getBroadcast(
                context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }

    /**
     * Get day ID for alarms that are scheduled by alarm manager.
     * The ID returned identifies a particular day of a particular shush alarm.
     *
     * @param day   int type for week days starting from monday and zero indexed.
     *              For e.g. - Monday is 0, Tuesday is 1, ... and so on
     * @param rowID Unique primary key of the shush alarm row.
     * @return The ID used by {@link AlarmManager} to schedule weekly alarms
     */
    private int getDayID(int day, int rowID) {
        return rowID + (day + 1) * 10000;
    }

    /**
     * Get day field of calendar indexed at 0 such that start day of week is monday.
     *
     * @param calendar The calendar whose day is to be returned
     * @return int type for week days starting from monday and zero indexed.
     * For e.g. - Monday is 0, Tuesday is 1, ... and so on
     */
    private int getDay(Calendar calendar) {
        int i = calendar.get(Calendar.DAY_OF_WEEK);
        i -= 2;
        if (i == -1) {
            i += 7;
        }
        return i;
    }
}
