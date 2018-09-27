package com.masterwok.simplevlcplayer.dagger.modules

import com.masterwok.simplevlcplayer.fragments.*
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
@Suppress("unused")
abstract class FragmentModule {

    @ContributesAndroidInjector
    internal abstract fun contributesLocalPlayerFragment(): LocalPlayerFragment

    @ContributesAndroidInjector
    internal abstract fun contributesRendererPlayerFragment(): CastPlayerFragment

    @ContributesAndroidInjector
    abstract fun contributesSubtitlesDialogFragment(): SubtitlesDialogFragment

    @ContributesAndroidInjector
    abstract fun contributesRendererItemDialogFragment(): RendererItemDialogFragment

}
