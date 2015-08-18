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
import com.nab_lab.spotifystreamer.custom.RecyclerItemClickListener;

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


    private OnFragmentInteractionListener mListener;


    EditText editTextArtistSearch;
    LinearLayout containerArtists;

    private boolean searchIsRunning = false;
    private final long SEARCH_TRIGGER_DELAY_IN_MS = 1000;

    private Handler mHandler;

    private List<Artist> mArtistList;


    public ArtistListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_artist_list, container, false);
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
                                searchTriggered();
                            }
                        }
                    }, SEARCH_TRIGGER_DELAY_IN_MS);
                }
            }
        });

        containerArtists = (LinearLayout) view.findViewById(R.id.containerArtists);

        return view;
    }

    public void searchTriggered() {
        new SearchAsync().execute(editTextArtistSearch.getText().toString());
    }

    /**
     * Programmatically adds the recycler view
     *
     * @param items the Artist list
     */
    private void drawRecyclerView(List<Artist> items) {
        containerArtists.removeAllViews();

        mArtistList = items;

        RecyclerView recyclerView = new RecyclerView(getActivity());
        recyclerView.setHasFixedSize(true);
        recyclerView.setVerticalScrollBarEnabled(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        RecyclerView.Adapter adapter = new CustomAdapter(items, getActivity());
        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Log.d(TAG, "clicked" + position);
                        mListener.onFragmentInteraction(mArtistList.get(position).id, mArtistList.get(position).name);
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
            searchIsRunning = false;
            if (artistsPager != null && !artistsPager.artists.items.isEmpty()) {
                drawRecyclerView(artistsPager.artists.items);
            } else {
                Toast.makeText(getActivity(), "Sorry, we couldn't find anything related to that artist", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
