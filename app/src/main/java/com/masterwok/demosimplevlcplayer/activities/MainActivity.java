package com.masterwok.demosimplevlcplayer.activities;

import android.app.Activity;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;

import com.masterwok.demosimplevlcplayer.R;
import com.masterwok.simplevlcplayer.activities.MediaPlayerActivity;
import com.masterwok.simplevlcplayer.utils.FileUtil;

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
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("*/*");

        startActivityForResult(intent, OpenDocumentRequestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != OpenDocumentRequestCode
                || resultCode != Activity.RESULT_OK
                || data == null) {
            return;
        }

        ClipData clipData = data.getClipData();

        if (clipData == null
                || clipData.getItemCount() == 0) {
            startMediaPlayerActivity(
                    data.getData(),
                    null
            );

            return;
        }

        startMediaPlayerActivity(
                clipData.getItemAt(1).getUri(),
                clipData.getItemCount() > 1
                        ? clipData.getItemAt(0).getUri()
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
//                "http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4"
////                "http://download.blender.org/peach/bigbuckbunny_movies/big_buck_bunny_1080p_stereo.avi"
//        ));

        // TODO: NEED TO GET CONTENT URIS FOR SUBTITLES WORKING
        // TODO: Seek partial files setTime fails when seeking past downloaded portion of file.

        startActivity(intent);
    }

}
