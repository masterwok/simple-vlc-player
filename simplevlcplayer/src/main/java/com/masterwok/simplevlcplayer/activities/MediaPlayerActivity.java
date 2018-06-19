package com.masterwok.simplevlcplayer.activities;

import android.os.Bundle;

import com.masterwok.simplevlcplayer.R;
import com.masterwok.simplevlcplayer.dagger.injectors.InjectableAppCompatActivity;
import com.masterwok.simplevlcplayer.fragments.LocalPlayerFragment;
import com.masterwok.simplevlcplayer.fragments.RendererPlayerFragment;

public class MediaPlayerActivity
        extends InjectableAppCompatActivity {

    public LocalPlayerFragment localPlayerFragment = new LocalPlayerFragment();
    public RendererPlayerFragment rendererPlayerFragment = new RendererPlayerFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_media_player);

//        showLocalPlayerFragment();
        showRendererPlayerFragment();
    }

    private void showRendererPlayerFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.framelayout_fragment_container, rendererPlayerFragment)
                .commit();
    }

    private void showLocalPlayerFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.framelayout_fragment_container, localPlayerFragment)
                .commit();
    }

}
