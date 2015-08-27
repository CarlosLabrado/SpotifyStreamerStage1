package com.nab_lab.spotifystreamer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.util.Log;

import com.nab_lab.spotifystreamer.custom.MyPlayerSingleton;
import com.nab_lab.spotifystreamer.custom.TopTrack;
import com.nab_lab.spotifystreamer.events.PlayButtonEvent;
import com.nab_lab.spotifystreamer.events.SeekBarProgressEvent;
import com.nab_lab.spotifystreamer.service.MusicService;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity implements ArtistListFragment.OnFragmentInteractionListener,
        TopTracksFragment.OnFragmentInteractionListener, PlaybackFragment.OnFragmentInteractionListener {

    private static final String SAVED_TOP_TRACKS = "SAVED_TOP_TRACKS";
    private static final String SAVED_POSITION = "SAVED_POSITION";
    private final String TAG = MainActivity.class.getSimpleName();

    Toolbar toolbar;


    private String mArtistName;
    private ArrayList<TopTrack> mTopTracks;
    private int mPosition;

    public static Bus bus;

    private MusicService mMusicService;
    private Intent mPlayIntent;
    private boolean mMusicBound = false;

    final Handler mHandler = new Handler();

    ScheduledExecutorService scheduleTaskExecutor;

    private boolean mSeekBarTaskIsRunning = false;

    private int mPauseSeekPosition = 0;

    private boolean isTablet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bus = new Bus();
        bus.register(this);

        isTablet = getResources().getBoolean(R.bool.isTablet);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        /**toolBar **/
        setUpToolBar();

        if (savedInstanceState == null) {
            /**
             * This dummy fragment is to prevent the transition error when popping the fragment backStack
             * Error: Attempt to invoke virtual method 'boolean android.support.v4.app.Fragment.getAllowReturnTransitionOverlap()' on a null object reference
             * https://code.google.com/p/android/issues/detail?id=82832
             */
            FragmentManager fragmentManager = getSupportFragmentManager();

            fragmentManager.beginTransaction()
                    .add(R.id.container, new Fragment())
                    .addToBackStack("dummy")
                    .commit();

            if (!isTablet) {
                fillContainerWithFragment(0, null, null, null, 0);
            }
        } else {
            mTopTracks = savedInstanceState.getParcelableArrayList(SAVED_TOP_TRACKS);
        }

        scheduleTaskExecutor = Executors.newScheduledThreadPool(5);

    }

    private ServiceConnection musicConnection;

    private void fillContainerWithFragment(int fragmentNumber, String artistId, String artistName, ArrayList<TopTrack> topTracks, int trackPosition) {
        Fragment fragment = null;
        String fragmentName = "";
        switch (fragmentNumber) {
            case 0:
                fragment = new ArtistListFragment();
                fragmentName = "Artist";
                break;
            case 1:
                new TopTracksFragment();
                fragment = TopTracksFragment.newInstance(artistId, artistName);
                fragmentName = "TopTracks";
                break;
            case 2:
                new PlaybackFragment();
                fragment = PlaybackFragment.newInstance(artistName, topTracks, trackPosition);
                fragmentName = "Playback";
            default:
                break;
        }
        if (fragment != null) { //lollipop fancy transitions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.explode));
                fragment.setExitTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));
            }
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(fragmentName)
                    .commit();
            Log.d(TAG, "fragment added " + fragment.getTag());
        } else {
            // error in creating fragment
            Log.e(TAG, "Error in creating fragment");
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mTopTracks != null && !mTopTracks.isEmpty()) {
            outState.putParcelableArrayList(SAVED_TOP_TRACKS, mTopTracks);
            outState.putInt(SAVED_POSITION, mPosition);
        }
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

    @Override
    public boolean onSupportNavigateUp() {
        getSupportFragmentManager().popBackStack();
        return true;
    }

