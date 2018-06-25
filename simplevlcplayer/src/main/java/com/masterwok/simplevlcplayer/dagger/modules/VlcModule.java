package com.masterwok.simplevlcplayer.dagger.modules;

import android.content.Context;

import com.masterwok.simplevlcplayer.VlcMediaPlayer;

import org.videolan.libvlc.LibVLC;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


/**
 * This module is responsible for providing VLC dependencies.
 */
@Module
public class VlcModule {

    @Singleton
    @Provides
    final LibVLC provideLibVlc(Context context) {
        return new LibVLC(context.getApplicationContext());
    }

    @Provides
    final com.masterwok.simplevlcplayer.contracts.VlcMediaPlayer provideVlcMediaPlayer(LibVLC libVlc) {
        return new VlcMediaPlayer(libVlc);
    }


}
