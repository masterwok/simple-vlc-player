package com.masterwok.demosimplevlcplayer.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;

import com.masterwok.demosimplevlcplayer.R;
import com.masterwok.simplevlcplayer.VlcOptionsProvider;
import com.masterwok.simplevlcplayer.activities.MediaPlayerActivity;
import com.nononsenseapps.filepicker.FilePickerActivity;


/**
 * Initial/launcher activity of the demo application.
 */
public class MainActivity extends AppCompatActivity {

    private static final int OpenDocumentRequestCode = 32106;

    private AppCompatButton buttonPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        bindViewComponents();
        subscribeToViewComponents();


        // VlcOptionsProvider can be used to provide LibVlc initialization options.
        VlcOptionsProvider
                .getInstance()
                .setOptions(
                        // The VlcOptionsProvider.Builder can be used to override only a
                        // few options rather than rebuilding them yourself.
                        new VlcOptionsProvider.Builder(this)
                                .setVerbose(true)
                                .build()
                );
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

            showOpenDocumentActivity();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Re-enable play button (prevent bounce)
        buttonPlay.setEnabled(true);
    }

    /**
     * Show the activity for picking file.
     */
    private void showOpenDocumentActivity() {
        Intent intent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Use storage access framework
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("*/*");
        } else {
            // Fallback to external file picker.
            intent = new Intent(this, FilePickerActivity.class);
            intent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
            intent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
            intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
            intent.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
        }

        startActivityForResult(intent, OpenDocumentRequestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != OpenDocumentRequestCode
                || resultCode != Activity.RESULT_OK
                || data == null) {
            return;
        }

        startMediaPlayerActivity(
                data.getData(),
                null
        );
    }

    /**
     * Start the simple-vlc-player media player activity. Subtitle must be local
     * file Uri as it appears libVLC does not support adding subtitle using a
     * FileDescriptor (like Media instance).
     *
     * @param videoUri    The selected video URI.
     * @param subtitleUri The selected subtitle URI (must be local file URI).
     */
    @SuppressWarnings("SameParameterValue")
    private void startMediaPlayerActivity(Uri videoUri, Uri subtitleUri) {
        Intent intent = new Intent(this, MediaPlayerActivity.class);

        // Example of using an http resource.
//        intent.putExtra(MediaPlayerActivity.MediaUri, Uri.parse(
//                "http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4"
////                "http://download.blender.org/peach/bigbuckbunny_movies/big_buck_bunny_1080p_stereo.avi"
//        ));

        intent.putExtra(MediaPlayerActivity.MediaUri, videoUri);
        intent.putExtra(MediaPlayerActivity.SubtitleUri, subtitleUri);

        startActivity(intent);
    }

}
