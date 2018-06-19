package com.masterwok.simplevlcplayer.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.masterwok.simplevlcplayer.dagger.DaggerInjector;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

/**
 * This abstract fragment is responsible for injecting dependencies using
 * the DaggerInjector class. The DaggerInjector class is used for injection
 * rather than the DaggerFragment as this module does not have an application
 * subclass to provide injectors.
 */
public abstract class InjectableFragment
        extends Fragment
        implements HasSupportFragmentInjector {

    @Inject
    DispatchingAndroidInjector<Fragment> childFragmentInjector;

    @Override
    public void onAttach(Context context) {
        DaggerInjector
                .getInstance(context.getApplicationContext())
                .supportFragmentInjector()
                .inject(this);

        super.onAttach(context);
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return childFragmentInjector;
    }

}
