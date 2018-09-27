package com.masterwok.simplevlcplayer.common.extensions

import android.content.Context
import android.net.Uri
import android.support.v4.provider.DocumentFile
import android.webkit.URLUtil
import java.lang.IllegalArgumentException


/**
 * Get the display name of a Content Uri or the last path segment of
 */
fun Uri?.getName(context: Context): String {
    require(this != null)

    return when {
        URLUtil.isContentUrl(toString()) -> DocumentFile.fromSingleUri(context, this).name
        URLUtil.isFileUrl(toString()) -> this!!.lastPathSegment
        else -> throw IllegalArgumentException("Uri must have File or Content schema.")
    }
}