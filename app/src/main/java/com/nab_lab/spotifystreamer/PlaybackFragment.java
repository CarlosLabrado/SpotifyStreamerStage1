package com.nab_lab.spotifystreamer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class PlaybackFragment extends DialogFragment {

    private static final String ARG_ARTIST_NAME = "PARAM_ARTIST_NAME";
    private static final String ARG_TOP_TRACKS = "PARAM_TOP_TRACKS";
    private static final String ARG_TRACK_POSITION = "PARAM_TRACK_POSITION";


    private String mArtistName;
    private ArrayList<TopTrack> mTopTracks;
    private int mPosition;

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

    public PlaybackFragment() {
    }


    /**
     * Factory method to create a new instance of this fragment
     *
     * @param artistName    Artist Name
     * @param topTracks     top tracks to play
     * @param trackPosition current track to play
     * @return A new instance of PlaybackFragment
     */
    public static PlaybackFragment newInstance(String artistName, ArrayList<TopTrack> topTracks, int trackPosition) {
        PlaybackFragment fragment = new PlaybackFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ARTIST_NAME, artistName);
        args.putParcelableArrayList(ARG_TOP_TRACKS, topTracks);
        args.putInt(ARG_TRACK_POSITION, trackPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mArtistName = getArguments().getString(ARG_ARTIST_NAME);
            mTopTracks = getArguments().getParcelableArrayList(ARG_TOP_TRACKS);
            mPosition = getArguments().getInt(ARG_TRACK_POSITION);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playback, container, false);

        textViewArtistName = (TextView) view.findViewById(R.id.textViewArtistName);
        textViewAlbumTitle = (TextView) view.findViewById(R.id.textViewAlbumTitle);
        textViewSongName = (TextView) view.findViewById(R.id.textViewSongName);
        imageViewAlbumImage = (ImageView) view.findViewById(R.id.imageViewAlbumImage);

        seekBarPlayback = (SeekBar) view.findViewById(R.id.seekBarPlayBack);
        textViewPlaybackCurrent = (TextView) view.findViewById(R.id.textViewPlaybackCurrent);
        textViewPlaybackTotalLength = (TextView) view.findViewById(R.id.textViewPlayBackTotalLength);

        buttonPlaybackPrevious = (ImageButton) view.findViewById(R.id.buttonPlaybackPrevious);
        buttonPlaybackPlay = (ImageButton) view.findViewById(R.id.buttonPlaybackPlay);
        buttonPlaybackNext = (ImageButton) view.findViewById(R.id.buttonPlaybackNext);

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

        return view;
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
                    if (mMusicService != null && mMusicService.isMusicPlaying()) {
                        int progress = (mMusicService.getProgress() / 1000);
                        Log.d("progress", String.valueOf(progress));
                        seekBarPlayback.setProgress(progress + 1);
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
        Picasso.with(getActivity())
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
    public void onStart() {
        super.onStart();
        if (mPlayIntent == null) {
            mPlayIntent = new Intent(getActivity(), MusicService.class);
            getActivity().bindService(mPlayIntent, musicConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(mPlayIntent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.app_toolbar_title_now_playing), mArtistName, true);

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mMusicBound) {
            getActivity().unbindService(musicConnection);
            mMusicBound = false;
        }
    }

    @Override
    public void onDestroy() {
        mSeekBarTaskIsRunning = false;
        getActivity().stopService(mPlayIntent);
        mMusicService = null;
        super.onDestroy();
    }

}
