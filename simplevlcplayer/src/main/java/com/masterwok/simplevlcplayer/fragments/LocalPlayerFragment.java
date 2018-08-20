package com.masterwok.simplevlcplayer.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.masterwok.simplevlcplayer.R;
import com.masterwok.simplevlcplayer.components.PlayerControlComponent;
import com.masterwok.simplevlcplayer.constants.SizePolicy;
import com.masterwok.simplevlcplayer.common.utils.ResourceUtil;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.Media;

import static com.masterwok.simplevlcplayer.constants.SizePolicy.SURFACE_FIT_SCREEN;


public class LocalPlayerFragment
        extends BasePlayerFragment
        implements IVLCVout.OnNewVideoLayoutListener {

    private static final String IsPlayingKey = "bundle.isplaying";
    private static final String LengthKey = "bundle.length";
    private static final String TimeKey = "bundle.time";

    private SizePolicy sizePolicy = SizePolicy.SURFACE_BEST_FIT;

    private final Handler mHandler = new Handler();

    private int mVideoHeight = 0;
    private int mVideoWidth = 0;
    private int mVideoVisibleHeight = 0;
    private int mVideoVisibleWidth = 0;
    private int mVideoSarNum = 0;
    private int mVideoSarDen = 0;

    private boolean resumeIsPlaying = true;
    private long resumeLength = 0;
    private long resumeTime = 0;

    private View.OnLayoutChangeListener surfaceLayoutListener;

    private PlayerControlComponent componentControls;
    private SurfaceView surfaceSubtitle;
    private SurfaceView surfaceMedia;

    private FrameLayout surfaceFrame;

    private BroadcastReceiver becomingNoisyReceiver;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        becomingNoisyReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                    if (serviceBinder == null) {
                        return;
                    }

                    // Pause playback whenever the user pulls out ( ͡° ͜ʖ ͡°)
                    serviceBinder.pause();
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();

        //noinspection ConstantConditions
        getContext().registerReceiver(
                becomingNoisyReceiver,
                new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        );
    }

    @Override
    public void onStop() {
        super.onStop();

        //noinspection ConstantConditions
        getContext().unregisterReceiver(becomingNoisyReceiver);
    }

    @Override
    protected void configure(
            boolean isPlaying,
            long time,
            long length
    ) {
        componentControls.configure(
                isPlaying,
                time,
                length
        );
    }

    @Override
    protected void onConnected() {
        startPlayback();
    }

    @Override
    protected void onDisconnected() {
        this.serviceBinder = null;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_player_local,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        bindViewComponents(view);
        configureSubtitleSurface();
        subscribeToViewComponents();
    }

    @Override
    public void onPause() {
        stopPlayback();

        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateVideoSurfaces();
    }

    private void updateResumeState() {
        final Activity activity = getActivity();

        if (activity == null) {
            return;
        }

        final PlaybackStateCompat playbackState = MediaControllerCompat
                .getMediaController(activity)
                .getPlaybackState();

        resumeIsPlaying = playbackState.getState() == PlaybackStateCompat.STATE_PLAYING;
        resumeTime = playbackState.getPosition();
        resumeLength = playbackState.getBufferedPosition();
    }

    private void subscribeToViewComponents() {
        componentControls.registerCallback(this);
    }

    private void bindViewComponents(View view) {
        surfaceFrame = view.findViewById(R.id.video_surface_frame);
        componentControls = view.findViewById(R.id.component_player);
        surfaceMedia = view.findViewById(R.id.surface_media);
        surfaceSubtitle = view.findViewById(R.id.surface_subtitle);
    }

    private void configureSubtitleSurface() {
        surfaceSubtitle.setZOrderMediaOverlay(true);

        surfaceSubtitle
                .getHolder()
                .setFormat(PixelFormat.TRANSLUCENT);
    }

    private void registerSurfaceLayoutListener() {
        surfaceLayoutListener = new View.OnLayoutChangeListener() {
            private final Runnable mRunnable = () -> updateVideoSurfaces();

            @Override
            public void onLayoutChange(
                    View v,
                    int left,
                    int top,
                    int right,
                    int bottom,
                    int oldLeft,
                    int oldTop,
                    int oldRight,
                    int oldBottom
            ) {
                if (left != oldLeft
                        || top != oldTop
                        || right != oldRight
                        || bottom != oldBottom) {
                    mHandler.removeCallbacks(mRunnable);
                    mHandler.post(mRunnable);
                }
            }
        };


        surfaceMedia.addOnLayoutChangeListener(surfaceLayoutListener);
    }

    private void startPlayback() {
        if (serviceBinder == null) {
            return;
        }

        attachSurfaces();
        registerSurfaceLayoutListener();
        updateVideoSurfaces();

        serviceBinder.setMedia(getContext(), mediaUri);
        serviceBinder.setSubtitle(subtitleUri);

        if (resumeIsPlaying) {
            serviceBinder.play();
        }
    }

    private void stopPlayback() {
        if (serviceBinder == null) {
            return;
        }

        updateResumeState();
        serviceBinder.stop();
        surfaceMedia.removeOnLayoutChangeListener(surfaceLayoutListener);
        detachSurfaces();
    }

    @Override
    public void onPlayerSeekStateChange(boolean canSeek) {
        super.onPlayerSeekStateChange(canSeek);

        if (!canSeek
                || serviceBinder == null) {
            return;
        }

        serviceBinder.setTime(resumeTime);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState == null) {
            return;
        }

        resumeIsPlaying = savedInstanceState.getBoolean(IsPlayingKey, true);
        resumeTime = savedInstanceState.getLong(TimeKey, 0);
        resumeLength = savedInstanceState.getLong(LengthKey, 0);

        configure(
                resumeIsPlaying,
                resumeTime,
                resumeLength
        );
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(IsPlayingKey, resumeIsPlaying);
        outState.putLong(TimeKey, resumeTime);
        outState.putLong(LengthKey, resumeLength);

        super.onSaveInstanceState(outState);
    }

    private void attachSurfaces() {
        if (serviceBinder == null) {
            return;
        }

        serviceBinder.attachSurfaces(
                surfaceMedia,
                surfaceSubtitle,
                this
        );
    }

    private void detachSurfaces() {
        if (serviceBinder == null) {
            return;
        }

        serviceBinder.detachSurfaces();
    }

    private void changeMediaPlayerLayout(int displayW, int displayH) {
        /* Change the video placement using the MediaPlayer API */
        switch (sizePolicy) {
            case SURFACE_BEST_FIT:
                serviceBinder.setAspectRatio(null);
                serviceBinder.setScale(0);
                break;
            case SURFACE_FIT_SCREEN:
            case SURFACE_FILL: {
                Media.VideoTrack videoTrack = serviceBinder.getCurrentVideoTrack();
                if (videoTrack == null)
                    return;
                final boolean videoSwapped = videoTrack.orientation == Media.VideoTrack.Orientation.LeftBottom
                        || videoTrack.orientation == Media.VideoTrack.Orientation.RightTop;
                if (sizePolicy == SURFACE_FIT_SCREEN) {
                    int videoW = videoTrack.width;
                    int videoH = videoTrack.height;

                    if (videoSwapped) {
                        int swap = videoW;
                        videoW = videoH;
                        videoH = swap;
                    }
                    if (videoTrack.sarNum != videoTrack.sarDen)
                        videoW = videoW * videoTrack.sarNum / videoTrack.sarDen;

                    float ar = videoW / (float) videoH;
                    float dar = displayW / (float) displayH;

                    float scale;
                    if (dar >= ar)
                        scale = displayW / (float) videoW; /* horizontal */
                    else
                        scale = displayH / (float) videoH; /* vertical */
                    serviceBinder.setScale(scale);
                    serviceBinder.setAspectRatio(null);
                } else {
                    serviceBinder.setScale(0);
                    serviceBinder.setAspectRatio(!videoSwapped ? "" + displayW + ":" + displayH
                            : "" + displayH + ":" + displayW);
                }
                break;
            }
            case SURFACE_16_9:
                serviceBinder.setAspectRatio("16:9");
                serviceBinder.setScale(0);
                break;
            case SURFACE_4_3:
                serviceBinder.setAspectRatio("4:3");
                serviceBinder.setScale(0);
                break;
            case SURFACE_ORIGINAL:
                serviceBinder.setAspectRatio(null);
                serviceBinder.setScale(1);
                break;
        }
    }

    private void updateVideoSurfaces() {
        if (serviceBinder == null) {
            return;
        }

        int sw = getActivity().getWindow().getDecorView().getWidth();
        int sh = getActivity().getWindow().getDecorView().getHeight();

        // sanity check
        if (sw * sh == 0) {
            return;
        }

        serviceBinder.getVout().setWindowSize(sw, sh);

        ViewGroup.LayoutParams lp = surfaceMedia.getLayoutParams();

        if (mVideoWidth * mVideoHeight == 0) {
            /* Case of OpenGL vouts: handles the placement of the video using MediaPlayer API */
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            surfaceMedia.setLayoutParams(lp);
            lp = surfaceFrame.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            surfaceFrame.setLayoutParams(lp);
            changeMediaPlayerLayout(sw, sh);
            return;
        }

        if (lp.width == lp.height && lp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
            /* We handle the placement of the video using Android View LayoutParams */
            serviceBinder.setAspectRatio(null);
            serviceBinder.setScale(0);
        }

        double dw = sw, dh = sh;
        final boolean isPortrait = ResourceUtil.deviceIsPortraitOriented(getContext());

        if (sw > sh && isPortrait || sw < sh && !isPortrait) {
            dw = sh;
            dh = sw;
        }

        // compute the aspect ratio
        double ar, vw;
        if (mVideoSarDen == mVideoSarNum) {
            /* No indication about the density, assuming 1:1 */
            vw = mVideoVisibleWidth;
            ar = (double) mVideoVisibleWidth / (double) mVideoVisibleHeight;
        } else {
            /* Use the specified aspect ratio */
            vw = mVideoVisibleWidth * (double) mVideoSarNum / mVideoSarDen;
            ar = vw / mVideoVisibleHeight;
        }

        // compute the display aspect ratio
        double dar = dw / dh;

        switch (sizePolicy) {
            case SURFACE_BEST_FIT:
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_FIT_SCREEN:
                if (dar >= ar)
                    dh = dw / ar; /* horizontal */
                else
                    dw = dh * ar; /* vertical */
                break;
            case SURFACE_FILL:
                break;
            case SURFACE_16_9:
                ar = 16.0 / 9.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_4_3:
                ar = 4.0 / 3.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_ORIGINAL:
                dh = mVideoVisibleHeight;
                dw = vw;
                break;
        }

        // set display size
        lp.width = (int) Math.ceil(dw * mVideoWidth / mVideoVisibleWidth);
        lp.height = (int) Math.ceil(dh * mVideoHeight / mVideoVisibleHeight);
        surfaceMedia.setLayoutParams(lp);
        if (surfaceSubtitle != null)
            surfaceSubtitle.setLayoutParams(lp);

        // set frame size (crop if necessary)
        lp = surfaceFrame.getLayoutParams();
        lp.width = (int) Math.floor(dw);
        lp.height = (int) Math.floor(dh);
        surfaceFrame.setLayoutParams(lp);

        surfaceMedia.invalidate();
        if (surfaceSubtitle != null)
            surfaceSubtitle.invalidate();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onNewVideoLayout(
            IVLCVout vOut,
            int width,
            int height,
            int visibleWidth,
            int visibleHeight,
            int sarNum,
            int sarDen
    ) {
        mVideoWidth = width;
        mVideoHeight = height;
        mVideoVisibleWidth = visibleWidth;
        mVideoVisibleHeight = visibleHeight;
        mVideoSarNum = sarNum;
        mVideoSarDen = sarDen;
        updateVideoSurfaces();
    }


}
