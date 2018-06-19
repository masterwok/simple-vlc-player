package com.masterwok.simplevlcplayer.contracts;


public interface PlayerView {

    void registerCallback(Callback callback);

    void updatePlaybackState();

    void setSurfaceSize(int width, int height);

    interface Callback {
        void togglePlayback();

        void onProgressChanged(int progress);
    }
}
