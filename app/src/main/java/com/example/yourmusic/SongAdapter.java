package com.example.yourmusic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends BaseAdapter {
    private Context context;
    private List<Song> objects;

    public SongAdapter(Context context, List<Song> objects) {
        this.context = context;
        this.objects = objects;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(context).inflate(R.layout.song_item, viewGroup, false);
        TextView txtName, txtCaSi;
        txtName = view.findViewById(R.id.name);
        txtCaSi = view.findViewById(R.id.casi);
        txtName.setText(objects.get(i).getTitle());
        txtCaSi.setText(objects.get(i).getArtist());
        return view;
    }
    public void updateReceiptsList(ArrayList<Song> newlist) {
        objects.clear();
        objects.addAll(newlist);
        this.notifyDataSetChanged();
    }
}
