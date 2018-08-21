package com.masterwok.simplevlcplayer.common.extensions

import android.content.Context
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat

/**
 * Get color using [ContextCompat] and the provided [id].
 */
internal fun Context.getCompatColor(@ColorRes id: Int) = ContextCompat.getColor(this, id)