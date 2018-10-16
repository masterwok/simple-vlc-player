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
            mediaName: String
            , openSubtitlesUserAgent: String?
            , subtitleLanguageCode: String?
    ): List<OpenSubtitleItem> = withContext(CommonPool) {
        val url = OpenSubtitlesUrlBuilder()
                .query(mediaName)
                .subLanguageId(subtitleLanguageCode ?: "eng")
                .build()

        val tmpUserAgent = openSubtitlesUserAgent
                ?: com.masterwok.opensubtitlesandroid.services.OpenSubtitlesService.TemporaryUserAgent

        openSubtitlesService
                .search(tmpUserAgent, url)
                .toList()
    }

    suspend fun downloadSubtitleItem(
            context: Context
            , openSubtitleItem: OpenSubtitleItem
            , destinationUri: Uri?
    ): Uri = withContext(CommonPool) {
        val downloadedSubtitleFileUri = getSubtitleItemDownloadUri(
                context
                , openSubtitleItem
                , destinationUri
        )

        openSubtitlesService.downloadSubtitle(
                context
                , openSubtitleItem
                , downloadedSubtitleFileUri
        )

        downloadedSubtitleFileUri
    }

    fun getSubtitleItemDownloadUri(
            context: Context
            , openSubtitleItem: OpenSubtitleItem
            , destinationUri: Uri?
    ): Uri = Uri.fromFile(File(
            (destinationUri ?: Uri.fromFile(context.cacheDir)).path
            , openSubtitleItem.getUniqueName()
    ))

    private fun OpenSubtitleItem.getUniqueName(): String = "${IDMovieImdb}_" +
            "${IDMovie}_" +
            "${IDSubMovieFile}_" +
            "${IDSubtitle}_" +
            "${IDSubtitleFile}_" +
            "${SubFileName.substringBeforeLast(".")}.$SubLanguageID.$SubFormat"

}