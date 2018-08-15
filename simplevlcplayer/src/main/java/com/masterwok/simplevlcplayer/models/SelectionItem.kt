package com.masterwok.simplevlcplayer.models


/**
 * This model represents a selection list item that has a display
 * name and backing value.
 */
data class SelectionItem<R>(

        /**
         * Whether or not the item is selected.
         */
        val isSelected: Boolean,

        /**
         * The display name of the item.
         */
        val displayName: String,

        /**
         * The backing value.
         */
        val value: R
)