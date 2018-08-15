package com.masterwok.simplevlcplayer.fragments

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import android.view.View
import com.masterwok.simplevlcplayer.R

class SubtitlesDialogFragment : AppCompatDialogFragment() {
    companion object {
        const val Tag = "tag.subtitlesdialogfragment"

        private const val DimAmount = 0.6F
    }

    private fun inflateView(): View = requireActivity()
            .layoutInflater
            .inflate(R.layout.dialog_subtitles, null)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = inflateView()

        bindViewComponents(view)
        subscribeToViewComponents()

        return createDialog(view)
    }

    private fun subscribeToViewComponents() {
    }

    private fun bindViewComponents(view: View) {

    }

    private fun createDialog(view: View): Dialog =
            AlertDialog.Builder(
                    requireActivity()
                    , R.style.AppTheme_DialogDark
            ).apply {
                setView(view)
                setTitle(R.string.dialog_select_subtitles)
                setNegativeButton(R.string.dialog_button_subtitles_negative, null)
            }.create().apply {
                setCanceledOnTouchOutside(false)
                window.setDimAmount(DimAmount)
            }

}