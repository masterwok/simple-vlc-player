package com.masterwok.simplevlcplayer.dagger.components

import android.content.Context
import com.masterwok.simplevlcplayer.dagger.DaggerInjector
import com.masterwok.simplevlcplayer.dagger.modules.*
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton


@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class
    , ActivityModule::class
    , FragmentModule::class
    , AndroidServiceModule::class
    , ServiceModule::class
    , VlcModule::class
])
interface AppComponent : AndroidInjector<DaggerInjector> {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun context(applicationContext: Context): AppComponent.Builder

        fun build(): AppComponent
    }

}