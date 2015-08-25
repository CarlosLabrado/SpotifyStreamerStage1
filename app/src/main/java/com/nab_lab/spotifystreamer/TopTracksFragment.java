package com.nab_lab.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nab_lab.spotifystreamer.custom.CustomAdapterTracks;
import com.nab_lab.spotifystreamer.custom.RecyclerItemClickListener;
import com.nab_lab.spotifystreamer.custom.TopTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TopTracksFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TopTracksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TopTracksFragment extends Fragment {
    private final String TAG = TopTracksFragment.class.getSimpleName();

    private static final String ARG_ARTIST_ID = "paramArtistID";
    private static final String ARG_ARTIST_NAME = "paramArtistName";
    private static final String PARCELABLE_TOP_TRACKS_LIST = "parcelableTopTracksList";

    private String mArtistId;
    private String mArtistName;

    private OnFragmentInteractionListener mListener;

    private LinearLayout mContainerTracks;

    private Tracks mTracks;

    private ArrayList<TopTrack> mTopTracks;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Artist id
     * @param param2 Artist name
     * @return A new instance of fragment TopTracksFragment.
     */
    public static TopTracksFragment newInstance(String param1, String param2) {
        TopTracksFragment fragment = new TopTracksFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ARTIST_ID, param1);
        args.putString(ARG_ARTIST_NAME, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public TopTracksFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mArtistId = getArguments().getString(ARG_ARTIST_ID);
            mArtistName = getArguments().getString(ARG_ARTIST_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        mContainerTracks = (LinearLayout) view.findViewById(R.id.containerTopTracks);
        if (savedInstanceState == null) {
            if (isNetworkAvailable(getActivity())) {
                new SearchTopTracksAsync().execute("");
            }
        } else {
            mTopTracks = savedInstanceState.getParcelableArrayList(PARCELABLE_TOP_TRACKS_LIST);
            drawRecyclerView(true);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(PARCELABLE_TOP_TRACKS_LIST, mTopTracks);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.app_toolbar_title_top_tracks), mArtistName, true);
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
     * Programmatically adds the recycler view
     *
     * @param isSavedState indicates if there was a saved state ej. User rotated screen
     */
    private void drawRecyclerView(boolean isSavedState) {
        if (mTopTracks != null && !mTopTracks.isEmpty()) {
            mContainerTracks.removeAllViews();

            RecyclerView recyclerView = new RecyclerView(getActivity());
            recyclerView.setHasFixedSize(true);
            recyclerView.setVerticalScrollBarEnabled(true);

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(layoutManager);

            RecyclerView.Adapter adapter = new CustomAdapterTracks(mTopTracks, getActivity());
            recyclerView.setAdapter(adapter);

            recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            Log.d(TAG, "clicked" + position);
                            mListener.onFragmentInteraction(mTopTracks,
                                    mArtistName, position);
                        }
                    })
            );
            mContainerTracks.addView(recyclerView);
        } else {
            getActivity().getSupportFragmentManager().popBackStack();
            // no results found
            Toast.makeText(getActivity(), R.string.toast_no_top_tracks, Toast.LENGTH_SHORT).show();
        }

    }

    private void transformIntoParcelable(List<Track> items) {
        if (items != null && !items.isEmpty()) {
            mTopTracks = new ArrayList<>();
            for (Track track : items) {
                TopTrack parcelableTrack = new TopTrack(track);
                mTopTracks.add(parcelableTrack);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Checks for an existing network connectivity
     *
     * @param context The {@link Context} which is needed to tap
     *                {@link Context#CONNECTIVITY_SERVICE}
     * @return True if network connection is available
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(ArrayList<TopTrack> mTopTracks, String mArtistName, int position);
    }

    private class SearchTopTracksAsync extends AsyncTask<String, Void, Tracks> {
        @Override
        protected Tracks doInBackground(String... strings) {
            SpotifyApi api = new SpotifyApi();

            SpotifyService spotify = api.getService();

            Map<String, Object> map = new HashMap<>();
            map.put("country", Locale.getDefault().getCountry());

            try {
                mTracks = spotify.getArtistTopTrack(mArtistId, map);
            } catch (Exception e) {
                Log.e(TAG, "there is an spotify API backend error");
                e.printStackTrace();
            }

            return mTracks;
        }

        @Override
        protected void onPostExecute(Tracks tracks) {
            super.onPostExecute(tracks);
            transformIntoParcelable(tracks.tracks);
            drawRecyclerView(false);
        }
    }

}
