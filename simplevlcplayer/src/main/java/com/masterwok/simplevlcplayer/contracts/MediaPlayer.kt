package com.masterwok.simplevlcplayer.contracts

import android.net.Uri

/**
 * This contract provides a way to interact with a media player.
 */
interface MediaPlayer {

    var time: Long

    var callback: Callback?

    val length: Long

    val isPlaying: Boolean

    fun release()

    fun play()

    fun pause()

    fun stop()

    fun setMedia(uri: Uri?)

    fun setSubtitleUri(uri: Uri?)

    interface Callback {
        fun onPlayerOpening()

        fun onPlayerSeekStateChange(canSeek: Boolean)

        fun onPlayerPlaying()

        fun onPlayerPaused()

        fun onPlayerStopped()

        fun onPlayerEndReached()

        fun onPlayerError()

        fun onPlayerTimeChange(timeChanged: Long)

        fun onBuffering(buffering: Float)

        fun onPlayerPositionChanged(positionChanged: Float)

        fun onSubtitlesCleared()
    }
}