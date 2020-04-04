package com.example.casiowatch.mainLogic;

/*
    there are 4 modes:
    first will display current time, weekday and day of month
    second screen displays alarm time
    third displays stopwatch
    fourth will display the entries from the first mode but user can edit the values, including the month-of-the-year which is not displayed on the first screen at all but is always there

    ISSUES that I got along writing the code
    1) Find the right thread class and utilize it ✔
        UI can only be modified from the main ui thread so use a handler or activity.runOnUIThread(Runnable r);
    2) App crashes when you setText for TextView as a raw int value ✔
        you cannot setText for textView as a raw int value, use String.format instead
    3) Handler thread and updating UI issue ✔
    remember that you cannot access the UI of an activity from another file, you need to create a callback to the activity with an Interface
        we can't update values in custom class and use setText for TextView in MainActivity. Values won't update
        we can't update values in custom class and use setText for TextView in that custom class
        we can't update and calculate values in MainActivity as it would contradict MVP model that we are trying to comply
    4) I can't get access to strings.xml inside class ✔
        https://stackoverflow.com/a/51279729/11107982
    5) How do I switch weekdays, month-days and months? ✔
        switching weekdays is not a big deal since they actually exist separately from month-days. One weekday always goes after another. It sounds as simple as it is
    6) How do I switch month-days? ✔
        the day of the month is changed when the day ends. Each month has its own maximum day count. Whenever the switching weekdays occurs new month-day value is calculated. If it's less than maxDays then it just
        iterates, if the maxDays is reached then the new month arrives and maxDays value starts all over with 1.
        There are always 28 days in Feb meaning that watch user will manually fix month-day, but it would happen only every 4 years. It's a compelled thing since we want to store as few variables as possible.
    7) Android Studio doesn't see sound file in project res directory ✔
        use only lowercase letters in file name
    8) Invisible buttons are not clickable ✔
        use
            android:background="@android:color/transparent"
        instead
        and keep in mind that invisible means invisible, but any spacing it would normally take up will still be used
    9) Is it a good idea to run two threads in two classes. How many threads can I run?


    Remarks:
        - if we create object inside Handler with get__() method, values of it won't update



    Jan - 31    1
    Feb - 28    2
    Mar - 31    3
    Apr - 30    4
    May - 31    5
    Jun - 30    6
    Jul - 31    7
    Aug - 31    8
    Sep - 30    9
    Oct - 31    10
    Nov - 30    11
    Dec - 31    12

    00:00	12
    01:00	1
    02:00	2
    03:00	3
    04:00	4
    05:00	5
    06:00	6
    07:00	7
    08:00	8
    09:00	9
    10:00	10
    11:00	11
    12:00	12 PM
    13:00	1 PM
    14:00	2 PM
    15:00	3 PM
    16:00	4 PM
    17:00	5 PM
    18:00	6 PM
    19:00	7 PM
    20:00	8 PM
    21:00	9 PM
    22:00	10 PM
    23:00	11 PM

    TODO backlight

    TODO calculations remain while app is minimized

    forceReturnToTheMainMode{
        (alarmMode && noSelection) if the minutes mark is equal 0 for the second time 
        (alarmMode && hoursSelected) if the minutes mark is equal 0 for the second time
        (alarmMode && minutesSelected) if the minutes mark is equal 0 for the second time

        (stopwatchMode && isRunning || stopwatchMode && !isRunning) = til' kingdom come

        (editMode && hoursSelected) if the minutes mark is equal 0 for the second time
        (editMode && minutesSelected) if the minute mark is equal to 0 for the second time
        (editMode && secondsSelected) if the minute mark is equal to 0 for the second time
        (editMode && monthSelected) if the minute mark is equal to 0 for the second time
        (editMode && daysOfMonthSelected) if the minute mark is equal to 0 for the second time
        (editMode && dayOfWeekSelected) if the minute mark is equal to 0 for the second time

        it resets if we switch hours - minutes - noSelection or press the button
    }

    ?Is there hourlyChime while (!mainViewsAreVisible)? yes
    ?что целесообразнее, в потоке постоянно проверять нажимались ли кнопки за последние 90 секунд и осуществлять действие если нет, или после каждого нажатия кнопки запускать таймер 90 секунд и осуществлять действие как он дойдет до нуля?
*/

