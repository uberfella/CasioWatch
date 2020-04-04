package com.example.casiowatch.mainLogic;

import android.content.Context;

import com.example.casiowatch.MainActivity;

public class Alarm {

    private int hours = 14;
    private int minutes = 0;

    private MainActivityInteractionInterface interactionInterface;
    private Context applicationContext;

    public Alarm(MainActivityInteractionInterface interactionInterface, Context applicationContext){
        this.interactionInterface = interactionInterface;
        this.applicationContext = applicationContext;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void iterateHours(){

        hours++;

        if(hours == 24){
            hours = 0;
        }
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

    public void iterateMinutes(){
        minutes++;

        if(minutes == 60){
            minutes = 0;
        }

        interactionInterface.updateUI();
    }
}
