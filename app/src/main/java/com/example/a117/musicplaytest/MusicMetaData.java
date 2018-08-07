package com.example.a117.musicplaytest;

import android.media.MediaMetadataRetriever;

import java.io.File;

public class MusicMetaData {

    public String title = null;
    public String album = null;
    public String mime = null;
    public String artist = null;
    public String duration = null;
    public String bitrate = null;
    public String date = null;
//    public byte[] picture = null;


    public MusicMetaData(File musicFile) {
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(musicFile.getPath());
            title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            bitrate = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
            date = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE);
//            picture = mmr.getEmbeddedPicture();
            mmr.release();
        }
        catch (Exception e) {
        }
    }
}