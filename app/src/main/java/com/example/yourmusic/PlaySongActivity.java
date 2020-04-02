package com.example.yourmusic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.yourmusic.event.OnChangeSong;
import com.example.yourmusic.event.OnPlayMusic;
import com.example.yourmusic.event.OnStopMusic;

import java.util.concurrent.atomic.AtomicBoolean;

public class PlaySongActivity extends AppCompatActivity {

    private TextView txtName, txtCaSi, txtCurrent, txtDuration;
    private SeekBar seekBar;
    ///
    private PlayMusicService playMusicService;
    Thread threadPlay;
    private AtomicBoolean stop = new AtomicBoolean(false);

    Handler handler;
    private boolean serviceBound = false;
    private DataSongs dataSongs;
    private boolean isReapet = false;
    private boolean isPlaying = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);

        init();

    }

    private void init() {
        Bundle getSongPicked = getIntent().getBundleExtra("songpick");
        if (getSongPicked == null) {
            finish();
            return;
        }
        handler = new Handler(getMainLooper());
        dataSongs = (DataSongs) getSongPicked.getSerializable("baihat");
        if (dataSongs == null) {
            finish();
            return;
        }
        Song songPicked;
        songPicked = dataSongs.getDsBaiHat().get(dataSongs.getIdBH());
        seekBar = findViewById(R.id.seekbar);
        txtName = findViewById(R.id.musicname);
        txtCaSi = findViewById(R.id.sn);
        txtCurrent = findViewById(R.id.timechay);
        txtDuration = findViewById(R.id.thoiluong);


        txtName.setText(songPicked.getTitle());
        txtCaSi.setText(songPicked.getArtist());

        if (!serviceBound) {
            Intent intent = new Intent(PlaySongActivity.this, PlayMusicService.class);
            intent.setAction(PlayMusicService.ACTION_PLAY);
            intent.setData(Uri.parse(songPicked.getUri()));
            intent.putExtra("songpick", getSongPicked);
            startService(intent);
            ((ImageButton) findViewById(R.id.play)).setImageResource(R.drawable.pause_64);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayMusicService.MusicBinder binder = (PlayMusicService.MusicBinder) service;
            playMusicService = binder.getService();
            playMusicService.setOnChangeSongMusic(new OnChangeSong() {
                @Override
                public void changeSong() {
                    Song songCurrent = playMusicService.getSongCurrent();
                    txtName.setText(songCurrent.getTitle());
                    txtCaSi.setText(songCurrent.getArtist());
                    Log.d("LQH_ON_PLAY", "play");
                    if (stop.get()) {
                        stop.set(false);
                        threadPlay = new Thread(new PlayThread());
                        threadPlay.start();
                    }
                    ((ImageButton) findViewById(R.id.play)).setImageResource(R.drawable.pause_64);
                    isPlaying = true;
                }
            });

            playMusicService.setOnStopMusic(new OnStopMusic() {
                @Override
                public void onStopMusic() {
                    stop.set(true);
                    isPlaying = false;
                }
            });

            playMusicService.setOnPlayMusic(new OnPlayMusic() {
                @Override
                public void onPlayMusic(boolean play) {
                    isPlaying = play;
                    ((ImageButton) findViewById(R.id.play)).setImageResource(play ? R.drawable.pause_64 : R.drawable.play);
                }
            });

            threadPlay = new Thread(new PlayThread());
            threadPlay.start();
            isPlaying = true;
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    public void back(View view) {
        playMusicService.stopSelf();
        stop.set(true);
        finish();
    }

    public void repeat(View view) {
        if (isReapet) {
            isReapet = false;
            playMusicService.loop(false);
            view.setBackgroundColor(Color.TRANSPARENT);
        } else {
            isReapet = true;
            playMusicService.loop(true);
            view.setBackgroundColor(Color.GRAY);
        }
    }

    public void stopsong(View view) {
        Intent intent = new Intent(PlaySongActivity.this, PlayMusicService.class);
        intent.setAction(PlayMusicService.ACTION_STOP);
        startService(intent);
        ((ImageButton) findViewById(R.id.play)).setImageResource(R.drawable.play);
    }

    public void presong(View view) {
        Intent intent = new Intent(PlaySongActivity.this, PlayMusicService.class);
        intent.setAction(PlayMusicService.ACTION_PREVIOUS);
        startService(intent);
    }

    public void plsong(View view) {
        Intent intent = new Intent(PlaySongActivity.this, PlayMusicService.class);
        if (isPlaying) {
            intent.setAction(PlayMusicService.ACTION_PAUSE);
        } else {
            intent.setAction(PlayMusicService.ACTION_PLAY);
        }
        startService(intent);
    }

    public void nextsong(View view) {
        Intent intent = new Intent(PlaySongActivity.this, PlayMusicService.class);
        intent.setAction(PlayMusicService.ACTION_NEXT);
        startService(intent);
    }

    class PlayThread implements Runnable {

        @Override
        public void run() {
            while (!stop.get()) {
                final int current = playMusicService.getCurrentPosition();
                final int duration = playMusicService.getDuration();
                if (current != -1 && duration != -1) {
                    seekBar.setProgress((int) ((double) current / (double) duration * 100));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            txtCurrent.setText(((current / 1000) / 60) + ":" + String.format("%02d", (current / 1000) % 60));
                            txtDuration.setText(((duration / 1000) / 60) + ":" + (String.format("%02d", (duration / 1000) % 60)));
                        }
                    });
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
