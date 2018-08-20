package com.masterwok.simplevlcplayer.viewmodels

import android.arch.lifecycle.ViewModel
import com.masterwok.opensubtitlesandroid.OpenSubtitlesUrlBuilder
import com.masterwok.opensubtitlesandroid.models.OpenSubtitleItem
import com.masterwok.opensubtitlesandroid.services.contracts.OpenSubtitlesService
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.withContext
import javax.inject.Inject

class SubtitlesDialogFragmentViewModel @Inject constructor(
        private val openSubtitlesService: OpenSubtitlesService
) : ViewModel() {

    suspend fun querySubtitles(mediaName: String): List<OpenSubtitleItem> = withContext(CommonPool) {
        val url = OpenSubtitlesUrlBuilder()
                .query(mediaName)
                .build()

        openSubtitlesService.search(
                com.masterwok.opensubtitlesandroid.services.OpenSubtitlesService.TemporaryUserAgent
                , url
        ).filter { it.SubFormat.toLowerCase() == "srt" }
    }

}