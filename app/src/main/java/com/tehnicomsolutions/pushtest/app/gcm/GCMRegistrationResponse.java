package com.tehnicomsolutions.pushtest.app.gcm;

/**
 * Created by pedja on 30.6.14. 17.20.
 * This class is part of the Dating
 * Copyright © 2014 ${OWNER}
 * @author Predrag Čokulov
 */
public class GCMRegistrationResponse
{
    public String registrationId;// actual registration id
    public boolean newRegistrationKeyReturned;//did we get new registration id, or we are using key from shared prefs

    public GCMRegistrationResponse(String registrationId, boolean newRegistrationKeyReturned)
    {
        this.registrationId = registrationId;
        this.newRegistrationKeyReturned = newRegistrationKeyReturned;
    }
}
