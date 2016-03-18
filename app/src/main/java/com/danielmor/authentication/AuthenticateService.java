package com.danielmor.authentication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by bixo on 17/03/2016.
 */
public class AuthenticateService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        Authenticator authenticator = new Authenticator(this);
        return authenticator.getIBinder();
    }
}
