package com.example.a117.musicplaytest;

import java.io.File;
import java.util.Arrays;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

public class OpenVariousFile {
    public static void open(Context context, File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);

        String type = MimeTypeUtil.getMIMEType(file.getPath());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", file);
            intent.setDataAndType(contentUri, type);
        }
        else {
            intent.setDataAndType(Uri.fromFile(file), type);
        }

        if(intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
        else {
            Toast.makeText(context, "没有找到可以打开此类型文件的应用", Toast.LENGTH_SHORT).show();
        }
    }
}
