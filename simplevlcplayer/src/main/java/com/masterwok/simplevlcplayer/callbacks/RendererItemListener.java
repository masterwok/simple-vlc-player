package com.masterwok.simplevlcplayer.callbacks;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.RendererDiscoverer;
import org.videolan.libvlc.RendererItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import static org.videolan.libvlc.RendererDiscoverer.Event.ItemAdded;
import static org.videolan.libvlc.RendererDiscoverer.Event.ItemDeleted;


/**
 * This class is responsible for tracking the available renderer items.
 */
public class RendererItemListener
        extends Observable
        implements RendererDiscoverer.EventListener {

    private ArrayList<RendererItem> rendererItems = new ArrayList<>();
    private ArrayList<RendererDiscoverer> rendererDiscoverers = new ArrayList<>();

    private boolean isStarted = false;

    /**
     * Start listening for renderer item events.
     */
    public void start(LibVLC libVlc) {
        isStarted = true;

        for (RendererDiscoverer.Description discoverer : RendererDiscoverer.list(libVlc)) {
            RendererDiscoverer rendererDiscoverer = new RendererDiscoverer(
                    libVlc,
                    discoverer.name
            );

            rendererDiscoverers.add(rendererDiscoverer);

            rendererDiscoverer.setEventListener(this);
            rendererDiscoverer.start();
        }
    }

    /**
     * Stop listening for renderer item events.
     */
    public void stop() {
        if (!isStarted) {
            return;
        }

        for (RendererDiscoverer discover : rendererDiscoverers) {
            discover.stop();
        }

        for (RendererItem renderItem : rendererItems) {
            renderItem.release();
        }

        rendererDiscoverers.clear();
        rendererItems.clear();

        isStarted = false;
    }

    /**
     * Get all of the renderer items currently available.
     *
     * @return A list of all of the renderer items available.
     */
    public List<RendererItem> getRenderItems() {
        return rendererItems;
    }

    /**
     * Invoked when a renderer item is added or removed.
     *
     * @param event The add or remove event.
     */
    @Override
    public void onEvent(RendererDiscoverer.Event event) {
        switch (event.type) {
            case ItemAdded:
                onItemAdded(event.getItem());
                break;
            case ItemDeleted:
                onItemDeleted(event.getItem());
                break;
        }
    }

    /**
     * Remove renderer item and notify subscribers.
     *
     * @param item The item to remove.
     */
    private void onItemDeleted(RendererItem item) {
        rendererItems.remove(item);

        setChanged();
        notifyObservers(rendererItems);
    }

    /**
     * Add renderer item and notify subscribers.
     *
     * @param item The item to add.
     */
    private void onItemAdded(RendererItem item) {
        rendererItems.add(item);

        setChanged();
        notifyObservers(rendererItems);
    }

}
