package com.tehnicomsolutions.pushtest.app.gcm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by pedja on 11/18/13 10.14.
 * This class is part of the ${PROJECT_NAME}
 * Copyright © 2014 ${OWNER}
 * @author Predrag Čokulov, Google(copy/paste from gcm setup guide :D)
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        // Explicitly specify that GcmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(), GcmIntentService.class.getName());
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }

}
