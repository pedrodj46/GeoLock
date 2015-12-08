package com.michele.appdegree;

import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.michele.fragmentexample.R;
import com.squareup.picasso.Picasso;

/**
 * Created by mattia on 06/12/15.
 */
public class afterNotification extends FragmentActivity {

    ImageView image;
    String nameImage;
    String latitude = null;
    String longitude = null;
    String directionDegree = null;
    String direction = null;
    String address = null;
    String mlooking = null;
    String mdegree = null;
    String distance = null;
    Integer transfered = null;

    Bundle extras;
    String idFoto;

    String urlPath = "http://esamiuniud.altervista.org/tesi/img/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        extras = getIntent().getExtras();
        idFoto = extras.getString("id_foto");

        setContentView(R.layout.after_notification);
        Log.d("prova", idFoto);

        findDetails();

        //String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()+"/AppDegreeTrial/";

        image = (ImageView)findViewById(R.id.image_selected);
        //image.setImageURI(Uri.fromFile(new File(path + nameImage)));

        Log.d("url", urlPath+nameImage);

        Picasso.with(this).load(urlPath+nameImage).into(image);

        TextView mNameImage = (TextView)findViewById(R.id.ImageName);
        TextView mLatitude = (TextView)findViewById(R.id.latitude);
        TextView mLongitude = (TextView)findViewById(R.id.longitude);
        TextView mAddress = (TextView)findViewById(R.id.address);
        TextView mDirection = (TextView)findViewById(R.id.direction);
        TextView looking = (TextView)findViewById(R.id.looking);
        TextView mDistance = (TextView)findViewById(R.id.distance);
        TextView mTransfered = (TextView)findViewById(R.id.transfered);

        mNameImage.setText(nameImage);

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

    }

    // void che interagisce con il database
    public void findDetails() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        // il cursore fornito dal class del database mi permette di selezionare gli elementi
        Cursor c = databaseHelper.getImageInfo();

        try {
            // loop tra tutti gli elementi del database
            for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                String idF=c.getString(0);
                if(idF.equals(idFoto)) {
                    // selezionata la riga giusta recupero latitudine e longitudine
                    nameImage = c.getString(1)+".jpg";
                    latitude = c.getString(4);
                    longitude = c.getString(5);
                    address = c.getString(6);
                    mlooking = c.getString(7);
                    mdegree = c.getString(8);
                    direction = c.getString(9);
                    directionDegree = c.getString(10);
                    distance = c.getString(11);
                    transfered = c.getInt(12);

                    Log.d("nomeImmagine", nameImage);
                }

            }
        }
        finally {
            // infine chiudo il database (che l'estrazione sia riuscita o meno
            c.close();
        }
    }


}
