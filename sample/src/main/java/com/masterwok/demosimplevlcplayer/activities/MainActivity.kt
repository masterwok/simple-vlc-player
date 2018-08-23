package com.masterwok.demosimplevlcplayer.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import com.masterwok.demosimplevlcplayer.R
import com.masterwok.demosimplevlcplayer.extensions.appCompatRequestPermissions
import com.masterwok.demosimplevlcplayer.extensions.isPermissionGranted
import com.masterwok.opensubtitlesandroid.SubtitleLanguage
import com.masterwok.opensubtitlesandroid.services.OpenSubtitlesService
import com.masterwok.simplevlcplayer.VlcOptionsProvider
import com.masterwok.simplevlcplayer.activities.MediaPlayerActivity
import com.nononsenseapps.filepicker.FilePickerActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {
    companion object {
        private const val OpenDocumentRequestCode = 32106
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        requestWriteExternalStoragePermission()
        bindViewComponents()
        subscribeToViewComponents()


        // VlcOptionsProvider can be used to provide LibVlc initialization options.
        VlcOptionsProvider
                .getInstance().options = VlcOptionsProvider.Builder(this)
                .setVerbose(true)
                .build()
    }

    private fun requestWriteExternalStoragePermission() {
        if (isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            return
        }

        appCompatRequestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                , 0
        )
    }

    override fun onRequestPermissionsResult(
            requestCode: Int
            , permissions: Array<out String>
            , grantResults: IntArray
    ) {
        val permissionWasDenied = grantResults.any { it != PackageManager.PERMISSION_GRANTED }

        if (permissionWasDenied) {
            // Required to read local subtitles in external storage.
            throw Exception("READ_EXTERNAL_STORAGE permission must be granted to run demo.")
        }
    }

    /**
     * Bind view components to private fields.
     */
    private fun bindViewComponents() {
        //        buttonPlay = findViewById(R.id.button_play);
    }

    /**
     * Subscribe to any bound view components.
     */
    private fun subscribeToViewComponents() {
        buttonPlay.setOnClickListener { view ->
            // Disable play button (prevent bounce/re-enabled in onStart())
            view.isEnabled = false

            showOpenDocumentActivity()
        }
    }

    override fun onStart() {
        super.onStart()

        // Re-enable play button (prevent bounce)
        buttonPlay.isEnabled = true
    }

    /**
     * Show the activity for picking file.
     */
    private fun showOpenDocumentActivity() {
        val intent: Intent

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Use storage access framework
            intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"
            }
        } else {
            // Fallback to external file picker.
            intent = Intent(this, FilePickerActivity::class.java).apply {
                putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)
                putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false)
                putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE)
                putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().path)
            }
        }

        startActivityForResult(intent, OpenDocumentRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != OpenDocumentRequestCode
                || resultCode != Activity.RESULT_OK
                || data ==
                null) {
            return
        }

        startMediaPlayerActivity(data.data, null)
    }

    /**
     * Start the simple-vlc-player media player activity. Subtitle must be local
     * file Uri as it appears libVLC does not support adding subtitle using a
     * FileDescriptor (like Media instance).
     *
     * @param videoUri    The selected video URI.
     * @param subtitleUri The selected subtitle URI (must be local file URI).
     */
    private fun startMediaPlayerActivity(videoUri: Uri?, subtitleUri: Uri?) =
            startActivity(Intent(this, MediaPlayerActivity::class.java).apply {

                val file = File("/storage/emulated/0/Download/derp.srt")

                val isFile = file.isFile

                val tmp = Uri.fromFile(file)
                putExtra(MediaPlayerActivity.SubtitleUri, tmp)

                putExtra(MediaPlayerActivity.MediaUri, videoUri)
//                putExtra(MediaPlayerActivity.SubtitleUri, subtitleUri)
                putExtra(MediaPlayerActivity.SubtitleDestinationUri, Uri.fromFile(cacheDir))

                // This should be the User-Agent you registered with opensubtitles.org
                // See: http://trac.opensubtitles.org/projects/opensubtitles/wiki/DevReadFirst
                putExtra(MediaPlayerActivity.OpenSubtitlesUserAgent, OpenSubtitlesService.TemporaryUserAgent)
                putExtra(MediaPlayerActivity.SubtitleLanguageCode, SubtitleLanguage.English)
            })
}
