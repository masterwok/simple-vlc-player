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
import com.masterwok.simplevlcplayer.models.SelectionItem;
import com.masterwok.simplevlcplayer.observables.RendererItemObservable;
import com.masterwok.simplevlcplayer.services.MediaPlayerService;
import com.masterwok.simplevlcplayer.utils.ResourceUtil;
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

    public static final String Tag = "tag.rendereritemdialogfragment";

    /**
     * This interface should be implemented by activities who started
     * the dialog fragment should they want a result when a renderer item
     * is selected.
     */
    public interface RendererItemSelectionListener {

        /**
         * Invoked when a renderer item is selected.
         *
         * @param rendererItem The selected renderer item. This value can be null.
         */
        void onRendererUpdate(RendererItem rendererItem);
    }

    private static final float DimAmount = 0.6f;

    private MediaPlayerService.Binder serviceBinder;

    private ListView listViewRendererItems;
    private SelectionListAdapter<RendererItem> rendererItemAdapter;

    /**
     * This observer is responsible for updating the display state
     * when renderer items are added or removed.
     */
    private final Observer rendererItemObserver = (observable, o) -> {
        if (o == null) {
            return;
        }

        //noinspection unchecked
        configure(
                (List<RendererItem>) o,
                serviceBinder.getRendererItem()
        );
    };


    /**
     * This service connection is responsible for registering an observer against the
     * media player service renderer item observable, and setting the initial display state.
     */
    private final ServiceConnection mediaPlayerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(
                ComponentName componentName,
                IBinder iBinder
        ) {
            serviceBinder = (MediaPlayerService.Binder) iBinder;

            RendererItemObservable rendererItemObservable = serviceBinder
                    .getRendererItemObservable();

            // Configure display state with initial renderer items.
            configure(
                    rendererItemObservable.getRenderItems(),
                    serviceBinder.getRendererItem()
            );

            // Register renderer item observer.
            rendererItemObservable.addObserver(rendererItemObserver);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceBinder = null;
        }
    };

    /**
     * Configure the display state of the dialog.
     *
     * @param rendererItems        The available renderer items.
     * @param selectedRendererItem The selected renderer item.
     */
    private void configure(
            List<RendererItem> rendererItems,
            RendererItem selectedRendererItem
    ) {
        List<SelectionItem<RendererItem>> selectionItems = new ArrayList<>();

        selectionItems.add(new SelectionItem<>(
                rendererItems.size() == 0 || selectedRendererItem == null,
                ResourceUtil.getStringResource(
                        getContext(),
                        R.string.dialog_none
                ),
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

        bindViewComponents(view);
        subscribeToViewComponents();

        initRendererItemAdapter();

        return createDialog(view);
    }

    /**
     * Subscribe to bound view component callbacks.
     */
    private void subscribeToViewComponents() {
        // On list view item tap, set the renderer item and dismiss the dialog.
        listViewRendererItems.setOnItemClickListener((parent, itemView, position, id) -> {
            notifyRendererItemListener(position);
            dismiss();
        });
    }

    /**
     * Initialize the renderer item list view adapter.
     */
    private void initRendererItemAdapter() {
        rendererItemAdapter = new SelectionListAdapter<>(
                getContext(),
                R.drawable.ic_check_black,
                R.color.player_dialog_check
        );

        listViewRendererItems.setAdapter(rendererItemAdapter);
    }

    /**
     * Notify the renderer item selection listener of a selection.
     *
     * @param position The position of the selected item.
     */
    private void notifyRendererItemListener(int position) {
        RendererItem selectedItem = null;

        try {
            selectedItem = rendererItemAdapter
                    .getItem(position)
                    .getValue();
        } catch (Exception ignored) {
            // Renderer item may not exist at this point.
        }

        serviceBinder.setSelectedRendererItem(selectedItem);
    }

    /**
     * Bind view components to private fields.
     *
     * @param view The dialog view.
     */
    private void bindViewComponents(View view) {
        listViewRendererItems = view.findViewById(R.id.listview_renderer_item);
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

        serviceBinder
                .getRendererItemObservable()
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