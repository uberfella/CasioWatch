package com.example.casiowatch;

import androidx.appcompat.app.AppCompatActivity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.casiowatch.mainLogic.TimeCalc;
import com.example.casiowatch.mainLogic.MainActivityInteractionInterface;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements MainActivityInteractionInterface{

    private TextView textViewSeconds;
    private TextView textViewMainTime;
    private TextView textViewWeekDay;
    private TextView textViewDayOfMonth;
    private TextView textViewTimeFormat;
    private TextView textViewPmFormat;

    public static boolean twentyFourHoursFormat = true;

    private TimeCalc m;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewSeconds = findViewById(R.id.textViewSeconds);
        textViewMainTime = findViewById(R.id.textViewMainTime);
        textViewWeekDay = findViewById(R.id.textViewWeekDay);
        textViewDayOfMonth = findViewById(R.id.textViewDayOfMonth);
        textViewTimeFormat = findViewById(R.id.textViewTimeFormat);
        textViewPmFormat = findViewById(R.id.textViewPmFormat);

        final MediaPlayer modeButtonSound = MediaPlayer.create(this, R.raw.mode_button);
        Button modeButton = this.findViewById(R.id.buttonMode);
        modeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modeButtonSound.start();
            }
        });
        enableTimeFlow();
    }

    private void enableTimeFlow(){
        m = new TimeCalc(this, getApplicationContext());
        m.runThread();
    }

    @Override
    public void updateUI() {
        String timeSeconds = String.format(Locale.getDefault(), "%02d", m.getSeconds());
        textViewSeconds.setText(timeSeconds);


        String timeMinutesAndHours = String.format(Locale.getDefault(), "%02d:%02d", m.getAdaptedHours(), m.getMinutes());
        textViewMainTime.setText(timeMinutesAndHours);


        textViewWeekDay.setText(m.getWeekDay());
        String monthDays = String.format(Locale.getDefault(), "%02d", m.getDayOfMonth());
        textViewDayOfMonth.setText(monthDays);
    }

    public void changeTimeFormat(View view) {
        twentyFourHoursFormat = !twentyFourHoursFormat;
        swapTimeFormatVisibility();
    }

    private void swapTimeFormatVisibility(){
        if(textViewTimeFormat.getVisibility() == View.VISIBLE){
            textViewTimeFormat.setVisibility(View.INVISIBLE);
            if(m.getHours() >= 12) {
                textViewPmFormat.setVisibility(View.VISIBLE);
            }
        } else {
            textViewTimeFormat.setVisibility(View.VISIBLE);
            textViewPmFormat.setVisibility(View.INVISIBLE);
        }
    }
}
