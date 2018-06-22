package com.masterwok.simplevlcplayer;

import android.net.Uri;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.RendererItem;

import static org.videolan.libvlc.Media.Slave.Type.Subtitle;
import static org.videolan.libvlc.MediaPlayer.Event.EncounteredError;
import static org.videolan.libvlc.MediaPlayer.Event.EndReached;
import static org.videolan.libvlc.MediaPlayer.Event.Opening;
import static org.videolan.libvlc.MediaPlayer.Event.Paused;
import static org.videolan.libvlc.MediaPlayer.Event.Playing;
import static org.videolan.libvlc.MediaPlayer.Event.PositionChanged;
import static org.videolan.libvlc.MediaPlayer.Event.SeekableChanged;
import static org.videolan.libvlc.MediaPlayer.Event.Stopped;
import static org.videolan.libvlc.MediaPlayer.Event.TimeChanged;


/**
 * This class is an implementation of the media player contract and wraps
 * the VLC media player.
 */
public class VlcMediaPlayer
        implements
        com.masterwok.simplevlcplayer.contracts.MediaPlayer
        , com.masterwok.simplevlcplayer.contracts.SurfaceMediaPlayer
        , com.masterwok.simplevlcplayer.contracts.RendererItemMediaPlayer
        , org.videolan.libvlc.MediaPlayer.EventListener
        , IVLCVout.OnNewVideoLayoutListener
        , IVLCVout.Callback {


    private final org.videolan.libvlc.MediaPlayer player;
    private final LibVLC libVlc;
    private Callback callback;
    private SurfaceView surfaceMedia;

    public VlcMediaPlayer(
            LibVLC libVlc
    ) {
        this.libVlc = libVlc;

        player = new org.videolan.libvlc.MediaPlayer(libVlc);
        player.setEventListener(this);
    }

    @Override
    public void release() {
        player.release();
    }

    @Override
    public void play() {
        player.play();
    }

    @Override
    public void pause() {
        player.pause();
    }

    @Override
    public void togglePlayback() {
        if (player.isPlaying()) {
            player.pause();
            return;
        }

        player.play();
    }

    @Override
    public void stop() {
        player.stop();
    }

    @Override
    public void setMedia(Uri uri) {
        final Media media = new Media(
                libVlc,
                uri
        );

        player.setMedia(media);
        media.release();
    }

    @Override
    public void setSubtitle(Uri uri) {
        player.addSlave(
                Subtitle,
                uri,
                true
        );
    }

    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public long getTime() {
        return player.getTime();
    }

    @Override
    public void setTime(long time) {
        player.setTime(time);
    }

    @Override
    public long getLength() {
        return player.getLength();
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public void onEvent(org.videolan.libvlc.MediaPlayer.Event event) {
        if (callback == null) {
            return;
        }

        switch (event.type) {
            case Opening:
                callback.onPlayerOpening();
                break;
            case SeekableChanged:
                callback.onPlayerSeekStateChange(event.getSeekable());
                break;
            case Playing:
                callback.onPlayerPlaying();
                break;
            case Paused:
                callback.onPlayerPaused();
                break;
            case Stopped:
                callback.onPlayerStopped();
                break;
            case EndReached:
                callback.onPlayerEndReached();
                break;
            case EncounteredError:
                callback.onPlayerError();
                break;
            case TimeChanged:
                callback.onPlayerTimeChange(event.getTimeChanged());
                break;
            case PositionChanged:
                callback.onPlayerPositionChange(event.getPositionChanged());
                break;
            default:
                break;
        }
    }

    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {
        // Nothing to do..
    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {
        // Nothing to do..
    }


    @Override
    public void onSurfaceChanged(int width, int height) {
        setSurfaceSize(width, height);
    }

    @Override
    public void onNewVideoLayout(
            IVLCVout vlcVout,
            int width,
            int height,
            int visibleWidth,
            int visibleHeight,
            int sarNum,
            int sarDen
    ) {
        setSurfaceSize(width, height);
    }

    private void setSurfaceSize(int width, int height) {
        SurfaceHolder holder = surfaceMedia.getHolder();
        holder.setFixedSize(width, height);

        ViewGroup.LayoutParams lp = surfaceMedia.getLayoutParams();
        lp.width = width;
        lp.height = height;
        surfaceMedia.setLayoutParams(lp);
    }

    @Override
    public void attachSurfaces(
            SurfaceView surfaceMedia,
            SurfaceView surfaceSubtitle
    ) {
        this.surfaceMedia = surfaceMedia;

        final IVLCVout vlcOut = player.getVLCVout();
        vlcOut.setVideoView(surfaceMedia);
        vlcOut.setSubtitlesView(surfaceSubtitle);
        vlcOut.attachViews(this);
    }

    @Override
    public void detachSurfaces() {
        final IVLCVout vlcOut = player.getVLCVout();

        if (!vlcOut.areViewsAttached()) {
            return;
        }

        vlcOut.detachViews();

        surfaceMedia = null;
    }

    @Override
    public void setRendererItem(RendererItem rendererItem) {
        player.setRenderer(rendererItem);
    }
}
