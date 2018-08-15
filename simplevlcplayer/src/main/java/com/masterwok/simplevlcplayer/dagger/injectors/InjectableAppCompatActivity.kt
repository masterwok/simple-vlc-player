package com.masterwok.simplevlcplayer.dagger.injectors

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.masterwok.simplevlcplayer.dagger.DaggerInjector
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasFragmentInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject


/**
 * This abstract activity is responsible for injecting dependencies using
 * the DaggerInjector class. The DaggerInjector class is used for injection
 * rather than the DaggerAppCompatActivity as this module does not have an
 * application subclass to provide injectors.
 */
abstract class InjectableAppCompatActivity : AppCompatActivity(), HasFragmentInjector, HasSupportFragmentInjector {

    @Inject
    lateinit var supportFragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var frameworkFragmentInjector: DispatchingAndroidInjector<android.app.Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerInjector
                .getInstance(applicationContext)
                .activityInjector()
                .inject(this)
    }

    override fun fragmentInjector(): AndroidInjector<android.app.Fragment> =
            frameworkFragmentInjector

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = supportFragmentInjector


}