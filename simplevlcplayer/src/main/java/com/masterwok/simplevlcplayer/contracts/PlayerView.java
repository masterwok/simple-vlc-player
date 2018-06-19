package com.masterwok.simplevlcplayer.contracts;


public interface PlayerView {

    void registerCallback(Callback callback);

    void updatePlaybackState(
            boolean isPlaying,
            long length,
            long time
    );

    interface Callback {
        void togglePlayback();

        void onProgressChanged(int progress);
    }
}
