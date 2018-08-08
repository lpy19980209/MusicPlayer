package com.example.a117.musicplaytest;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import static android.media.AudioManager.AUDIOFOCUS_GAIN;
import static android.media.AudioManager.AUDIOFOCUS_LOSS;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "mymessage";

    Boolean hasInit = false;
    Boolean showNotification = true;

    private String PLAY_PROGRESS = "PLAY_PROGRESS";
    private String PLAY_INDEX = "PLAY_INDEX";
    private String PLAY_STATE = "PLAY_STATE";

    private BroadcastReceiver renewListReceivder = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String path = intent.getStringExtra("path");

            Log.d("mymessagee", "handleMessage: " + "开始执行onRetrive");
            walkMusicFiles(path);
            Log.d("mymessagee", "handleMessage: " + "onRetrive执行完毕");
//            Toast.makeText(MainActivity.this, "正在添加音乐\n  请稍等片刻", Toast.LENGTH_SHORT).show();
        }

    };

    private BroadcastReceiver noticationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "com.lpy.nextmusic":
                    nextMusic();
                    break;
                case "com.lpy.lastmusic":
                    lastMusic();
                    break;
                case "com.lpy.ppmusic":
                    if (mediaPlayer.isPlaying())
                        pauseMusic();
                    else
                        playMusic();
                    break;
                default:

            }
        }
    };

    private Thread playProgressRecord = new Thread(new Runnable() {
        @Override
        public void run() {
            int duration;
            int currentPosition;

            while (true) {
                if (mediaPlayer.isPlaying()) {
                    duration = mediaPlayer.getDuration();
                    currentPosition = mediaPlayer.getCurrentPosition();
                    Log.d("progress", "run: duration=" + TimeUtil.sToHMS(duration) + ", currentPosition=" + TimeUtil.sToHMS(currentPosition));
                    updateProgress(TimeUtil.sToHMS(duration), TimeUtil.sToHMS(currentPosition), (double) currentPosition / (double) duration);
                }

                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    });

    static final int MUSIC_FILE_LOAD_FINISH = 0;
    MusicListviewAdapter musicListviewAdapter;


    final Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d("mymessagee", "handleMessage: " + "开始执行handle");
            if (msg.what == MUSIC_FILE_LOAD_FINISH) {
                Log.d("mydebug", "walkMusicFiles: " + musicFileList.toString());


                musicFileList = musicFileListBuffer;

//                musicListviewAdapter = new MusicListviewAdapter(MainActivity.this, musicFileList);
                //一定要分辨清楚耗时操作
                //不要再傻逼的把简单操作放进子线程
                //把上面的耗时操作留在主线程

                listView.setAdapter(musicListviewAdapter);

                if (!hasInit) {
                    playMusic(index);

                    mediaPlayer.seekTo(playProgress);

                    String duration = TimeUtil.sToHMS(mediaPlayer.getDuration());
                    String progress = TimeUtil.sToHMS(playProgress);
                    double percent = playProgress / (double) mediaPlayer.getDuration();

                    updateProgress(duration, progress, percent);

                    if (!isPlayingBefore)
                        pauseMusic();

                    playProgressRecord.start();

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            playMusic(i);

                        }
                    });
                }
//                musicListviewAdapter.notifyDataSetChanged();
                hasInit = true;
            }
            Log.d("mymessagee", "handleMessage: " + "handle执行完毕");
            //
            musicLoadProgressBar.setVisibility(View.GONE);
        }
    };

    private LocalBroadcastManager localBroadcastManager;

    AudioManager am;
    AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;

    private ImageButton lastButton;
    private ImageButton nextButton;
    private ImageButton playOrPauseButton;
    private ProgressBar musicLoadProgressBar;
    private NavigationView leftNav;
    private DrawerLayout drawer;
    private ListView listView;

    private int index = 0;
    private int playProgress = 0;
    private boolean isPlayingBefore = false;
    //用于只是 destory 之前是否在播放

    List<File> musicFileList = new ArrayList<>();

    List<MusicMetaData> musicMetaList = new ArrayList<>();

    List<File> musicFileListBuffer;
    MediaPlayer mediaPlayer = new MediaPlayer();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        applyPermissions();

        findElements();
        mRegisterReceiver();
        setMusicStream();
        setMediaCallback();
        setButtonBinding();
        setNavSelection();

        restoreMusicList();

        if (savedInstanceState == null) {
            restorePlayStateFromPerferences();
        }

