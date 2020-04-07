package com.example.casiowatch;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.casiowatch.mainLogic.Alarm;
import com.example.casiowatch.mainLogic.Stopwatch;
import com.example.casiowatch.mainLogic.TimeCalc;
import com.example.casiowatch.mainLogic.MainActivityInteractionInterface;
import java.util.Locale;

import static com.example.casiowatch.mainLogic.TimeCalc.minuteMarkPassedCount;

public class MainActivity extends AppCompatActivity implements MainActivityInteractionInterface{

    private ImageView imageView;
    private Button buttonBackLight;

    private TextView textViewColon;

    //main mode
    private TextView textViewMainSeconds;
    private TextView textViewMainHours;
    private TextView textViewMainMinutes;
    private TextView textViewWeekDay;
    private TextView textViewDayOfMonth;
    private TextView textViewTimeFormat;
    private TextView textViewPmFormat;

    //month is not presented in the main mode but it is always there
    private TextView textViewMonth;

    //alarm mode
    private TextView textViewAlarmHours;
    private TextView textViewAlarmMinutes;
    private TextView textViewAlarmText;
    private ImageView imageViewHourlyChime;
    private ImageView imageViewAlarm;

    //stopwatch mode
    private TextView textViewStopwatchTenMilliseconds;
    private TextView textViewStopwatchMinutes;
    private TextView textViewStopwatchSeconds;
    private TextView textViewStopwatchText;
    private TextView textViewStopwatchSplit;

    //booleans for determining the mode
    private boolean mainScreenViewsAreVisible = true;
    private boolean alarmViewsAreVisible = false;
    private boolean stopWatchViewsAreVisible = false;
    private boolean editModIsOn = false;
    private int modeCount = 1;
    private boolean returnToTheMainMode = false;

    //booleans for alarm mode and alarm/hourlyChime signs
    public static boolean alarmIsRinging;
    public static boolean alarmStop;
    public static boolean twentyFourHoursFormat = true;
    public static boolean isRunning = false;

    private boolean hourlyChimeIsOn = true;
    private boolean alarmIsOn = true;
    private boolean alarmAnimationStarted;
    private boolean hourlyChimeBoolean = true;
    private boolean stopWatchSplitSequence;
    private boolean check2;

    //this boolean purpose is to not let updateUI thread to show textViewPmFormat || textViewTimeFormat
    private boolean monthModeIsOn;

    //this boolean is needed for cases where we switch from the edit mode with month, dayOfMonth, dayOfWeek animated to the main mode
    private boolean monthAnimationOccurs;

    //SELECTIONS
    //booleans for alarm mode selections
    private boolean alarmNoSelection = true;
    private boolean alarmHoursSelected = false;
    private boolean alarmMinutesSelected = false;

    //booleans for edit mode selections
    private boolean editSecondsSelected = true;
    private boolean editHoursSelected = false;
    private boolean editMinutesSelected = false;
    private boolean editMonthSelected = false;
    private boolean editDayOfMonthSelected = false;
    private boolean editDayOfWeekSelected = false;

    public static boolean startIdleCalculations;
    public static boolean forceReturnToTheMainMode;

    //reference variables of respected classes
    private TimeCalc m;
    private Alarm alarm;
    private Stopwatch stopWatch;
    private Animation anim;

    //time thread StringBuilder objects
    //main mode
    private StringBuilder timeSeconds;
    private StringBuilder mainHours;
    private StringBuilder mainMinutes;
    private StringBuilder monthDays;
    private StringBuilder month;

    //alarm mode
    private StringBuilder alarmHours;
    private StringBuilder alarmMinutes;

    //seconds
    private StringBuilder tenMilliseconds;
    private StringBuilder stopwatchMinutes;
    private StringBuilder stopwatchSeconds;

