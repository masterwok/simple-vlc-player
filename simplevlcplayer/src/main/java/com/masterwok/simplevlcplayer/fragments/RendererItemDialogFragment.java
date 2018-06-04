package com.masterwok.simplevlcplayer.fragments;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.View;
import android.widget.ListView;

import com.masterwok.simplevlcplayer.R;
import com.masterwok.simplevlcplayer.adapters.SelectionListAdapter;
import com.masterwok.simplevlcplayer.callbacks.RendererItemListener;
import com.masterwok.simplevlcplayer.models.SelectionItem;
import com.masterwok.simplevlcplayer.services.MediaPlayerService;
import com.masterwok.simplevlcplayer.utils.ThreadUtil;
import com.masterwok.simplevlcplayer.utils.ViewUtil;

import org.videolan.libvlc.RendererItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;


/**
 * This dialog fragment is used for selecting renderer items.
 */
public class RendererItemDialogFragment
        extends AppCompatDialogFragment {

    private static final float DimAmount = 0.6f;

    private MediaPlayerService.MediaPlayerServiceBinder mediaPlayerServiceBinder;

    private ListView listViewRendererItem;
    private SelectionListAdapter<RendererItem> rendererItemAdapter;

    private final Observer rendererItemObserver = (observable, o) -> {
        if (o == null) {
            return;
        }

        //noinspection unchecked
        configure(
                (List<RendererItem>) o,
                mediaPlayerServiceBinder.getSelectedRendererItem()
        );
    };


    private final ServiceConnection mediaPlayerServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(
                ComponentName componentName,
                IBinder iBinder
        ) {
            mediaPlayerServiceBinder = (MediaPlayerService.MediaPlayerServiceBinder) iBinder;

            RendererItemListener rendererItemObservable = mediaPlayerServiceBinder
                    .getRenderItemObservable();

            configure(
                    rendererItemObservable.getRenderItems(),
                    mediaPlayerServiceBinder.getSelectedRendererItem()
            );

            mediaPlayerServiceBinder
                    .getRenderItemObservable()
                    .addObserver(rendererItemObserver);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mediaPlayerServiceBinder = null;
        }
    };

    private void configure(
            List<RendererItem> rendererItems,
            RendererItem selectedRendererItem
    ) {
        List<SelectionItem<RendererItem>> selectionItems = new ArrayList<>();

        selectionItems.add(new SelectionItem<>(
                rendererItems.size() == 0,
                "None",
                null
        ));

        for (RendererItem rendererItem : rendererItems) {
            selectionItems.add(new SelectionItem<>(
                    selectedRendererItem != null
                            && rendererItem.name.equals(selectedRendererItem.name),
                    rendererItem.displayName,
                    rendererItem
            ));
        }

        ThreadUtil.onMain(() -> rendererItemAdapter.configure(selectionItems));
    }

    /**
     * Inflate the View to be contained within the Dialog.
     *
     * @return A new View instance.
     */
    private View inflateView() {
        return getActivity()
                .getLayoutInflater()
                .inflate(R.layout.dialog_renderer_item, null);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = inflateView();

        bindViewControls(view);
        initRendererItemAdapter();

        return createDialog(view);
    }

    private void initRendererItemAdapter() {
        rendererItemAdapter = new SelectionListAdapter<>(
                getContext(),
                R.drawable.ic_check_black,
                R.color.media_player_dialog_check
        );

        listViewRendererItem.setAdapter(rendererItemAdapter);

        listViewRendererItem.setOnItemClickListener((parent, itemView, position, id) -> {
            RendererItem rendererItem = rendererItemAdapter
                    .getItem(position)
                    .getValue();

            mediaPlayerServiceBinder.setRenderer(rendererItem);

            dismiss();
        });
    }

    private void bindViewControls(View view) {
        listViewRendererItem = view.findViewById(R.id.listview_renderer_item);
    }

    /**
     * Create the dialog with the supplied view parameter.
     *
     * @param view The view contained within the dialog.
     * @return A new Dialog instance.
     */
    private Dialog createDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                getActivity(),
                R.style.AppTheme_DialogDark
        );

        builder.setView(view);
        builder.setTitle(R.string.dialog_title_renderer);
        builder.setNegativeButton(R.string.dialog_button_renderer_negative, null);

        AlertDialog dialog = builder.create();

        ViewUtil.setDimAmount(dialog.getWindow(), DimAmount);

        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }


    @Override
    public void onStart() {
        super.onStart();

        startAndBindMediaPlayerService();
    }

    @Override
    public void onStop() {
        //noinspection ConstantConditions
        getActivity()
                .unbindService(mediaPlayerServiceConnection);

        mediaPlayerServiceBinder
                .getRenderItemObservable()
                .deleteObserver(rendererItemObserver);

        super.onStop();
    }

    private void startAndBindMediaPlayerService() {
        Intent intent = new Intent(
                getActivity().getApplicationContext(),
                MediaPlayerService.class
        );

        //noinspection ConstantConditions
        getActivity().startService(intent);

        getActivity().bindService(
                intent,
                mediaPlayerServiceConnection,
                Context.BIND_AUTO_CREATE
        );

    }

}
