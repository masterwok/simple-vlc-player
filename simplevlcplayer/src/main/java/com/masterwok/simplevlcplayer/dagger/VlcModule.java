package com.masterwok.simplevlcplayer.dagger;

import android.content.Context;

import com.masterwok.simplevlcplayer.contracts.MediaPlayer;
import com.masterwok.simplevlcplayer.contracts.MediaPlayerManager;

import org.videolan.libvlc.LibVLC;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


/**
 * This module is responsible for providing VLC dependencies.
 */
@Module
class VlcModule {

    @Singleton
    @Provides
    final LibVLC provideLibVlc(Context context) {
        return new LibVLC(context.getApplicationContext());
    }

    @Provides
    final MediaPlayer provideVlcPlayer(LibVLC libVlc) {
        return new com.masterwok.simplevlcplayer.MediaPlayer(libVlc);
    }

}
