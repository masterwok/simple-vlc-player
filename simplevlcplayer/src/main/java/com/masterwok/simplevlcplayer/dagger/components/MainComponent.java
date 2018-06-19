package com.masterwok.simplevlcplayer.dagger.components;

import android.content.Context;

import com.masterwok.simplevlcplayer.dagger.DaggerInjector;
import com.masterwok.simplevlcplayer.dagger.modules.ActivityModule;
import com.masterwok.simplevlcplayer.dagger.modules.AppModule;
import com.masterwok.simplevlcplayer.dagger.modules.FragmentModule;
import com.masterwok.simplevlcplayer.dagger.modules.ServiceModule;
import com.masterwok.simplevlcplayer.dagger.modules.VlcModule;

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
        ServiceModule.class,
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
