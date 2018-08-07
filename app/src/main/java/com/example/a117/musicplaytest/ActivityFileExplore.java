package com.example.a117.musicplaytest;

import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;


public class ActivityFileExplore extends AppCompatActivity implements ConnInterfaceForFileExplore {

    private LocalBroadcastManager localBroadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_explore);

        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        initFragment();
    }

    @Override
    public void fileListClicked(String path) {
        if(new File(path).isDirectory()) {
            Fragment fragment = new FragmentForFileExplore();
            Bundle bundle = new Bundle();
            bundle.putString("path", path);
            fragment.setArguments(bundle);
            replaceFragment(fragment);
        }
        else {
            OpenVariousFile.open(ActivityFileExplore.this, new File(path));
        }
    }

    @Override
    public void fileListLongClicked(String path) {
        if(new File(path).isDirectory()) {
            finish();
            Intent intent = new Intent("com.lpy.renewplaylist");
            intent.putExtra("path", path);
            localBroadcastManager.sendBroadcast(intent);
        }
        else {
            Toast.makeText(ActivityFileExplore.this, "长按目录可以添加到播放列表", Toast.LENGTH_SHORT);
        }
    }

    public void replaceFragment(Fragment fragment) {

        getSupportFragmentManager().beginTransaction()
//                .setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right)
                .setCustomAnimations(R.anim.alpha_in, R.anim.alpha_out, R.anim.alpha_in, R.anim.alpha_out)
                .replace(R.id.whole_framelayout, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void initFragment() {

        String path = Environment.getExternalStorageDirectory().getPath();

        Log.d("mymessage", "initFragment: " + path);

        Fragment fragment = new FragmentForFileExplore();
        Bundle bundle = new Bundle();
        bundle.putString("path", path);
        fragment.setArguments(bundle);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.whole_framelayout, fragment);
        transaction.commit();
    }
}
