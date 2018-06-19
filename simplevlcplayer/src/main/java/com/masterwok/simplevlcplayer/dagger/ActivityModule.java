package com.masterwok.simplevlcplayer.dagger;

import com.masterwok.simplevlcplayer.activities.MediaPlayerActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class ActivityModule
{
    @ContributesAndroidInjector
    abstract MediaPlayerActivity contributeMainActivity();
}
