package com.masterwok.simplevlcplayer.contracts;


public interface PlayerView {

    void registerCallback(Callback callback);

    void updatePlaybackState();

    interface Callback {
        void togglePlayback();

        void onProgressChanged(int progress);
    }
}
