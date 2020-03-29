package com.example.yourmusic;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.IOException;

public class PlayMusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    public static final String ACTION_PLAY = "vn.huy.phatnhac.PLAY";

    private final IBinder musicBind = new MusicBinder();
    OnCompletePlayMusic onCompletePlayMusic;
    OnPlayMusic onPlayMusic;

    MediaPlayer mediaPlayer;

    int resumePosition;

    @Override
    public void onCompletion(MediaPlayer mp) {
        onCompletePlayMusic.setOnCompletePlayMusic();
    }

    public void setonCompletePlayMusic(OnCompletePlayMusic onCompletePlayMusic){
        this.onCompletePlayMusic = onCompletePlayMusic;
    }

    public void setOnPlayMusic(OnPlayMusic onPlayMusic){
        this.onPlayMusic = onPlayMusic;
    }


    public class MusicBinder extends Binder {
        PlayMusicService getService() {
            return PlayMusicService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ACTION_PLAY)) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.reset();
            try {
                Uri uri = intent.getData();
                mediaPlayer.setDataSource(this, uri);
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
                stopSelf();
            }

        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            this.onPlayMusic.onPlayMusic();
        }
    }

    public void changeMusic(Uri uri) {
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void pauseMusic() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        } else {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }

    public void stopMusic() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    public int getDuration(){
        return mediaPlayer.getDuration();
    }

    public int getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public void onDestroy() {
        if(mediaPlayer != null) {
            mediaPlayer.stop();
        }
        super.onDestroy();
    }
}
