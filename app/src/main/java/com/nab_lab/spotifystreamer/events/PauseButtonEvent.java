package com.nab_lab.spotifystreamer.events;

/**
 * Created by Vazh on 20/8/2015.
 */
public class PauseButtonEvent {
    int progress;

    public PauseButtonEvent(int progress) {
        this.progress = progress;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
