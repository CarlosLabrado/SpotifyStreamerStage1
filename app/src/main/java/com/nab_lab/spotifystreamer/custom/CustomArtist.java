package com.nab_lab.spotifystreamer.custom;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Artist;

public class CustomArtist implements Parcelable {

    public String imageURL;
    public String name;
    public String id;

    protected CustomArtist(Parcel in) {
        imageURL = in.readString();
        name = in.readString();
        id = in.readString();
    }


    public CustomArtist(Artist artist) {
        id = artist.id;
        name = artist.name;
        if (artist.images != null && !artist.images.isEmpty()) {
            imageURL = artist.images.get(0).url;
        } else {
            imageURL = "";
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageURL);
        dest.writeString(name);
        dest.writeString(id);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CustomArtist> CREATOR = new Parcelable.Creator<CustomArtist>() {
        @Override
        public CustomArtist createFromParcel(Parcel in) {
            return new CustomArtist(in);
        }

        @Override
        public CustomArtist[] newArray(int size) {
            return new CustomArtist[size];
        }
    };
}