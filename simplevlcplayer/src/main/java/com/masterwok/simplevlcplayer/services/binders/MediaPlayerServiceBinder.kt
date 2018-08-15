package com.masterwok.simplevlcplayer.services.binders

import android.content.Context
import android.net.Uri
import android.support.v4.media.session.MediaSessionCompat
import android.view.SurfaceView
import com.masterwok.simplevlcplayer.contracts.MediaPlayer
import com.masterwok.simplevlcplayer.observables.RendererItemObservable
import com.masterwok.simplevlcplayer.services.MediaPlayerService
import org.videolan.libvlc.IVLCVout
import org.videolan.libvlc.Media
import org.videolan.libvlc.RendererItem
import java.lang.ref.WeakReference


/**
 * This is the binder for the media player service. It was created as a separate
 * class to avoid an implicit reference to the service. The service is referenced
 * through a weak reference to prevent memory leaks.
 */
class MediaPlayerServiceBinder(service: MediaPlayerService) : android.os.Binder() {

    private val serviceWeakReference: WeakReference<MediaPlayerService> = WeakReference(service)

    private val mediaPlayerService: MediaPlayerService?
        get() = serviceWeakReference.get()

    val rendererItemObservable: RendererItemObservable?
        get() = mediaPlayerService?.rendererItemObservable

    val vout: IVLCVout?
        get() = mediaPlayerService?.vout

    val mediaSession: MediaSessionCompat?
        get() = mediaPlayerService?.mediaSession

    val currentVideoTrack: Media.VideoTrack?
        get() = mediaPlayerService?.currentVideoTrack

    var selectedRendererItem: RendererItem?
        get() = mediaPlayerService?.selectedRendererItem
        set(rendererItem) {
            mediaPlayerService?.selectedRendererItem = rendererItem
        }

    val isPlaying: Boolean
        get() = mediaPlayerService?.isPlaying ?: false

    fun setMedia(context: Context, mediaUri: Uri) = mediaPlayerService?.setMedia(
            context,
            mediaUri
    )

    fun setSubtitle(subtitleUri: Uri) = mediaPlayerService?.setSubtitle(subtitleUri)

    fun play() = mediaPlayerService?.play()

    fun stop() = mediaPlayerService?.stop()

    fun setCallback(callback: MediaPlayer.Callback) {
        mediaPlayerService?.callback = callback
    }

    fun setTime(time: Long) = mediaPlayerService?.setTime(time)

    fun setProgress(progress: Int) = mediaPlayerService?.setProgress(progress)

    fun togglePlayback() = mediaPlayerService?.togglePlayback()

    fun pause() = mediaPlayerService?.pause()

    fun setAspectRatio(aspectRatio: String) = mediaPlayerService?.setAspectRatio(aspectRatio)

    fun setScale(scale: Float) = mediaPlayerService?.setScale(scale)

    fun attachSurfaces(
            surfaceMedia: SurfaceView,
            surfaceSubtitle: SurfaceView,
            listener: IVLCVout.OnNewVideoLayoutListener
    ) = mediaPlayerService?.attachSurfaces(
            surfaceMedia,
            surfaceSubtitle,
            listener
    )

    fun detachSurfaces() = mediaPlayerService?.detachSurfaces()

    fun setVolume(volume: Int) = mediaPlayerService?.setVolume(volume)
}