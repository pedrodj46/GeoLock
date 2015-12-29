package com.michele.appdegree;

import android.app.ProgressDialog;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.michele.fragmentexample.R;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by mattia on 29/12/15.
 */
public class notificationDetails extends Fragment {

    String url = "http://esamiuniud.altervista.org/tesi/getNotifications.php?idN=";
    ProgressDialog progressDialog;
    String idN = "";

    ImageView image;
    String mImagePath;
    String latitude = null;
    String longitude = null;
    String directionDegree = null;
    String direction = null;
    String address = null;
    String mlooking = null;
    String mdegree = null;
    String distance = null;
    Integer transfered = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View baseView = inflater.inflate(R.layout.after_notification, container, false);

        image = (ImageView) baseView.findViewById(R.id.image_selected);

        TextView nameImage = (TextView) baseView.findViewById(R.id.ImageName);
        TextView mLatitude = (TextView) baseView.findViewById(R.id.latitude);
        TextView mLongitude = (TextView) baseView.findViewById(R.id.longitude);
        TextView mAddress = (TextView) baseView.findViewById(R.id.address);
        TextView mDirection = (TextView) baseView.findViewById(R.id.direction);
        TextView looking = (TextView) baseView.findViewById(R.id.looking);
        TextView mDistance = (TextView) baseView.findViewById(R.id.distance);
        TextView mTransfered = (TextView) baseView.findViewById(R.id.transfered);

        String result = GetData();

        if(result.equals("1")) {
            // get the url from the data you passed to the `Map`
            String url = "http://esamiuniud.altervista.org/tesi/img/" + mImagePath + ".jpg";
            // do Picasso
            Picasso.with(getActivity()).load(url).into(image);

            nameImage.setText(mImagePath);

            // setto il testo del textview
            mAddress.setText(address);

            if (direction.equals("Soggetto fermo")) {
                mDirection.setText(direction);
            } else {
                mDirection.setText(directionDegree + " gradi " + direction);
            }

            String metro = null;
            String uno = "1";
            if (distance.equals(uno)) {
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

            // setto il testo
            mLatitude.setText(latitude);
            mLongitude.setText(longitude);

            looking.setText(mdegree + " gradi " + mlooking);

            if (transfered == 0) {
                mTransfered.setText("No");
            } else if (transfered == 1) {
                mTransfered.setText("Si");
            }
        }

        return baseView;
    }

    public void notificationID(String idNotification){
        idN = idNotification;
    }

    public String GetData(){

        //initialize
        InputStream is = null;
        String result = "";
        JSONArray jArray = null;
        //http post
        try{
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url+idN);
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        }
        catch(Exception e){
            Log.e("log_tag", "Error in http connection " + e.toString());
        }
        //convert response to string
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            result=sb.toString();
        }
        catch(Exception e){
            Log.e("log_tag", "Error converting result "+e.toString());
        }
        //try parse the string to a JSON object
        try{
            jArray = new JSONArray(result);
            Log.d("json arrivo",jArray.toString());
        }
        catch(Exception e){
            Log.e("log_tag", "Error parsing data "+e.toString());
        }

        try{
            JSONObject c = jArray.getJSONObject(0);
            Log.d("json", c.toString());
            mImagePath = c.getString("nome");
            latitude = c.getString("lat");
            longitude = c.getString("lon");
            address = c.getString("indirizzo");
            mlooking = c.getString("dirUtente");
            mdegree = c.getString("angUtente");
            direction = c.getString("dirSogg");
            directionDegree = c.getString("angSogg");
            distance = c.getString("distanza");
            transfered = 1;
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return "1";
    }

}
