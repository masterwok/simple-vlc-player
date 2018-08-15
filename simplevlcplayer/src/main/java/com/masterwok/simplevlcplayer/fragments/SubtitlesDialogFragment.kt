package com.masterwok.simplevlcplayer.fragments

import android.app.Dialog
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import com.masterwok.simplevlcplayer.R
import com.masterwok.simplevlcplayer.dagger.injectors.InjectableAppCompatDialogFragment
import com.masterwok.simplevlcplayer.viewmodels.SubtitlesDialogFragmentViewModel
import javax.inject.Inject

class SubtitlesDialogFragment : InjectableAppCompatDialogFragment() {
    companion object {
        const val Tag = "tag.subtitlesdialogfragment"
        const val MediaNameKey = "key.medianame"

        private const val DimAmount = 0.6F

        @JvmStatic
        fun createInstance(mediaName: String): SubtitlesDialogFragment =
                SubtitlesDialogFragment().apply {
                    arguments = Bundle().apply {
                        putString(MediaNameKey, mediaName)
                    }
                }
    }

    @Inject
    lateinit var viewModel: SubtitlesDialogFragmentViewModel

    private lateinit var mediaName: String

    private fun inflateView(): View = requireActivity()
            .layoutInflater
            .inflate(R.layout.dialog_subtitles, null)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        val view = inflateView()

        bindViewComponents(view)
        subscribeToViewComponents()

        mediaName = arguments!!.getString(MediaNameKey)

        // TODO: Populate recycler view.
        AsyncTask.execute {
            val result = viewModel.querySubtitles(mediaName)
            val qwer = 1
        }

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