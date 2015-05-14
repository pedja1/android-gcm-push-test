package com.tehnicomsolutions.pushtest.app.gcm;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.tehnicomsolutions.pushtest.app.BuildConfig;
import com.tehnicomsolutions.pushtest.app.Constants;

import java.io.IOException;

/**
 * Created by pedja on 11/18/13 10.16.
 * This class is part of the ${PROJECT_NAME}
 * Copyright © 2014 ${OWNER}
 * @author Predrag Čokulov, Google(copy/paste from gcm setup guide :D)
 */
public class GCMUtilities
{
    public static final String EXTRA_MESSAGE = "data";
    private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";


    private GoogleCloudMessaging gcm;
    private final SharedPreferences prefs;
    private final Activity activity;
    private static final String SENDER_ID = "1081801912224";

    public GCMUtilities(Activity activity)
    {
        this.activity = activity;
        prefs = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    public GCMUtilities(SharedPreferences prefs, Activity activity)
    {
        this.prefs = prefs;
        this.activity = activity;
    }


    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    public GCMRegistrationResponse getRegistrationId(boolean forceLocal)
    {
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if(forceLocal)return new GCMRegistrationResponse(registrationId, false);
        if (registrationId.isEmpty())
        {
            if(BuildConfig.DEBUG)Log.i(Constants.LOG_TAG, "Registration not found, registering...");
            return register();
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion)
        {
            if(BuildConfig.DEBUG)Log.i(Constants.LOG_TAG, "App version changed, register again");
            return register();
        }
        return new GCMRegistrationResponse(registrationId, false);
    }

    /**
     * Unregister from gcm and clear shared prefs*/
    public void unregister()
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(PROPERTY_REG_ID);
        editor.apply();
        if (gcm == null)
        {
            gcm = GoogleCloudMessaging.getInstance(activity);
        }
        try
        {
            gcm.unregister();
        }
        catch (IOException e)
        {
            if(BuildConfig.DEBUG)e.printStackTrace();
        }
    }

    /**
     * Perform registration on GCM*/
    private GCMRegistrationResponse register()
    {
        String regid;
        try
        {
            if (gcm == null)
            {
                gcm = GoogleCloudMessaging.getInstance(activity);
            }
            regid = gcm.register(SENDER_ID);

            // Persist the regID - no need to register again.
            storeRegistrationId(regid);
            return new GCMRegistrationResponse(regid, true);
        }
        catch (IOException e)
        {
            if(BuildConfig.DEBUG)e.printStackTrace();
        }
        return null;
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private int getAppVersion()
    {
        try
        {
            PackageInfo packageInfo = activity.getPackageManager()
                    .getPackageInfo(activity.getPackageName(), 0);
            return packageInfo.versionCode;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param regId registration ID
     */
    private void storeRegistrationId( String regId)
    {
        int appVersion = getAppVersion();
        if(BuildConfig.DEBUG)Log.i(Constants.LOG_TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }

}
