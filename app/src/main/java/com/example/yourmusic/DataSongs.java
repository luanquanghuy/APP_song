package com.example.yourmusic;

import java.io.Serializable;
import java.util.List;

public class DataSongs implements Serializable {
    private List<Song> dsBaiHat;
    private int idBH;

    public DataSongs(List<Song> dsBaiHat, int idBH) {
        this.dsBaiHat = dsBaiHat;
        this.idBH = idBH;
    }

    public List<Song> getDsBaiHat() {
        return dsBaiHat;
    }

    public void setDsBaiHat(List<Song> dsBaiHat) {
        this.dsBaiHat = dsBaiHat;
    }

    public int getIdBH() {
        return idBH;
    }

    public void setIdBH(int idBH) {
        this.idBH = idBH;
    }
}
