package com.masterwok.simplevlcplayer.sessions;

import android.content.Context;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.SurfaceView;

import com.masterwok.simplevlcplayer.callbacks.RendererItemListener;
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

    private RendererItemListener rendererItemListener = new RendererItemListener();

    private final LibVLC libVlc;
    private final MediaPlayer mediaPlayer;
    private final PlaybackStateCompat.Builder playbackStateBuilder;
    private RendererItem selectedRendererItem;
    private final Context context;

    public VlcMediaPlayerSession(
            Context context,
            String tag
    ) {
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

        rendererItemListener.start(libVlc);
    }

    /**
     * Set renderer to VLC render item. Invoke this method to cast media.
     *
     * @param renderItem The render item to play on.
     */
    public void setRenderer(RendererItem renderItem) {
        this.selectedRendererItem = renderItem;

        detachSurfaceViews();

        mediaPlayer.setRenderer(renderItem);

        restartPlayback();
    }

    private void restartPlayback() {
        mediaPlayer.stop();
        mediaPlayer.play();
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
        this.selectedRendererItem = null;

        // Clear any previous renderer
        mediaPlayer.setRenderer(null);

        attachSurfaceViews(
                mediaSurfaceView,
                subtitleSurfaceView,
                layoutListener
        );

        restartPlayback();
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


    /**
     * Get the renderer item listener (observable).
     *
     * @return The renderer item listener instance.
     */
    public RendererItemListener getRenderItemObservable() {
        return rendererItemListener;
    }


    /**
     * Get the user selected renderer item.
     *
     * @return If selected, the renderer item. Else, null.
     */
    public RendererItem getSelectedRendererItem() {
        return selectedRendererItem;
    }
}
