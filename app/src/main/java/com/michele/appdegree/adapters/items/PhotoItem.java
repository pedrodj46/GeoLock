package com.michele.appdegree.adapters.items;

// Created by Michele Zardetto

import android.net.Uri;


public class PhotoItem {

    // questa classe viene utilizzata per contenere e gestire le informazioni riguardati la
    // posizione in memoria delle immagini

    private Uri fullImageUri;

    public PhotoItem(Uri fullImageUri) {
        this.fullImageUri = fullImageUri;
    }

    public Uri getFullImageUri() {
        return fullImageUri;
    }

    public void setFullImageUri(Uri fullImageUri) {
        this.fullImageUri = fullImageUri;
    }
}
