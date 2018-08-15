package com.masterwok.simplevlcplayer.dagger.injectors

import android.content.Context
import android.support.v4.app.Fragment
import com.masterwok.simplevlcplayer.dagger.DaggerInjector
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject


/**
 * This abstract fragment is responsible for injecting dependencies using
 * the DaggerInjector class. The DaggerInjector class is used for injection
 * rather than the DaggerFragment as this module does not have an application
 * subclass to provide injectors.
 */
abstract class InjectableFragment : Fragment(), HasSupportFragmentInjector {

    @Inject
    lateinit var childFragmentInjector: DispatchingAndroidInjector<Fragment>

    override fun onAttach(context: Context) {
        DaggerInjector
                .getInstance(context.applicationContext)
                .supportFragmentInjector()
                .inject(this)

        super.onAttach(context)
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = childFragmentInjector
}
