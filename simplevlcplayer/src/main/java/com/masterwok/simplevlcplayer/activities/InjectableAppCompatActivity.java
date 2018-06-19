package com.masterwok.simplevlcplayer.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.masterwok.simplevlcplayer.dagger.DaggerInjector;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasFragmentInjector;
import dagger.android.support.HasSupportFragmentInjector;


/**
 * This abstract activity is responsible for injecting dependencies using
 * the DaggerInjector class. The DaggerInjector class is used for injection
 * rather than the DaggerAppCompatActivity as this module does not have an
 * application subclass to provide injectors.
 */
public abstract class InjectableAppCompatActivity
        extends AppCompatActivity
        implements HasFragmentInjector
        , HasSupportFragmentInjector {

    @Inject
    DispatchingAndroidInjector<Fragment> supportFragmentInjector;

    @Inject
    DispatchingAndroidInjector<android.app.Fragment> frameworkFragmentInjector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerInjector
                .getInstance(getApplicationContext())
                .activityInjector()
                .inject(this);
    }

    @Override
    public AndroidInjector<android.app.Fragment> fragmentInjector() {
        return frameworkFragmentInjector;
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return supportFragmentInjector;
    }


}
