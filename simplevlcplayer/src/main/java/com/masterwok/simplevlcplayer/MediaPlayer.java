package com.masterwok.simplevlcplayer;

import android.net.Uri;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;

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
public class MediaPlayer
        implements
        com.masterwok.simplevlcplayer.contracts.MediaPlayer,
        org.videolan.libvlc.MediaPlayer.EventListener {


    private final org.videolan.libvlc.MediaPlayer player;
    private final LibVLC libVlc;
    private Callback callback;

    public MediaPlayer(LibVLC libVlc) {
        this.libVlc = libVlc;

        player = new org.videolan.libvlc.MediaPlayer(libVlc);
        player.setEventListener(this);
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
    public void onEvent(org.videolan.libvlc.MediaPlayer.Event event) {
        if (callback == null) {
            return;
        }

        switch (event.type) {
            case Opening:
                callback.onOpening();
                break;
            case SeekableChanged:
                callback.onSeekStateChange(event.getSeekable());
                break;
            case Playing:
                callback.onPlaying();
                break;
            case Paused:
                callback.onPaused();
                break;
            case Stopped:
                callback.onStopped();
                break;
            case EndReached:
                callback.onEndReached();
                break;
            case EncounteredError:
                callback.onError();
                break;
            case TimeChanged:
                callback.onTimeChange(event.getTimeChanged());
                break;
            case PositionChanged:
                callback.onPositionChange(event.getPositionChanged());
                break;
            default:
                break;
        }
    }

    public interface Callback {
        void onOpening();

        void onSeekStateChange(boolean canSeek);

        void onPlaying();

        void onPaused();

        void onStopped();

        void onEndReached();

        void onError();

        void onTimeChange(long timeChanged);

        void onPositionChange(float positionChanged);
    }
}
