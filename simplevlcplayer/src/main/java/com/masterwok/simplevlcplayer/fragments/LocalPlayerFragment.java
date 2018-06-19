package com.masterwok.simplevlcplayer.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.masterwok.simplevlcplayer.components.PlayerComponent;
import com.masterwok.simplevlcplayer.contracts.VlcPlayer;

import javax.inject.Inject;

public class LocalPlayerFragment
        extends InjectableFragment {

    @Inject
    public VlcPlayer vlcPlayer;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return new PlayerComponent(getContext());
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);
    }
}
