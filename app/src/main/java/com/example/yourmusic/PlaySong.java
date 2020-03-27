package com.example.yourmusic;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class PlaySong extends AppCompatActivity {

    private Song songPicked;
    private TextView txtName, txtCaSi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);
        init();


    }

    private void init() {
        Bundle getSongPicked = getIntent().getBundleExtra("songpick");
        String[] s = getSongPicked.getStringArray("songpick");
        Song songPicked = new Song(Long.valueOf(s[3]), s[0], s[1], s[2]);

        txtName = findViewById(R.id.musicname);
        txtCaSi = findViewById(R.id.sn);


        txtName.setText(songPicked.getTitle());
        txtCaSi.setText(songPicked.getArtist());
    }

    public void back(View view) {
        finish();
    }
}
