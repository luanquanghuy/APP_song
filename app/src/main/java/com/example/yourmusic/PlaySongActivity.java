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
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

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
        if(dataSongs == null){
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
            playMusicService.setonCompletePlayMusic(new OnCompletePlayMusic() {
                @Override
                public void setOnCompletePlayMusic() {
                    if (isReapet) {
                        playMusicService.changeMusic(Uri.parse(dataSongs.getDsBaiHat().get(dataSongs.getIdBH()).getUri()));
                    } else if (!stop.get()) {
                        if (dataSongs.getIdBH() >= dataSongs.getDsBaiHat().size() - 1) {
                            dataSongs.setIdBH(0);
                        } else {
                            dataSongs.setIdBH(dataSongs.getIdBH() + 1);
                        }
                        changeBH();

                    } else {
                        isPlaying = false;
                    }
                }
            });
            playMusicService.setOnPlayMusic(new OnPlayMusic() {
                @Override
                public void onPlayMusic() {
                    threadPlay = new Thread(new PlayThread());
                    stop.set(false);
                    if(!isPlaying) {
                        changeBH();
                    }
                    threadPlay.start();
                    ((ImageButton) findViewById(R.id.play)).setImageResource(R.drawable.pause_64);
                    isPlaying = true;
                }
            });
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    public void back(View view) {
        playMusicService.stopSelf();
        finish();
    }

    public void plsong(View view) {
        if (isPlaying) {
            ((ImageButton) view).setImageResource(R.drawable.play);
        } else {
            if (stop.get()) {
                threadPlay = new Thread(new PlayThread());
                stop.set(false);
                changeBH();
                threadPlay.start();
            }
            ((ImageButton) view).setImageResource(R.drawable.pause_64);
        }
        isPlaying = !isPlaying;
        playMusicService.pauseMusic();
    }

    public void nextsong(View view) {
        if (dataSongs.getIdBH() >= dataSongs.getDsBaiHat().size() - 1) {
            dataSongs.setIdBH(0);
        } else {
            dataSongs.setIdBH(dataSongs.getIdBH() + 1);
        }
        changeBH();
    }

    public void presong(View view) {
        if (dataSongs.getIdBH() <= 0) {
            dataSongs.setIdBH(dataSongs.getDsBaiHat().size() - 1);
        } else {
            dataSongs.setIdBH(dataSongs.getIdBH() - 1);
        }
        changeBH();
    }

    private void changeBH() {
        Song songPicked = dataSongs.getDsBaiHat().get(dataSongs.getIdBH());
        txtName.setText(songPicked.getTitle());
        txtCaSi.setText(songPicked.getArtist());
        playMusicService.changeMusic(Uri.parse(dataSongs.getDsBaiHat().get(dataSongs.getIdBH()).getUri()));
    }

    public void repeat(View view) {
        if (isReapet) {
            isReapet = false;
            view.setBackgroundColor(Color.TRANSPARENT);
        } else {
            isReapet = true;
            view.setBackgroundColor(Color.GRAY);
        }
    }

    public void stopsong(View view) {
        playMusicService.stopMusic();
        stop.set(true);
        if (isPlaying) {
            isPlaying = false;
            ((ImageButton) findViewById(R.id.play)).setImageResource(R.drawable.play);
        }
    }

    class PlayThread implements Runnable {

        @Override
        public void run() {
            while (!stop.get()) {
                seekBar.setProgress((int) ((double) playMusicService.getCurrentPosition() / (double) playMusicService.getDuration() * 100));
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        txtCurrent.setText(((playMusicService.getCurrentPosition() / 1000) / 60) + ":" + String.format("%02d", (playMusicService.getCurrentPosition() / 1000) % 60));
                        txtDuration.setText(((playMusicService.getDuration() / 1000) / 60) + ":" + (String.format("%02d", (playMusicService.getDuration() / 1000) % 60)));
                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
