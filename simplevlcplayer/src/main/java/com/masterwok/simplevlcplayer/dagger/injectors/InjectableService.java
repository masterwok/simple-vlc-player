package com.masterwok.simplevlcplayer.dagger.injectors;

import android.app.Service;

import com.masterwok.simplevlcplayer.dagger.DaggerInjector;

/**
 * This abstract fragment is responsible for injecting dependencies using
 * the DaggerInjector class. The DaggerInjector class is used for injection
 * rather than the DaggerFragment as this module does not have an application
 * subclass to provide injectors.
 */
public abstract class InjectableService
        extends Service {

    @Override
    public void onCreate() {
        DaggerInjector
                .getInstance(getApplicationContext())
                .serviceInjector()
                .inject(this);

        super.onCreate();
    }
}
