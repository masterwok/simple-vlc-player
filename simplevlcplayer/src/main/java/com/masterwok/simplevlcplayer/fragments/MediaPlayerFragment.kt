package com.masterwok.simplevlcplayer.fragments

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import com.masterwok.simplevlcplayer.services.binders.MediaPlayerServiceBinder

class MediaPlayerFragment {

    enum class Type {
        LOCAL,
        CAST
    }

    companion object {

        private const val MediaUriKey = "bundle.mediauri"
        private const val SubtitleUriKey = "bundle.subtitleuri"
        private const val SubtitleDestinationUriKey = "bundle.subtitledestinationuri"
        private const val SubtitleLanguageCodeKey = "bundle.subtitlelanguagecode"
        private const val OpenSubtitlesUserAgentKey = "bundle.useragent"

        @JvmStatic
        fun createInstance(
                type: Type
                , mediaUri: Uri
                , subtitleUri: Uri?
                , subtitleDestinationUri: Uri
                , subtitlesLanguageCode: String
                , openSubtitlesUserAgent: String
        ): Fragment {
            val bundle = Bundle().apply {
                putParcelable(MediaUriKey, mediaUri)
                putParcelable(SubtitleUriKey, subtitleUri)
                putParcelable(SubtitleDestinationUriKey, subtitleDestinationUri)
                putString(SubtitleLanguageCodeKey, subtitlesLanguageCode)
                putString(OpenSubtitlesUserAgentKey, openSubtitlesUserAgent)
            }

            return when (type) {
                Type.LOCAL -> LocalPlayerFragment().apply { arguments = bundle }
                Type.CAST -> RendererPlayerFragment().apply { arguments = bundle }
            }
        }

    }


}