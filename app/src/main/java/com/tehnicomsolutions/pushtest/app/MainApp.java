package com.tehnicomsolutions.pushtest.app;

import android.app.Application;
import android.content.Context;

/**
 * Created by pedja on 10/8/13 10.15.
 * This class is part of the ${PROJECT_NAME}
 * Copyright © 2014 ${OWNER}
 *
 * Main Application class
 * Created when application is first started
 * It stays in memory as long as app is alive
 *
 * @author Predrag Čokulov
 */
public class MainApp extends Application
{
    private static  MainApp mainApp = null;
    private static Context context;

    public synchronized static MainApp getInstance()
    {
        if(mainApp == null)
        {
            mainApp = new MainApp();
        }
        return mainApp;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        context = this.getApplicationContext();
        mainApp = this;
    }


    public static Context getContext()
    {
        return context;
    }

}