// we don't need this now, but the mockups show it so I'll leave it under comments
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    // Artist List
    @Override
    public void onFragmentInteraction(String artistId, String name) {
        if (artistId != null) {
            fillContainerWithFragment(1, artistId, name, null, 0);
            Log.d(TAG, artistId);
        }
    }

    // Top tracks
    @Override
    public void onFragmentInteraction(ArrayList<TopTrack> topTracks, String artistName, int position) {
        mArtistName = artistName;
        mTopTracks = topTracks;
        mPosition = position;

        attemptMusicServiceConnection();

        if (isTablet) {
            new PlaybackFragment();
            FragmentManager fm = getSupportFragmentManager();
            DialogFragment fragment = PlaybackFragment.newInstance(artistName, topTracks, position);
            fragment.show(fm, "Dialog Fragment");
        } else {
            fillContainerWithFragment(2, null, artistName, topTracks, position);
        }
    }

    private void attemptMusicServiceConnection() {
        try {
            musicConnection = new ServiceConnection() {
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
            bindService(mPlayIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(mPlayIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Playback
    @Override
    public void onFragmentInteraction(int buttonPressed, boolean seekBarPressed, int progress) {
        if (mMusicService == null) {
            attemptMusicServiceConnection();
        } else {

            if (seekBarPressed) {
                mMusicService.setSeekTo(progress * 1000); // seek to wants milliseconds
            } else {
                switch (buttonPressed) {
                    case 0:
                        playClicked();
                        break;
                    case 1:
                        pauseClicked();
                        break;
                    case 2:
                        previousClicked();
                        break;
                    case 3:
                        nextClicked();
                        break;
                }
            }
        }

    }

    /**
     * Play Button Clicked
     */
    private void playClicked() {
        if (mPauseSeekPosition != 0) {
            mMusicService.setSeekTo(mPauseSeekPosition);
            mPauseSeekPosition = 0;
        } else {
            mMusicService.setPosition(mPosition);
            mMusicService.playSong();
        }
        executeTaskForSeekBar();
    }

    private void pauseClicked() {
        if (mPauseSeekPosition != 0) {
            mMusicService.setSeekTo(mPauseSeekPosition);
        }
        mPauseSeekPosition = mMusicService.pauseSong();
    }

    /**
     * Next Button Clicked
     */
    private void nextClicked() {
        mPauseSeekPosition = 0;
        int newPosition = mPosition + 1;
        if (mMusicBound && (newPosition >= 0 && newPosition < mTopTracks.size())) {

            PlaybackFragment.bus.post(newPosition);

            mMusicService.setPosition(newPosition);
            mMusicService.playSong();
            mPosition = newPosition;

            PlaybackFragment.bus.post(new PlayButtonEvent(1));
        }
        executeTaskForSeekBar();
    }

    /**
     * Previous Button Clicked
     */
    private void previousClicked() {
        int newPosition = mPosition - 1;
        if (mMusicBound && (newPosition >= 0 && newPosition < mTopTracks.size())) {

            PlaybackFragment.bus.post(newPosition);

            mMusicService.setPosition(newPosition);
            mMusicService.playSong();
            mPosition = newPosition;

            PlaybackFragment.bus.post(new PlayButtonEvent(1));
        }
        executeTaskForSeekBar();
    }


    /**
     * This task will track the progress of the song and update the seekbar
     */
    public void executeTaskForSeekBar() {
        PlaybackFragment.bus.post(new SeekBarProgressEvent(mPauseSeekPosition));

        if (!mSeekBarTaskIsRunning) {
            mSeekBarTaskIsRunning = true;
            // This schedule a runnable task every second
            scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    MyPlayerSingleton myPlayerSingleton = MyPlayerSingleton.getInstance();
                    MediaPlayer mediaPlayer = myPlayerSingleton.getMediaPlayer();
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    if (mediaPlayer.isPlaying()) {
                        int progress = (currentPosition / 1000);
                        Log.d("progress", String.valueOf(currentPosition));
                        PlaybackFragment.bus.post(new SeekBarProgressEvent(progress + 1));
                    }

                    mHandler.postDelayed(this, 1000);
                }
            }, 0, 1, TimeUnit.SECONDS);
        }
    }

    @Subscribe
    public void setCurrentTrackPosition(Integer position) {
        mPosition = position;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mPlayIntent == null) {
            mPlayIntent = new Intent(this, MusicService.class);
            if (musicConnection != null) {
                bindService(mPlayIntent, musicConnection, Context.BIND_AUTO_CREATE);
                startService(mPlayIntent);
            }

        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mMusicBound) {
            if (musicConnection != null) {

                unbindService(musicConnection);
                mMusicBound = false;
            }
        }
    }

    @Override
    public void onDestroy() {
        mSeekBarTaskIsRunning = false;
        stopService(mPlayIntent);
        mMusicService = null;
        super.onDestroy();
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


    @Override
    public void onBackPressed() {
        int fragments = getSupportFragmentManager().getBackStackEntryCount();
        if (fragments > 2) { // we now have a dummy all the time, plus the initial fragment
            super.onBackPressed();
        } else {
            finish();
        }
    }


}
