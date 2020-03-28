package com.example.yourmusic;

import java.io.Serializable;

public class Song implements Serializable {

    private long id;
    private String title;
    private String artist;
    private String uri;

    public Song(long id, String title, String artist, String uri) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.uri = uri;
    }

    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    public long getId() { return id; }
    public String getUri() { return uri; }
}
