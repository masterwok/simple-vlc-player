package com.masterwok.simplevlcplayer.callbacks;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.masterwok.simplevlcplayer.interfaces.ParamRunnable;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import static com.masterwok.simplevlcplayer.sessions.VlcMediaPlayerSession.SubtitleExtra;

/**
 * This listener is responsible for responding to media player events and
 * media session callbacks. This means that all playback state updates are
 * managed within this class.
 */
public class VlcMediaPlayerEventListener
        extends MediaSessionCompat.Callback
        implements MediaPlayer.EventListener {

    private final MediaSessionCompat mediaSessionCompat;
    private final LibVLC libVlc;
    private final MediaPlayer mediaPlayer;
    private final PlaybackStateCompat.Builder playbackStateBuilder;
    private final ParamRunnable<PlaybackStateCompat> setPlaybackState;
    private final Runnable onStop;
    private final String positionExtra;
    private final String lengthExtra;
    private final String timeExtra;
    private final AudioManager audioManager;

    public VlcMediaPlayerEventListener(
            Context context,
            MediaSessionCompat mediaSessionCompat,
            LibVLC libVlc,
            MediaPlayer mediaPlayer,
            PlaybackStateCompat.Builder playbackStateBuilder,
            ParamRunnable<PlaybackStateCompat> setPlaybackState,
            Runnable onStop,
            String positionExtra,
            String lengthExtra,
            String timeExtra
    ) {
        this.mediaSessionCompat = mediaSessionCompat;
        this.libVlc = libVlc;
        this.mediaPlayer = mediaPlayer;
        this.playbackStateBuilder = playbackStateBuilder;

        this.setPlaybackState = setPlaybackState;
        this.onStop = onStop;
        this.positionExtra = positionExtra;
        this.lengthExtra = lengthExtra;
        this.timeExtra = timeExtra;

        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * Request audio focus so other applications pause playback.
     */
    private void requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .build();

            AudioFocusRequest audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttributes)
                    .setAcceptsDelayedFocusGain(false)
                    .build();

            audioManager.requestAudioFocus(audioFocusRequest);

            return;
        }

        //noinspection deprecation
        audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
        );
    }


    @Override
    public void onPrepareFromUri(Uri uri, Bundle extras) {
        mediaPlayer.stop();
        mediaPlayer.setMedia(new Media(libVlc, uri));

        if (extras == null || !extras.containsKey(SubtitleExtra)) {
            return;
        }

        String subtitlePath = extras.getString(SubtitleExtra);

        if (subtitlePath == null) {
            return;
        }

        //noinspection ConstantConditions
        mediaPlayer.addSlave(
                Media.Slave.Type.Subtitle,
                subtitlePath,
                true
        );

        mediaSessionCompat.setActive(true);
    }

    @Override
    public void onSeekTo(long pos) {
        if (!mediaPlayer.isSeekable()) {
            return;
        }

        mediaPlayer.setTime(pos);
    }

    @Override
    public void onPlay() {
        requestAudioFocus();

        mediaPlayer.play();
    }


    @Override
    public void onPause() {
        mediaPlayer.pause();

        setPausedPlaybackState();
    }


    @Override
    public void onStop() {
        mediaPlayer.stop();

        // Allow the media session to release any resources.
        onStop.run();

        mediaSessionCompat.setActive(false);
    }

    @Override
    public void onEvent(MediaPlayer.Event event) {
        switch (event.type) {
            case MediaPlayer.Event.EndReached:
                setStateStopped();
                break;
            case MediaPlayer.Event.EncounteredError:
                releasePlayer();
                break;
            case MediaPlayer.Event.Opening:
                setStateBuffering();
                break;
            case MediaPlayer.Event.PositionChanged:
                setStatePlaying();
                break;
        }
    }

    private void releasePlayer() {
        // No player to release, do nothing.
        if (mediaPlayer == null) {
            return;
        }

        mediaPlayer.stop();

        mediaPlayer
                .getVLCVout()
                .detachViews();
    }

    private void setPausedPlaybackState() {
        PlaybackStateCompat newState = playbackStateBuilder.setState(
                PlaybackStateCompat.STATE_PAUSED,
                mediaPlayer.getTime(),
                1
        ).setExtras(buildPlaybackStateBundle()).build();

        setPlaybackState.run(newState);
    }

    /**
     * This method is invoked when the media player is opening media.
     * It will set the playback state to STATE_OPENING.
     */
    private void setStateBuffering() {
        PlaybackStateCompat newState = playbackStateBuilder.setState(
                PlaybackStateCompat.STATE_BUFFERING,
                mediaPlayer.getTime(),
                1
        ).build();

        setPlaybackState.run(newState);
    }

    /**
     * This method is invoked when the media player reaches the end
     * of the media being played. This method is responsible for updating
     * the playback state of the session to stopped.
     */
    private void setStateStopped() {
        PlaybackStateCompat newState = playbackStateBuilder.setState(
                PlaybackStateCompat.STATE_STOPPED,
                0,
                0
        ).build();

        setPlaybackState.run(newState);
    }

    /**
     * This method is invoked when the media player playback position changes.
     * This method is responsible for updating the position, length, time, and
     * the playback state.
     */
    private void setStatePlaying() {
        PlaybackStateCompat newState = playbackStateBuilder.setState(
                PlaybackStateCompat.STATE_PLAYING,
                mediaPlayer.getTime(),
                1
        ).setExtras(buildPlaybackStateBundle()).build();

        setPlaybackState.run(newState);
    }

    /**
     * Build playback state bundle. This bundle contains time, position,
     * and length of media.
     *
     * @return A new playback state bundle instance.
     */
    private Bundle buildPlaybackStateBundle() {
        Bundle bundle = new Bundle();

        bundle.putFloat(positionExtra, mediaPlayer.getPosition());
        bundle.putLong(lengthExtra, mediaPlayer.getLength());
        bundle.putLong(timeExtra, mediaPlayer.getTime());

        return bundle;
    }
}
