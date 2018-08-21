package com.masterwok.simplevlcplayer.viewmodels

import android.arch.lifecycle.ViewModel
import android.content.Context
import android.net.Uri
import com.masterwok.opensubtitlesandroid.OpenSubtitlesUrlBuilder
import com.masterwok.opensubtitlesandroid.models.OpenSubtitleItem
import com.masterwok.opensubtitlesandroid.services.contracts.OpenSubtitlesService
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.withContext
import java.io.File
import javax.inject.Inject

class SubtitlesDialogFragmentViewModel @Inject constructor(
        private val openSubtitlesService: OpenSubtitlesService
) : ViewModel() {

    suspend fun querySubtitles(
            openSubtitlesUserAgent: String?
            , mediaName: String
    ): List<OpenSubtitleItem> = withContext(CommonPool) {
        val url = OpenSubtitlesUrlBuilder()
                .query(mediaName)
                .build()

        val tmpUserAgent = openSubtitlesUserAgent
                ?: com.masterwok.opensubtitlesandroid.services.OpenSubtitlesService.TemporaryUserAgent

        openSubtitlesService
                .search(tmpUserAgent, url)
                .filter { it.SubFormat.toLowerCase() == "srt" }
    }

    suspend fun downloadSubtitleItem(
            context: Context
            , openSubtitleItem: OpenSubtitleItem
            , destinationUri: Uri?
    ): Uri = withContext(CommonPool) {
        // Default to cache directory if destination Uri was not provided.
        val outputFile = Uri.fromFile(File(
                (destinationUri ?: Uri.fromFile(context.cacheDir)).path
                , openSubtitleItem.SubFileName
        ))

        openSubtitlesService.downloadSubtitle(
                context
                , openSubtitleItem
                , outputFile
        )

        outputFile
    }

}