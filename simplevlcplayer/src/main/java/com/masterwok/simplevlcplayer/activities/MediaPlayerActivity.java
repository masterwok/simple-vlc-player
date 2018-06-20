package com.masterwok.simplevlcplayer.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.masterwok.simplevlcplayer.R;
import com.masterwok.simplevlcplayer.dagger.injectors.InjectableAppCompatActivity;
import com.masterwok.simplevlcplayer.fragments.LocalPlayerFragment;
import com.masterwok.simplevlcplayer.fragments.RendererPlayerFragment;
import com.masterwok.simplevlcplayer.services.MediaPlayerService;

public class MediaPlayerActivity
        extends InjectableAppCompatActivity {

    public LocalPlayerFragment localPlayerFragment = new LocalPlayerFragment();
    public RendererPlayerFragment rendererPlayerFragment = new RendererPlayerFragment();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_media_player);

        startMediaPlayerService();

        showLocalPlayerFragment();
    }

    @Override
    protected void onStart() {
        super.onStart();

        registerRendererBroadcastReceiver();
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

        super.onStop();
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

    private void startMediaPlayerService() {
        startService(new Intent(
                getApplicationContext(),
                MediaPlayerService.class
        ));
    }

}
