package com.masterwok.simplevlcplayer.services

import android.app.Notification
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent
import android.view.SurfaceView
import android.widget.Toast

import com.masterwok.simplevlcplayer.R
import com.masterwok.simplevlcplayer.common.utils.AudioUtil
import com.masterwok.simplevlcplayer.common.utils.BitmapUtil
import com.masterwok.simplevlcplayer.common.utils.FileUtil
import com.masterwok.simplevlcplayer.common.utils.NotificationUtil
import com.masterwok.simplevlcplayer.common.utils.ResourceUtil
import com.masterwok.simplevlcplayer.contracts.MediaPlayer
import com.masterwok.simplevlcplayer.contracts.VlcMediaPlayer
import com.masterwok.simplevlcplayer.dagger.injectors.InjectableService
import com.masterwok.simplevlcplayer.observables.RendererItemObservable
import com.masterwok.simplevlcplayer.services.binders.MediaPlayerServiceBinder

import org.videolan.libvlc.Dialog
import org.videolan.libvlc.IVLCVout
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.RendererItem

import java.lang.ref.WeakReference

import javax.inject.Inject

class MediaPlayerService : InjectableService(), MediaPlayer.Callback, Dialog.Callbacks {


    @Inject
    @JvmField
    var libVlc: LibVLC? = null

    @Inject
    @JvmField
    var player: VlcMediaPlayer? = null

    var rendererItemObservable: RendererItemObservable? = null
    var mediaSession: MediaSessionCompat? = null
    var callback: MediaPlayer.Callback? = null

    private var audioFocusChangeListener: WeakReference<AudioManager.OnAudioFocusChangeListener>? = null
    private var audioManager: AudioManager? = null
    private var notificationManager: NotificationManager? = null
    private var stateBuilder: PlaybackStateCompat.Builder? = null
    private var binder: MediaPlayerServiceBinder? = null
    private var mediaBitmap: Bitmap? = null
    private var defaultBitmap: Bitmap? = null

    private var lastUpdateTime = 0L

    companion object {

        private const val Tag = "MediaPlayerService"

        const val RendererClearedAction = "action.rendererclearedaction"
        const val RendererSelectionAction = "action.rendererselectionaction"

        private const val MediaPlayerServiceChannelName = "Media Player Service"
        private const val MediaPlayerServiceChannelId = "channel.mediaplayerservice"
        private const val SimpleVlcSessionTag = "tag.simplevlcsession"
        private const val MediaPlayerServiceNotificationId = 1

        private fun getPauseAction(context: Context): NotificationCompat.Action {
            return NotificationCompat.Action(
                    R.drawable.ic_pause_black_36dp,
                    "Pause",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                            context,
                            PlaybackStateCompat.ACTION_PAUSE)
            )
        }

