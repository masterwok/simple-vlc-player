package com.masterwok.simplevlcplayer.dagger.modules;

import android.content.Context;

import com.masterwok.simplevlcplayer.VlcMediaPlayer;
import com.masterwok.simplevlcplayer.VlcOptionsProvider;

import org.videolan.libvlc.LibVLC;

import java.util.ArrayList;

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
        final Context appContext = context.getApplicationContext();

        ArrayList<String> options = VlcOptionsProvider
                .getInstance()
                .getOptions();

        return options == null || options.size() == 0
                // No options provided, build defaults.
                ? new LibVLC(appContext, new VlcOptionsProvider.Builder(context).build())
                // Use provided options.
                : new LibVLC(appContext, options);
    }

    @Provides
    final com.masterwok.simplevlcplayer.contracts.VlcMediaPlayer provideVlcMediaPlayer(LibVLC libVlc) {
        return new VlcMediaPlayer(libVlc);
    }


}
