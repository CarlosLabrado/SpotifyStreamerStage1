package com.nab_lab.spotifystreamer;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.nab_lab.spotifystreamer.custom.CustomAdapterTracks;

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

    private static final String ARG_PARAM1 = "param1";

    private String mArtistId;

    RecyclerView mRecyclerViewTopTracks;

    private OnFragmentInteractionListener mListener;

    private LinearLayout mContainerTracks;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment TopTracksFragment.
     */
    public static TopTracksFragment newInstance(String param1) {
        TopTracksFragment fragment = new TopTracksFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
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
            mArtistId = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_top_tracks, container, false);
//        mRecyclerViewTopTracks = (RecyclerView) view.findViewById(R.id.recycler_view_top_tracks);

        mContainerTracks = (LinearLayout) view.findViewById(R.id.containerTopTracks);
        new SearchTopTracksAsync().execute("");

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
     * @param items the Artist list
     */
    private void drawRecyclerView(List<Track> items) {
        mContainerTracks.removeAllViews();

        RecyclerView recyclerView = new RecyclerView(getActivity());
        recyclerView.setHasFixedSize(true);
        recyclerView.setVerticalScrollBarEnabled(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        RecyclerView.Adapter adapter = new CustomAdapterTracks(items, getActivity());
        recyclerView.setAdapter(adapter);

//        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(View view, int position) {
//                        Log.d(TAG, "clicked" + position);
//                        mListener.onFragmentInteraction(mArtistList.get(position).id);
//                    }
//                })
//        );

        mContainerTracks.addView(recyclerView);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private class SearchTopTracksAsync extends AsyncTask<String, Void, Tracks> {
        @Override
        protected Tracks doInBackground(String... strings) {
            SpotifyApi api = new SpotifyApi();

            SpotifyService spotify = api.getService();

            Map<String, Object> map = new HashMap<>();
            map.put("country", Locale.getDefault().getCountry());

            Tracks tracks = spotify.getArtistTopTrack(mArtistId, map);

            return tracks;
        }

        @Override
        protected void onPostExecute(Tracks tracks) {
            super.onPostExecute(tracks);
            drawRecyclerView(tracks.tracks);
        }
    }

}
