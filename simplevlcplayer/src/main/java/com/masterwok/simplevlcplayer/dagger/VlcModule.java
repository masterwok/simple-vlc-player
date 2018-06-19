package com.masterwok.simplevlcplayer.dagger;

import android.content.Context;

import com.masterwok.simplevlcplayer.VlcMediaPlayer;
import com.masterwok.simplevlcplayer.contracts.SurfaceMediaPlayer;

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
    final SurfaceMediaPlayer provideSurfaceVlcPlayer(LibVLC libVlc) {
        return new VlcMediaPlayer(libVlc);
    }


}
