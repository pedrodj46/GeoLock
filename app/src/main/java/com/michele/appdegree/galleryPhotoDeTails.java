package com.michele.appdegree;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.michele.fragmentexample.R;

import java.io.File;


public class galleryPhotoDeTails extends Fragment {

    // Fragment che gestisce la visualizzazione della immagini presenti nella galleria e le
    // relative informazioni

    ImageView image;
    static String mImagePath;
    String latitude = null;
    String longitude = null;
    String directionDegree = null;
    String direction = null;
    String address = null;
    String mlooking = null;
    String mdegree = null;
    String distance = null;
    Integer transfered = null;

    public galleryPhotoDeTails() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View baseView = inflater.inflate(R.layout.gallery_photo_details, container, false);

        image = (ImageView) baseView.findViewById(R.id.image_selected);
        image.setImageURI(Uri.fromFile(new File(mImagePath)));

        TextView nameImage = (TextView) baseView.findViewById(R.id.ImageName);
        TextView mLatitude = (TextView) baseView.findViewById(R.id.latitude);
        TextView mLongitude = (TextView) baseView.findViewById(R.id.longitude);
        TextView mAddress = (TextView) baseView.findViewById(R.id.address);
        TextView mDirection = (TextView) baseView.findViewById(R.id.direction);
        TextView looking = (TextView) baseView.findViewById(R.id.looking);
        TextView mDistance = (TextView) baseView.findViewById(R.id.distance);
        TextView mTransfered = (TextView) baseView.findViewById(R.id.transfered);

        // elimino il percorso del file e tengo solo il nome
        String actualImageName = (mImagePath.substring(mImagePath.lastIndexOf("/")+1));
        nameImage.setText(actualImageName);

        // recupero alcuni dettagli dal database
        findDetails(getActivity());

        // setto il testo del textview
        mAddress.setText(address);

        if(direction.equals("Soggetto fermo")) {
            mDirection.setText(direction);
        } else {
            mDirection.setText(directionDegree + " gradi " + direction);
        }

        String metro = null;
        String uno = "1";
        if(distance.equals(uno)) {
            metro = "metro";
        } else {
            metro = "metri";
        }
        mDistance.setText(distance + " " + metro);

        // trasformo le coordinate geografiche per renderle graficamente piu' gradevoli
        Double Dlatitude = Double.parseDouble(latitude);
        Double Dlongitude = Double.parseDouble(longitude);
        latitude = Location.convert(Dlatitude, Location.FORMAT_SECONDS);
        // poi aggiungo la direzione cardinale
        if(Dlatitude>0) {
            latitude = latitude+" N";
        } else {
            latitude = latitude+" S";
        }
        longitude = Location.convert(Dlongitude, Location.FORMAT_SECONDS);
        if(Dlongitude>0) {
            longitude = longitude+" E";
        } else {
            longitude = longitude+" W";
        }

        // setto il testo
        mLatitude.setText(latitude);
        mLongitude.setText(longitude);

        looking.setText(mdegree + " gradi " + mlooking);

        if(transfered==0){
            mTransfered.setText("No");
        }
        else if(transfered==1){
            mTransfered.setText("Si");
        }

        return baseView;
    }

    public void displayImage(String imagePath) {
        mImagePath = imagePath;

    }

    // void che interagisce con il database
    public void findDetails(Context context) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        // il cursore fornito dal class del database mi permette di selezionare gli elementi
        Cursor c = databaseHelper.getImageInfo();
        String mImageName = (new File(mImagePath)).getName();

        try {
            // loop tra tutti gli elementi del database
            for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                String imageName = c.getString(1)+".jpg";
                if(imageName.equals(mImageName)) {
                    // selezionata la riga giusta recupero latitudine e longitudine
                    latitude = c.getString(4);
                    longitude = c.getString(5);
                    address = c.getString(6);
                    mlooking = c.getString(7);
                    mdegree = c.getString(8);
                    direction = c.getString(9);
                    directionDegree = c.getString(10);
                    distance = c.getString(11);
                    transfered = c.getInt(12);
                }

            }
        }
        finally {
            // infine chiudo il database (che l'estrazione sia riuscita o meno
            c.close();
        }
    }

}
