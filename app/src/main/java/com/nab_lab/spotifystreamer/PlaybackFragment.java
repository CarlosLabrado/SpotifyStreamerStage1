package com.nab_lab.spotifystreamer;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nab_lab.spotifystreamer.custom.TopTrack;
import com.nab_lab.spotifystreamer.events.PlayButtonEvent;
import com.nab_lab.spotifystreamer.events.SeekBarProgressEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


/**
 */
public class PlaybackFragment extends DialogFragment {

    private static final String ARG_ARTIST_NAME = "PARAM_ARTIST_NAME";
    private static final String ARG_TOP_TRACKS = "PARAM_TOP_TRACKS";
    private static final String ARG_TRACK_POSITION = "PARAM_TRACK_POSITION";

    private OnFragmentInteractionListener mListener;

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

    public static Bus bus;

    boolean isPauseButtonShowing = false;

    public PlaybackFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(int buttonPressed, boolean seekBarPressed, int progress);
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
        bus = new Bus();
        bus.register(this);
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
                if (isPauseButtonShowing) {
                    mListener.onFragmentInteraction(1, false, 0); // then we call for pause
                    buttonPlaybackPlay.setImageResource(R.drawable.button_play);
                    isPauseButtonShowing = false;
                } else {
                    mListener.onFragmentInteraction(0, false, 0); // then we call for play
                    buttonPlaybackPlay.setImageResource(R.drawable.button_pause);
                    isPauseButtonShowing = true; // it is now
                }
            }
        });

        buttonPlaybackNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onFragmentInteraction(3, false, 0);
            }
        });

        buttonPlaybackPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onFragmentInteraction(2, false, 0);
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
                mListener.onFragmentInteraction(0, true, seekBar.getProgress());
            }
        });



        return view;
    }

    @Subscribe
    public void setSeekBarProgress(SeekBarProgressEvent event) {
        seekBarPlayback.setProgress(event.getProgress());
    }

    @Subscribe
    public void setPlayButtonImage(PlayButtonEvent event) {
        if (event.getProgress() == 1) {
            isPauseButtonShowing = true;
            buttonPlaybackPlay.setImageResource(R.drawable.button_pause);

        } else {
            isPauseButtonShowing = false;
            buttonPlaybackPlay.setImageResource(R.drawable.button_play);
        }

    }


    @Subscribe
    public void initArtLayout(Integer newPosition) {
        textViewAlbumTitle.setText(mTopTracks.get(newPosition).albumName);
        textViewSongName.setText(mTopTracks.get(newPosition).trackName);
        Picasso.with(getActivity())
                .load(mTopTracks.get(newPosition).imageURL)
                .into(imageViewAlbumImage);
    }


    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.app_toolbar_title_now_playing), mArtistName, true);

    }


}