//        showDrawer();


    }

    private void findElements() {
        lastButton = findViewById(R.id.last_music);
        nextButton = findViewById(R.id.next_music);
        playOrPauseButton = findViewById(R.id.play_or_pause);
        listView = findViewById(R.id.music_listview);
        musicLoadProgressBar = findViewById(R.id.music_load_processbar);
        leftNav = findViewById(R.id.left_nav);
        drawer = findViewById(R.id.drawer);
    }

    private void walkMusicFiles(final String musicDir) {

        Log.d("mymessagee", "handleMessage: " + "开始执行walk");

        final File musicDirPath = new File(musicDir);

        if (!musicDirPath.exists()) {
            Toast.makeText(MainActivity.this, "路径不存在", Toast.LENGTH_SHORT).show();
            return;
        }

        //
        musicLoadProgressBar.setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            @Override
            public void run() {

                Log.d("mymessagee", "handleMessage: " + "开始进入新线程");

                musicFileListBuffer = new ArrayList<>();

                for (File music : musicDirPath.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.getName().matches(".*\\.(mp3|flac)");
                    }
                })) {
                    musicFileListBuffer.add(music);
                }
                Log.d("wanttosee", "run: " + "数据更新完毕");

                Collections.sort(musicFileListBuffer, new Comparator<File>() {
                    @Override
                    public int compare(File f1, File f2) {
                        return f1.getName().compareTo(f2.getName());
                    }
                });

                musicFileListBuffer.addAll(0, musicFileList);

                //保存音乐列表
                saveMusicList(musicFileListBuffer);

                constructMetaList(musicFileListBuffer);

                saveMetaList();

                musicListviewAdapter = new MusicListviewAdapter(MainActivity.this, musicMetaList);


                myHandler.sendEmptyMessage(MUSIC_FILE_LOAD_FINISH);
                Log.d("mymessagee", "handleMessage: " + "新线程执行完毕");
            }
        }).start();
        Log.d("mymessagee", "handleMessage: " + "walk执行完毕");
    }

    private void setButtonBinding() {
        playOrPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {
                    pauseMusic();
                } else {
                    playMusic();
                }
            }
        });


        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextMusic();
            }
        });

        lastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                lastMusic();
            }
        });
    }

    public void applyPermissions() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

        } else {
            Toast.makeText(MainActivity.this, "获取权限失败", Toast.LENGTH_LONG);
        }
    }

    public void mRegisterReceiver() {
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter("com.lpy.renewplaylist");
        localBroadcastManager.registerReceiver(renewListReceivder, filter);

        IntentFilter filter2 = new IntentFilter();
        filter2.addAction("com.lpy.ppmusic");
        filter2.addAction("com.lpy.nextmusic");
        filter2.addAction("com.lpy.lastmusic");
        registerReceiver(noticationReceiver, filter2);
    }

    public void setMusicStream() {
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        am = (AudioManager) MainActivity.this.getSystemService(Context.AUDIO_SERVICE);

        audioFocusChangeListener =
                new AudioManager.OnAudioFocusChangeListener() {
                    @Override
                    public void onAudioFocusChange(int i) {
                        Toast.makeText(MainActivity.this, "音频焦点改变了", Toast.LENGTH_SHORT);
                        if (i == AUDIOFOCUS_LOSS_TRANSIENT) {
                            pauseMusic();
                        } else if (i == AUDIOFOCUS_GAIN) {
                            playMusic();
                        } else if (i == AUDIOFOCUS_LOSS) {
                            pauseMusic();
                            am.abandonAudioFocus(audioFocusChangeListener);
                        }
                    }
                };

        int result = am.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

        } else {
            Toast.makeText(MainActivity.this, "获取音频焦点失败", Toast.LENGTH_LONG);
        }
    }


    public void playMusic() {
        mediaPlayer.start();
        if (mediaPlayer.isPlaying())
            playOrPauseButton.setImageResource(R.drawable.pause_music);
        else
            Toast.makeText(MainActivity.this, "当前没有歌曲可以播放，请添加文件夹", Toast.LENGTH_LONG).show();
        if (showNotification)
            sendNotication();
    }


    public void pauseMusic() {
        mediaPlayer.pause();
        playOrPauseButton.setImageResource(R.drawable.play_music);
        if (showNotification)
            sendNotication();
    }


    public boolean playMusic(int i) {
        if (i >= 0 && i < musicFileList.size()) {
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(musicFileList.get(i).getPath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                index = i;

                if (mediaPlayer.isPlaying())
                    playOrPauseButton.setImageResource(R.drawable.pause_music);

                if (showNotification)
                    sendNotication();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        } else
            return false;
    }

    public void nextMusic() {
        if (!playMusic(index + 1))
            Toast.makeText(MainActivity.this, "没有下一首了", Toast.LENGTH_SHORT).show();
    }

    public void lastMusic() {
        if (!playMusic(index - 1))
            Toast.makeText(MainActivity.this, "没有上一首了", Toast.LENGTH_SHORT).show();
    }

    public void setMediaCallback() {
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                nextMusic();
            }
        });
    }

    public void gotoFileExplore() {
        Intent intent = new Intent(MainActivity.this, ActivityFileExplore.class);
        startActivity(intent);
    }

    private void setNavSelection() {
        leftNav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getTitle().toString()) {
                    case "添加歌曲文件夹":
                        hideDrawer();
                        gotoFileExplore();
                        break;
                    case "退出应用":
                        pauseMusic();
                        am.abandonAudioFocus(audioFocusChangeListener);
                        clearNotication();
                        finish();
                        break;
                    case "开启通知栏切歌":
                        sendNotication();
                        showNotification = true;
                        break;
                    case "关闭通知栏切歌":
                        clearNotication();
                        showNotification = false;
                        break;
                    case "清空音乐列表":
                        deleteFile("musicList.xml");
                        SharedPreferences.Editor editor = getSharedPreferences("play_state.preference", MODE_PRIVATE).edit();
                        editor.clear();
                        editor.apply();
                        finish();
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(intent);
                    default:
                        Toast.makeText(MainActivity.this, "操作未定义", Toast.LENGTH_SHORT);
                }
                return true;
            }
        });
    }

    private void sendNotication() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notication_layout);

        if (mediaPlayer.isPlaying())
            remoteViews.setImageViewResource(R.id.play_or_pause, R.drawable.pause_music);
        else
            ;//do nothing

        Intent pPIntent = new Intent("com.lpy.ppmusic");
        PendingIntent pPPending = PendingIntent.getBroadcast(MainActivity.this, 1, pPIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.play_or_pause, pPPending);

        Intent nextIntent = new Intent("com.lpy.nextmusic");
        PendingIntent nextPending = PendingIntent.getBroadcast(MainActivity.this, 2, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.next_music, nextPending);

        Intent lastintent = new Intent("com.lpy.lastmusic");
        PendingIntent lastPending = PendingIntent.getBroadcast(MainActivity.this, 3, lastintent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.last_music, lastPending);


        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContent(remoteViews);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        String channelID = "1";
        String channelName = "channel_name";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_LOW);

            channel.enableLights(false);
            channel.enableVibration(false);

            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channelID);
        }

        builder.setOngoing(true);

        Notification notification = builder.build();

        notificationManager.notify(1, notification);
    }

    private void clearNotication() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    private void showDrawer() {
        drawer.openDrawer(leftNav);
    }

    private void hideDrawer() {
        drawer.closeDrawer(leftNav);
    }

    private void saveMusicList(List<File> files) {
        try {
            XmlSerializer serializer = Xml.newSerializer();
            OutputStream os = openFileOutput("musicList.xml", Context.MODE_PRIVATE);
            serializer.setOutput(os, "UTF_8");
            serializer.startDocument("UTF-8", true);

            serializer.startTag(null, "musics");

            for (File file : files) {
                serializer.startTag(null, "path");
                serializer.text(file.getPath());
                serializer.endTag(null, "path");
            }

            serializer.endTag(null, "musics");

            serializer.endDocument();
        } catch (IOException e) {

        }
    }

    private void restoreMusicList() {

        Log.d(TAG, "restoreMusicList: 进入 restore");

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: directory");
                if (new File(getFilesDir(), "musicList.xml").exists()) {

                    musicLoadProgressBar.setVisibility(View.VISIBLE);

                    try {
                        Log.d(TAG, "restoreMusicList: 进入 restore in run");
                        XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                        Reader reader = new BufferedReader(new InputStreamReader(openFileInput("musicList.xml")));
                        parser.setInput(reader);
                        int event = parser.getEventType();
                        String tagName = null;
                        while (event != XmlPullParser.END_DOCUMENT) {
                            switch (event) {
                                case XmlPullParser.START_DOCUMENT:
                                    musicFileListBuffer = new ArrayList<>();
                                    break;
                                case XmlPullParser.START_TAG:
                                    tagName = parser.getName();
                                    if (tagName.equals("path")) {
                                        musicFileListBuffer.add(new File(parser.nextText()));
                                    }
                                    break;
                                case XmlPullParser.END_TAG:
                                    break;
                            }
                            event = parser.next();
                        }

                        restoreMetaList();

                        musicListviewAdapter = new MusicListviewAdapter(MainActivity.this, musicMetaList);

                        myHandler.sendEmptyMessage(MUSIC_FILE_LOAD_FINISH);
                        Log.d(TAG, "run: restore结束");
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d(TAG, "run: restore出错");
                    } finally {

                    }
                } else {
                    showDrawer();
                }
            }
        }).start();

    }

    private void saveMetaList() {
        ObjectOutputStream objOut = null;
        try {
            objOut = new ObjectOutputStream(openFileOutput("metaListObject.obj", MODE_PRIVATE));
            objOut.writeObject(musicMetaList);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if(objOut != null) {
                try {
                    objOut.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void restoreMetaList() {
        ObjectInputStream objIn = null;
        try {
            objIn = new ObjectInputStream(openFileInput("metaListObject.obj"));
            musicMetaList = (List<MusicMetaData>) objIn.readObject();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if(objIn != null) {
                try {
                    objIn.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateProgress(final String duration, final String currentPosition, final double percent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.current_position)).setText(currentPosition);
                ((TextView) findViewById(R.id.duration)).setText(duration);
                ((ProgressBar) findViewById(R.id.play_progress)).setProgress((int) (percent * 1000));
            }
        });
    }

    private void restorePlayStateFromPerferences() {
        SharedPreferences pref = getSharedPreferences("play_state.preference", MODE_PRIVATE);
        index = pref.getInt(PLAY_INDEX, 0);
        playProgress = pref.getInt(PLAY_PROGRESS, 0);
        isPlayingBefore = false;

        String duration = TimeUtil.sToHMS(mediaPlayer.getDuration());
        String progress = TimeUtil.sToHMS(playProgress);
        double percent = playProgress / (double) mediaPlayer.getDuration();

        updateProgress(duration, progress, percent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PLAY_INDEX, index);
        outState.putBoolean(PLAY_STATE, mediaPlayer.isPlaying());

        try {
            outState.putInt(PLAY_PROGRESS, mediaPlayer.getCurrentPosition());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //可能会在特定时刻导致错误
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        index = savedInstanceState.getInt(PLAY_INDEX);
        isPlayingBefore = savedInstanceState.getBoolean(PLAY_STATE);

        try {
            playProgress = savedInstanceState.getInt(PLAY_PROGRESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        localBroadcastManager.unregisterReceiver(renewListReceivder);

        unregisterReceiver(noticationReceiver);

        SharedPreferences.Editor editor = getSharedPreferences("play_state.preference", Context.MODE_PRIVATE).edit();
        editor.putInt(PLAY_INDEX, index);
        editor.putInt(PLAY_PROGRESS, mediaPlayer.getCurrentPosition());
        editor.putBoolean(PLAY_STATE, false);
        editor.apply();

        super.onDestroy();
    }


    private List<MusicMetaData> constructMetaList(List<File> fileList) {
        List<File> filesToDeleted = new ArrayList<>();

        for (File file : fileList) {
            MusicMetaData metaData = new MusicMetaData(file);

            if (metaData.title != null) {
                musicMetaList.add(metaData);
            } else {
                filesToDeleted.add(file);
            }
        }

        for (File file : filesToDeleted) {
            fileList.remove(file);
        }

        return musicMetaList;
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart: ");
        super.onRestart();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: ");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: ");
        super.onStop();
    }


    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
    }
}

//todo
//解决播放列表的加载问题
//可以考虑对象序列化
//也可以考虑将 metaDateList 保存下来