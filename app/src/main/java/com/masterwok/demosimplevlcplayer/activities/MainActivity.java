package com.masterwok.demosimplevlcplayer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;

import com.masterwok.demosimplevlcplayer.R;
import com.masterwok.demosimplevlcplayer.helpers.FileHelper;
import com.masterwok.simplevlcplayer.activities.MediaPlayerActivity;
import com.masterwok.simplevlcplayer.utils.ThreadUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


/**
 * Initial/launcher activity of the demo application.
 */
public class MainActivity extends AppCompatActivity {

    private AppCompatButton buttonPlay;

    private File demoVideoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        demoVideoFile = new File(getCacheDir() + "/sample.mp4");

        bindViewComponents();
        subscribeToViewComponents();

        ThreadUtil.onBackground(this::copyRawVideoResourceToCache);
    }

    /**
     * A URI must be passed to the session when preparing media. For this reason,
     * we must copy the raw video resource that is included in this project to the
     * cache before it can be played.
     */
    private void copyRawVideoResourceToCache() {
        InputStream sampleInputStream = getResources()
                .openRawResource(R.raw.sample);

        try {
            FileHelper.copy(sampleInputStream, demoVideoFile);

            // Enable play button once copy is complete
            ThreadUtil.onMain(() -> buttonPlay.setEnabled(true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Bind view components to private fields.
     */
    private void bindViewComponents() {
        buttonPlay = findViewById(R.id.button_play);
    }

    /**
     * Subscribe to any bound view components.
     */
    private void subscribeToViewComponents() {
        buttonPlay.setOnClickListener(view -> {
            // Disable play button (prevent bounce/re-enabled in onStart())
            view.setEnabled(false);

            startMediaPlayerActivity(demoVideoFile);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Re-enable play button (prevent bounce)
        buttonPlay.setEnabled(true);
    }

    private static final int MediaPlayerRequestCode = 32106;

    /**
     * Start the simple-vlc-player media player activity. This method
     * does not ensure activity is started on main thread.
     */
    private void startMediaPlayerActivity(File videoFile) {
        Intent intent = new Intent(this, MediaPlayerActivity.class);

        intent.putExtra(
                MediaPlayerActivity.RequestCodeExtra,
                MediaPlayerRequestCode
        );

        intent.putExtra(
                MediaPlayerActivity.VideoPathExtra,
                videoFile.getPath()
        );

        // No subtitle file for demo..
//        intent.putExtra(
//                MediaPlayerActivity.SubtitlePathExtra,
//                (String) null
//        );

        intent.putExtra(
                MediaPlayerActivity.PlaybackPositionExtra,
                0
        );

        // We don't pass any options in the demo..
        intent.putExtra(
                MediaPlayerActivity.VlcOptions,
                new String[0]
        );

        startActivityForResult(intent, MediaPlayerRequestCode);
    }

    private static final String LogTag = "tag.mainactivity";

    /**
     * This method demonstrates how to get the final playback position of the
     * media played.
     *
     * @param requestCode The request code that started the media player.
     * @param resultCode  Not needed.
     * @param intent      The intent containing the resultant playback position.
     */
    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent intent
    ) {
        super.onActivityResult(requestCode, resultCode, intent);

        // These are not the request codes you're looking for, do nothing.
        if (requestCode != MediaPlayerRequestCode
                || intent == null) {
            return;
        }

        long position = intent.getLongExtra(MediaPlayerActivity.PlaybackPositionExtra, -1);

        Log.d(LogTag, "Playback position result (ms): " + position);
    }
}
