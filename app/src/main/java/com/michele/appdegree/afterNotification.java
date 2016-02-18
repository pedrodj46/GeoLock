package com.michele.appdegree;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    Integer aperta = null;
    String messaggio = null;

    Bundle extras;
    String idFoto;
    String idNotifica;

    JSONObject obj;

    String urlPath = "http://esamiuniud.altervista.org/tesi/img/";

    public static final String MY_PREFS_NAME = "ricordamiLogin";


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

        final ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.custom_actionbar);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#03A9F4")));

        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(getResources().getColor(R.color.icons), PorterDuff.Mode.SRC_ATOP);
        actionBar.setHomeAsUpIndicator(upArrow);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(getResources().getColor(R.color.primary_dark));
        window.setNavigationBarColor(getResources().getColor(R.color.primary));

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        if (prefs.getString("username", "0") != "0" && prefs.getString("password", "0") != "0") {
            String username = new String(Base64.decode(prefs.getString("username", "0"), Base64.DEFAULT));
            String password = new String(Base64.decode(prefs.getString("password", "0"), Base64.DEFAULT));

            Log.d("username", username);
            Log.d("password", password);

            String idU;

            ServerConnection sendLogin = new ServerConnection();
            idU = sendLogin.sendLogin(username, password, this);

            Log.d("idutente", idU);

            if (idU.equals("0")) {
                Intent intent = new Intent(afterNotification.this, mainActivity.class);
                Bundle b = new Bundle();
                b.putString("idN", "0");
                b.putInt("case", 0);
                intent.putExtras(b);
                startActivity(intent);
                finish();
            }
            else{
                globals idUtente = (globals) getApplicationContext();
                idUtente.setId(idU);

                ServerConnection getInfoUser = new ServerConnection();
                getInfoUser.getInfoUser(idU, this);
            }
        }
        else{
            Intent intent = new Intent(afterNotification.this, mainActivity.class);
            Bundle b = new Bundle();
            b.putString("idN", "0");
            b.putInt("case", 0);
            intent.putExtras(b);
            startActivity(intent);
            finish();
        }

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
            aperta = obj.getInt("aperta");
            messaggio = obj.getString("messaggio");
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

        if(aperta==0){
            mTransfered.setText("No");
            LinearLayout msg = (LinearLayout) findViewById(R.id.innerFrameLayout08);
            msg.setVisibility(View.VISIBLE);
            TextView mMessaggio = (TextView) findViewById(R.id.messaggio);
            mMessaggio.setText(messaggio);
        }
        else if(aperta==1){
            mTransfered.setText("Si");
            Button btn1 = (Button) findViewById(R.id.btnReality);
            btn1.setVisibility(View.VISIBLE);
            btn1.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    btn1Clicked();
                }
            });
            Button btn2 = (Button) findViewById(R.id.btnContinua);
            btn2.setVisibility(View.VISIBLE);
            btn2.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    btn2Clicked();
                }
            });
            Button btn3 = (Button) findViewById(R.id.btnChiudi);
            btn3.setVisibility(View.VISIBLE);
            btn3.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    btn3Clicked();
                }
            });
        }

    }

    public void btn1Clicked() {

        Intent intent = new Intent(afterNotification.this, mainActivity.class);
        Bundle b = new Bundle();
        b.putString("idN", idNotifica);
        b.putInt("case", 1);
        intent.putExtras(b);
        startActivity(intent);
        finish();

    }

    public void btn2Clicked() {

        Intent intent = new Intent(afterNotification.this, mainActivity.class);
        Bundle b = new Bundle();
        b.putString("idN", idNotifica);
        b.putInt("case", 2);
        intent.putExtras(b);
        startActivity(intent);
        finish();

    }

    public void btn3Clicked() {

        Intent intent = new Intent(afterNotification.this, mainActivity.class);
        Bundle b = new Bundle();
        b.putString("idN", idNotifica);
        b.putInt("case", 3);
        intent.putExtras(b);
        startActivity(intent);
        finish();
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
