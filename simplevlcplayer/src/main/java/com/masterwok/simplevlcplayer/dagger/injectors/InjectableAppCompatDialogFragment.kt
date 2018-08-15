package com.masterwok.simplevlcplayer.dagger.injectors

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatDialogFragment
import com.masterwok.simplevlcplayer.dagger.DaggerInjector
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

abstract class InjectableAppCompatDialogFragment : AppCompatDialogFragment()
        , HasSupportFragmentInjector {

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