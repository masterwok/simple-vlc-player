package com.masterwok.simplevlcplayer.fragments

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.masterwok.simplevlcplayer.R
import com.masterwok.simplevlcplayer.common.utils.ResourceUtil
import com.masterwok.simplevlcplayer.constants.SizePolicy
import kotlinx.android.synthetic.main.fragment_player_local.*
import org.videolan.libvlc.IVLCVout
import org.videolan.libvlc.Media

class LocalPlayerFragment : BasePlayerFragment()
        , IVLCVout.OnNewVideoLayoutListener {

    companion object {
        const val SetProvidedSubtitle = "bundle.setprovidedsubtitleonnextplayback"
        const val IsPlayingKey = "bundle.isplaying"
        const val LengthKey = "bundle.length"
        const val TimeKey = "bundle.time"
    }

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

    private val becomingNoisyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent.action) {
                // Pause playback whenever the user pulls out ( ͡° ͜ʖ ͡°)
                serviceBinder?.pause()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        context?.registerReceiver(
                becomingNoisyReceiver,
                IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        )
    }

    override fun onStop() {
        super.onStop()

        context?.unregisterReceiver(becomingNoisyReceiver)
    }


    override fun configure(
            isPlaying: Boolean,
            time: Long,
            length: Long
    ) = componentPlayerControl.configure(
            isPlaying,
            time,
            length
    )

    override fun onConnected() = startPlayback()

    override fun onDisconnected() {
        this.serviceBinder = null
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

    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        configureSubtitleSurface()
        subscribeToViewComponents()
    }

    override fun onPause() {
        stopPlayback()

        super.onPause()
    }


    private fun stopPlayback() {
        surfaceViewSubtitle.removeOnLayoutChangeListener(surfaceLayoutListener)

        updateResumeState()
        serviceBinder?.stop()
        detachSurfaces()
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

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        updateVideoSurfaces()
    }

    private fun subscribeToViewComponents() {
        componentPlayerControl.registerCallback(this)
    }

    private fun configureSubtitleSurface() = surfaceViewSubtitle.apply {
        setZOrderMediaOverlay(true)
        holder.setFormat(PixelFormat.TRANSLUCENT)
    }

    private fun startPlayback() {
        surfaceViewMedia.addOnLayoutChangeListener(surfaceLayoutListener)

        attachSurfaces()
        updateVideoSurfaces()

        serviceBinder?.setMedia(context!!, mediaUri!!)

        if (setProvidedSubtitle) {
            serviceBinder?.setSubtitle(subtitleUri)
        } else {
            serviceBinder?.setSubtitle(serviceBinder?.selectedSubtitleUri)
        }

        if (resumeIsPlaying) {
            serviceBinder?.play()
        }
    }

    private val handler = Handler()

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

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (savedInstanceState == null) {
            return
        }

        setProvidedSubtitle = savedInstanceState.getBoolean(SetProvidedSubtitle)
        resumeIsPlaying = savedInstanceState.getBoolean(IsPlayingKey, true)
        resumeTime = savedInstanceState.getLong(TimeKey, 0)
        resumeLength = savedInstanceState.getLong(LengthKey, 0)

        configure(
                resumeIsPlaying,
                resumeTime,
                resumeLength
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val selectedSubtitleUri = serviceBinder?.selectedSubtitleUri

        outState.putBoolean(SetProvidedSubtitle, selectedSubtitleUri === subtitleUri)
        outState.putBoolean(IsPlayingKey, resumeIsPlaying)
        outState.putLong(TimeKey, resumeTime)
        outState.putLong(LengthKey, resumeLength)

        super.onSaveInstanceState(outState)
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

    override fun onSubtitlesCleared() = startPlayback()

    override fun onPlayerSeekStateChange(canSeek: Boolean) {
        super.onPlayerSeekStateChange(canSeek)

        if (!canSeek || serviceBinder == null) {
            return
        }

        serviceBinder!!.setTime(resumeTime)
    }

    private fun detachSurfaces() = serviceBinder?.detachSurfaces()

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
        if (serviceBinder == null) {
            return
        }

        val sw = activity!!.window.decorView.width
        val sh = activity!!.window.decorView.height

        // sanity check
        if (sw * sh == 0) {
            return
        }

        serviceBinder!!.vOut!!.setWindowSize(sw, sh)

        var lp = surfaceViewMedia.getLayoutParams()

        if (mVideoWidth * mVideoHeight == 0) {
            /* Case of OpenGL vouts: handles the placement of the video using MediaPlayer API */
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT
            surfaceViewMedia.setLayoutParams(lp)
            lp = frameLayoutVideoSurface.getLayoutParams()
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT
            frameLayoutVideoSurface.setLayoutParams(lp)
            changeMediaPlayerLayout(sw, sh)
            return
        }

        if (lp.width == lp.height && lp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
            /* We handle the placement of the video using Android View LayoutParams */
            serviceBinder!!.setAspectRatio(null)
            serviceBinder!!.setScale(0f)
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