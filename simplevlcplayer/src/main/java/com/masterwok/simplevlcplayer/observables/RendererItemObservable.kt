package com.masterwok.simplevlcplayer.observables


import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.RendererDiscoverer
import org.videolan.libvlc.RendererDiscoverer.Event.ItemAdded
import org.videolan.libvlc.RendererDiscoverer.Event.ItemDeleted
import org.videolan.libvlc.RendererItem
import java.util.*


/**
 * This class is responsible for tracking the available renderer items.
 */
class RendererItemObservable(private val libVlc: LibVLC) : Observable(), RendererDiscoverer.EventListener {
    private val rendererItems = ArrayList<RendererItem>()
    private val rendererDiscoverers = ArrayList<RendererDiscoverer>()

    /**
     * Get all of the renderer items currently available.
     */
    val renderItems: List<RendererItem>
        get() = rendererItems

    /**
     * Start listening for renderer item events.
     */
    fun start() {
        for (discoverer in RendererDiscoverer.list(libVlc)) {
            val rendererDiscoverer = RendererDiscoverer(
                    libVlc,
                    discoverer.name
            )

            rendererDiscoverers.add(rendererDiscoverer)

            rendererDiscoverer.setEventListener(this)
            rendererDiscoverer.start()
        }
    }

    /**
     * Stop listening for renderer item events.
     */
    fun stop() {
        for (discover in rendererDiscoverers) {
            discover.stop()
        }

        for (renderItem in rendererItems) {
            renderItem.release()
        }

        rendererDiscoverers.clear()
        rendererItems.clear()
    }

    /**
     * See: https://stackoverflow.com/questions/43784161/how-to-implement-finalize-in-kotlin
     */
    @Suppress("ProtectedInFinal", "unused")
    protected fun finalize() {
        // Stop the discoverers and release the renderer items.
        stop()
    }

    /**
     * Invoked when a renderer item is added or removed.
     *
     * @param event The add or remove event.
     */
    override fun onEvent(event: RendererDiscoverer.Event) {
        when (event.type) {
            ItemAdded -> onItemAdded(event.item)
            ItemDeleted -> onItemDeleted(event.item)
        }
    }

    /**
     * Remove renderer item and notify subscribers.
     *
     * @param item The item to remove.
     */
    private fun onItemDeleted(item: RendererItem) {
        rendererItems.remove(item)

        setChanged()
        notifyObservers(rendererItems)
    }

    /**
     * Add renderer item and notify subscribers.
     *
     * @param item The item to add.
     */
    private fun onItemAdded(item: RendererItem) {
        rendererItems.add(item)

        setChanged()
        notifyObservers(rendererItems)
    }

}