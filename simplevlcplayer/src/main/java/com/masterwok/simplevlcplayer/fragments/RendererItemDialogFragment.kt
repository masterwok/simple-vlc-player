package com.masterwok.simplevlcplayer.fragments

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.masterwok.simplevlcplayer.R
import com.masterwok.simplevlcplayer.adapters.SelectionListAdapter
import com.masterwok.simplevlcplayer.common.AndroidJob
import com.masterwok.simplevlcplayer.models.SelectionItem
import kotlinx.android.synthetic.main.dialog_renderer_item.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.videolan.libvlc.RendererItem
import java.util.*

class RendererItemDialogFragment : MediaPlayerServiceDialogFragment() {

    companion object {
        const val Tag = "tag.rendereritemdialogfragment"

        private const val DimAmount = 0.6F
    }

    private val rootJob: AndroidJob = AndroidJob(lifecycle)

    private lateinit var dialogView: View
    private lateinit var rendererItemAdapter: SelectionListAdapter<RendererItem>

    private val rendererItemObserver = Observer { _: Observable?, value: Any? ->
        @Suppress("UNCHECKED_CAST")
        configure(
                value as List<RendererItem>
                , serviceBinder?.selectedRendererItem
        )
    }

    private fun configure(
            rendererItems: List<RendererItem>?
            , selectedRendererItem: RendererItem?
    ) {
        val selectionItems = ArrayList(rendererItems?.map {
            SelectionItem<RendererItem>(
                    selectedRendererItem != null && it.name == selectedRendererItem.name
                    , it.displayName
                    , it
            )
        })

        selectionItems.add(0, SelectionItem<RendererItem>(
                rendererItems?.isEmpty() == true || selectedRendererItem == null
                , getString(R.string.dialog_none)
                , null
        ))

        launch(UI, parent = rootJob) { rendererItemAdapter.configure(selectionItems) }
    }

    override fun onServiceConnected() {
        val rendererItemObservable = serviceBinder?.rendererItemObservable

        configure(
                rendererItemObservable?.renderItems
                , serviceBinder?.selectedRendererItem
        )

        rendererItemObservable?.addObserver(rendererItemObserver)
    }

    private fun inflateView(): View = requireActivity()
            .layoutInflater
            .inflate(R.layout.dialog_renderer_item, null)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = inflateView()

        dialogView = view

        return createDialog(view)
    }

    private fun createDialog(view: View): Dialog =
            AlertDialog.Builder(
                    requireActivity()
                    , R.style.AppTheme_DialogDark
            ).apply {
                setView(view)
                setTitle(R.string.dialog_title_renderer)
                setNegativeButton(R.string.dialog_button_renderer_negative, null)
            }.create().apply {
                setCanceledOnTouchOutside(false)
                window.setDimAmount(DimAmount)
            }

    override fun onCreateView(
            inflater: LayoutInflater
            , container: ViewGroup?
            , savedInstanceState: Bundle?
    ): View? = dialogView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRendererItemAdapter()
        subscribeToViewComponents()
    }

    private fun initRendererItemAdapter() {
        rendererItemAdapter = SelectionListAdapter(
                context,
                R.drawable.ic_check_black,
                R.color.player_dialog_check
        )

        listview_renderer_item.adapter = rendererItemAdapter
    }

    private fun subscribeToViewComponents() {
        // On list view item tap, set the renderer item and dismiss the dialog.
        listview_renderer_item.setOnItemClickListener { parent, itemView, position, id ->
            notifyRendererItemListener(position)
            dismiss()
        }
    }

    private fun notifyRendererItemListener(position: Int) {
        serviceBinder?.selectedRendererItem = rendererItemAdapter
                .getItem(position)
                ?.value
    }

    override fun onStop() {

        serviceBinder
                ?.rendererItemObservable
                ?.deleteObserver(rendererItemObserver)

        super.onStop()
    }

    override fun show(manager: FragmentManager?, tag: String?) {
        val transaction = manager?.beginTransaction()
        transaction?.add(this, tag)
        transaction?.commitAllowingStateLoss()
    }

}