package com.masterwok.simplevlcplayer

import android.net.Uri
import android.view.SurfaceView
import com.masterwok.simplevlcplayer.contracts.MediaPlayer.Callback
import com.masterwok.simplevlcplayer.contracts.VlcMediaPlayer
import org.videolan.libvlc.*
import org.videolan.libvlc.Media.Slave.Type.Subtitle
import org.videolan.libvlc.MediaPlayer.Event.*
import java.io.FileDescriptor

class VlcMediaPlayer constructor(
        private val libVlc: LibVLC
) : VlcMediaPlayer
        , MediaPlayer.EventListener
        , IVLCVout.Callback {

    private var player: MediaPlayer = MediaPlayer(libVlc).apply {
        setEventListener(this@VlcMediaPlayer)
    }

    override var callback: Callback? = null

    override var selectedRendererItem: RendererItem? = null

    override var selectedSubtitleUri: Uri? = null

    override val media: Media?
        get() = player.media

    override val vOut: IVLCVout
        get() = player.vlcVout

    override var time: Long
        get() = player.time
        set(value) {
            player.time = value
        }

    override val length: Long
        get() = player.length

    override val isPlaying: Boolean
        get() = player.isPlaying

    override val currentVideoTrack: Media.VideoTrack?
        get() = player.currentVideoTrack

    override fun onEvent(event: MediaPlayer.Event?) {
        when (event?.type) {
            Opening -> callback?.onPlayerOpening()
            SeekableChanged -> callback?.onPlayerSeekStateChange(event.seekable)
            Playing -> callback?.onPlayerPlaying()
            Paused -> callback?.onPlayerPaused()
            Stopped -> callback?.onPlayerStopped()
            EndReached -> callback?.onPlayerEndReached()
            EncounteredError -> callback?.onPlayerError()
            TimeChanged -> callback?.onPlayerTimeChange(event.timeChanged)
            PositionChanged -> callback?.onPlayerPositionChanged(event.positionChanged)
            Buffering -> callback?.onBuffering(event.buffering)
        }
    }

    override fun setAspectRatio(aspectRatio: String?) {
        player.aspectRatio = aspectRatio
    }

    private fun hasSlaves() = player.media?.slaves?.size ?: 0 > 0

    override fun play() = player.play()

    override fun detachSurfaces() = vOut.detachViews()

    override fun release() = player.release()

    override fun pause() = player.pause()

    override fun stop() = player.stop()

    override fun setVolume(volume: Int) {
        player.volume = volume
    }

    override fun setScale(scale: Float) {
        player.scale = scale
    }

    override fun onSurfacesCreated(p0: IVLCVout?) {
        // Nothing to do..
    }

    override fun onSurfacesDestroyed(p0: IVLCVout?) {
        // Nothing to do..
    }

    override fun setMedia(uri: Uri?) {
        player.media = Media(libVlc, uri)
    }

    override fun setSubtitleUri(uri: Uri?) {
        selectedSubtitleUri = uri

        if (uri == null) {
            if (hasSlaves()) {
                callback?.onSubtitlesCleared()
            }

            return
        }

        player.addSlave(
                Subtitle
                , uri
                , true
        )
    }

    override fun setMedia(fileDescriptor: FileDescriptor?) {
        val media = Media(libVlc, fileDescriptor)

        player.media = media

        media.release()
    }


    override fun attachSurfaces(
            surfaceMedia: SurfaceView
            , surfaceSubtitles: SurfaceView
            , layoutListener: IVLCVout.OnNewVideoLayoutListener
    ) {
        selectedRendererItem = null

        vOut.setVideoView(surfaceMedia)
        vOut.setSubtitlesView(surfaceSubtitles)
        vOut.attachViews(layoutListener)
    }


    override fun setRendererItem(rendererItem: RendererItem?) {
        selectedRendererItem = rendererItem
        player.setRenderer(rendererItem)
    }

}