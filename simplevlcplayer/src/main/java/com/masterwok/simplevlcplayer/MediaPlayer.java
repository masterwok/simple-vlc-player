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
    public void setTime(long time) {
        player.setTime(time);
    }

    @Override
    public void onEvent(org.videolan.libvlc.MediaPlayer.Event event) {
        switch (event.type) {
            case Opening:
                break;
            case SeekableChanged:
                break;
            case Playing:
                break;
            case Paused:
                break;
            case Stopped:
                break;
            case EndReached:
                break;
            case EncounteredError:
                break;
            case TimeChanged:
                break;
            case PositionChanged:
                break;
            default:
                break;
        }
    }
}
