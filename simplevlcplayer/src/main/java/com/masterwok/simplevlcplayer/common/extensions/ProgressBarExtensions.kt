package com.masterwok.simplevlcplayer.common.extensions

import android.support.v4.content.ContextCompat
import android.widget.ProgressBar


/**
 * Set the color of the [ProgressBar] using the provided [colorId].
 */
internal fun ProgressBar.setColor(colorId: Int) = indeterminateDrawable.setColorFilter(
        ContextCompat.getColor(context, colorId)
        , android.graphics.PorterDuff.Mode.SRC_IN
)