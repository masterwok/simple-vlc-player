package com.masterwok.simplevlcplayer.dagger

import android.app.Activity
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ContentProvider
import android.content.Context
import android.support.v4.app.Fragment
import com.masterwok.simplevlcplayer.dagger.components.DaggerAppComponent
import dagger.android.*
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject


/**
 * This class is responsible for providing injectors in an environment
 * that does not have a subclassed application.
 */
class DaggerInjector private constructor(
        appContext: Context
) : HasActivityInjector
        , HasFragmentInjector
        , HasSupportFragmentInjector
        , HasServiceInjector
        , HasBroadcastReceiverInjector
        , HasContentProviderInjector {

    init {
        DaggerAppComponent
                .builder()
                .context(appContext)
                .build()
                .inject(this)
    }

    @Inject
    lateinit var activityInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var broadcastReceiverInjector: DispatchingAndroidInjector<BroadcastReceiver>

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<android.app.Fragment>

    @Inject
    lateinit var supportFragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var serviceInjector: DispatchingAndroidInjector<Service>

    @Inject
    lateinit var contentProviderInjector: DispatchingAndroidInjector<ContentProvider>

    override fun fragmentInjector(): DispatchingAndroidInjector<android.app.Fragment> =
            fragmentInjector

    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> =
            supportFragmentInjector

    override fun broadcastReceiverInjector(): DispatchingAndroidInjector<BroadcastReceiver> =
            broadcastReceiverInjector

    override fun serviceInjector(): DispatchingAndroidInjector<Service> = serviceInjector

    override fun contentProviderInjector(): AndroidInjector<ContentProvider> =
            contentProviderInjector

    override fun activityInjector(): AndroidInjector<Activity> = activityInjector

    companion object {

        private var instance: DaggerInjector? = null

        fun getInstance(appContext: Context): DaggerInjector {
            // Avoid synchronization if instance already exists.
            if (instance != null) {
                return instance!!
            }

            synchronized(DaggerInjector::class.java) {
                if (instance == null) {
                    instance = DaggerInjector(appContext)
                }
            }

            return instance!!
        }

    }

}