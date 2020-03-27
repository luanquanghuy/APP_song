package com.example.yourmusic.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "database";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "songlist";
    public static final String TITLE = "title";
    public static final String ARTIST = "artist";
    public static final String ID = "id";
    public static final String URI = "uri";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create_table = String.format("CREATE TABLE IF NOT EXISTS %s(%s INTEGER PRIMARY KEY, %s TEXT, %s TEXT, %s TEXT)", TABLE_NAME, ID, TITLE, ARTIST, URI);
        db.execSQL(create_table);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        String drop = String.format("DROP TABLE IF EXISTS %s", TABLE_NAME);
        db.execSQL(drop);

        onCreate(db);
    }

}
