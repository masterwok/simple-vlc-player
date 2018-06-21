package com.masterwok.simplevlcplayer.dagger.modules;

import com.masterwok.simplevlcplayer.services.MediaPlayerService;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ServiceModule {
    @ContributesAndroidInjector
    abstract MediaPlayerService contributeMediaPlayerService();
}
