@file:Suppress("unused")

package com.masterwok.simplevlcplayer.dagger.extensions

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

fun <T : ViewModel> T.createFactory(): ViewModelProvider.Factory {
    val viewModel = this

    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return viewModel as T
        }
    }
}