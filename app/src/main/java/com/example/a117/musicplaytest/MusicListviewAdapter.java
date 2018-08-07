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
    private List<File> fileList;
    private List<MusicMetaData> musicMetaList = new ArrayList<>();

    public MusicListviewAdapter(Context context, List<File> fileList) {
        super();
        mContext = context;
        this.fileList = fileList;
        List<File> filesToDeleted = new ArrayList<>();

        for (File file : fileList) {
            MusicMetaData metaData = new MusicMetaData(file);

            if(metaData.title != null) {
                musicMetaList.add(metaData);
            }
            else {
                filesToDeleted.add(file);
            }
        }

        for(File file : filesToDeleted) {
            fileList.remove(file);
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public Object getItem(int i) {
        return fileList.get(i);
    }

    @Override
    public int getCount() {
        return fileList.size();
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



        Log.d("wanttosee", "getView: " + i);
        String title = musicMetaList.get(i).title;
        title = title == null ? "* " + fileList.get(i).getName() : title;

        String artist = musicMetaList.get(i).artist;
        artist  = artist == null ? "* " + "未知歌手" : artist;

        String album = musicMetaList.get(i).album;
        album  = album == null ? "* " + "未知唱片" : album;

        titleTextView.setText(title);
        artistTextView.setText(artist + " - " + album);
        return view;
    }
}
