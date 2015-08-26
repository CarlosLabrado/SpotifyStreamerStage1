package com.nab_lab.spotifystreamer;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nab_lab.spotifystreamer.custom.CustomAdapter;
import com.nab_lab.spotifystreamer.custom.CustomArtist;
import com.nab_lab.spotifystreamer.custom.RecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ArtistListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ArtistListFragment extends Fragment {

    private final String TAG = ArtistListFragment.class.getSimpleName();

    private final String SAVED_ARTIST_LIST = "SAVED_ARTIST_LIST";
    private final String SAVED_ORIGINAL_SEARCH = "SAVED_ORIGINAL_SEARCH";

    private ArrayList<CustomArtist> mCustomArtists;

    private OnFragmentInteractionListener mListener;


    EditText editTextArtistSearch;
    LinearLayout containerArtists;

    private boolean searchIsRunning = false;
    private final long SEARCH_TRIGGER_DELAY_IN_MS = 1000;

    private Handler mHandler;

    private String originalSearch;

    volatile boolean running;

    public ArtistListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        running = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_artist_list, container, false);

        if (savedInstanceState != null) {
            mCustomArtists = savedInstanceState.getParcelableArrayList(SAVED_ARTIST_LIST);
            originalSearch = savedInstanceState.getString(SAVED_ORIGINAL_SEARCH);
        }
        editTextArtistSearch = (EditText) view.findViewById(R.id.editTextSearch);

        editTextArtistSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() > 0) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!searchIsRunning) {
                                searchIsRunning = true;
                                if (savedInstanceState == null || textChanged()) {
                                    searchTriggered();
                                } else {
                                    searchIsRunning = false;
                                    drawRecyclerView();
                                }
                            }
                        }
                    }, SEARCH_TRIGGER_DELAY_IN_MS);
                }
            }
        });

        containerArtists = (LinearLayout) view.findViewById(R.id.containerArtists);

        return view;
    }

    private boolean textChanged() {
        if (editTextArtistSearch.getText().toString().equalsIgnoreCase(originalSearch)) {
            return false;
        } else {
            originalSearch = editTextArtistSearch.getText().toString();
            return true;
        }
    }

    public void searchTriggered() {
        new SearchAsync().execute(editTextArtistSearch.getText().toString());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVED_ARTIST_LIST, mCustomArtists);
        if (editTextArtistSearch != null) {
            outState.putString(SAVED_ORIGINAL_SEARCH, originalSearch = editTextArtistSearch.getText().toString());
        }
    }

    /**
     * Programmatically adds the recycler view
     */
    private void drawRecyclerView() {
        containerArtists.removeAllViews();

        RecyclerView recyclerView = new RecyclerView(getActivity());
        recyclerView.setHasFixedSize(true);
        recyclerView.setVerticalScrollBarEnabled(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        RecyclerView.Adapter adapter = new CustomAdapter(mCustomArtists, getActivity());
        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Log.d(TAG, "clicked" + position);
                        mListener.onFragmentInteraction(mCustomArtists.get(position).id, mCustomArtists.get(position).name);
                    }
                })
        );

        containerArtists.addView(recyclerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.app_toolbar_title), null, false);
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

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
    }

    public void translateArtistListToCustom(List<Artist> items) {
        if (items != null && !items.isEmpty()) {

            mCustomArtists = new ArrayList<>();
            for (Artist artist : items) {
                CustomArtist customArtist = new CustomArtist(artist);
                mCustomArtists.add(customArtist);
            }
        }
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
        public void onFragmentInteraction(String artistId, String name);
    }

    /**
     * Async search subclass
     */
    private class SearchAsync extends AsyncTask<String, Void, ArtistsPager> {

        @Override
        protected ArtistsPager doInBackground(String... strings) {
            SpotifyApi api = new SpotifyApi();

            try {
                SpotifyService spotify = api.getService();

                if (!strings[0].isEmpty()) {
                    return spotify.searchArtists(strings[0]);
                } else {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArtistsPager artistsPager) {
            super.onPostExecute(artistsPager);
            if (running) {
                searchIsRunning = false;
                if (artistsPager != null && !artistsPager.artists.items.isEmpty()) {
                    translateArtistListToCustom(artistsPager.artists.items);
                    drawRecyclerView();
                } else {
                    Toast.makeText(getActivity(), "Sorry, we couldn't find anything related to that artist", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
