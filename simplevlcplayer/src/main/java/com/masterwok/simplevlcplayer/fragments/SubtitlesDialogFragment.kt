package com.masterwok.simplevlcplayer.fragments

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.masterwok.opensubtitlesandroid.models.OpenSubtitleItem
import com.masterwok.simplevlcplayer.common.AndroidJob
import com.masterwok.simplevlcplayer.R
import com.masterwok.simplevlcplayer.adapters.SelectionListAdapter
import com.masterwok.simplevlcplayer.dagger.injectors.InjectableAppCompatDialogFragment
import com.masterwok.simplevlcplayer.models.SelectionItem
import com.masterwok.simplevlcplayer.viewmodels.SubtitlesDialogFragmentViewModel
import kotlinx.android.synthetic.main.dialog_subtitles.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
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

    private val rootJob: AndroidJob = AndroidJob(lifecycle)

    private lateinit var adapterSubtitles: SelectionListAdapter<OpenSubtitleItem>
    private lateinit var dialogView: View

    private fun inflateView(): View = requireActivity()
            .layoutInflater
            .inflate(R.layout.dialog_subtitles, null)

    private fun subscribeToViewComponents() {
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

    override fun onCreateView(
            inflater: LayoutInflater
            , container: ViewGroup?
            , savedInstanceState: Bundle?
    ): View? = dialogView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        dialogView = inflateView()

        return createDialog(dialogView)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapterSubtitles = SelectionListAdapter(
                context
                , R.drawable.ic_check_black
                , R.color.player_dialog_check
        )

        listViewSubtitles.adapter = adapterSubtitles

        querySubtitles(arguments!!.getString(MediaNameKey))
    }

    private fun querySubtitles(mediaName: String) = launch(UI, parent = rootJob) {
        val subtitles = viewModel.querySubtitles(mediaName)

        adapterSubtitles.configure(subtitles.map {
            SelectionItem(false
                    , it.SubFileName
                    , it
            )
        })
    }

}