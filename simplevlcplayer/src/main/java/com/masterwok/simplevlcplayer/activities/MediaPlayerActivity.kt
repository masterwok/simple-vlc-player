package com.masterwok.simplevlcplayer.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import com.masterwok.simplevlcplayer.R
import com.masterwok.simplevlcplayer.dagger.injectors.InjectableAppCompatActivity
import com.masterwok.simplevlcplayer.fragments.BasePlayerFragment
import com.masterwok.simplevlcplayer.fragments.LocalPlayerFragment
import com.masterwok.simplevlcplayer.fragments.RendererPlayerFragment
import com.masterwok.simplevlcplayer.services.MediaPlayerService

class MediaPlayerActivity : InjectableAppCompatActivity() {

    companion object {
        const val MediaUri = BasePlayerFragment.MediaUri
        const val SubtitleUri = BasePlayerFragment.SubtitleUri
        const val SubtitleDestinationUri = BasePlayerFragment.SubtitleDestinationUri
        const val OpenSubtitlesUserAgent = BasePlayerFragment.OpenSubtitlesUserAgent
        const val SubtitleLanguageCode = BasePlayerFragment.SubtitleLanguageCode
    }

    private var localPlayerFragment: LocalPlayerFragment? = null
    private var rendererPlayerFragment: RendererPlayerFragment? = null

    private val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return

            when (action) {
                MediaPlayerService.RendererClearedAction -> showLocalPlayerFragment()
                MediaPlayerService.RendererSelectionAction -> showRendererPlayerFragment()
            }
        }
    }

    val mediaPlayerServiceIntent: Intent
        get() = Intent(
                applicationContext,
                MediaPlayerService::class.java
        )


    private fun showLocalPlayerFragment() {
        rendererPlayerFragment = null
        localPlayerFragment = LocalPlayerFragment()
        showFragment(localPlayerFragment!!)
    }

    private fun showRendererPlayerFragment() {
        localPlayerFragment = null
        rendererPlayerFragment = RendererPlayerFragment()
        showFragment(rendererPlayerFragment!!)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_media_player)

        if (savedInstanceState != null) {
            return
        }

        showLocalPlayerFragment()
    }

    override fun onStart() {
        super.onStart()

        registerRendererBroadcastReceiver()

        startService(mediaPlayerServiceIntent)
    }

    override fun onBackPressed() {
        // Always ensure that we stop the media player service when navigating back.
        stopService(mediaPlayerServiceIntent)

        super.onBackPressed()
    }

    private fun registerRendererBroadcastReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(MediaPlayerService.RendererClearedAction)
        intentFilter.addAction(MediaPlayerService.RendererSelectionAction)

        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(broadCastReceiver, intentFilter)
    }

    override fun onStop() {
        LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(broadCastReceiver)

        rendererPlayerFragment = null

        super.onStop()
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.framelayout_fragment_container, fragment)
                .commit()
    }


}