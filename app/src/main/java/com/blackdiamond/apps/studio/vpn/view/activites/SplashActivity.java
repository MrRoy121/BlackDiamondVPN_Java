package com.blackdiamond.apps.studio.vpn.view.activites;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.blackdiamond.apps.studio.vpn.R;

public class SplashActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.music);
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mediaPlayer.start();
            }
        }, 500);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, ControllerActivity.class));
                finish();
            }
        }, 5000);
    }
}