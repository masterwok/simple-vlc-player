package com.masterwok.demosimplevlcplayer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;

import com.masterwok.demosimplevlcplayer.R;
import com.masterwok.simplevlcplayer.activities.MediaPlayerActivity;

public class MainActivity extends AppCompatActivity {

    private AppCompatButton buttonPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        bindViewComponents();
        subscribeToViewComponents();
    }

    private void bindViewComponents() {
        buttonPlay = findViewById(R.id.button_play);
    }

    private void subscribeToViewComponents() {
        buttonPlay.setOnClickListener(view -> {
            // Disable play button (prevent bounce/re-enabled in onStart())
            view.setEnabled(false);

            startMediaPlayerActivity();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Re-enable play button (prevent bounce)
        buttonPlay.setEnabled(true);
    }

    private void startMediaPlayerActivity() {
        Intent intent = new Intent(this, MediaPlayerActivity.class);

        startActivity(intent);
    }

}
