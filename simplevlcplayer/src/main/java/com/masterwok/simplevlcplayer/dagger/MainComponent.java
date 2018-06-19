package com.masterwok.simplevlcplayer.dagger;

import android.content.Context;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

@Singleton
@Component(modules = {
        AndroidSupportInjectionModule.class,
        AppModule.class,
        ActivityModule.class,
        FragmentModule.class,
        VlcModule.class
})
public interface MainComponent
        extends AndroidInjector<DaggerInjector> {

    @Override
    void inject(DaggerInjector instance);

    @Component.Builder
    interface Builder {

        @BindsInstance
        MainComponent.Builder context(Context applicationContext);

        MainComponent build();
    }

}
