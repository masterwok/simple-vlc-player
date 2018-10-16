package com.masterwok.simplevlcplayer.dagger.modules

import com.masterwok.simplevlcplayer.services.MediaPlayerService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
@Suppress("unused")
abstract class AndroidServiceModule {

    @ContributesAndroidInjector
    abstract fun contributesMediaPlayerService(): MediaPlayerService

}
