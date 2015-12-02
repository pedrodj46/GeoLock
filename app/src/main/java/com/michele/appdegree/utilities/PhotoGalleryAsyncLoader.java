package com.michele.appdegree.utilities;

// Created by Michele Zardetto

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.michele.appdegree.adapters.items.PhotoItem;

import java.util.List;


public class PhotoGalleryAsyncLoader extends AsyncTaskLoader<List<PhotoItem>> {

    // questa classe gestice il processo asincrono di recupero delle immagini dalla memoria

    // lista foto
    private List<PhotoItem> mPhotoListItems;

    public PhotoGalleryAsyncLoader(Context context) {
        super(context);
    }

    public static String getBucketId(String path) {
        return String.valueOf(path.toLowerCase().hashCode());
    }

    // recupera le foto in background
    @Override
    public List<PhotoItem> loadInBackground() {
        final Context context = getContext();
        List<PhotoItem> photos = PhotoGalleryImageProvider.getAlbumThumbnails(context);
        return photos;
    }

    // richiamato ogni volta ci sono nuovi dati da inviare
    @Override public void deliverResult(List<PhotoItem> newPhotoListItems) {
        // quando non ci sono risultati, non vengono inviati dati
        if (isReset()) {
            if (newPhotoListItems != null) {
                onReleaseResources(newPhotoListItems);
            }
        }
        List<PhotoItem> oldPhotos = mPhotoListItems;
        mPhotoListItems = newPhotoListItems;

        if (isStarted()) {
            // se il caricamento e' iniziato invia subito i risultati gia' ottenuti
            super.deliverResult(newPhotoListItems);
        }

        // rilascio delle risorse non piu' necessarie
        if (oldPhotos != null) {
            onReleaseResources(oldPhotos);
        }
    }

    @Override protected void onStartLoading() {
        if (mPhotoListItems != null) {
            // se ci sono risultati disponibili, li invia subito
            deliverResult(mPhotoListItems);
        } else {
            // altrimenti forza un caricamento
            forceLoad();
        }
    }

    @Override protected void onStopLoading() {
        // forza lo stop di ulteriori caricamenti
        cancelLoad();
    }

    @Override public void onCanceled(List<PhotoItem> photoListItems) {
        super.onCanceled(photoListItems);

        // rilascia le risorse non piu' necessarie allo stop del caricamento
        onReleaseResources(photoListItems);
    }


    @Override protected void onReset() {
        super.onReset();

        // controlla che non ci siano caricamenti in atto
        onStopLoading();

        // rilascia tutte le risorse in uso
        if (mPhotoListItems != null) {
            onReleaseResources(mPhotoListItems);
            mPhotoListItems = null;
        }
    }

    protected void onReleaseResources(List<PhotoItem> photoListItems) {

    }
}