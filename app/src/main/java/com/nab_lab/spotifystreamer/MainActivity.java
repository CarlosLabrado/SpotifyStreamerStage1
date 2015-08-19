package com.nab_lab.spotifystreamer;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.util.Log;

import com.nab_lab.spotifystreamer.custom.TopTrack;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements ArtistListFragment.OnFragmentInteractionListener,
        TopTracksFragment.OnFragmentInteractionListener {

    private final String TAG = MainActivity.class.getSimpleName();

    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        /**toolBar **/
        setUpToolBar();

        if (savedInstanceState == null) {
            // on first time display view for first nav item
            fillContainerWithFragment(0, null, null);
        }

    }

    private void fillContainerWithFragment(int position, String artistId, String artistName) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new ArtistListFragment();
                break;
            case 1:
                new TopTracksFragment();
                fragment = TopTracksFragment.newInstance(artistId, artistName);
                break;
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
                    .addToBackStack(null)
                    .replace(R.id.container, fragment)
                    .commit();
            Log.d(TAG, "fragment added " + fragment.getTag());
        } else {
            // error in creating fragment
            Log.e(TAG, "Error in creating fragment");
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

    @Override
    public void onFragmentInteraction(String artistId, String name) {
        if (artistId != null) {
            fillContainerWithFragment(1, artistId, name);
            Log.d(TAG, artistId);
        }
    }

    @Override
    public void onFragmentInteraction(ArrayList<TopTrack> topTracks, String artistName, int position) {
        Intent intent = new Intent(this, PlaybackActivity.class);
        intent.putExtra("artistName", artistName);
        intent.putExtra("position", position);
        intent.putParcelableArrayListExtra("topTracks", topTracks);
        startActivity(intent);
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
        if (fragments > 1) {
            super.onBackPressed();
        } else {
            finish();
        }
    }

}
