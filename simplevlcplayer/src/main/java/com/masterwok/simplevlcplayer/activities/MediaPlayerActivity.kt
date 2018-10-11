package com.masterwok.simplevlcplayer.activities

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.masterwok.simplevlcplayer.R
import com.masterwok.simplevlcplayer.dagger.injectors.InjectableAppCompatActivity
import com.masterwok.simplevlcplayer.fragments.CastPlayerFragment
import com.masterwok.simplevlcplayer.fragments.LocalPlayerFragment
import com.masterwok.simplevlcplayer.services.MediaPlayerService
import com.masterwok.simplevlcplayer.services.binders.MediaPlayerServiceBinder


class MediaPlayerActivity : InjectableAppCompatActivity() {

    companion object {
        @JvmStatic
        val MediaUri = "extra.mediauri"
        @JvmStatic
        val SubtitleUri = "extra.subtitleuri"
        @JvmStatic
        val SubtitleDestinationUri = "extra.subtitledestinationuri"
        @JvmStatic
        val SubtitleLanguageCode = "extra.subtitlelanguagecode"
        @JvmStatic
        val OpenSubtitlesUserAgent = "extra.useragent"
    }

    private var mediaController: MediaControllerCompat? = null
    private var mediaPlayerServiceBinder: MediaPlayerServiceBinder? = null
    private var localPlayerFragment: LocalPlayerFragment? = null
    private var castPlayerFragment: CastPlayerFragment? = null

    private val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return

            when (action) {
                MediaPlayerService.RendererClearedAction -> showLocalPlayerFragment()
                MediaPlayerService.RendererSelectionAction -> showCastPlayerFragment()
            }
        }
    }

    private val mediaPlayerServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            mediaPlayerServiceBinder = iBinder as MediaPlayerServiceBinder

            registerMediaController(iBinder)

            if (mediaPlayerServiceBinder?.selectedRendererItem == null) {
                showLocalPlayerFragment()
            } else {
                showCastPlayerFragment()
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            mediaPlayerServiceBinder = null

            mediaController?.unregisterCallback(controllerCallback)
        }
    }

    private fun registerMediaController(serviceBinder: MediaPlayerServiceBinder?) {
        if (serviceBinder == null) {
            return
        }

        mediaController = MediaControllerCompat(
                this,
                serviceBinder.mediaSession!!
        ).apply {
            registerCallback(controllerCallback)
        }

        MediaControllerCompat.setMediaController(this, mediaController)
    }

    private val controllerCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            localPlayerFragment?.configure(state)
            castPlayerFragment?.configure(state)
        }
    }

    private fun getLocalPlayerFragment(): LocalPlayerFragment = supportFragmentManager
            .findFragmentByTag(LocalPlayerFragment.Tag) as? LocalPlayerFragment
            ?: LocalPlayerFragment.createInstance(
                    mediaUri = intent.getParcelableExtra(MediaUri)
                    , subtitleUri = intent.getParcelableExtra(SubtitleUri)
                    , subtitleDestinationUri = intent.getParcelableExtra(SubtitleDestinationUri)
                    , openSubtitlesUserAgent = intent.getStringExtra(OpenSubtitlesUserAgent)
                    , subtitleLanguageCode = intent.getStringExtra(SubtitleLanguageCode)
            )

    private fun getCastPlayerFragment(): CastPlayerFragment = supportFragmentManager
            .findFragmentByTag(CastPlayerFragment.Tag) as? CastPlayerFragment
            ?: CastPlayerFragment.createInstance(
                    mediaUri = intent.getParcelableExtra(MediaUri)
                    , subtitleUri = intent.getParcelableExtra(SubtitleUri)
                    , subtitleDestinationUri = intent.getParcelableExtra(SubtitleDestinationUri)
                    , openSubtitlesUserAgent = intent.getStringExtra(OpenSubtitlesUserAgent)
                    , subtitleLanguageCode = intent.getStringExtra(SubtitleLanguageCode)
            )

    private fun showFragment(
            fragment: Fragment
            , tag: String
    ) = supportFragmentManager
            .beginTransaction()
            .replace(R.id.framelayout_fragment_container, fragment, tag)
            .commit()

    private fun showLocalPlayerFragment() {
        castPlayerFragment = null
        localPlayerFragment = getLocalPlayerFragment()

        showFragment(localPlayerFragment!!, LocalPlayerFragment.Tag)
    }

    private fun showCastPlayerFragment() {
        localPlayerFragment = null
        castPlayerFragment = getCastPlayerFragment()

        showFragment(castPlayerFragment!!, CastPlayerFragment.Tag)
    }

    private fun registerRendererBroadcastReceiver() = LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(broadCastReceiver, IntentFilter().apply {
                addAction(MediaPlayerService.RendererClearedAction)
                addAction(MediaPlayerService.RendererSelectionAction)
            })

    private fun bindMediaPlayerService() = bindService(
            Intent(applicationContext, MediaPlayerService::class.java)
            , mediaPlayerServiceConnection
            , Context.BIND_AUTO_CREATE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_media_player)
    }

    override fun onStart() {
        super.onStart()

        bindMediaPlayerService()
        registerRendererBroadcastReceiver()

        startService(Intent(applicationContext, MediaPlayerService::class.java))
    }

    override fun onStop() {
        unbindService(mediaPlayerServiceConnection)

        mediaController?.unregisterCallback(controllerCallback)

        LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(broadCastReceiver)

        castPlayerFragment = null

        super.onStop()
    }

    override fun onBackPressed() {
        // Always ensure that we stop the media player service when navigating back.
        stopService(Intent(applicationContext, MediaPlayerService::class.java))

        super.onBackPressed()
    }


}