package com.michele.appdegree;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.jaredrummler.android.device.DeviceName;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ServerConnection {

    // fragment per la gestione dell'invio delle informazioni contenute nel database al server
    // dedicato, per un loro futuro riutilizzo

    Activity Main;

    HttpPost httppost;
    StringBuffer buffer;
    HttpResponse response;
    HttpEntity entity;
    HttpClient httpclient;
    String id;

    com.michele.appdegree.adapters.items.Person person;
    String ImageName;
    String Latitude;
    String Longitude;
    String Address;
    String DirezioneDispCard;
    Float DirezioneDispDegree;
    String DirezioneSoggCard;
    Float DirezioneSoggDegree;
    Integer Distanza;
    Boolean Send;
    Long idFoto;
    String idUt;
    String idNo;

    public boolean sendData(Long midFoto, String mImageName, String mLatitude, String mLongitude, String mAddress,
                         String mDirezioneDispCard, Float mDirezioneDispDegree,
                         String mDirezioneSoggCard, Float mDirezioneSoggDegree, Integer mDistanza,
                         Activity main) {

    //public void sendData(Activity main) {


        Main = main;
        idFoto=midFoto;
        ImageName = mImageName;
        Latitude = mLatitude;
        Longitude = mLongitude;
        Address = mAddress;
        DirezioneDispCard = mDirezioneDispCard;
        DirezioneDispDegree = mDirezioneDispDegree;
        DirezioneSoggCard = mDirezioneSoggCard;
        DirezioneSoggDegree = mDirezioneSoggDegree;
        Distanza = mDistanza;
        Boolean Ok=false;

        globals idUtente = (globals) main.getApplicationContext();
        idUt = idUtente.getId();

        globals idNotifica = (globals) main.getApplicationContext();
        idNo= idNotifica.getIdNotifica();

        if(isConnected()) {
            //Toast.makeText(Main, "Connesso con il server!", Toast.LENGTH_SHORT).show();

            // indirizzo a cui inviare i dati
            //new HttpAsyncTask().execute("http://esamiuniud.altervista.org/tesi/ajax.php");

            if (invia()) {
                //Toast.makeText(Main, "Data Sent", Toast.LENGTH_LONG).show();
                if(uploadImage()){
                    Ok = true;
                }
            } else {
                Toast.makeText(Main, "Data Not Sent", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(Main, "Connessione fallita!", Toast.LENGTH_SHORT).show();

        }

        return Ok;

    }

    public boolean uploadImage(){

        boolean upload=false;
        try{
            httpclient = new DefaultHttpClient();
            httppost = new HttpPost("http://esamiuniud.altervista.org/tesi/sendImage.php");

            File image = new File(Environment.getExternalStoragePublicDirectory
                    (Environment.DIRECTORY_PICTURES).toString()+"/AppDegreeTrial/"+ImageName+".jpg");
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);

            Log.d("immagine: ", "decode: " + bitmap);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);

            ArrayList<NameValuePair> imageToSend = new ArrayList<>();
            imageToSend.add(new BasicNameValuePair("immagine", encodedImage));
            imageToSend.add(new BasicNameValuePair("nomeImmagine", ImageName));

            httppost.setEntity(new UrlEncodedFormEntity(imageToSend));
            HttpResponse response1 = httpclient.execute(httppost);

            HttpEntity entity1 = response1.getEntity();
            String id1 = EntityUtils.toString(entity1, HTTP.UTF_8);

            if(id1.equals("1")){
                Toast.makeText(Main, "upload", Toast.LENGTH_LONG).show();
                upload=true;
            }
            else{
                Toast.makeText(Main, "NO upload", Toast.LENGTH_LONG).show();
                upload=false;
            }

        }
        catch (Exception e){
            Log.d("errore upload", "errore: "+e);
        }

        return upload;
    }

    public boolean invia(){
        try{
            httpclient = new DefaultHttpClient();
            httppost = new HttpPost("http://esamiuniud.altervista.org/tesi/sendData.php");
            //nameValuePairs = new ArrayList<NameValuePair>(2);

            //nameValuePairs.add(new BasicNameValuePair("username", "pedrodj46"));
            //nameValuePairs.add(new BasicNameValuePair("password", "prova"));

            String json = "";

            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("idUtente", idUt);
            jsonObject.accumulate("idNotifica", idNo);
            jsonObject.accumulate("name", ImageName);
            jsonObject.accumulate("latitude", Latitude);
            jsonObject.accumulate("longitude", Longitude);
            jsonObject.accumulate("address", Address);
            jsonObject.accumulate("direzioneUtente", DirezioneDispCard);
            jsonObject.accumulate("gradiUtente", DirezioneDispDegree.toString());
            jsonObject.accumulate("direzioneSoggetto", DirezioneSoggCard);
            jsonObject.accumulate("gradiSoggetto", DirezioneSoggDegree.toString());
            jsonObject.accumulate("distanza", Distanza.toString());

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("jsonImage", jsonObject.toString()));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            response = httpclient.execute(httppost);
            entity = response.getEntity();
            id = EntityUtils.toString(entity, HTTP.UTF_8);

            if(id.equals("1")){
                //Toast.makeText(Main,"Collegato: "+id,Toast.LENGTH_SHORT).show();

                Send=true;
            }
            else{
                //Toast.makeText(Main,"Errore: "+id,Toast.LENGTH_LONG).show();

                Send=false;
            }
        }
        catch (Exception e){
            Log.d("errore", "errore "+e);
        }

        return Send;
    }

    public String sendLogin(String username, String password, Activity main){

        try{

            httpclient = new DefaultHttpClient();
            httppost = new HttpPost("http://esamiuniud.altervista.org/tesi/sendLogin.php");

            String json = "";

            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("username", username.toString());
            jsonObject.accumulate("password", password.toString());
            jsonObject.accumulate("device", DeviceName.getDeviceName());
            jsonObject.accumulate("androidid", Settings.Secure.getString(main.getContentResolver(), Settings.Secure.ANDROID_ID));

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("jsonLogin", jsonObject.toString()));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            response = httpclient.execute(httppost);
            entity = response.getEntity();
            id = EntityUtils.toString(entity, HTTP.UTF_8);

        }
        catch (Exception e){
            Log.d("errore", "errore "+e);
        }

        return id;
    }

    public String getPhotoDetails(String idFoto){

        try{
            httpclient = new DefaultHttpClient();
            httppost = new HttpPost("http://esamiuniud.altervista.org/tesi/getPhotoDetails.php");

            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("idF", idFoto.toString());

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("jsonIdPhoto", jsonObject.toString()));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            response = httpclient.execute(httppost);
            entity = response.getEntity();
            id = EntityUtils.toString(entity, HTTP.UTF_8);

        }
        catch (Exception e){
            Log.d("errore", "errore "+e);
        }

        return id;
    }

    public void updateLocation(double lat, double lon, float acc, float speed, Activity main){
        try{

            httpclient = new DefaultHttpClient();
            httppost = new HttpPost("http://esamiuniud.altervista.org/tesi/sendPosition.php");

            String json = "";

            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("lat", lat);
            jsonObject.accumulate("lon", lon);
            jsonObject.accumulate("acc", acc);
            jsonObject.accumulate("speed", speed);
            jsonObject.accumulate("androidid", Settings.Secure.getString(main.getContentResolver(), Settings.Secure.ANDROID_ID));

            globals idUtente = (globals) main.getApplicationContext();
            String idU = idUtente.getId();

            jsonObject.accumulate("id_utente", idU);

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("jsonPosition", jsonObject.toString()));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            response = httpclient.execute(httppost);
            //entity = response.getEntity();
            //id = EntityUtils.toString(entity, HTTP.UTF_8);

        }
        catch (Exception e){
            Log.d("errore", "errore "+e);
        }
    }

    public void updateNotification(String idN){
        try{

            httpclient = new DefaultHttpClient();
            httppost = new HttpPost("http://esamiuniud.altervista.org/tesi/updateNotification.php");

            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("idN", idN);
            jsonObject.accumulate("letta", 1);

            Log.d("id notifica", idN);

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("jsonNotification", jsonObject.toString()));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            response = httpclient.execute(httppost);
            entity = response.getEntity();
            id = EntityUtils.toString(entity, HTTP.UTF_8);

            Log.d("errore",id);

        }
        catch (Exception e){
            Log.d("errore", "errore "+e);
        }
    }

    public String closeNotification(String idN, String messaggio){
        try{

            httpclient = new DefaultHttpClient();
            httppost = new HttpPost("http://esamiuniud.altervista.org/tesi/closeNotification.php");

            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("idN", idN);
            jsonObject.accumulate("messaggio", messaggio);

            Log.d("id notifica",idN);

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("jsonClose", jsonObject.toString()));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            response = httpclient.execute(httppost);
            entity = response.getEntity();
            id = EntityUtils.toString(entity, HTTP.UTF_8);

            Log.d("errore",id);

        }
        catch (Exception e){
            Log.d("errore", "errore "+e);
        }

        return id;
    }

    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager)
                Main.getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }
}