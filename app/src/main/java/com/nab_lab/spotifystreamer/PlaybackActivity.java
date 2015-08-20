package com.nab_lab.spotifystreamer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nab_lab.spotifystreamer.custom.TopTrack;
import com.nab_lab.spotifystreamer.service.MusicService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 */
public class PlaybackActivity extends AppCompatActivity {

    Toolbar toolbar;
    String mArtistName;
    ArrayList<TopTrack> mTopTracks;
    int mPosition;

    TextView textViewArtistName;
    TextView textViewAlbumTitle;
    TextView textViewSongName;
    ImageView imageViewAlbumImage;

    SeekBar seekBarPlayback;
    TextView textViewPlaybackCurrent;
    TextView textViewPlaybackTotalLength;

    ImageButton buttonPlaybackPrevious;
    ImageButton buttonPlaybackPlay;
    ImageButton buttonPlaybackNext;

    private MusicService mMusicService;
    private Intent mPlayIntent;
    private boolean mMusicBound = false;

    final Handler mHandler = new Handler();

    ScheduledExecutorService scheduleTaskExecutor;

    private boolean mSeekBarTaskIsRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);

        Intent intent = getIntent();
        mArtistName = intent.getStringExtra("artistName");
        mTopTracks = intent.getParcelableArrayListExtra("topTracks");
        mPosition = intent.getIntExtra("position", 0);


        toolbar = (Toolbar) findViewById(R.id.toolbar);

        /**toolBar **/
        setUpToolBar();

        textViewArtistName = (TextView) findViewById(R.id.textViewArtistName);
        textViewAlbumTitle = (TextView) findViewById(R.id.textViewAlbumTitle);
        textViewSongName = (TextView) findViewById(R.id.textViewSongName);
        imageViewAlbumImage = (ImageView) findViewById(R.id.imageViewAlbumImage);

        seekBarPlayback = (SeekBar) findViewById(R.id.seekBarPlayBack);
        textViewPlaybackCurrent = (TextView) findViewById(R.id.textViewPlaybackCurrent);
        textViewPlaybackTotalLength = (TextView) findViewById(R.id.textViewPlayBackTotalLength);

        buttonPlaybackPrevious = (ImageButton) findViewById(R.id.buttonPlaybackPrevious);
        buttonPlaybackPlay = (ImageButton) findViewById(R.id.buttonPlaybackPlay);
        buttonPlaybackNext = (ImageButton) findViewById(R.id.buttonPlaybackNext);

        textViewArtistName.setText(mArtistName);
        initArtLayout(mPosition);

        buttonPlaybackPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClicked();
            }
        });

        buttonPlaybackNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextClicked();
            }
        });

        buttonPlaybackPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousClicked();
            }
        });

        seekBarPlayback.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 100) {
                    progress = 0;
                }
                String progressString;
                if (progress < 10) {
                    progressString = "0" + progress;
                } else {
                    progressString = String.valueOf(progress);
                }
                textViewPlaybackCurrent.setText("0:" + progressString);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        scheduleTaskExecutor = Executors.newScheduledThreadPool(5);


    }

    /**
     * This task will track the progress of the song and update the seekbar
     */
    public void executeTaskForSeekBar() {

        if (!mSeekBarTaskIsRunning) {
            mSeekBarTaskIsRunning = true;
            // This schedule a runnable task every second
            scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    if (mMusicService != null) {
                        int progress = (mMusicService.getProgress() / 1000);
                        Log.d("progress", String.valueOf(progress));
                        seekBarPlayback.setProgress(progress);
                    }

                    if (mMusicBound) mHandler.postDelayed(this, 1000);
                }
            }, 0, 1, TimeUnit.SECONDS);
        }
    }

    /**
     * Play Button Clicked
     */
    private void playClicked() {
        mMusicService.setPosition(mPosition);
        mMusicService.playSong();
        executeTaskForSeekBar();
    }

    /**
     * Next Button Clicked
     */
    private void nextClicked() {
        int newPosition = mPosition + 1;
        if (mMusicBound && (newPosition >= 0 && newPosition < mTopTracks.size())) {

            initArtLayout(newPosition);

            mMusicService.setPosition(newPosition);
            mMusicService.playSong();
            mPosition = newPosition;
        }
        executeTaskForSeekBar();
    }

    /**
     * Previous Button Clicked
     */
    private void previousClicked() {
        int newPosition = mPosition - 1;
        if (mMusicBound && (newPosition >= 0 && newPosition < mTopTracks.size())) {

            initArtLayout(newPosition);

            mMusicService.setPosition(newPosition);
            mMusicService.playSong();
            mPosition = newPosition;
        }
        executeTaskForSeekBar();
    }

    private void initArtLayout(int newPosition) {
        textViewAlbumTitle.setText(mTopTracks.get(newPosition).albumName);
        textViewSongName.setText(mTopTracks.get(newPosition).trackName);
        Picasso.with(this)
                .load(mTopTracks.get(newPosition).imageURL)
                .into(imageViewAlbumImage);
    }


    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            mMusicService = binder.getService();
            mMusicService.setTopTracks(mTopTracks);
            mMusicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMusicBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (mPlayIntent == null) {
            mPlayIntent = new Intent(this, MusicService.class);
            bindService(mPlayIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(mPlayIntent);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mMusicBound) {
            unbindService(musicConnection);
            mMusicBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        mSeekBarTaskIsRunning = false;
        stopService(mPlayIntent);
        mMusicService = null;
        super.onDestroy();
    }

    /**
     * sets up the top bar
     */
    private void setUpToolBar() {
        setSupportActionBar(toolbar);
        setActionBarTitle(getString(R.string.app_toolbar_title), null, false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        // enabling action bar app icon and behaving it as toggle button
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    /**
     * Gets called from the fragments onResume and its because only the first doesn't have the up
     * button on the actionBar
     *
     * @param title          The title to show on the ActionBar
     * @param subtitle       The subtitle to show on the ActionBar
     * @param showNavigateUp if true, shows the up button
     */
    public void setActionBarTitle(String title, String subtitle, boolean showNavigateUp) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            if (subtitle != null) {
                getSupportActionBar().setSubtitle(subtitle);
            } else {
                getSupportActionBar().setSubtitle(null);
            }
            if (showNavigateUp) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            } else {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }
    }
}
