package com.masterwok.simplevlcplayer.dagger.modules;

import android.content.Context;

import com.masterwok.simplevlcplayer.contracts.ApplicationContext;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class AppModule {

    @SuppressWarnings("unused")
    @Binds
    @ApplicationContext
    abstract Context bindContext(Context appContext);

}
