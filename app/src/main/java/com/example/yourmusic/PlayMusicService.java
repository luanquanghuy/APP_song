package com.example.yourmusic;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.yourmusic.event.OnChangeSong;
import com.example.yourmusic.event.OnCompletePlayMusic;
import com.example.yourmusic.event.OnPlayMusic;
import com.example.yourmusic.event.OnStopMusic;

import java.io.IOException;
import java.util.Objects;

public class PlayMusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_STOP = "ACTION_STOP";

    private static final int NOTIFICATION_ID = 102;

    private final IBinder musicBind = new MusicBinder();
    OnCompletePlayMusic onCompletePlayMusic;
    OnPlayMusic onPlayMusic;
    OnStopMusic onStopMusic;
    OnChangeSong onChangeSong;

    MediaPlayer mediaPlayer;

    private DataSongs dataSongs;

    int resumePosition;

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d("LQH_MP_COMPLETE", getCurrentPosition() + " : " + getDuration());
        nextsong();;
    }


    public void setOnChangeSongMusic(OnChangeSong onChangeSong){
        this.onChangeSong = onChangeSong;
    }

    public void setOnStopMusic(OnStopMusic onStopMusic){
        this.onStopMusic = onStopMusic;
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
        if (Objects.equals(intent.getAction(), ACTION_PLAY) && dataSongs == null) {
            if (dataSongs == null) {
                Bundle getSongPicked = intent.getBundleExtra("songpick");
                dataSongs = (DataSongs) getSongPicked.getSerializable("baihat");
            }
            if (mediaPlayer == null) {
                initMediaPlayer();
            }
            createNotification(true);
        }else {
            handleIncomingActions(intent);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
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

    public void loop(boolean isLoop) {
        mediaPlayer.setLooping(isLoop);
    }

    public int getDuration() {
        if(mediaPlayer != null && mediaPlayer.isPlaying()) {
            return mediaPlayer.getDuration();
        }else return -1;
    }

    public int getCurrentPosition() {
        if(mediaPlayer != null && mediaPlayer.isPlaying()) {
            return mediaPlayer.getCurrentPosition();
        }else return -1;
    }

    public void presong() {
        if (dataSongs.getIdBH() <= 0) {
            dataSongs.setIdBH(dataSongs.getDsBaiHat().size() - 1);
        } else {
            dataSongs.setIdBH(dataSongs.getIdBH() - 1);
        }
        initMediaPlayer();
        createNotification(true);
    }

    public void nextsong() {
        if (dataSongs.getIdBH() >= dataSongs.getDsBaiHat().size() - 1) {
            dataSongs.setIdBH(0);
        } else {
            dataSongs.setIdBH(dataSongs.getIdBH() + 1);
        }
        initMediaPlayer();
        createNotification(true);
    }

    public Song getSongCurrent(){
        return dataSongs.getDsBaiHat().get(dataSongs.getIdBH());
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        super.onDestroy();
    }

    private void initMediaPlayer(){
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(this, Uri.parse(dataSongs.getDsBaiHat().get(dataSongs.getIdBH()).getUri()));
            mediaPlayer.prepareAsync();
            if(this.onChangeSong != null) {
                this.onChangeSong.changeSong();
            }
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
    }

    private void createNotification(boolean isPlay) {
        PendingIntent playIntent;
        int icon_play;
        if (isPlay) {
            playIntent = playbackAction(1);
            icon_play = R.drawable.pause_64;
        } else {
            playIntent = playbackAction(0);
            icon_play = R.drawable.play;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CHANNEL_ID123456")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setShowWhen(false)
                .setOngoing(isPlay)
                .addAction(R.drawable.back_64, "Previous", playbackAction(3))
                .addAction(icon_play, "Pause", playIntent)
                .addAction(R.drawable.next_64, "next", playbackAction(2))
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(0, 1, 2)
                )
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(dataSongs.getDsBaiHat().get(dataSongs.getIdBH()).getTitle())
                .setContentText(dataSongs.getDsBaiHat().get(dataSongs.getIdBH()).getArtist());

        if(!isPlay){
            builder.setDeleteIntent(playbackAction(4));
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, PlayMusicService.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, PendingIntent.FLAG_UPDATE_CURRENT);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, PendingIntent.FLAG_UPDATE_CURRENT);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, PendingIntent.FLAG_UPDATE_CURRENT);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, PendingIntent.FLAG_UPDATE_CURRENT);
            case 4:
                // Previous track
                playbackAction.setAction(ACTION_STOP);
                return PendingIntent.getService(this, actionNumber, playbackAction, PendingIntent.FLAG_UPDATE_CURRENT);
            default:
                break;
        }
        return null;
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            if(onPlayMusic != null) {
                onPlayMusic.onPlayMusic(true);
            }
            pauseMusic();
            createNotification(true);
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            if(onPlayMusic != null) {
                onPlayMusic.onPlayMusic(false);
            }
            pauseMusic();
            createNotification(false);
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            stopMusic();
            mediaPlayer.reset();
            nextsong();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            stopMusic();
            mediaPlayer.reset();
            presong();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
//            stopMusic();
            mediaPlayer.reset();
            this.onStopMusic.onStopMusic();
            stopSelf();
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }
}
