package com.masterwok.simplevlcplayer.dagger.modules;

import com.masterwok.simplevlcplayer.fragments.LocalPlayerFragment;
import com.masterwok.simplevlcplayer.fragments.RendererPlayerFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class FragmentModule {

    @ContributesAndroidInjector
    abstract LocalPlayerFragment contributeLocalPlayerFragment();

    @ContributesAndroidInjector
    abstract RendererPlayerFragment contributeRendererPlayerFragment();

}