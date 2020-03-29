package com.example.yourmusic;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

public class ListSongActivity extends AppCompatActivity {
    private List<Song> songList;
    private ListView lvBaiHat;
    private SongAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_song);
        init();
        event();
    }

    private void event() {
        lvBaiHat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("baihat", new DataSongs(songList, i));
                Intent intent = new Intent(ListSongActivity.this, PlaySongActivity.class);
                intent.putExtra("songpick", bundle);
                startActivity(intent);
            }
        });
        lvBaiHat.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int pos, long l) {
                new AlertDialog.Builder(ListSongActivity.this)
                        .setTitle("Xoá "+songList.get(pos).getTitle())
                        .setMessage("Bạn có chắc chắn muốn xoá bài hát "+songList.get(pos).getTitle()+" - ca sĩ "+songList.get(pos).getArtist())
                        .setCancelable(true)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                MainActivity.dbManager.deleteSong(songList.get(pos).getId());
                                songList.remove(pos);
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
                return false;
            }
        });
    }

    private void init() {
        lvBaiHat = findViewById(R.id.lv_list_song);
        songList = MainActivity.dbManager.getAllSong();
        ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i < songList.size(); i++){
            strings.add(songList.get(i).getTitle());
        }
        adapter = new SongAdapter(getApplicationContext(), songList);
        lvBaiHat.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // show menu when menu button is pressed
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // display a message when a button was pressed
        String message = "";
        if (item.getItemId() == R.id.load) {
            //drop and add to database
            MainActivity.dbManager.deleteAll();
            ArrayList<Song> songList = getSongList();
            for (int i=0; i < songList.size(); i++){
                MainActivity.dbManager.addSong(songList.get(i));
            }
            adapter.updateReceiptsList(songList);
            Toast.makeText(this, "cập nhật thành công!", Toast.LENGTH_SHORT).show();
        }
        else if (item.getItemId() == R.id.exit) {
            finish();
        }

        return true;
    }


    public ArrayList<Song> getSongList(){
        ArrayList list = new ArrayList<Song>();
        //query external audio
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        //iterate over results if valid
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);

            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                Uri uri = ContentUris.withAppendedId(
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        thisId);
                list.add(new Song(thisId, thisTitle, thisArtist, uri.toString()));
            }while (musicCursor.moveToNext());
        }
        return list;
    }
}
