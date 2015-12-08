package com.michele.appdegree;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.michele.appdegree.adapters.CustomListAdapter;
import com.michele.appdegree.adapters.items.PhotoItem;
import com.michele.appdegree.utilities.PhotoGalleryAsyncLoader;
import com.michele.fragmentexample.R;

import java.util.ArrayList;
import java.util.List;


public class mainGallery extends Fragment implements ListView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<List<PhotoItem>> {

    // Fragment che gestisce la visualizzazione della galleria immagini

    ListView list;

    static protected ArrayList<PhotoItem> mPhotoListItem;
    static protected ProgressDialog mLoadingProgressDialog;
    protected TextView mEmptyTextView;
    protected CustomListAdapter adapter;
    Boolean noPhotos = false;


    // segue l'interfaccia per lo scambio dei dati con la mainActivity
    ToolbarListener activityCallback;

    public interface ToolbarListener {
        public void onPictureSelected(String imagePath);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try {
            // interfaccia
            activityCallback = (ToolbarListener) activity;

            if(mPhotoListItem == null) {
                // Show a progress dialog.
                mLoadingProgressDialog = new ProgressDialog(getActivity());
                mLoadingProgressDialog.setMessage("Looking for Photos...");
                mLoadingProgressDialog.setCancelable(true);
                mLoadingProgressDialog.show();
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ToolbarListener");
        }
    }

    public void pictureSelected(String imagePath) {
        activityCallback.onPictureSelected(imagePath);
    }
    // termine delle righe necessarie a impostare il trasferimento dati tramite interfaccia


    public mainGallery() {
        super();
    }


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // inizializzo l'adapter
        mPhotoListItem = new ArrayList<PhotoItem>() ;
        adapter = new CustomListAdapter(getActivity(), mPhotoListItem, null);

        // inizializzo il caricamento delle immagini
        getLoaderManager().initLoader(0, null, this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View rowView = null;
        rowView = inflater.inflate(R.layout.main_gallery, container, false);

        list = (ListView) rowView.findViewById(R.id.lista01);
        ((AdapterView<ListAdapter>) list).setAdapter(adapter);

        // testo invisibile che visualizza un messaggio di errore in assenza di foto
        mEmptyTextView = (TextView)rowView.findViewById(R.id.empty01);
        // valuta se visualizzare il messaggio di errore o meno - e' strettamente legato ad adapter
        resolveEmptyText();

        list.setOnItemClickListener(this);

        return rowView;
    }


    // risultato di setOnItemClickListener(this) dentro onCreateView - funziona grazie
    // all'implementazione di ListView.OnItemClickListener
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // recupero il percoso legato all'immagine cliccata nella listview
        PhotoItem photoListItem = (PhotoItem)this.adapter.getItem(position);
        String imagePath = photoListItem.getFullImageUri().getPath();
        pictureSelected(imagePath);

        Log.d("path immagine", imagePath);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        cancelProgressDialog();
    }

    @Override
    public void onPause(){
        super.onPause();
        cancelProgressDialog();
    }

    @Override
    public void onStop(){
        super.onStop();
        cancelProgressDialog();
    }


    // metodo per caricare le immagini in background
    @Override
    public Loader<List<PhotoItem>> onCreateLoader(int id, Bundle args) {
        // inizializza il caricamento e ne genera uno nuovo quando serve
        return new PhotoGalleryAsyncLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<PhotoItem>> loader, List<PhotoItem> data) {
        // inserisce i dati nell'adapter dopo averlo ripulito
        mPhotoListItem.clear();

        if(data.size()>0) {
            for (int i = 0; i < data.size(); i++) {
                PhotoItem item = data.get(i);
                mPhotoListItem.add(item);
            }
        } else {
            noPhotos = true;
        }

        // per ogni nuovo elemento aggiunto viene attivata la public view dell'adapter
        adapter.notifyDataSetChanged();
        // vista la presenza di foto viene disattivato il messaggio di errore
        resolveEmptyText();
        // e viene disattivato il messaggio di caricamento delle foto
        cancelProgressDialog();
    }

    @Override
    public void onLoaderReset(Loader<List<PhotoItem>> loader) {
        // ripulisce il contenuto dell'adapter
        mPhotoListItem.clear();
        adapter.notifyDataSetChanged();
        resolveEmptyText();
        cancelProgressDialog();
    }


    // gestisce il messaggio di caricamento delle immagini che compare ad apertura del fragment
    private void cancelProgressDialog(){
        if(mLoadingProgressDialog != null){
            if(mLoadingProgressDialog.isShowing()){
                mLoadingProgressDialog.cancel();
            }
        }
    }


    // una volta controllata la presenza di foto gestisce la comparsa o meno di un avviso che
    // comunica l'assenza di foto in memoria
    protected void resolveEmptyText(){
        // controlla se l'adapter ha recuperato delle foto
        if(adapter.isEmpty()){
            // se e' vuoto viene reso visibile e visualizza un messaggio di errore
            mEmptyTextView.setVisibility(View.VISIBLE);
            setEmptyText();
        } else {
            // se e' pieno viene reso invisibile e si passa alla visualizzazione delle foto trovate
            mEmptyTextView.setVisibility(View.INVISIBLE);
        }
    }


    // gestisce quello che viene scritto nel messaggio di errore (assenza o meno di foto)
    public void setEmptyText() {
        if(noPhotos) {
            mEmptyTextView.setText("No Photos!");
        } else {
            mEmptyTextView.setText("Loading...");
        }
    }

}