        private fun getPlayAction(context: Context): NotificationCompat.Action {
            return NotificationCompat.Action(
                    R.drawable.ic_play_arrow_black_36dp,
                    "Play",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                            context,
                            PlaybackStateCompat.ACTION_PLAY)
            )
        }

        private fun getStopAction(context: Context): NotificationCompat.Action {
            return NotificationCompat.Action(
                    R.drawable.ic_clear_black_36dp,
                    "Stop",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                            context,
                            PlaybackStateCompat.ACTION_STOP)
            )
        }
    }

    val vOut: IVLCVout? get() = player?.vOut

    val currentVideoTrack: Media.VideoTrack? get() = player?.currentVideoTrack

    var selectedRendererItem: RendererItem?
        get() = player?.selectedRendererItem
        set(rendererItem) {
            // No need for local audio focus, abandon it.
            if (rendererItem != null) {
                abandonAudioFocus()
            }

            player?.detachSurfaces()
            player?.setRendererItem(rendererItem)

            sendRendererSelectedBroadcast(rendererItem)
        }

    var selectedSubtitleUri: Uri?
        get() = player?.selectedSubtitleUri
        set(subtitleUri) {
            player?.setSubtitleUri(subtitleUri)
        }

    val isPlaying: Boolean get() = player?.isPlaying == true

    override fun onCreate() {
        super.onCreate()

        audioFocusChangeListener = WeakReference(createAudioFocusListener())
        audioManager = AudioUtil.getAudioManager(applicationContext)

        Dialog.setCallbacks(libVlc, this)

        defaultBitmap = BitmapUtil.drawableToBitmap(ResourceUtil.getDrawable(
                applicationContext,
                R.drawable.ic_stream_cover
        ))

        binder = MediaPlayerServiceBinder(this)

        notificationManager = NotificationUtil.getNotificationManager(
                applicationContext
        )

        stateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_SEEK_TO)
                .setState(PlaybackStateCompat.STATE_PAUSED, 0L, 1f)

        createMediaSession()
        createNotificationChannel()

        player?.callback = this

        rendererItemObservable = RendererItemObservable(libVlc)
        rendererItemObservable?.start()
    }

    override fun onStartCommand(
            intent: Intent?
            , flags: Int
            , startId: Int
    ): Int {
        // Pass notification button intents to the media session callback.
        MediaButtonReceiver.handleIntent(mediaSession, intent)

        return START_STICKY
    }

    override fun onDestroy() {
        stopForeground(true)
        Dialog.setCallbacks(libVlc, null)
        player?.release()
        libVlc?.release()
        mediaSession?.release()
        rendererItemObservable?.stop()
        binder = null
        player = null
        libVlc = null
        mediaSession = null
        rendererItemObservable = null

        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onPlayerOpening() {
        lastUpdateTime = 0L

        updatePlaybackState()
        updateNotification()

        callback?.onPlayerOpening()
    }

    override fun onPlayerSeekStateChange(canSeek: Boolean) {
        updatePlaybackState()

        callback?.onPlayerSeekStateChange(canSeek)
    }

    override fun onPlayerPlaying() {
        updatePlaybackState()
        updateNotification()

        mediaSession?.isActive = true

        if (player?.selectedRendererItem != null) {
            enterForeground()
        }

        callback?.onPlayerPlaying()
    }

    override fun onPlayerPaused() {
        updatePlaybackState()
        updateNotification()

        callback?.onPlayerPaused()
    }

    override fun onPlayerStopped() {
        updatePlaybackState()
        updateNotification()

        mediaSession?.isActive = false

        stopForeground(true)

        callback?.onPlayerStopped()
    }

    override fun onPlayerEndReached() {
        updatePlaybackState()
        updateNotification()

        mediaSession?.isActive = false

        callback?.onPlayerEndReached()
    }

    override fun onPlayerError() {
        updatePlaybackState()
        updateNotification()

        mediaSession?.isActive = false

        callback?.onPlayerError()
    }

    override fun onPlayerTimeChange(timeChanged: Long) {
        val time = (player?.time ?: 0) / 1000L

        // At least one second has elapsed, update playback state.
        if (time >= lastUpdateTime + 1 || time <= lastUpdateTime) {
            updatePlaybackState()
            lastUpdateTime = time
        }

        callback?.onPlayerTimeChange(timeChanged)
    }

    override fun onBuffering(buffering: Float) {
        updatePlaybackState()

        callback?.onBuffering(buffering)
    }

    override fun onPlayerPositionChanged(positionChanged: Float) {
        callback?.onPlayerPositionChanged(positionChanged)
    }

    override fun onSubtitlesCleared() {
        callback?.onSubtitlesCleared()
    }

    override fun onDisplay(errorMessage: Dialog.ErrorMessage) {

    }

    override fun onDisplay(loginDialog: Dialog.LoginDialog) {

    }

    override fun onDisplay(questionDialog: Dialog.QuestionDialog) {
        val dialogTitle = questionDialog.title ?: return

        when (dialogTitle.toLowerCase()) {
            "broken or missing index" -> onBrokenOrMissingIndexDialog(questionDialog)
            "insecure site" -> onInsecureSiteDialog(questionDialog)
            "performance warning" -> onPerformanceWarningDialog(questionDialog)
            else -> Log.w(Tag, "Unhandled dialog: $dialogTitle")
        }
    }

    private fun onPerformanceWarningDialog(questionDialog: Dialog.QuestionDialog) {
        // Let the user know casting will eat their battery.
        Toast.makeText(
                applicationContext,
                R.string.toast_casting_performance_warning,
                Toast.LENGTH_LONG
        ).show()

        // Accept and dismiss performance warning dialog.
        questionDialog.postAction(1)
        questionDialog.dismiss()
    }

    private fun onInsecureSiteDialog(questionDialog: Dialog.QuestionDialog) {
        if (questionDialog.action1Text.toLowerCase() == "view certificate") {
            questionDialog.postAction(1)
        } else if (questionDialog.action2Text.toLowerCase() == "accept permanently") {
            questionDialog.postAction(2)
        }

        questionDialog.dismiss()
    }

    private fun onBrokenOrMissingIndexDialog(questionDialog: Dialog.QuestionDialog) {
        // Let the user know seeking will not work properly.
        Toast.makeText(
                applicationContext,
                R.string.toast_missing_index_warning,
                Toast.LENGTH_LONG
        ).show()

        questionDialog.postAction(2)
        questionDialog.dismiss()
    }

    override fun onDisplay(progressDialog: Dialog.ProgressDialog) {

    }

    override fun onCanceled(dialog: Dialog) {

    }

    private fun createAudioFocusListener(): AudioManager.OnAudioFocusChangeListener =
            AudioManager.OnAudioFocusChangeListener { focusChange ->
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_GAIN -> setVolume(100)
                    AudioManager.AUDIOFOCUS_LOSS -> pause()
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->
                        // Lower volume, continue playing.
                        setVolume(50)
                }
            }

    private fun enterForeground() {
        mediaSession?.setMetadata(
                getMediaMetadata(mediaBitmap)
        )

        startForeground(
                MediaPlayerServiceNotificationId,
                buildPlaybackNotification()
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        NotificationUtil.createNotificationChannel(
                applicationContext,
                MediaPlayerServiceChannelId,
                MediaPlayerServiceChannelName,
                false,
                false,
                false
        )
    }

    private fun getMediaMetadata(bitmap: Bitmap?): MediaMetadataCompat {
        val builder = MediaMetadataCompat.Builder()

        builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)

        return builder.build()
    }

    private fun updatePlaybackState() {
        stateBuilder?.setBufferedPosition(player?.length ?: 0)
        stateBuilder?.setState(
                if (player?.isPlaying == true)
                    PlaybackStateCompat.STATE_PLAYING
                else
                    PlaybackStateCompat.STATE_PAUSED,
                player?.time ?: 0,
                1f
        )

        mediaSession?.setPlaybackState(stateBuilder?.build())
    }

    private fun updateNotification() {
        if (player?.selectedRendererItem == null) {
            return
        }

        notificationManager?.notify(
                MediaPlayerService.MediaPlayerServiceNotificationId,
                buildPlaybackNotification()
        )

    }

    private fun sendRendererSelectedBroadcast(rendererItem: RendererItem?) {
        val intent = if (rendererItem == null)
            Intent(RendererClearedAction)
        else
            Intent(RendererSelectionAction)

        LocalBroadcastManager
                .getInstance(applicationContext)
                .sendBroadcast(intent)
    }

    private fun createMediaSession() {
        mediaSession = MediaSessionCompat(this, SimpleVlcSessionTag).apply {
            setMediaButtonReceiver(null)
            setCallback(PlayerSessionCallback())
            setPlaybackState(stateBuilder?.build())
            setFlags(
                    MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
        }
    }

    override fun onProgressUpdate(progressDialog: Dialog.ProgressDialog) {

    }

    private inner class PlayerSessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            player?.play()
        }

        override fun onPause() {
            player?.pause()
        }

        override fun onStop() {
            super.onStop()
        }

        override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
            val action = mediaButtonEvent.action

            if (action == null || action != Intent.ACTION_MEDIA_BUTTON) {
                return false
            }

            val keyEvent = mediaButtonEvent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)

            when (keyEvent.keyCode) {
                KeyEvent.KEYCODE_MEDIA_PLAY -> player?.play()
                KeyEvent.KEYCODE_MEDIA_PAUSE -> player?.pause()
                KeyEvent.KEYCODE_MEDIA_STOP -> player?.stop()
            }

            return true
        }
    }

    private fun buildPlaybackNotification(): Notification {
        val context = applicationContext

        val title = player
                ?.media
                ?.uri
                ?.lastPathSegment
                ?: ""

        val builder = NotificationCompat.Builder(
                context,
                MediaPlayerService.MediaPlayerServiceChannelId
        )

        builder.setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setSmallIcon(R.drawable.ic_play_arrow_black_36dp)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setLargeIcon(mediaBitmap)
                .setContentTitle(title)
                .setContentText(null)
                .setTicker(title)
                .setAutoCancel(false)
                .setOngoing(true)

        if (player?.isPlaying == true) {
            builder.addAction(getPauseAction(context))
        } else {
            builder.addAction(getPlayAction(context))
        }

        builder.addAction(getStopAction(context))

        builder.setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession!!.sessionToken)
                .setShowActionsInCompactView(0, 1)
        )

        return builder.build()
    }

    private fun setMediaBitmap(mediaUri: Uri): Bitmap {
        if (mediaBitmap != null) {
            return mediaBitmap as Bitmap
        }

        mediaBitmap = BitmapUtil.getBitmap(
                applicationContext,
                mediaUri
        )

        // Unable to get bitmap, use fallback bitmap.
        if (mediaBitmap == null) {
            mediaBitmap = defaultBitmap
        }

        return mediaBitmap!!
    }


    fun setMedia(
            context: Context?,
            mediaUri: Uri?
    ) {
        if (context == null || mediaUri == null) {
            return
        }

        val schema = mediaUri.scheme

        setMediaBitmap(mediaUri)

        // Use file descriptor when dealing with content schemas.
        if (schema != null && schema == ContentResolver.SCHEME_CONTENT) {
            player?.setMedia(FileUtil.getUriFileDescriptor(
                    context.applicationContext,
                    mediaUri,
                    "r"
            ))

            return
        }

        player?.setMedia(mediaUri)
    }

    fun setSubtitle(subtitleUri: Uri?) = player?.setSubtitleUri(subtitleUri)

    fun play() {
        gainAudioFocus()

        player?.play()
    }

    fun stop() {
        abandonAudioFocus()

        player?.stop()
    }

    private fun gainAudioFocus() {
        // Only gain audio focus when playing locally.
        if (player?.selectedRendererItem != null) {
            return
        }

        AudioUtil.requestAudioFocus(
                audioManager!!,
                audioFocusChangeListener!!.get()
        )
    }

    private fun abandonAudioFocus() {
        audioManager?.abandonAudioFocus(audioFocusChangeListener!!.get())
    }

    fun setTime(time: Long) {
        player?.time = time
    }

    fun setProgress(progress: Int) {
        player?.time = (progress.toFloat() / 100 * (player?.length ?: 0)).toLong()
    }

    fun togglePlayback() {
        if (player?.isPlaying == true) {
            pause()
            return
        }

        play()
    }

    fun pause() = player?.pause()

    fun setAspectRatio(aspectRatio: String?) = player?.setAspectRatio(aspectRatio)

    fun setScale(scale: Float) = player?.setScale(scale)

    fun setVolume(volume: Int) = player?.setVolume(volume)

    fun attachSurfaces(
            surfaceMedia: SurfaceView,
            surfaceSubtitle: SurfaceView,
            listener: IVLCVout.OnNewVideoLayoutListener
    ) = player?.attachSurfaces(
            surfaceMedia,
            surfaceSubtitle,
            listener
    )

    fun detachSurfaces() = player?.detachSurfaces()


}