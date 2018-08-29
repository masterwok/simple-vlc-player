package com.masterwok.simplevlcplayer.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.provider.DocumentFile
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.masterwok.simplevlcplayer.R
import com.masterwok.simplevlcplayer.common.AndroidJob
import com.masterwok.simplevlcplayer.common.extensions.setColor
import com.masterwok.simplevlcplayer.common.utils.ResourceUtil
import com.masterwok.simplevlcplayer.components.PlayerControlComponent
import com.masterwok.simplevlcplayer.contracts.MediaPlayer
import com.masterwok.simplevlcplayer.dagger.injectors.InjectableFragment
import com.masterwok.simplevlcplayer.services.MediaPlayerService
import com.masterwok.simplevlcplayer.services.binders.MediaPlayerServiceBinder
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

abstract class BasePlayerFragment : InjectableFragment()
        , PlayerControlComponent.Callback
        , MediaPlayer.Callback {

    companion object {
        const val MediaUri = "extra.mediauri"
        const val SubtitleUri = "extra.subtitleuri"
        const val OpenSubtitlesUserAgent = "extra.useragent"
        const val SubtitleDestinationUri = "extra.subtitledestinationuri"
        const val SubtitleLanguageCode = "extra.subtitlelanguagecode"
    }

    protected var serviceBinder: MediaPlayerServiceBinder? = null

    private var mediaController: MediaControllerCompat? = null
    private var subtitleDestinationUri: Uri? = null
    private var openSubtitlesUserAgent: String? = null
    private var subtitleLanguageCode: String? = null

    protected var subtitleUri: Uri? = null
    protected var mediaUri: Uri? = null

    private lateinit var progressBar: ProgressBar

    private val rootJob: AndroidJob = AndroidJob(lifecycle)

    protected abstract fun configure(
            isPlaying: Boolean
            , time: Long
            , length: Long
    )

    protected abstract fun onConnected()

    protected abstract fun onDisconnected()

    private val mediaPlayerServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            this@BasePlayerFragment.serviceBinder = iBinder as MediaPlayerServiceBinder

            serviceBinder?.callback = this@BasePlayerFragment

            onConnected()

            registerMediaController(serviceBinder)
        }


        override fun onServiceDisconnected(componentName: ComponentName) {
            serviceBinder = null

            onDisconnected()
        }
    }

    private val controllerCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            configure(
                    state.state == PlaybackStateCompat.STATE_PLAYING,
                    state.position,
                    state.bufferedPosition
            )
        }
    }

    private fun registerMediaController(serviceBinder: MediaPlayerServiceBinder?) {
        val activity = activity

        if (activity == null || serviceBinder == null) {
            return
        }

        mediaController = MediaControllerCompat(
                activity,
                serviceBinder.mediaSession!!
        ).apply {
            registerCallback(controllerCallback)
        }

        MediaControllerCompat.setMediaController(activity, mediaController)
    }

    override fun onStart() {
        super.onStart()

        bindMediaPlayerService()
    }

    override fun onStop() {
        serviceBinder?.callback = null
        mediaController?.unregisterCallback(controllerCallback)
        unbindMediaPlayerService()

        super.onStop()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        readIntent()
    }

    private fun readIntent() {
        val intent = activity?.intent

        mediaUri = intent?.getParcelableExtra(MediaUri)
        subtitleUri = intent?.getParcelableExtra(SubtitleUri);
        subtitleDestinationUri = intent?.getParcelableExtra(SubtitleDestinationUri);
        openSubtitlesUserAgent = intent?.getStringExtra(OpenSubtitlesUserAgent);
        subtitleLanguageCode = intent?.getStringExtra(SubtitleLanguageCode);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initProgressBar()
    }

    private fun initProgressBar() {
        progressBar = ProgressBar(
                context
                , null
                , android.R.attr.progressBarStyleLarge
        ).apply {
            visibility = View.GONE
            setColor(R.color.progress_bar_spinner)
        }

        val params = FrameLayout.LayoutParams(
                ResourceUtil.getDimenDp(context!!, R.dimen.player_spinner_width),
                ResourceUtil.getDimenDp(context!!, R.dimen.player_spinner_height)
        ).apply {
            gravity = Gravity.CENTER
        }

        (view as ViewGroup).addView(
                progressBar
                , params
        )
    }

    private fun unbindMediaPlayerService() {
        activity?.unbindService(mediaPlayerServiceConnection)
        serviceBinder = null
    }

    private fun bindMediaPlayerService() = activity?.bindService(
            getMediaPlayerIntent()
            , mediaPlayerServiceConnection
            , Context.BIND_AUTO_CREATE
    )

    private fun getMediaPlayerIntent(): Intent? {
        if (activity == null) {
            return null
        }

        return Intent(activity, MediaPlayerService::class.java)
    }


    override fun onPlayPauseButtonClicked() {
        serviceBinder?.togglePlayback()
    }

    override fun onCastButtonClicked() = RendererItemDialogFragment().show(
            fragmentManager,
            RendererItemDialogFragment.Tag
    )


    override fun onSubtitlesButtonClicked() {
        val fragmentManager = fragmentManager ?: return

        val mediaName = if (URLUtil.isContentUrl(mediaUri.toString()))
            DocumentFile.fromSingleUri(context, mediaUri).name
        else {
            mediaUri?.lastPathSegment
        }

        SubtitlesDialogFragment.createInstance(
                mediaName!!
                , subtitleUri
                , openSubtitlesUserAgent
                , subtitleLanguageCode
                , subtitleDestinationUri
        ).show(fragmentManager, SubtitlesDialogFragment.Tag)
    }

    override fun onProgressChangeStarted() {
        serviceBinder?.pause()
    }

    override fun onProgressChanged(progress: Int) {
        serviceBinder?.setProgress(progress)
        serviceBinder?.play()
    }

    override fun onPlayerOpening() {}

    override fun onBuffering(buffering: Float) {
        if (buffering == 100f) {
            launch(UI, parent = rootJob) { progressBar.visibility = View.GONE }
            return
        }

        if (progressBar.visibility == View.VISIBLE) {
            return
        }

        launch(UI, parent = rootJob) { progressBar.visibility = View.VISIBLE }
    }

    override fun onPlayerSeekStateChange(canSeek: Boolean) {}

    override fun onPlayerPlaying() {}

    override fun onPlayerPaused() {}

    override fun onPlayerStopped() {}

    override fun onPlayerEndReached() {
        activity?.finish()
    }

    override fun onPlayerError() {}

    override fun onPlayerTimeChange(timeChanged: Long) {}

    override fun onPlayerPositionChanged(positionChanged: Float) {}

    override fun onSubtitlesCleared() {}
}