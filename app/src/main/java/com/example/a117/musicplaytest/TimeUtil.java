package com.example.a117.musicplaytest;

public class TimeUtil {
    public static String sToHMS(int second) {
        int ms = second / 1000;
        int h = ms /3600;
        int m = ms % 3600 / 60;
        int s = ms % 3600 % 60;

        String result;
        if(h > 0)
            result = String.format("%02d:%02d:%02d", h, m, s);
        else
            result = String.format("%02d:%02d", m, s);
        return result;
    }
}
