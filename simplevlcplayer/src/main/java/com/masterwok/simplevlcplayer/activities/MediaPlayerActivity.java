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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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

    /**
     * Get the playback state (i.e. PlaybackStateCompat.STATE_PLAYING) of the media.
     *
     * @return The playback state.
     */
    private int getPlaybackStateCompat() {
        return mediaController
                .getPlaybackState()
                .getState();
    }

    /**
     * Get the length of the media from the playback state.
     *
     * @return The length of the media in milliseconds.
     */
    private long getMediaLength() {
        PlaybackStateCompat playbackState = mediaController.getPlaybackState();
        Bundle extras = playbackState.getExtras();

        return extras == null
                ? 0
                : extras.getLong(VlcMediaPlayerSession.LengthExtra);
    }

    /**
     * Prepare the media for the provided video and subtitle files.
     *
     * @param videoFilePath    The path to the video file.
     * @param subtitleFilePath The path to the subtitle file.
     */
    private void prepareMedia(
            String videoFilePath,
            String subtitleFilePath
    ) {
        if (transportControls == null) {
            return;
        }

        Bundle bundle = new Bundle();

        bundle.putString(
                MediaPlayerActivity.SubtitlePathExtra,
                subtitleFilePath
        );

        transportControls.prepareFromUri(
                Uri.fromFile(new File(videoFilePath)),
                bundle
        );
    }

    @Override
    public void onRendererUpdate(RendererItem rendererItem) {
        setRenderer(rendererItem);
    }

    private void setLocalRenderer() {
        mediaPlayerSession.setRenderer(
                mediaPlayerComponent.getMediaSurfaceView(),
                mediaPlayerComponent.getSubtitleSurfaceView(),
                mediaPlayerComponent
        );
    }


    /**
     * Set the current renderer item. If the renderer is null, then
     * local playback is used.
     *
     * @param rendererItem The renderer item.
     */
    private void setRenderer(RendererItem rendererItem) {
        // No renderer selected, set local renderer.
        if (rendererItem == null) {
            setLocalRenderer();
            return;
        }

        mediaPlayerSession.setRenderer(rendererItem);
    }


}
