/*
* Copyright (C) 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.nab_lab.spotifystreamer.custom;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nab_lab.spotifystreamer.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Provide views to RecyclerView with data from mDataSet.
 */
public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    private static final String TAG = CustomAdapter.class.getSimpleName();

    private List<CustomArtist> mDataset;
    private Context mContext;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView mImageView;
        public TextView mTextView;
        public CardView mContainer;

        public ViewHolder(CardView v) {
            super(v);
            mContainer = v;
            mContainer.setCardBackgroundColor(v.getResources().getColor(R.color.color_cards));
            mImageView = (ImageView) v.findViewById(R.id.imageViewArtistImage);
            mTextView = (TextView) v.findViewById(R.id.textViewArtistName);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public CustomAdapter(List<CustomArtist> myDataset, Context context) {
        mDataset = myDataset;
        mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CustomAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
//        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.artist_item, parent, false);
//        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder((CardView) v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mTextView.setText(mDataset.get(position).name);
        if (!mDataset.get(position).imageURL.equalsIgnoreCase("")) {
            Picasso.with(mContext).
                    load(mDataset.get(position).imageURL).
                    into(holder.mImageView);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
