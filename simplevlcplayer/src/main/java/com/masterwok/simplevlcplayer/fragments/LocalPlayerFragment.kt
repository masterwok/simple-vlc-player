package com.masterwok.simplevlcplayer.fragments

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.BundleCompat
import android.support.v4.app.Fragment
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.masterwok.simplevlcplayer.R
import com.masterwok.simplevlcplayer.common.AndroidJob
import com.masterwok.simplevlcplayer.common.extensions.getName
import com.masterwok.simplevlcplayer.common.extensions.setColor
import com.masterwok.simplevlcplayer.common.utils.ResourceUtil
import com.masterwok.simplevlcplayer.components.PlayerControlComponent
import com.masterwok.simplevlcplayer.constants.SizePolicy
import com.masterwok.simplevlcplayer.contracts.MediaPlayer
import com.masterwok.simplevlcplayer.services.binders.MediaPlayerServiceBinder
import kotlinx.android.synthetic.main.fragment_player_local.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.videolan.libvlc.IVLCVout
import org.videolan.libvlc.Media


internal class LocalPlayerFragment : MediaPlayerServiceFragment()
        , PlayerControlComponent.Callback
        , MediaPlayer.Callback
        , IVLCVout.OnNewVideoLayoutListener {

    private val mediaUri: Uri get() = arguments!!.getParcelable(MediaUriKey)
    private val subtitleUri: Uri? get() = arguments!!.getParcelable(SubtitleUriKey)
    private val subtitleDestinationUri: Uri get() = arguments!!.getParcelable(SubtitleDestinationUriKey)
    private val subtitleLanguageCode: String get() = arguments!!.getString(SubtitleLanguageCodeKey)
    private val openSubtitlesUserAgent: String get() = arguments!!.getString(OpenSubtitlesUserAgentKey)

    private var sizePolicy: SizePolicy = SizePolicy.SURFACE_BEST_FIT
    private var mVideoHeight = 0
    private var mVideoWidth = 0
    private var mVideoVisibleHeight = 0
    private var mVideoVisibleWidth = 0
    private var mVideoSarNum = 0
    private var mVideoSarDen = 0
    private var setProvidedSubtitle = true
    private var resumeIsPlaying = true
    private var resumeLength: Long = 0
    private var resumeTime: Long = 0

    private val rootJob: AndroidJob = AndroidJob(lifecycle)
    private val handler = Handler()

    private lateinit var progressBar: ProgressBar

    companion object {

        const val Tag = "tag.localplayerfragment"

        private const val MediaUriKey = "bundle.mediauri"
        private const val SubtitleUriKey = "bundle.subtitleuri"
        private const val SubtitleDestinationUriKey = "bundle.subtitledestinationuri"
        private const val SubtitleLanguageCodeKey = "bundle.subtitlelanguagecode"
        private const val OpenSubtitlesUserAgentKey = "bundle.useragent"

        const val SetProvidedSubtitleKey = "bundle.setprovidedsubtitleonnextplayback"
        const val IsPlayingKey = "bundle.isplaying"
        const val LengthKey = "bundle.length"
        const val TimeKey = "bundle.time"

        @JvmStatic
        fun createInstance(
                mediaUri: Uri
                , subtitleUri: Uri?
                , subtitleDestinationUri: Uri
                , subtitleLanguageCode: String
                , openSubtitlesUserAgent: String
        ): LocalPlayerFragment = LocalPlayerFragment().apply {
            arguments = Bundle().apply {
                putParcelable(MediaUriKey, mediaUri)
                putParcelable(SubtitleUriKey, subtitleUri)
                putParcelable(SubtitleDestinationUriKey, subtitleDestinationUri)
                putString(SubtitleLanguageCodeKey, subtitleLanguageCode)
                putString(OpenSubtitlesUserAgentKey, openSubtitlesUserAgent)
            }
        }
    }

    private val surfaceLayoutListener = object : View.OnLayoutChangeListener {
        private val mRunnable = { updateVideoSurfaces() }

        override fun onLayoutChange(
                v: View,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
        ) {
            if (left != oldLeft
                    || top != oldTop
                    || right != oldRight
                    || bottom != oldBottom) {
                handler.removeCallbacks(mRunnable)
                handler.post(mRunnable)
            }
        }
    }

    private val becomingNoisyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent.action) {
                // Pause playback whenever the user pulls out ( ͡° ͜ʖ ͡°)
                serviceBinder?.pause()
            }
        }
    }

    private fun configureSubtitleSurface() = surfaceViewSubtitle.apply {
        setZOrderMediaOverlay(true)
        holder.setFormat(PixelFormat.TRANSLUCENT)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(
            R.layout.fragment_player_local,
            container,
            false
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initProgressBar()
        subscribeToViewComponents()
        configureSubtitleSurface()
    }

    private fun subscribeToViewComponents() {
        componentPlayerControl.registerCallback(this)
    }

    override fun onServiceConnected() {
        serviceBinder?.callback = this

        startPlayback()
    }

    override fun onResume() {
        super.onResume()

        context?.registerReceiver(
                becomingNoisyReceiver,
                IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        )
    }

    override fun onPause() {
        stopPlayback()

        serviceBinder?.callback = null

        context?.unregisterReceiver(becomingNoisyReceiver)

        super.onPause()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        updateVideoSurfaces()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val selectedSubtitleUri = serviceBinder?.selectedSubtitleUri

        super.onSaveInstanceState(outState.apply {
            putBoolean(SetProvidedSubtitleKey, selectedSubtitleUri === subtitleUri)
            putBoolean(IsPlayingKey, resumeIsPlaying)
            putLong(TimeKey, resumeTime)
            putLong(LengthKey, resumeLength)
        })
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (savedInstanceState == null) {
            return
        }

        setProvidedSubtitle = savedInstanceState.getBoolean(SetProvidedSubtitleKey)
        resumeIsPlaying = savedInstanceState.getBoolean(IsPlayingKey, true)
        resumeTime = savedInstanceState.getLong(TimeKey, 0)
        resumeLength = savedInstanceState.getLong(LengthKey, 0)

        configure(
                resumeIsPlaying,
                resumeTime,
                resumeLength
        )
    }

    private fun updateResumeState() {
        val activity = activity ?: return

        val playbackState = MediaControllerCompat
                .getMediaController(activity)
                .playbackState

        resumeIsPlaying = playbackState.state == PlaybackStateCompat.STATE_PLAYING
        resumeTime = playbackState.position
        resumeLength = playbackState.bufferedPosition
    }

    private fun stopPlayback() {
        surfaceViewSubtitle.removeOnLayoutChangeListener(surfaceLayoutListener)

        updateResumeState()
        serviceBinder?.stop()
        detachSurfaces()
    }

    private fun attachSurfaces() {
        if (serviceBinder?.vOut?.areViewsAttached() == true) {
            return
        }

        serviceBinder?.attachSurfaces(
                surfaceViewMedia
                , surfaceViewSubtitle
                , this
        )
    }

    private fun detachSurfaces() = serviceBinder?.detachSurfaces()

    private fun startPlayback() {
        surfaceViewMedia.addOnLayoutChangeListener(surfaceLayoutListener)

        attachSurfaces()
        updateVideoSurfaces()

        serviceBinder?.setMedia(
                requireContext()
                , mediaUri
        )

        if (setProvidedSubtitle) {
            serviceBinder?.setSubtitle(subtitleUri)
        } else {
            serviceBinder?.setSubtitle(serviceBinder?.selectedSubtitleUri)
        }

        if (resumeIsPlaying) {
            serviceBinder?.play()
        }
    }

    private fun configure(
            isPlaying: Boolean,
            time: Long,
            length: Long
    ) = componentPlayerControl.configure(
            isPlaying,
            time,
            length
    )

    fun configure(state: PlaybackStateCompat) = configure(
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
        serviceBinder?.play()
    }

    override fun onProgressChangeStarted() {
        serviceBinder?.pause()
    }

    override fun onSubtitlesButtonClicked() {
        val fragmentManager = fragmentManager ?: return

        SubtitlesDialogFragment.createInstance(
                mediaUri.getName(requireContext())
                , subtitleUri
                , openSubtitlesUserAgent
                , subtitleLanguageCode
                , subtitleDestinationUri
        ).show(fragmentManager, SubtitlesDialogFragment.Tag)
    }

    override fun onPlayerOpening() {
        // Intentionally left blank..
    }

    override fun onPlayerSeekStateChange(canSeek: Boolean) {
        if (!canSeek) {
            return
        }

        serviceBinder?.setTime(resumeTime)
    }

    override fun onPlayerPlaying() {
        // Intentionally left blank..
    }

    override fun onPlayerPaused() {
        // Intentionally left blank..
    }

    override fun onPlayerStopped() {
        // Intentionally left blank..
    }

    override fun onPlayerEndReached() {
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

    override fun onSubtitlesCleared() = startPlayback()

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

    private fun changeMediaPlayerLayout(displayW: Int, displayH: Int) {
        /* Change the video placement using the MediaPlayer API */
        when (sizePolicy) {
            SizePolicy.SURFACE_BEST_FIT -> {
                serviceBinder?.setAspectRatio(null)
                serviceBinder?.setScale(0f)
            }
            SizePolicy.SURFACE_FIT_SCREEN, SizePolicy.SURFACE_FILL -> {
                val videoTrack = serviceBinder?.currentVideoTrack ?: return
                val videoSwapped = videoTrack.orientation == Media.VideoTrack.Orientation.LeftBottom || videoTrack.orientation == Media.VideoTrack.Orientation.RightTop
                if (sizePolicy == SizePolicy.SURFACE_FIT_SCREEN) {
                    var videoW = videoTrack.width
                    var videoH = videoTrack.height

                    if (videoSwapped) {
                        val swap = videoW
                        videoW = videoH
                        videoH = swap
                    }
                    if (videoTrack.sarNum != videoTrack.sarDen)
                        videoW = videoW * videoTrack.sarNum / videoTrack.sarDen

                    val ar = videoW / videoH.toFloat()
                    val dar = displayW / displayH.toFloat()

                    val scale: Float = if (dar >= ar)
                        displayW / videoW.toFloat() /* horizontal */
                    else
                        displayH / videoH.toFloat() /* vertical */

                    serviceBinder?.setScale(scale)
                    serviceBinder?.setAspectRatio(null)
                } else {
                    serviceBinder?.setScale(0f)
                    serviceBinder?.setAspectRatio(if (!videoSwapped)
                        "$displayW:$displayH"
                    else
                        "$displayH:$displayW")
                }
            }
            SizePolicy.SURFACE_16_9 -> {
                serviceBinder?.setAspectRatio("16:9")
                serviceBinder?.setScale(0f)
            }
            SizePolicy.SURFACE_4_3 -> {
                serviceBinder?.setAspectRatio("4:3")
                serviceBinder?.setScale(0f)
            }
            SizePolicy.SURFACE_ORIGINAL -> {
                serviceBinder?.setAspectRatio(null)
                serviceBinder?.setScale(1f)
            }
        }
    }

    private fun updateVideoSurfaces() {
        if (activity == null || serviceBinder == null) {
            return
        }

        val decorView = requireActivity()
                .window
                .decorView

        val sw = decorView.width
        val sh = decorView.height

        // sanity check
        if (sw * sh == 0) {
            return
        }

        serviceBinder?.vOut?.setWindowSize(sw, sh)

        var lp = surfaceViewMedia.layoutParams

        if (mVideoWidth * mVideoHeight == 0) {
            /* Case of OpenGL vouts: handles the placement of the video using MediaPlayer API */
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT
            surfaceViewMedia.layoutParams = lp
            lp = frameLayoutVideoSurface.layoutParams
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT
            frameLayoutVideoSurface.layoutParams = lp
            changeMediaPlayerLayout(sw, sh)
            return
        }

        if (lp.width == lp.height && lp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
            /* We handle the placement of the video using Android View LayoutParams */
            serviceBinder?.setAspectRatio(null)
            serviceBinder?.setScale(0f)
        }

        var dw = sw.toDouble()
        var dh = sh.toDouble()
        val isPortrait = ResourceUtil.deviceIsPortraitOriented(context)

        if (sw > sh && isPortrait || sw < sh && !isPortrait) {
            dw = sh.toDouble()
            dh = sw.toDouble()
        }

        // compute the aspect ratio
        var ar: Double
        val vw: Double
        if (mVideoSarDen == mVideoSarNum) {
            /* No indication about the density, assuming 1:1 */
            vw = mVideoVisibleWidth.toDouble()
            ar = mVideoVisibleWidth.toDouble() / mVideoVisibleHeight.toDouble()
        } else {
            /* Use the specified aspect ratio */
            vw = mVideoVisibleWidth * mVideoSarNum.toDouble() / mVideoSarDen
            ar = vw / mVideoVisibleHeight
        }

        // compute the display aspect ratio
        val dar = dw / dh

        when (sizePolicy) {
            SizePolicy.SURFACE_BEST_FIT -> if (dar < ar)
                dh = dw / ar
            else
                dw = dh * ar
            SizePolicy.SURFACE_FIT_SCREEN -> if (dar >= ar)
                dh = dw / ar /* horizontal */
            else
                dw = dh * ar /* vertical */
            SizePolicy.SURFACE_FILL -> {
            }
            SizePolicy.SURFACE_16_9 -> {
                ar = 16.0 / 9.0
                if (dar < ar)
                    dh = dw / ar
                else
                    dw = dh * ar
            }
            SizePolicy.SURFACE_4_3 -> {
                ar = 4.0 / 3.0
                if (dar < ar)
                    dh = dw / ar
                else
                    dw = dh * ar
            }
            SizePolicy.SURFACE_ORIGINAL -> {
                dh = mVideoVisibleHeight.toDouble()
                dw = vw
            }
        }

        // set display size
        lp.width = Math.ceil(dw * mVideoWidth / mVideoVisibleWidth).toInt()
        lp.height = Math.ceil(dh * mVideoHeight / mVideoVisibleHeight).toInt()
        surfaceViewMedia.layoutParams = lp
        if (surfaceViewSubtitle != null)
            surfaceViewSubtitle.layoutParams = lp

        // set frame size (crop if necessary)
        lp = frameLayoutVideoSurface.layoutParams
        lp.width = Math.floor(dw).toInt()
        lp.height = Math.floor(dh).toInt()
        frameLayoutVideoSurface.layoutParams = lp

        surfaceViewMedia.invalidate()
        if (surfaceViewSubtitle != null)
            surfaceViewSubtitle.invalidate()
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onNewVideoLayout(
            vOut: IVLCVout,
            width: Int,
            height: Int,
            visibleWidth: Int,
            visibleHeight: Int,
            sarNum: Int,
            sarDen: Int
    ) {
        mVideoWidth = width
        mVideoHeight = height
        mVideoVisibleWidth = visibleWidth
        mVideoVisibleHeight = visibleHeight
        mVideoSarNum = sarNum
        mVideoSarDen = sarDen
        updateVideoSurfaces()
    }

}
