package com.example.a117.musicplaytest;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class FragmentForFileExplore extends Fragment {

    private ConnInterfaceForFileExplore connIF;
    private String currentPath;


    public class FileListAdapter extends BaseAdapter{
        LayoutInflater inflater;
        List<String> files = new ArrayList<>();

        private class ViewContainer {
            TextView fileNameTextView;
            ImageView fileIconImageView;
        }

        public FileListAdapter(LayoutInflater inflater, String path) {
            this.inflater = inflater;
            files = new ArrayList<>(Arrays.asList(new File(path).list()));

//            Collections.sort(files, new Comparator<String>() {
//                @Override
//                public int compare(String f1, String f2) {
//                    File file1 = new File(currentPath, f1);
//                    File file2 = new File(currentPath, f2);
//
//                    if( (file1.isDirectory() && file2.isDirectory()) || (file1.isFile() && file2.isFile()) )
//                        return f1.compareTo(f2);
//
//                    else
//                        return file1.isDirectory() ? -1 : 1;
//                }
//            });
            //排序规则略复杂， 有轻微卡顿
            Collections.sort(files, new Comparator<String>() {
                @Override
                public int compare(String s, String t1) {
                    return s.toLowerCase().compareTo(t1.toLowerCase());
                }
            });
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {

            View view;
            TextView fileNameTextView;
            ImageView fileIconImageView;

            if(convertView != null) {
                view = convertView;
                ViewContainer container = (ViewContainer) view.getTag();
                fileNameTextView = container.fileNameTextView;
                fileIconImageView = container.fileIconImageView;
            }
            else {
                view = inflater.inflate(R.layout.item_file_explore_list_layout, viewGroup, false);
                ViewContainer container = new ViewContainer();
                fileNameTextView = view.findViewById(R.id.item_file_explore_filename);
                fileIconImageView = view.findViewById(R.id.item_file_explore_fileicon);

                container.fileNameTextView = fileNameTextView;
                container.fileIconImageView = fileIconImageView;
                view.setTag(container);
            }

            fileNameTextView.setText(files.get(i));

            if(new File(currentPath, files.get(i)).isDirectory()) {
                fileIconImageView.setImageResource(R.drawable.icon_dir);
            }
            else {
                fileIconImageView.setImageResource(R.drawable.icon_file);
            }

            return view;
        }

        @Override
        public int getCount() {
            return files.size();
        }

        @Override
        public Object getItem(int i) {
            return currentPath + "/" + files.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }
    }

    public FragmentForFileExplore() {
        super();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentPath = getArguments().getString("path");

        Log.d("mymessage", "onCreate: " + currentPath);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view;

        if(new File(currentPath).list().length > 0) {
            view = inflater.inflate(R.layout.fragment_file_explore, container, false);
            ListView listView = view.findViewById(R.id.listview_file_explore);
            final FileListAdapter adapter = new FileListAdapter(inflater, currentPath);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    connIF.fileListClicked((String) adapterView.getAdapter().getItem(i));
                }
            });
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    connIF.fileListLongClicked((String) adapterView.getAdapter().getItem(i));
                    return true;
                }
            });
        }

        else {
            view = inflater.inflate(R.layout.fragment_file_explore_empty_directory, container, false);
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        connIF = (ConnInterfaceForFileExplore) context;
    }
}
