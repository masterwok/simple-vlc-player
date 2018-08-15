package com.masterwok.simplevlcplayer.dagger.modules

import com.masterwok.simplevlcplayer.activities.MediaPlayerActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
@Suppress("unused")
abstract class ActivityModule {

    @ContributesAndroidInjector()
    abstract fun contributeMediaPlayerActivity(): MediaPlayerActivity

}
