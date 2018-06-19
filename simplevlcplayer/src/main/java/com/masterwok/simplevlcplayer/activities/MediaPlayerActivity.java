package com.masterwok.simplevlcplayer.activities;

import android.os.Bundle;

import com.masterwok.simplevlcplayer.R;
import com.masterwok.simplevlcplayer.fragments.LocalPlayerFragment;

public class MediaPlayerActivity
        extends InjectableAppCompatActivity {

    public LocalPlayerFragment localPlayerFragment = new LocalPlayerFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_media_player);

        showLocalPlayerFragment();
    }

    private void showLocalPlayerFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.framelayout_fragment_container, localPlayerFragment)
                .commit();
    }

}