import android.content.Context;
import android.os.Handler;
import com.example.casiowatch.MainActivity;
import com.example.casiowatch.R;

import static com.example.casiowatch.MainActivity.startIdleCalculations;
import static com.example.casiowatch.MainActivity.forceReturnToTheMainMode;

public class TimeCalc {

    private MainActivityInteractionInterface interactionInterface;
    private Context applicationContext;

    public TimeCalc(MainActivityInteractionInterface interactionInterface, Context applicationContext){
        this.interactionInterface = interactionInterface;
        this.applicationContext = applicationContext;
    }

    public static int idleSeconds = 0;

    public static int minuteMarkPassedCount = 0;

    private int seconds = 50;
    private int minutes = 59;
    private int hours = 12;
    private int month = 2;
    private int dayOfMonth = 31;

    private int weekDayCount;
    private String weekDay = weekDayDefaultVal();

    public int getSeconds(){
        return seconds;
    }
    public int getMinutes(){
        return minutes;
    }
    public int getHours(){
        return hours;
    }
    public int getAdaptedHours() {
        if (MainActivity.twentyFourHoursFormat) {
            return hours;
        } else if (!MainActivity.twentyFourHoursFormat) {
            if (hours == 0) {
                return 12;
            }
            if (hours > 0 && hours <= 12) {
                return hours;
            }
            if (hours > 12) {
                return hours - 12;
            }
        }
        return hours;
    }

    public int getMonth(){
        return month;
    }
    public int getDayOfMonth(){
        return dayOfMonth;
    }
    public String getWeekDay(){
        return weekDay;
    }

    //make seconds, minutes, hours, days and months flow
    public void runThread(){
        final Handler handler = new Handler();
        handler.post(new Runnable(){
            @Override
            public void run(){
                seconds++;
                if(seconds==60){
                    seconds=0;
                    minutes++;
                    if(startIdleCalculations) {
                        minuteMarkPassedCount++;
                        if(minuteMarkPassedCount == 2){
                            forceReturnToTheMainMode = true;
                        }
                    }
                    if(minutes==60){
                        minutes=0;
                        hours++;
                        if(hours==24){
                            hours=0;
                            switchWeekDay();
                            switchDayOfMonth();
                        }
                    }
                }
                interactionInterface.updateUI();
                handler.postDelayed(this, 1000);
            }
        });
    }

    public void switchWeekDay(){
        weekDayCount++;
        if(weekDayCount>6){
            weekDayCount = 0;
        }
        String [] array = LocalStringsAssistance.getRes().getStringArray(R.array.weekDays);
        weekDay = array[weekDayCount];
    }

    private String weekDayDefaultVal(){
        String [] array = LocalStringsAssistance.getRes().getStringArray(R.array.weekDays);
        return array[0];
    }

    private int maxDaysInCurrentMonth(int month){
        int maxDays = 0;
        switch(month){
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                maxDays = 31;
                break;
            case 2:
                maxDays = 28;
                break;
            case 4:
            case 6:
            case 9:
            case 11:
                maxDays = 30;
                break;
        }
        return maxDays;
    }

    private void switchDayOfMonth(){
        dayOfMonth++;
        if (dayOfMonth > maxDaysInCurrentMonth(month)) {
            month++;
            if(month>12){
                month = 1;
            }
            dayOfMonth = 1;
        }
    }

    public void iterateHours(){
        hours++;
        if(hours == 24){
            hours = 0;
        }
    }

    public void iterateMinutes(){
        minutes++;
        if(minutes == 60){
            minutes = 0;
        }
    }

    public void resetSeconds(){
        seconds = 0;
    }

    public void iterateMonth(){
        month++;
        if(month==13){
            month = 1;
        }
    }

    //Casio watch suggests that there are always 28 days in February, you can only set it to 29 manually
    //it makes sense considering that user gets the right day of month every 3 years out of 4 without editing anything
    //that's why there is the separate method
    public void iterateDayOfMonth(){
        dayOfMonth++;
        if(month == 2) {
            if(dayOfMonth > 29){
                dayOfMonth = 1;
            }
        } else if (dayOfMonth > maxDaysInCurrentMonth(month)) {
            dayOfMonth = 1;
        }
    }

}
