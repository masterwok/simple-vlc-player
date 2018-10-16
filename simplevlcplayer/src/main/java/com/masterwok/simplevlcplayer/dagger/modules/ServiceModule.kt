package com.masterwok.simplevlcplayer.dagger.modules

import com.masterwok.opensubtitlesandroid.services.contracts.OpenSubtitlesService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
@Suppress("unused")
class ServiceModule {

    @Singleton
    @Provides
    fun providesOpenSubtitlesService(): OpenSubtitlesService =
            com.masterwok.opensubtitlesandroid.services.OpenSubtitlesService()
}