package com.example.yourmusic.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.yourmusic.Song;

import java.util.ArrayList;
import java.util.List;

public class DBManager {

    private DatabaseHelper databaseHelper;
    public DBManager(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }


    public void addSong(Song song) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.ID, song.getId());
        values.put(DatabaseHelper.TITLE, song.getTitle());
        values.put(DatabaseHelper.ARTIST, song.getArtist());
        values.put(DatabaseHelper.URI, song.getUri());

        db.insert(DatabaseHelper.TABLE_NAME, null, values);
        db.close();
    }

    public List getAllSong() {
        List<Song>  songList = new ArrayList<>();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_NAME;

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        while(!cursor.isAfterLast()) {
            Song song = new Song(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
            songList.add(song);
            cursor.moveToNext();
        }
        return songList;
    }
    public void deleteSong(long id) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper.ID +" = "+id, null);

    }
    public void deleteAll(){
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        String delete = String.format("DELETE FROM %s", databaseHelper.TABLE_NAME);
        db.execSQL(delete);
    }
}
