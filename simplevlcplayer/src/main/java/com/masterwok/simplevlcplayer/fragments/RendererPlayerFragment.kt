package com.masterwok.simplevlcplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.masterwok.simplevlcplayer.R
import kotlinx.android.synthetic.main.fragment_player_renderer.*

class RendererPlayerFragment : BasePlayerFragment() {

    override fun onConnected() {
        if (serviceBinder?.isPlaying == true) {
            return
        }

        startPlayback()
    }

    override fun onDisconnected() {
        this.serviceBinder = null
    }

    override fun configure(
            isPlaying: Boolean,
            time: Long,
            length: Long
    ) = componentPlayerControl.configure(
            isPlaying,
            time,
            length
    )

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(
            R.layout.fragment_player_renderer,
            container,
            false
    )

    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        subscribeToViewComponents()
    }

    private fun subscribeToViewComponents() {
        componentPlayerControl.registerCallback(this)
    }

    private fun startPlayback() {
        serviceBinder?.setMedia(context!!, mediaUri!!)
        serviceBinder?.setSubtitle(subtitleUri)
        serviceBinder?.play()
    }

}