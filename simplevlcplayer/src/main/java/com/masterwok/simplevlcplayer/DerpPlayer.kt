package com.masterwok.simplevlcplayer

import android.net.Uri
import android.view.SurfaceView
import com.masterwok.simplevlcplayer.contracts.MediaPlayer.Callback
import org.videolan.libvlc.*
import org.videolan.libvlc.Media.Slave.Type.Subtitle
import org.videolan.libvlc.MediaPlayer.Event.*
import java.io.FileDescriptor

class DerpPlayer constructor(
        val libVlc: LibVLC
) : com.masterwok.simplevlcplayer.contracts.VlcMediaPlayer
        , MediaPlayer.EventListener
        , IVLCVout.Callback {

    private var selectedRendererItem: RendererItem? = null

    private var player: MediaPlayer = MediaPlayer(libVlc).apply {
        setEventListener(this@DerpPlayer)
    }

    private var callback: Callback? = null

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

    override fun getMedia(): Media? = player.media

    override fun play() = player.play()

    override fun getVout(): IVLCVout = player.vlcVout

    override fun detachSurfaces() = vout.detachViews()

    override fun release() = player.release()

    override fun pause() = player.pause()

    override fun stop() = player.stop()

    override fun getTime(): Long = player.time

    override fun getLength(): Long = player.length

    override fun isPlaying(): Boolean = player.isPlaying

    override fun getSelectedRendererItem(): RendererItem? = selectedRendererItem

    override fun getCurrentVideoTrack(): Media.VideoTrack = player.currentVideoTrack

    override fun setVolume(volume: Int) {
        player.volume = volume
    }

    override fun setScale(scale: Float) {
        player.scale = scale
    }

    override fun setTime(time: Long) {
        player.time = time
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

    override fun setSubtitle(uri: Uri?) {
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

    override fun setCallback(callback: Callback?) {
        this.callback = callback
    }

    override fun attachSurfaces(
            surfaceMedia: SurfaceView?
            , surfaceSubtitles: SurfaceView?
            , layoutListener: IVLCVout.OnNewVideoLayoutListener?
    ) {
        selectedRendererItem = null

        vout.setVideoView(surfaceMedia)
        vout.setSubtitlesView(surfaceSubtitles)
        vout.attachViews(layoutListener)
    }


    override fun setRendererItem(rendererItem: RendererItem?) {
        selectedRendererItem = rendererItem
        player.setRenderer(rendererItem)
    }

}