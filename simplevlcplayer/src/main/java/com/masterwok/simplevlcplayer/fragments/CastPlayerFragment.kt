package com.masterwok.simplevlcplayer.fragments

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.BundleCompat
import android.support.v4.app.Fragment
import android.support.v4.media.session.PlaybackStateCompat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.masterwok.simplevlcplayer.R
import com.masterwok.simplevlcplayer.common.AndroidJob
import com.masterwok.simplevlcplayer.common.extensions.setColor
import com.masterwok.simplevlcplayer.common.utils.ResourceUtil
import com.masterwok.simplevlcplayer.components.PlayerControlComponent
import com.masterwok.simplevlcplayer.contracts.MediaPlayer
import com.masterwok.simplevlcplayer.services.binders.MediaPlayerServiceBinder
import kotlinx.android.synthetic.main.fragment_player_cast.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch


internal class CastPlayerFragment : MediaPlayerServiceFragment()
        , PlayerControlComponent.Callback
        , MediaPlayer.Callback {

    private val mediaUri: Uri get() = arguments!!.getParcelable(MediaUriKey)

    private lateinit var progressBar: ProgressBar

    private val rootJob: AndroidJob = AndroidJob(lifecycle)

    companion object {

        const val Tag = "tag.castplayerfragment"

        private const val MediaUriKey = "bundle.mediauri"
        private const val SubtitleUriKey = "bundle.subtitleuri"
        private const val SubtitleDestinationUriKey = "bundle.subtitledestinationuri"
        private const val SubtitleLanguageCodeKey = "bundle.subtitlelanguagecode"
        private const val OpenSubtitlesUserAgentKey = "bundle.useragent"

        @JvmStatic
        fun createInstance(
                mediaUri: Uri
                , subtitleUri: Uri?
                , subtitleDestinationUri: Uri
                , subtitleLanguageCode: String
                , openSubtitlesUserAgent: String
        ): CastPlayerFragment = CastPlayerFragment().apply {
            arguments = Bundle().apply {
                putParcelable(MediaUriKey, mediaUri)
                putParcelable(SubtitleUriKey, subtitleUri)
                putParcelable(SubtitleDestinationUriKey, subtitleDestinationUri)
                putString(SubtitleLanguageCodeKey, subtitleLanguageCode)
                putString(OpenSubtitlesUserAgentKey, openSubtitlesUserAgent)
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(
            R.layout.fragment_player_cast,
            container,
            false
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initProgressBar()
        subscribeToViewComponents()
    }

    override fun onStart() {
        super.onStart()

        serviceBinder?.callback = this
    }

    override fun onPause() {
        serviceBinder?.callback = null

        super.onPause()
    }

    private fun subscribeToViewComponents() {
        componentPlayerControl.registerCallback(this)
    }

    override fun onServiceConnected() {
        serviceBinder?.callback = this

        startPlayback()
    }

    private fun startPlayback() {
        if (serviceBinder?.isPlaying == true) {
            return
        }

        serviceBinder?.setMedia(requireContext(), mediaUri)
        serviceBinder?.play()
    }

    fun configure(state: PlaybackStateCompat) = componentPlayerControl.configure(
            state.state == PlaybackStateCompat.STATE_PLAYING,
            state.position,
            state.bufferedPosition
    )

    override fun onPlayPauseButtonClicked() {
        serviceBinder?.togglePlayback()
    }

    override fun onCastButtonClicked() = RendererItemDialogFragment().show(
            fragmentManager,
            RendererItemDialogFragment.Tag
    )

    override fun onProgressChanged(progress: Int) {
        serviceBinder?.setProgress(progress)
    }

    override fun onProgressChangeStarted() {
        // Intentionally left blank..
    }

    override fun onSubtitlesButtonClicked() {
        // Cast subtitles are not yet supported..
    }

    override fun onPlayerOpening() {
        // Intentionally left blank..
    }

    override fun onPlayerSeekStateChange(canSeek: Boolean) {
        // Intentionally left blank..
    }

    override fun onPlayerPlaying() {
        // Intentionally left blank..
    }

    override fun onPlayerPaused() {
        // Intentionally left blank..
    }

    override fun onPlayerStopped() {
        serviceBinder?.callback = null
        activity?.finish()
    }

    override fun onPlayerEndReached() {
        serviceBinder?.callback = null
        activity?.finish()
    }

    override fun onPlayerError() {
        // Intentionally left blank..
    }

    override fun onPlayerTimeChange(timeChanged: Long) {
        // Intentionally left blank..
    }

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

    override fun onPlayerPositionChanged(positionChanged: Float) {
        // Intentionally left blank..
    }

    override fun onSubtitlesCleared() {
        // Intentionally left blank..
    }

    private fun initProgressBar() {
        val context = requireContext()

        progressBar = ProgressBar(
                context
                , null
                , android.R.attr.progressBarStyleLarge
        ).apply {
            visibility = View.GONE
            setColor(R.color.progress_bar_spinner)
        }

        val params = FrameLayout.LayoutParams(
                ResourceUtil.getDimenDp(context, R.dimen.player_spinner_width),
                ResourceUtil.getDimenDp(context, R.dimen.player_spinner_height)
        ).apply {
            gravity = Gravity.CENTER
        }

        (view as ViewGroup).addView(
                progressBar
                , params
        )
    }
}
