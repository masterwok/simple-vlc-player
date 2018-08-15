package com.masterwok.simplevlcplayer.viewmodels

import android.arch.lifecycle.ViewModel
import com.masterwok.opensubtitlesandroid.OpenSubtitlesUrlBuilder
import com.masterwok.opensubtitlesandroid.models.OpenSubtitleItem
import com.masterwok.opensubtitlesandroid.services.contracts.OpenSubtitlesService
import javax.inject.Inject

class SubtitlesDialogFragmentViewModel @Inject constructor(
        private val openSubtitlesService: OpenSubtitlesService
) : ViewModel() {

    fun querySubtitles(mediaName: String): List<OpenSubtitleItem> {
        val url = OpenSubtitlesUrlBuilder()
                .query(mediaName)
                .build()

        return openSubtitlesService.search(
                com.masterwok.opensubtitlesandroid.services.OpenSubtitlesService.TemporaryUserAgent
                , url
        ).filter { it.SubFormat.toLowerCase() == "srt" }
    }

}