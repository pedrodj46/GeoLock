package com.michele.appdegree;

import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.michele.fragmentexample.R;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

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
    String idNotifica;

    JSONObject obj;

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
        idNotifica = extras.getString("id_notifica");

        setContentView(R.layout.after_notification);

        String json=photoDetails();

        Log.d("json:", json);

        try{
            obj = new JSONObject(json);

            nameImage = obj.getString("nome")+".jpg";
            latitude = obj.getString("lat");
            longitude = obj.getString("lon");
            address = obj.getString("indirizzo");
            mlooking = obj.getString("dirUtente");
            mdegree = obj.getString("angUtente");
            direction = obj.getString("dirSogg");
            directionDegree = obj.getString("angSogg");
            distance = obj.getString("distanza");
            transfered = 1;
        }
        catch (Exception e){
            Log.d("errore",e.toString());
        }

        //String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()+"/AppDegreeTrial/";

        image = (ImageView)findViewById(R.id.image_selected);
        //image.setImageURI(Uri.fromFile(new File(path + nameImage)));

        Log.d("url", urlPath+nameImage);

        Picasso.with(this).load(urlPath+nameImage).placeholder(R.drawable.picasso_loading).into(image);

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

    // void che interagisce con il server
    public String photoDetails() {

        ServerConnection updateNotification = new ServerConnection();
        updateNotification.updateNotification(idNotifica);

        ServerConnection getPhotoDetails = new ServerConnection();
        String json=getPhotoDetails.getPhotoDetails(idFoto);

        return json;
    }
}
