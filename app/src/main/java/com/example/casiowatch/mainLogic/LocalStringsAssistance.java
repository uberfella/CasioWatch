package com.example.casiowatch.mainLogic;

import android.app.Application;
import android.content.res.Resources;

public class LocalStringsAssistance extends Application {
    private static LocalStringsAssistance mInstance;
    private static Resources res;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        res = getResources();
    }

    public static LocalStringsAssistance getInstance() {
        return mInstance;
    }

    public static Resources getRes() {
        return res;
    }

}