    MediaPlayer modeButtonSound;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);

        //main mode
        textViewMainSeconds = findViewById(R.id.textViewMainSeconds);
        textViewMainHours = findViewById(R.id.textViewMainHours);
        textViewMainMinutes = findViewById(R.id.textViewMainMinutes);
        textViewWeekDay = findViewById(R.id.textViewWeekDay);
        textViewDayOfMonth = findViewById(R.id.textViewDayOfMonth);
        textViewTimeFormat = findViewById(R.id.textViewTimeFormat);
        textViewPmFormat = findViewById(R.id.textViewPmFormat);

        textViewColon = findViewById(R.id.textViewColon);

        //alarm mode
        textViewAlarmHours = findViewById(R.id.textViewAlarmHours);
        textViewAlarmMinutes = findViewById(R.id.textViewAlarmMinutes);
        textViewAlarmText = findViewById(R.id.textViewAlarmText);
        imageViewHourlyChime = findViewById(R.id.imageViewHourlyChime);
        imageViewAlarm = findViewById(R.id.imageViewAlarmOn);

        //stopwatch mode
        textViewStopwatchTenMilliseconds = findViewById(R.id.textViewStopwatchTenMilliseconds);
        textViewStopwatchMinutes = findViewById(R.id.textViewStopwatchMinutes);
        textViewStopwatchSeconds = findViewById(R.id.textViewStopwatchSeconds);
        textViewStopwatchText = findViewById(R.id.textViewStopwatchText);
        textViewStopwatchSplit = findViewById(R.id.textViewStopwatchSplit);

        //month
        textViewMonth = findViewById(R.id.textViewMonth);

        modeButtonSound = MediaPlayer.create(this, R.raw.mode_button);

        enableTimeFlow();

        //invoking animation for blinking
        anim = new AlphaAnimation(0.0f, 1.0f);

        timeSeconds = new StringBuilder();
        mainHours = new StringBuilder();
        mainMinutes = new StringBuilder();
        monthDays = new StringBuilder();
        alarmHours = new StringBuilder();
        alarmMinutes = new StringBuilder();
        tenMilliseconds = new StringBuilder();
        stopwatchMinutes = new StringBuilder();
        stopwatchSeconds = new StringBuilder();
        month = new StringBuilder();

        buttonBackLight = findViewById(R.id.buttonBackLight);

        //we have to use onTouchListener and invoke onClick method there because we want to implement
        //on press and on release actions which would be impossible otherwise
        buttonBackLight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    imageView.setImageResource(R.drawable.watch);
                    return true;
                }
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    backlightSlashReset(v);
                    imageView.setImageResource(R.drawable.watch_backlight);
                    return true;
                }
                return false;
            }
        });
    }

    private void enableTimeFlow(){

        m = new TimeCalc(this, getApplicationContext());
        m.runThread();

        alarm = new Alarm(this, getApplicationContext());

        stopWatch = new Stopwatch(this, getApplicationContext());
        stopWatch.runThread();

    }

    @Override
    public void updateUI() {
        //main mode
        timeSeconds.setLength(0);
        timeSeconds.append(String.format(Locale.getDefault(), "%02d", m.getSeconds()));
        textViewMainSeconds.setText(timeSeconds);

        mainHours.setLength(0);
        if(m.getAdaptedHours() < 10){
            mainHours.append(String.format(Locale.getDefault(), "%d", m.getAdaptedHours()));
        } else {
            mainHours.append(String.format(Locale.getDefault(), "%02d", m.getAdaptedHours()));
        }
        textViewMainHours.setText(mainHours);

        mainMinutes.setLength(0);
        mainMinutes.append(String.format(Locale.getDefault(), "%02d", m.getMinutes()));
        textViewMainMinutes.setText(mainMinutes);

        //hourly chime sequence
        if (hourlyChimeIsOn && hourlyChimeBoolean && m.getMinutes() == 0 && m.getSeconds() == 0) {
            modeButtonSound.start();
            hourlyChimeBoolean = false;
        }
        if (m.getMinutes() == 59) {
            hourlyChimeBoolean = true;
        }

        monthDays.setLength(0);
        if(m.getDayOfMonth() < 10) {
            monthDays.append(String.format(Locale.getDefault(), "%d", m.getDayOfMonth()));
        } else {
            monthDays.append(String.format(Locale.getDefault(), "%02d", m.getDayOfMonth()));
        }
        textViewDayOfMonth.setText(monthDays);

        textViewWeekDay.setText(m.getWeekDay());

        //alarm mode
        //1 digit
        if(alarm.getAdaptedHours() >= 1 || alarm.getAdaptedHours() <= 9) {
            alarmHours.setLength(0);
            alarmHours.append(String.format(Locale.getDefault(), "%d", alarm.getAdaptedHours()));
        //2 digits
        } else {
            alarmHours.setLength(0);
            alarmHours.append(String.format(Locale.getDefault(), "%02d", alarm.getAdaptedHours()));
        }
        textViewAlarmHours.setText(alarmHours);

        alarmMinutes.setLength(0);
        alarmMinutes.append(String.format(Locale.getDefault(), "%02d", alarm.getMinutes()));
        textViewAlarmMinutes.setText(alarmMinutes);

        //alarm sequence
        if(alarm.getHours() == m.getHours() && alarm.getMinutes() == m.getMinutes() && m.getSeconds() <= 20 && m.getSeconds() >= 0 && !alarmStop){
            //replace sound TODO
            modeButtonSound.start();
            alarmIsRinging = true;
        } else {
            alarmIsRinging = false;
        }

        if(alarmIsRinging && !alarmAnimationStarted){
            startAnimation(imageViewAlarm);
            alarmAnimationStarted = true;
        } else if (!alarmIsRinging){
            imageViewAlarm.clearAnimation();
            alarmAnimationStarted = false;
        }

        if(alarmStop && m.getSeconds() > 20){
            alarmStop = false;
        }

        //stopwatch mode
        if(!stopWatchSplitSequence) {
            tenMilliseconds.setLength(0);
            tenMilliseconds.append(String.format(Locale.getDefault(), "%02d", (stopWatch.getMilliseconds())));
            textViewStopwatchTenMilliseconds.setText(tenMilliseconds);

            stopwatchMinutes.setLength(0);
            stopwatchMinutes.append(String.format(Locale.getDefault(), "%02d", stopWatch.getMinutes()));
            textViewStopwatchMinutes.setText(stopwatchMinutes);

            stopwatchSeconds.setLength(0);
            stopwatchSeconds.append(String.format(Locale.getDefault(), "%02d", stopWatch.getSeconds()));
            textViewStopwatchSeconds.setText(stopwatchSeconds);
        }

        //month
        month.setLength(0);
        if(m.getMonth() < 10) {
            month.append(String.format(Locale.getDefault(), "%d", m.getMonth()));
        } else {
            month.append(String.format(Locale.getDefault(), "%02d", m.getMonth()));
        }
        textViewMonth.setText(month);

        //pm/24h sequence for main and edit mode
        if((mainScreenViewsAreVisible || editModIsOn) && twentyFourHoursFormat && !monthModeIsOn){
            textViewTimeFormat.setVisibility(View.VISIBLE);
            textViewPmFormat.setVisibility(View.INVISIBLE);
        } else if((mainScreenViewsAreVisible || editModIsOn) && !twentyFourHoursFormat && !monthModeIsOn){
            textViewTimeFormat.setVisibility(View.INVISIBLE);
            if(m.getHours() >= 12) {
                textViewPmFormat.setVisibility(View.VISIBLE);
            } else {
                textViewPmFormat.setVisibility(View.INVISIBLE);
            }
        }

        //pm/24h sequence for alarm mode
        if(alarmViewsAreVisible && twentyFourHoursFormat){
            textViewTimeFormat.setVisibility(View.VISIBLE);
            textViewPmFormat.setVisibility(View.INVISIBLE);
        } else if(alarmViewsAreVisible && !twentyFourHoursFormat){
            textViewTimeFormat.setVisibility(View.INVISIBLE);
            if(alarm.getHours() >= 12) {
                textViewPmFormat.setVisibility(View.VISIBLE);
            } else {
                textViewPmFormat.setVisibility(View.INVISIBLE);
            }
        }

        //return to the main mode if idleTime > 90
        if(forceReturnToTheMainMode){
            forceReturnToTheMainMode();
            forceReturnToTheMainMode = false;
            minuteMarkPassedCount = 0;
        }
    }

    boolean check = false;
    public void changeTimeFormatOrStartStopStopWatch(View view) {

        if(!monthAnimationOccurs) {
            alarmStop = true;
        }

        //main mode
        if(mainScreenViewsAreVisible) {
            twentyFourHoursFormat = !twentyFourHoursFormat;
        }

        //alarm mode
        if(alarmViewsAreVisible){

            returnToTheMainMode = true;

            minuteMarkPassedCount = 0;

            if(alarmNoSelection) {
                modeButtonSound.start();
                hourlyChimeIsOn = !hourlyChimeIsOn;
                if (hourlyChimeIsOn) {
                    imageViewHourlyChime.setVisibility(View.VISIBLE);
                } else {
                    imageViewHourlyChime.setVisibility(View.INVISIBLE);
                }
                if (check) {
                    check = false;
                } else {
                    alarmIsOn = !alarmIsOn;
                    check = true;
                }
                if (alarmIsOn) {
                    imageViewAlarm.setVisibility(View.VISIBLE);
                } else {
                    imageViewAlarm.setVisibility(View.INVISIBLE);
                }
            }

            if(alarmHoursSelected){
                alarm.iterateHours();
            }
            if(alarmMinutesSelected){
                alarm.iterateMinutes();
            }
        }

        //stopwatch mode
        if(stopWatchViewsAreVisible){

            if(!isRunning){
                startAnimation(textViewColon);
                isRunning = true;
            } else {
                textViewColon.clearAnimation();
                isRunning = false;
            }

            modeButtonSound.start();

            returnToTheMainMode = true;

        }

        //edit mode
        if(editModIsOn){
            //this workaround is needed for the cases where we the time is equal to the alarm time and we edit the time fast, e.g. iterating hours when the time is still equal to the alarm time, this way the alarm will start again which is correct
            alarmStop = false;

            minuteMarkPassedCount = 0;

            if(editSecondsSelected){
                m.resetSeconds();
            }
            if(editHoursSelected){
                m.iterateHours();
            }
            if(editMinutesSelected){
                m.iterateMinutes();
            }
            if(editMonthSelected){
                m.iterateMonth();
            }
            if(editDayOfMonthSelected){
                //TODO 1 digit
                m.iterateDayOfMonth();
            }
            if(editDayOfWeekSelected){
                m.switchWeekDay();
            }
        }

    }

    public void switchMode(View view) {

        alarmStop = true;

        modeButtonSound.start();

        while(true) {

            //switching from the main mode to the alarm mode
            if (mainScreenViewsAreVisible) {

                returnToTheMainMode = false;

                alarmNoSelection = true;

                textViewMainHours.setVisibility(View.INVISIBLE);
                textViewMainMinutes.setVisibility(View.INVISIBLE);
                textViewMainSeconds.setVisibility(View.INVISIBLE);
                textViewDayOfMonth.setVisibility(View.INVISIBLE);
                textViewWeekDay.setVisibility(View.INVISIBLE);

                textViewAlarmHours.setVisibility(View.VISIBLE);
                textViewAlarmMinutes.setVisibility(View.VISIBLE);
                textViewAlarmText.setVisibility(View.VISIBLE);

                //for splitSequence
                check2 = true;

                //
                startIdleCalculations = true;

                modeCount = 2;
                break;
            }

            //switching from the alarm mode to the stopwatch mode (or to main mode if alarm mode elements were used)
            if (alarmViewsAreVisible) {

                textViewAlarmHours.setVisibility(View.INVISIBLE);
                textViewAlarmMinutes.setVisibility(View.INVISIBLE);
                textViewAlarmText.setVisibility(View.INVISIBLE);
                textViewTimeFormat.setVisibility(View.INVISIBLE);
                textViewPmFormat.setVisibility(View.INVISIBLE);

                textViewAlarmHours.clearAnimation();
                textViewAlarmMinutes.clearAnimation();

                startIdleCalculations = false;

                if (returnToTheMainMode) {

                    textViewMainHours.setVisibility(View.VISIBLE);
                    textViewMainMinutes.setVisibility(View.VISIBLE);
                    textViewMainSeconds.setVisibility(View.VISIBLE);
                    textViewDayOfMonth.setVisibility(View.VISIBLE);
                    textViewWeekDay.setVisibility(View.VISIBLE);

                    modeCount = 1;
                    break;
                } else {

                    textViewStopwatchMinutes.setVisibility(View.VISIBLE);
                    textViewStopwatchSeconds.setVisibility(View.VISIBLE);
                    textViewStopwatchTenMilliseconds.setVisibility(View.VISIBLE);
                    textViewStopwatchText.setVisibility(View.VISIBLE);

                    if(stopWatchSplitSequence){
                        textViewStopwatchSplit.setVisibility(View.VISIBLE);
                    }

                    if(isRunning){
                        startAnimation(textViewColon);
                    }

                    modeCount = 3;
                    break;
                }
            }

            //switching from the stopwatch mode to the edit mode (or to main mode if stopwatch elements were used)
            if (stopWatchViewsAreVisible) {

                textViewStopwatchMinutes.setVisibility(View.INVISIBLE);
                textViewStopwatchSeconds.setVisibility(View.INVISIBLE);
                textViewStopwatchTenMilliseconds.setVisibility(View.INVISIBLE);
                textViewStopwatchText.setVisibility(View.INVISIBLE);

                textViewColon.clearAnimation();
                textViewStopwatchSplit.setVisibility(View.INVISIBLE);
                stopWatchSplitSequence = false;

                textViewMainHours.setVisibility(View.VISIBLE);
                textViewMainMinutes.setVisibility(View.VISIBLE);
                textViewMainSeconds.setVisibility(View.VISIBLE);
                textViewDayOfMonth.setVisibility(View.VISIBLE);
                textViewWeekDay.setVisibility(View.VISIBLE);



                if (!returnToTheMainMode) {

                    startAnimation(textViewMainSeconds);

                    startIdleCalculations = true;

                    modeCount = 4;
                    break;
                }

                modeCount = 1;
                break;

            }

            //switching from the edit mode to the main mode
            if (editModIsOn) {

                textViewMainSeconds.clearAnimation();

                textViewMainHours.clearAnimation();
                textViewMainMinutes.clearAnimation();
                textViewDayOfMonth.clearAnimation();
                textViewWeekDay.clearAnimation();
                textViewMonth.clearAnimation();

                //if we switch from the edit mode to the main mode with month, dayOfMonth, dayOfWeek animated at the time
                if (monthAnimationOccurs) {
                    textViewMainSeconds.setVisibility(View.VISIBLE);
                    textViewMainHours.setVisibility(View.VISIBLE);
                    textViewMainMinutes.setVisibility(View.VISIBLE);
                    textViewWeekDay.setVisibility(View.VISIBLE);
                    textViewDayOfMonth.setVisibility(View.VISIBLE);

                    textViewColon.setVisibility(View.VISIBLE);
                }

                textViewMonth.setVisibility(View.INVISIBLE);

                monthModeIsOn = false;
                editSecondsSelected = true;

                modeCount = 1;
                break;
            }
        }

        //using booleans to know which mode is active now
        //this block of code should be located in bottom
        //main mode is up
        mainScreenViewsAreVisible = (modeCount == 1);

        //alarm mode is up
        alarmViewsAreVisible = (modeCount == 2);

        //stopwatch mode is up
        stopWatchViewsAreVisible = (modeCount == 3);

        //edit mode is up
        editModIsOn = (modeCount == 4);
    }

    private void startAnimation(View view){
        anim.setDuration(100);
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        view.startAnimation(anim);
    }

    public void backlightSlashReset(View view) {

        //main mode
        if(mainScreenViewsAreVisible){
            alarmStop = true;
        }

        //alarm mode
        while (alarmViewsAreVisible) {

            alarmStop = true;

            minuteMarkPassedCount = 0;

            returnToTheMainMode = true;

            if (alarmNoSelection) {
                alarmNoSelection = false;
                alarmHoursSelected = true;

                startAnimation(textViewAlarmHours);
                break;
            }
            if (alarmHoursSelected) {
                alarmHoursSelected = false;
                textViewAlarmHours.clearAnimation();
                alarmMinutesSelected = true;

                startAnimation(textViewAlarmMinutes);
                break;
            }
            if (alarmMinutesSelected) {
                alarmMinutesSelected = false;
                textViewAlarmMinutes.clearAnimation();
                alarmNoSelection = true;

                break;
            }
        }

        //stopwatch mode
        if (stopWatchViewsAreVisible) {

            alarmStop = true;

            returnToTheMainMode = true;

            minuteMarkPassedCount = 0;

            if (isRunning) {
                if (check2) {
                    textViewStopwatchSplit.setVisibility(View.VISIBLE);
                    stopWatchSplitSequence = true;
                    check2 = false;
                } else {
                    textViewStopwatchSplit.setVisibility(View.INVISIBLE);
                    stopWatchSplitSequence = false;
                    check2 = true;
                }
            }

            if (!isRunning) {

                if (!stopWatchSplitSequence) {
                    stopWatch.resetTime();
                }

                textViewStopwatchSplit.setVisibility(View.INVISIBLE);
                stopWatchSplitSequence = false;
            }
        }

        //edit mode
        while (editModIsOn) {

            if (alarmIsRinging) {
                alarmStop = true;
                break;
            }
            if (editSecondsSelected) {
                textViewMainSeconds.clearAnimation();
                startAnimation(textViewMainHours);
                editHoursSelected = true;
                editSecondsSelected = false;
                break;
            }
            if (editHoursSelected) {
                textViewMainHours.clearAnimation();
                startAnimation(textViewMainMinutes);
                editMinutesSelected = true;
                editHoursSelected = false;
                break;
            }
            if (editMinutesSelected) {
                textViewMainMinutes.clearAnimation();
                textViewMainSeconds.setVisibility(View.INVISIBLE);
                textViewMainHours.setVisibility(View.INVISIBLE);
                textViewMainMinutes.setVisibility(View.INVISIBLE);
                textViewColon.setVisibility(View.INVISIBLE);
                textViewTimeFormat.setVisibility(View.INVISIBLE);
                textViewPmFormat.setVisibility(View.INVISIBLE);

                //we set monthModeIsOn true if we hide everything apart from month, dayOfWeek and dayOfMonth
                //we don't hide textViewDayOfMonth and textViewWeekDay here
                monthModeIsOn = true;
                textViewMonth.setVisibility(View.VISIBLE);
                startAnimation(textViewMonth);
                monthAnimationOccurs = true;
                editMonthSelected = true;
                editMinutesSelected = false;
                break;
            }
            if (editMonthSelected) {
                textViewMonth.clearAnimation();
                editDayOfMonthSelected = true;
                editMonthSelected = false;
                startAnimation(textViewDayOfMonth);
                break;
            }
            if (editDayOfMonthSelected) {
                textViewDayOfMonth.clearAnimation();
                startAnimation(textViewWeekDay);
                editDayOfWeekSelected = true;
                editDayOfMonthSelected = false;
                break;
            }
            if (editDayOfWeekSelected) {
                textViewWeekDay.clearAnimation();
                textViewMonth.setVisibility(View.INVISIBLE);
                textViewMainSeconds.setVisibility(View.VISIBLE);
                textViewMainHours.setVisibility(View.VISIBLE);
                textViewMainMinutes.setVisibility(View.VISIBLE);
                textViewColon.setVisibility(View.VISIBLE);
                startAnimation(textViewMainSeconds);
                monthModeIsOn = false;
                monthAnimationOccurs = false;
                editSecondsSelected = true;
                editDayOfWeekSelected = false;
                break;
            }
        }
    }

    public void forceReturnToTheMainMode() {

        //from the alarm mode
        textViewAlarmHours.setVisibility(View.INVISIBLE);
        textViewAlarmMinutes.setVisibility(View.INVISIBLE);
        textViewAlarmText.setVisibility(View.INVISIBLE);
        textViewTimeFormat.setVisibility(View.INVISIBLE);
        textViewPmFormat.setVisibility(View.INVISIBLE);
        textViewAlarmHours.clearAnimation();
        textViewAlarmMinutes.clearAnimation();
        textViewMainHours.setVisibility(View.VISIBLE);
        textViewMainMinutes.setVisibility(View.VISIBLE);
        textViewMainSeconds.setVisibility(View.VISIBLE);
        textViewDayOfMonth.setVisibility(View.VISIBLE);
        textViewWeekDay.setVisibility(View.VISIBLE);

        //from the edit mode
        textViewMainSeconds.clearAnimation();
        textViewMainHours.clearAnimation();
        textViewMainMinutes.clearAnimation();
        textViewDayOfMonth.clearAnimation();
        textViewWeekDay.clearAnimation();
        textViewMonth.clearAnimation();
        textViewMainSeconds.setVisibility(View.VISIBLE);
        textViewMainHours.setVisibility(View.VISIBLE);
        textViewMainMinutes.setVisibility(View.VISIBLE);
        textViewWeekDay.setVisibility(View.VISIBLE);
        textViewDayOfMonth.setVisibility(View.VISIBLE);
        textViewColon.setVisibility(View.VISIBLE);
        textViewMonth.setVisibility(View.INVISIBLE);

        monthModeIsOn = false;
        editSecondsSelected = true;

        mainScreenViewsAreVisible = true;
    }

}



