package com.michele.appdegree.adapters;

// Created by Michele Zardetto

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.michele.appdegree.DatabaseHelper;
import com.michele.appdegree.adapters.items.PhotoItem;
import com.michele.fragmentexample.R;

import java.util.List;


public class CustomListAdapter extends ArrayAdapter<PhotoItem> {

    // questa classe gestice la personalizzazione dell'elenco della listview della galleria immagini

    private final Activity context;
    private final Integer[] imgid;

    String latitude = null;
    String longitude = null;
    static int nElemento = 0;

    public CustomListAdapter(Activity context, List<PhotoItem> items, Integer[] imgid) {
        super(context, R.layout.mylist, items);

        this.context = context;
        this.imgid = imgid;
    }

    // holder per imageview e textview - scelta dovuta alla sua maggior velocita' di esecuzione
    private class ViewHolder {
        ImageView photoImageView;
        TextView txtTitle;
        TextView coord;
    }

    public View getView(int position, View view, ViewGroup parent) {
        nElemento++;

        // a ogni nuovo elemento notificato dalla main gallery viene attivato questo public view
        ViewHolder holder = null;
        PhotoItem photoItem = getItem(position);
        View viewToUse = null;

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (view == null) {
            holder = new ViewHolder();
            viewToUse = mInflater.inflate(R.layout.mylist, null);
            holder.photoImageView = (ImageView) viewToUse.findViewById(R.id.icon);

            holder.txtTitle = (TextView) viewToUse.findViewById(R.id.item);
            holder.coord = (TextView) viewToUse.findViewById(R.id.testo1);
            viewToUse.setTag(holder);
        } else {
            viewToUse = view;
            holder = (ViewHolder) viewToUse.getTag();
        }

        // gestione dei colori degli elementi della galleria immagini
        LinearLayout LL = (LinearLayout) viewToUse.findViewById(R.id.sfondo_lista);
        if(isOdd(nElemento)) {
            LL.setBackgroundColor(Color.parseColor("#5c665c"));
        } else {
            LL.setBackgroundColor(Color.parseColor("#6e8a6e"));
        }

        // recupero il percorso URI del file immagine
        String imageName = photoItem.getFullImageUri().toString();
        // elimino il percorso del file e tengo solo il nome
        imageName = imageName.substring(imageName.lastIndexOf("/")+1);


        String imageUri = Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_PICTURES).toString()+"/AppDegreeTrial/"+imageName;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(imageUri, options);

        int THUMBSIZE = 100;
        Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(bitmap, THUMBSIZE, THUMBSIZE);
        if(ThumbImage != null) {
            // se l'anteprima viene creata con successo viene inserita nell'imageview
            holder.photoImageView.setImageBitmap(ThumbImage);
        }

        // inserisco i testi nei contenitori della view
        holder.txtTitle.setText(imageName);

        coordImage(imageName);

        holder.coord.setText(latitude+" "+longitude);

        return viewToUse;
    }

    boolean isOdd( int val ) { return (val & 0x01) != 0; }

    // funzione per il recupero delle coordinate dal database
    public void coordImage (String actualFile) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        Cursor c = databaseHelper.getImageInfo();

        try {
            for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                String imageName = c.getString(1)+".jpg";
                if(imageName.equals(actualFile)) {
                    latitude = c.getString(4);
                    longitude = c.getString(5);
                }

            }
        }
        finally {
            c.close();
        }

        // gestione del  format con cui vengono visualizzate le coordinate
        if(latitude != null && longitude != null) {
            Double Dlatitude = Double.parseDouble(latitude);
            Double Dlongitude = Double.parseDouble(longitude);
            latitude = Location.convert(Dlatitude, Location.FORMAT_SECONDS);
            if (Dlatitude > 0) {
                latitude = latitude + " N";
            } else {
                latitude = latitude + " S";
            }
            longitude = Location.convert(Dlongitude, Location.FORMAT_SECONDS);
            if (Dlongitude > 0) {
                longitude = longitude + " E";
            } else {
                longitude = longitude + " W";
            }
        } else {
            // se per qualche motivo le coordinate sono assenti, vengono sotituite con un messaggio
            // di errore
            latitude = "Error: not Found";
            longitude = "Error: not Found";
        }
    }

}
