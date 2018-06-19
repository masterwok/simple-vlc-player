package com.masterwok.simplevlcplayer.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.masterwok.simplevlcplayer.R;
import com.masterwok.simplevlcplayer.dagger.injectors.InjectableAppCompatActivity;
import com.masterwok.simplevlcplayer.fragments.LocalPlayerFragment;
import com.masterwok.simplevlcplayer.fragments.RendererPlayerFragment;
import com.masterwok.simplevlcplayer.services.MediaPlayerService;

public class MediaPlayerActivity
        extends InjectableAppCompatActivity {

    public LocalPlayerFragment localPlayerFragment = new LocalPlayerFragment();
    public RendererPlayerFragment rendererPlayerFragment = new RendererPlayerFragment();
    private MediaPlayerService.Binder serviceBinder;

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            serviceBinder = (MediaPlayerService.Binder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceBinder = null;
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_media_player);

        startMediaPlayerService();

//        showLocalPlayerFragment();
        showRendererPlayerFragment();
    }

    @Override
    protected void onStart() {
        bindService();
        super.onStart();
    }

    @Override
    protected void onStop() {
        unbindService();
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

    private void stopMediaPlayerService() {
        stopService(new Intent(
                getApplicationContext(),
                MediaPlayerService.class
        ));
    }

    private void bindService() {
        bindService(
                new Intent(
                        getApplicationContext(),
                        MediaPlayerService.class
                ),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );

    }

    private void unbindService() {
        unbindService(serviceConnection);
    }

}
