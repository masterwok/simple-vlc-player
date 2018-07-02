package com.masterwok.simplevlcplayer.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;

import com.masterwok.simplevlcplayer.R;
import com.masterwok.simplevlcplayer.dagger.injectors.InjectableAppCompatActivity;
import com.masterwok.simplevlcplayer.fragments.BasePlayerFragment;
import com.masterwok.simplevlcplayer.fragments.LocalPlayerFragment;
import com.masterwok.simplevlcplayer.fragments.RendererPlayerFragment;
import com.masterwok.simplevlcplayer.services.MediaPlayerService;

public class MediaPlayerActivity
        extends InjectableAppCompatActivity {

    public static final String MediaUri = BasePlayerFragment.MediaUri;

    public LocalPlayerFragment localPlayerFragment;
    public RendererPlayerFragment rendererPlayerFragment;

    private final BroadcastReceiver broadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action == null) {
                return;
            }

            switch (action) {
                case MediaPlayerService.RendererClearedAction:
                    showLocalPlayerFragment();
                    break;
                case MediaPlayerService.RendererSelectionAction:
                    showRendererPlayerFragment();
                    break;
            }
        }
    };


    private void showLocalPlayerFragment() {
        rendererPlayerFragment = null;
        localPlayerFragment = new LocalPlayerFragment();
        showFragment(localPlayerFragment);
    }

    private void showRendererPlayerFragment() {
        localPlayerFragment = null;
        rendererPlayerFragment = new RendererPlayerFragment();
        showFragment(rendererPlayerFragment);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_media_player);

        if (savedInstanceState != null) {
            return;
        }

        showLocalPlayerFragment();
    }

    @Override
    protected void onStart() {
        super.onStart();

        registerRendererBroadcastReceiver();

        startService(getMediaPlayerServiceIntent());
    }

    public Intent getMediaPlayerServiceIntent() {
        return new Intent(
                getApplicationContext(),
                MediaPlayerService.class
        );
    }

    @Override
    public void onBackPressed() {
        // Always ensure that we stop the media player service when navigating back.
        stopService(getMediaPlayerServiceIntent());

        super.onBackPressed();
    }

    private void registerRendererBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MediaPlayerService.RendererClearedAction);
        intentFilter.addAction(MediaPlayerService.RendererSelectionAction);

        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(broadCastReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(broadCastReceiver);

        rendererPlayerFragment = null;

        super.onStop();
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.framelayout_fragment_container, fragment)
                .commit();
    }

}
