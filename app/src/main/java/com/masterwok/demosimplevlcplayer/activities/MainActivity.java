package com.masterwok.demosimplevlcplayer.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;

import com.masterwok.demosimplevlcplayer.R;
import com.masterwok.simplevlcplayer.activities.MediaPlayerActivity;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;

import java.io.File;


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
        Intent i = new Intent(this, FilePickerActivity.class);

        // Set these depending on your use case. These are the defaults.
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

        startActivityForResult(i, OpenDocumentRequestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != OpenDocumentRequestCode
                || resultCode != Activity.RESULT_OK
                || data == null) {
            return;
        }

        File file = Utils.getFileForUri(
                Utils.getSelectedFilesFromResult(data).get(0)
        );

        startMediaPlayerActivity(Uri.fromFile(file));
    }

    /**
     * Start the simple-vlc-player media player activity. This method
     * does not ensure activity is started on main thread.
     *
     * @param videoUri The selected video URI.
     */
    private void startMediaPlayerActivity(Uri videoUri) {
        Intent intent = new Intent(this, MediaPlayerActivity.class);

        intent.putExtra(MediaPlayerActivity.MediaUri, videoUri);

        startActivity(intent);
    }

}
