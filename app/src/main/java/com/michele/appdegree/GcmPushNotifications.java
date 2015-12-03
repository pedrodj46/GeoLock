package com.michele.appdegree;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by mattia on 03/12/15.
 */
public class GcmPushNotifications extends mainActivity {

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    //Inserite il vostro numero di progetto ottenibile dalla console sviluppatori.
    String SENDER_ID = "294340718624";

    static final String TAG = "GeoLock";

    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    Context context;
    String regid;
    Activity Main;

    public void registrationDevice(Activity main) {

        Main = main;

        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(main);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }

    //funzione di verifica per la presenza dei play service, se non sono presenti verrà mostrato una
    // dialogbox con il link per l'installazione dei play service
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(Main);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, Main,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    //salvataggio dell'id ottenuto da gcm
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    // controllo sulla presenza dell registrationID, se è assente si avvia la procedura
    // di registrazione
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }

        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    //comunicazione col server GCM per la registrazione del terminale
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;
                    Log.d("registration", msg);
                    sendRegistrationIdToBackend();

                    //salvataggio dell'id
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                //avete ottenuto l'id, da qui potete proseguire avviando un'altra Activity
            }
        }.execute(null, null, null);
    }

    private static int getAppVersion(Context context) {
        /*
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo packageInfo = manager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {

            throw new RuntimeException("Could not get package name: " + e);
        }
        */
        //Integer versionCode = BuildConfig.VERSION_CODE;

        return 101;

        /*
        try {
            int version = 0;

            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionCode;

            return version;
        }
        catch (Exception e){
            Log.d("Errore", "errore: "+e);
        }

        return 0;
        */
    }

    private SharedPreferences getGcmPreferences(Context context) {
        return Main.getSharedPreferences(mainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    private void sendRegistrationIdToBackend() {

        try {
            HttpPost httppost;
            HttpResponse response;
            HttpClient httpclient;
            HttpEntity entity;
            String id;

            httpclient = new DefaultHttpClient();
            httppost = new HttpPost("http://esamiuniud.altervista.org/tesi/sendRegid.php");

            Log.d("regid", regid.toString());

            //JSONObject jsonObject = new JSONObject();
            //jsonObject.accumulate("regid", regid.toString());

            //List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            //nameValuePairs.add(new BasicNameValuePair("jsonRegid", jsonObject.toString()));

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("regid", regid));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            response = httpclient.execute(httppost);
            entity = response.getEntity();
            id = EntityUtils.toString(entity, HTTP.UTF_8);

            if(id.equals("1")){
                Log.d("regid", "inserito");
            }
            else{
                Log.d("regid", "non inserito");
            }

        } catch (Exception e) {
            Log.d("errore", "errore " + e);
        }
    }
}



                /*




                String regid=params[0];
                HttpClient client=new DefaultHttpClient();
                HttpPost request=new HttpPost("http://esamiuniud.altervista.org/tesi/sendRegid.php");
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("regid", regid));
                try {
                    request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response=client.execute(request);
                    int status=response.getStatusLine().getStatusCode();
                } catch (UnsupportedEncodingException e) {
                    return null;
                } catch (ClientProtocolException e) {
                    return null;
                } catch (IOException e) {
                    return null;
                }

                return null;
            }
        }.execute(regid);
    }

}
*/