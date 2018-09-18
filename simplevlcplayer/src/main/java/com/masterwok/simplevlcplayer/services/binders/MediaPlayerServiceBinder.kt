package com.masterwok.simplevlcplayer.services.binders

import android.content.Context
import android.net.Uri
import android.os.Binder
import android.support.v4.media.session.MediaSessionCompat
import android.view.SurfaceView
import com.masterwok.simplevlcplayer.contracts.MediaPlayer
import com.masterwok.simplevlcplayer.observables.RendererItemObservable
import com.masterwok.simplevlcplayer.services.MediaPlayerService
import org.videolan.libvlc.IVLCVout
import org.videolan.libvlc.Media
import org.videolan.libvlc.RendererItem
import java.lang.ref.WeakReference

class MediaPlayerServiceBinder constructor(
        mediaPlayerService: MediaPlayerService
) : Binder() {

    private var serviceWeakReference: WeakReference<MediaPlayerService> = WeakReference(mediaPlayerService)

    val rendererItemObservable: RendererItemObservable?
        get() = serviceWeakReference
                .get()
                ?.rendererItemObservable

    var selectedRendererItem: RendererItem?
        get() = serviceWeakReference
                .get()
                ?.selectedRendererItem
        set(value) {
            serviceWeakReference
                    .get()
                    ?.selectedRendererItem = value
        }

    var selectedSubtitleUri: Uri?
        get() = serviceWeakReference
                .get()
                ?.selectedSubtitleUri
        set(value) {
            serviceWeakReference
                    .get()
                    ?.selectedSubtitleUri = value
        }

    var vOut: IVLCVout? = null
        get() = serviceWeakReference
                .get()
                ?.vOut

    fun setMedia(context: Context, mediaUri: Uri) = serviceWeakReference
            .get()
            ?.setMedia(context, mediaUri)

    fun setSubtitle(subtitleUri: Uri?) = serviceWeakReference
            .get()
            ?.setSubtitle(subtitleUri)

    fun play() = serviceWeakReference
            .get()
            ?.play()

    fun stop() = serviceWeakReference
            .get()
            ?.stop()

    var callback: MediaPlayer.Callback?
        get() = serviceWeakReference
                .get()
                ?.callback
        set(value) {
            serviceWeakReference
                    .get()
                    ?.callback = value
        }

    var mediaSession: MediaSessionCompat? = null
        get() = serviceWeakReference
                .get()
                ?.mediaSession

    fun setTime(time: Long) = serviceWeakReference
            .get()
            ?.setTime(time)

    fun setProgress(progress: Int) = serviceWeakReference
            .get()
            ?.setProgress(progress)

    fun togglePlayback() = serviceWeakReference
            .get()
            ?.togglePlayback()

    fun pause() = serviceWeakReference
            .get()
            ?.pause()

    fun setAspectRatio(aspectRatio: String?) = serviceWeakReference
            .get()
            ?.setAspectRatio(aspectRatio)

    fun setScale(scale: Float) = serviceWeakReference
            .get()
            ?.setScale(scale)

    var currentVideoTrack: Media.VideoTrack? = null
        get() = serviceWeakReference
                .get()
                ?.currentVideoTrack

    fun attachSurfaces(
            surfaceMedia: SurfaceView
            , subtitleSurface: SurfaceView
            , listener: IVLCVout.OnNewVideoLayoutListener
    ) = serviceWeakReference
            .get()
            ?.attachSurfaces(
                    surfaceMedia
                    , subtitleSurface
                    , listener
            )

    fun detachSurfaces() = serviceWeakReference
            .get()
            ?.detachSurfaces()

    val isPlaying: Boolean
        get() = serviceWeakReference
                .get()
                ?.isPlaying
                ?: false

}