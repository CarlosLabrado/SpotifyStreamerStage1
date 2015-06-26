package com.nab_lab.spotifystreamer.custom;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Parcelable topTrack so we can save the state when the user rotates the phone
 * and not waste spotify api calls
 * used http://www.parcelabler.com/
 */
public class TopTrack implements Parcelable {

    public String trackName;

    public String albumName;

    public String imageURL;


    protected TopTrack(Parcel in) {
        trackName = in.readString();
        albumName = in.readString();
        imageURL = in.readString();
    }

    public TopTrack(Track track) {
        trackName = track.name;
        albumName = track.album.name;
        // we always get the first image of that album
        imageURL = track.album.images.get(0).url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(trackName);
        dest.writeString(albumName);
        dest.writeString(imageURL);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<TopTrack> CREATOR = new Parcelable.Creator<TopTrack>() {
        @Override
        public TopTrack createFromParcel(Parcel in) {
            return new TopTrack(in);
        }

        @Override
        public TopTrack[] newArray(int size) {
            return new TopTrack[size];
        }
    };
}