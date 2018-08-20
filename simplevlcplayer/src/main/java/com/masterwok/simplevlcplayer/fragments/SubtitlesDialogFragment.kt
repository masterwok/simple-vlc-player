package com.masterwok.simplevlcplayer.fragments

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.masterwok.opensubtitlesandroid.models.OpenSubtitleItem
import com.masterwok.simplevlcplayer.R
import com.masterwok.simplevlcplayer.adapters.SelectionListAdapter
import com.masterwok.simplevlcplayer.common.AndroidJob
import com.masterwok.simplevlcplayer.common.extensions.setColor
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
        const val DestinationUriKey = "key.destinationuri"

        private const val DimAmount = 0.6F

        @JvmStatic
        fun createInstance(
                mediaName: String
                , subtitleDestinationUri: Uri?
        ): SubtitlesDialogFragment =
                SubtitlesDialogFragment().apply {
                    arguments = Bundle().apply {
                        putString(MediaNameKey, mediaName)
                        putParcelable(DestinationUriKey, subtitleDestinationUri)
                    }
                }
    }

    @Inject
    lateinit var viewModel: SubtitlesDialogFragmentViewModel

    private lateinit var adapterSubtitles: SelectionListAdapter<OpenSubtitleItem>
    private lateinit var dialogView: View

    private val rootJob: AndroidJob = AndroidJob(lifecycle)

    private fun inflateView(): View = requireActivity()
            .layoutInflater
            .inflate(R.layout.dialog_subtitles, null)

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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        dialogView = inflateView()

        return createDialog(dialogView)
    }

    private fun subscribeToViewComponents() {
        listViewSubtitles.onItemClickListener = AdapterView
                .OnItemClickListener { parent: AdapterView<*>
                                       , itemView: View
                                       , position: Int
                                       , id: Long ->
                    onSubtitleSelected(position)
                }
    }

    private fun setLoadingViewState() {
        progressBarSubtitles.visibility = View.VISIBLE
        listViewSubtitles.visibility = View.GONE
    }

    private fun setLoadedViewState() {
        progressBarSubtitles.visibility = View.GONE
        listViewSubtitles.visibility = View.VISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureListViewAndAdapter()
        subscribeToViewComponents()

        progressBarSubtitles.setColor(R.color.progress_bar_spinner)

        querySubtitles(arguments!!.getString(MediaNameKey))
    }

    private fun configureListViewAndAdapter() {
        adapterSubtitles = SelectionListAdapter(
                context
                , R.drawable.ic_check_black
                , R.color.player_dialog_check
        )

        listViewSubtitles.adapter = adapterSubtitles
    }

    override fun onCreateView(
            inflater: LayoutInflater
            , container: ViewGroup?
            , savedInstanceState: Bundle?
    ): View? = dialogView


    private fun querySubtitles(mediaName: String) = launch(UI, parent = rootJob) {
        setLoadingViewState()

        val subtitles = viewModel.querySubtitles(mediaName)

        adapterSubtitles.configure(subtitles.map {
            SelectionItem(false
                    , it.SubFileName
                    , it
            )
        })

        setLoadedViewState()
    }

    private fun onSubtitleSelected(position: Int) = launch(UI, parent = rootJob) {
        val selectedItem = adapterSubtitles.getItem(position)
        val context = context!!

        viewModel.downloadSubtitleItem(
                context
                , selectedItem.value
                , arguments?.getParcelable(DestinationUriKey)
        )

        dismiss()
    }

}