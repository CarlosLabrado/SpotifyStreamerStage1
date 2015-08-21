package com.nab_lab.spotifystreamer.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.nab_lab.spotifystreamer.PlaybackFragment;
import com.nab_lab.spotifystreamer.custom.TopTrack;
import com.nab_lab.spotifystreamer.events.PlayButtonEvent;

import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private MediaPlayer mMediaPlayer;
    ArrayList<TopTrack> mTopTracks;
    int mPosition;
    private final IBinder mMusicBind = new MusicBinder();

    int mTrackPosition = 0;

    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
//        throw new UnsupportedOperationException("Not yet implemented");
        return mMusicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
//        mMediaPlayer.stop();
//        mMediaPlayer.release();
        return false;
    }

    public boolean isMusicPlaying() {
        return mMediaPlayer.isPlaying();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mMediaPlayer = new MediaPlayer();
        initMusicPlayer();
    }

    public void initMusicPlayer() {
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    public int getProgress() {
        return mMediaPlayer.getCurrentPosition();
    }

    synchronized public void playSong() {
        mMediaPlayer.stop();
        synchronized (this) {
            mMediaPlayer.reset();
            TopTrack playTrack = mTopTracks.get(mPosition);
            String url = playTrack.previewURL;
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mMediaPlayer.setDataSource(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMediaPlayer.prepareAsync();
        }

    }

    public int pauseSong() {
        mTrackPosition = mMediaPlayer.getCurrentPosition();
        mMediaPlayer.pause();
        return mTrackPosition;
    }

    public void setTopTracks(ArrayList<TopTrack> topTracks) {
        mTopTracks = topTracks;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    public void setSeekTo(int position) {
        mMediaPlayer.start();
        mMediaPlayer.seekTo(position);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d("Media player", "Completed song");
        PlaybackFragment.bus.post(new PlayButtonEvent(0));
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
}
