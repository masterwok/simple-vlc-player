package com.masterwok.simplevlcplayer.sessions;

import android.content.Context;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.SurfaceView;

import com.masterwok.simplevlcplayer.callbacks.VlcMediaPlayerEventListener;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.RendererItem;

public class VlcMediaPlayerSession
        extends MediaSessionCompat {

    public static final String SubtitleExtra = "extra.subtitle";
    public static final String PositionExtra = "extra.position";
    public static final String LengthExtra = "extra.length";
    public static final String TimeExtra = "extra.time";

    private final LibVLC libVlc;
    private final MediaPlayer mediaPlayer;
    private final PlaybackStateCompat.Builder playbackStateBuilder;
    private final Context context;

    public VlcMediaPlayerSession(Context context, String tag) {
        super(context, tag);

        this.context = context;
        this.libVlc = new LibVLC(context);
        this.mediaPlayer = new MediaPlayer(libVlc);
        this.playbackStateBuilder = new PlaybackStateCompat.Builder();

        init();
    }

    private void init() {
        VlcMediaPlayerEventListener vlcMediaPlayerEventListener = new VlcMediaPlayerEventListener(
                context,
                this,
                libVlc,
                mediaPlayer,
                playbackStateBuilder,
                this::setPlaybackState,
                this::detachSurfaceViews,
                PositionExtra,
                LengthExtra,
                TimeExtra
        );

        mediaPlayer.setEventListener(vlcMediaPlayerEventListener);

        // We want to handle media controller actions.
        setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        setPlaybackState(playbackStateBuilder.build());

        setCallback(vlcMediaPlayerEventListener);
    }

    /**
     * Set renderer to VLC render item. Invoke this method to cast media.
     *
     * @param renderItem The render item to play on.
     */
    public void setRenderer(RendererItem renderItem) {
        detachSurfaceViews();

        mediaPlayer.setRenderer(renderItem);
    }

    /**
     * Play media locally using the provided surface views.
     *
     * @param mediaSurfaceView    The SurfaceView to render the media in.
     * @param subtitleSurfaceView The SurfaceView to render the subtitles in.
     * @param layoutListener      A layout listener for responding to layout changes.
     */
    public void setRenderer(
            SurfaceView mediaSurfaceView,
            SurfaceView subtitleSurfaceView,
            IVLCVout.OnNewVideoLayoutListener layoutListener
    ) {
        attachSurfaceViews(
                mediaSurfaceView,
                subtitleSurfaceView,
                layoutListener
        );
    }

    /**
     * Attach the video and subtitle surface views.
     */
    private void attachSurfaceViews(
            SurfaceView mediaSurfaceView,
            SurfaceView subtitleSurfaceView,
            IVLCVout.OnNewVideoLayoutListener layoutListener
    ) {
        IVLCVout vlcOut = mediaPlayer.getVLCVout();

        if (!vlcOut.areViewsAttached()) {
            vlcOut.setVideoView(mediaSurfaceView);
            vlcOut.setSubtitlesView(subtitleSurfaceView);
            vlcOut.attachViews(layoutListener);
        }
    }

    /**
     * Detach the video and subtitle surface views.
     */
    private void detachSurfaceViews() {
        IVLCVout vlcOut = mediaPlayer.getVLCVout();

        if (!vlcOut.areViewsAttached()) {
            return;
        }

        vlcOut.detachViews();
    }
}
