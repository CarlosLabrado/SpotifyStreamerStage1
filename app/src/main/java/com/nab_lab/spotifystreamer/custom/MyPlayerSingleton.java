package com.nab_lab.spotifystreamer.custom;

import android.media.MediaPlayer;

/**
 * Singleton for the media player
 */
public class MyPlayerSingleton {

    static MediaPlayer mediaPlayer;

    private static volatile MyPlayerSingleton instance = null;

//    private static MyPlayer ourInstance = new MyPlayer();

    public static MyPlayerSingleton getInstance() {
        if (instance == null) {
            synchronized (MyPlayerSingleton.class) {
                if (instance == null) {
                    mediaPlayer = new MediaPlayer();
                    instance = new MyPlayerSingleton();
                }
            }
        }
        return instance;
    }

    private MyPlayerSingleton() {
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
}
