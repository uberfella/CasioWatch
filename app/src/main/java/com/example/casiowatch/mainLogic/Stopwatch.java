package com.example.casiowatch.mainLogic;

import android.content.Context;
import android.os.Handler;

import static com.example.casiowatch.MainActivity.isRunning;

public class Stopwatch {

    private int milliseconds = 0;
    private int seconds = 0;
    private int minutes = 0;

    public int getMilliseconds() {
        return milliseconds;
    }

    public int getSeconds(){
        return seconds;
    }

    public int getMinutes(){
        return minutes;
    }

    private MainActivityInteractionInterface interactionInterface;
    private Context applicationContext;

    public Stopwatch(MainActivityInteractionInterface interactionInterface, Context applicationContext){
        this.interactionInterface = interactionInterface;
        this.applicationContext = applicationContext;
    }

    public void resetTime(){
        milliseconds = 0;
        seconds = 0;
        minutes = 0;
    }

    public void runThread(){
        final Handler handler = new Handler();
        handler.post(new Runnable(){
            @Override
            public void run(){
                if(isRunning) {
                    milliseconds++;
                    if (milliseconds == 20) {
                        milliseconds = 0;
                        seconds++;
                        if(seconds == 60){
                            seconds = 0;
                            minutes++;
                            if(minutes == 100){
                                minutes = 0;
                            }
                        }
                    }
                }
                interactionInterface.updateUI();
                handler.postDelayed(this, 50); //Android refresh the UI every 16msec (to have a rate of 60fps), so setting the handler to updateUI in a lesser time would make no sense and maybe also interfier with it.
            }
        });
    }
}
