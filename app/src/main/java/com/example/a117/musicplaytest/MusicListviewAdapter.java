package com.example.a117.musicplaytest;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MusicListviewAdapter extends BaseAdapter {

    private class ViewContainer {
        TextView titltTextView;
        TextView artistTextView;
    }

    private Context mContext;

    private List<MusicMetaData> musicMetaList = new ArrayList<>();

    public MusicListviewAdapter(Context context, List<MusicMetaData> musicMetaList) {
        super();
        mContext = context;
        this.musicMetaList = musicMetaList;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public Object getItem(int i) {
        return musicMetaList.get(i);
    }

    @Override
    public int getCount() {
        return musicMetaList.size();
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {

        View view;
        TextView titleTextView;
        TextView artistTextView;

        if(convertView != null) {
            view = convertView;
            ViewContainer container = (ViewContainer) view.getTag();
            titleTextView = container.titltTextView;
            artistTextView = container.artistTextView;
        }
        else {
            view = LayoutInflater.from(mContext).inflate(R.layout.music_list_item, parent, false);
            titleTextView = view.findViewById(R.id.music_listitem_title);
            artistTextView = view.findViewById(R.id.music_listitem_artist);

            ViewContainer container = new ViewContainer();
            container.artistTextView = artistTextView;
            container.titltTextView = titleTextView;

            view.setTag(container);
        }

        String title = musicMetaList.get(i).title;
        String artist = musicMetaList.get(i).artist;
        String album = musicMetaList.get(i).album;

        titleTextView.setText(title);
        artistTextView.setText(artist + " - " + album);

        return view;
    }

}
