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
import java.util.List;


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
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, true);
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

        final List<Uri> selectedFiles = Utils.getSelectedFilesFromResult(data);

        File videoFile = Utils.getFileForUri(
                selectedFiles.get(0)
        );

        // If a second file was selected, assume it was a subtitle file.
        startMediaPlayerActivity(
                Uri.fromFile(videoFile),
                selectedFiles.size() > 1
                        ? filterSubtitleSelection(Utils.getFileForUri(selectedFiles.get(1)))
                        : null
        );
    }

    /**
     * For the sake of the demo, assume that the second file selected is the subtitle.
     *
     * @param subtitleFile The selected subtitle file.
     * @return If valid subtitle file type, the file uri. Else, null.
     */
    private Uri filterSubtitleSelection(File subtitleFile) {
        if (subtitleFile == null) {
            return null;
        }

        final String path = subtitleFile
                .getAbsolutePath()
                .toLowerCase();

        return path.endsWith("vtt") || path.endsWith("srt")
                ? Uri.fromFile(subtitleFile)
                : null;
    }

    /**
     * Start the simple-vlc-player media player activity. This method
     * does not ensure activity is started on main thread.
     *
     * @param videoUri    The selected video URI.
     * @param subtitleUri The selected subtitle URI.
     */
    private void startMediaPlayerActivity(Uri videoUri, Uri subtitleUri) {
        Intent intent = new Intent(this, MediaPlayerActivity.class);

        intent.putExtra(MediaPlayerActivity.MediaUri, videoUri);
        intent.putExtra(MediaPlayerActivity.SubtitleUri, subtitleUri);
//        intent.putExtra(MediaPlayerActivity.MediaUri, Uri.parse(
////                "http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4"
//                "http://download.blender.org/peach/bigbuckbunny_movies/big_buck_bunny_1080p_stereo.avi"
//        ));

        startActivity(intent);
    }

}
