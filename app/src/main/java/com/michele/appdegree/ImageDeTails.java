package com.michele.appdegree;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.devadvance.circularseekbar.CircularSeekBar;
import com.devadvance.circularseekbar.CircularSeekBar.OnCircularSeekBarChangeListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.michele.fragmentexample.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ImageDeTails extends Fragment implements OnMapReadyCallback {

    // Fragment che gestisce i dati raccolti e l'interimento dei dati aggiuntivi da parte
    // dell'utente - chiede poi conferma del salvataggio di immagine e dati a fine processo

    static View mainView;

    // variabili seekbar circolare
    static CircularSeekBar seekbar;
    static float DirezSoggeDegree = 0.0f;
    static float Rotation = 0.0f;
    static String DirezSogg = "Nord";
    static String DirezTelef = null;
    static Boolean seekbarActive = true;

    // checkbox
    static CheckBox SoggInMov;

    // number picker
    static NumberPicker np;
    static int distanza = 0;

    private GoogleMap googleMap;
    static double mLatitude;
    static double mLongitude;

    private static TextView textview1;
    private static TextView textview2;
    private static TextView textview4;
    private static EditText edittext1;
    private static EditText edittext2;
    private static EditText edittext3;

    String mTextNew;

    Button button;

    static String Latitude;
    static String Longitude;

    // variabili bussola
    static float mDegree = 0f;
    // mostra l'immagine relativa alla bussola
    private ImageView mPointer;
    // textview per i gradi
    static TextView tvHeading;
    static TextView direzioneSogg;

    ProgressDialog progressDialog;


    // interfaccia di collegamento con la main activity
    ToolbarListener activityCallback;

    public interface ToolbarListener {
        public Boolean onSendName(String name, Float direzioneDispDegree, String direzioneDispCard,
                               Float direzioneSoggDegree, String direzioneSoggCard,
                               Integer distanza);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            activityCallback = (ToolbarListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ToolbarListener");
        }
    }
    // fine interfaccia di collegamento con la main activity


    public ImageDeTails() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_image_details, container, false);

        defaultLayout();

        return mainView;
    }

    public void defaultLayout() {
        textview1 = (TextView) mainView.findViewById(R.id.textView1);
        textview2 = (TextView) mainView.findViewById(R.id.textView2);
        textview4 = (TextView) mainView.findViewById(R.id.textView4);

        edittext1 = (EditText) mainView.findViewById(R.id.editText1);
        edittext2 = (EditText) mainView.findViewById(R.id.editText2);
        edittext3 = (EditText) mainView.findViewById(R.id.editText3);

        // accelerometro e bussola
        // imageview per visualizzare la bussola
        mPointer = (ImageView) mainView.findViewById(R.id.pointer);

        seekbar = (CircularSeekBar) mainView.findViewById(R.id.circularSeekBar1);
        seekbar.setMax(360);
        seekbar.setLockEnabled(false);
        seekbar.setOnSeekBarChangeListener(new CircleSeekBarListener());

        SoggInMov = (CheckBox) mainView.findViewById(R.id.checkMovement);

        // textview per i gradi
        tvHeading = (TextView) mainView.findViewById(R.id.tvHeading);
        tvHeading.setText(Float.toString(mDegree) + " gradi" + " " + DirezTelef);
        direzioneSogg = (TextView) mainView.findViewById(R.id.textView9);

        // number picker
        np = (NumberPicker) mainView.findViewById(R.id.numberPicker01);
        np.setMinValue(0);
        np.setMaxValue(1000);

        button = (Button) mainView.findViewById(R.id.accept_button);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        edittext1.setText(timeStamp);
        edittext1.setGravity(Gravity.CENTER_HORIZONTAL);

        edittext2.setText("La: " + Latitude);
        edittext3.setText("Lo: " + Longitude);

        FragmentTransaction transition =
                getActivity().getSupportFragmentManager().beginTransaction();
        Fragment ft = getActivity().getSupportFragmentManager().findFragmentById(R.id.map);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });

        SoggInMov.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCheckboxClicked(v);
            }
        });

        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                distanza = newVal;
            }
        });

        // rotazione bussola dal punto zero a quello desiderato
        RotateAnimation ra = new RotateAnimation(
                0, Rotation,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
        // durata dell'animazione della bussola (la bussola viene animata a inizio Fragment per
        // posizionarsi nella direzione prefissata
        ra.setDuration(710);
        ra.setFillAfter(true);
        // avvia l'animazione
        mPointer.startAnimation(ra);

    }

    public void buttonClicked (View view) {

        mTextNew = edittext1.getText().toString();

        new SendData().execute();
    }

    public class SendData extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Attendere...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {

            Boolean result=false;

            if (imageNameControl()) {
                // invio nome immagine e direzione oggetto
                result = activityCallback.onSendName(mTextNew, mDegree,
                        DirezTelef, DirezSoggeDegree, DirezSogg, distanza);
            } else {
                Toast.makeText(getActivity().getApplicationContext(),
                        "Nome immagine gia' in uso! Inserire un nome differente!", Toast.LENGTH_LONG)
                        .show();
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            progressDialog.dismiss();

            changeText(result);
        }
    }

    public Boolean imageNameControl() {
        Boolean nameOk = true;
        String mImageName = edittext1.getText().toString();
        String imageName = mImageName+".jpg";

        String path = Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_PICTURES).toString()+"/AppDegreeTrial/";

        File f = new File(path);
        if(f.isDirectory()) {
            // recupero dei file contenuti nella cartella
            File file[] = f.listFiles();
            for (int i = 0; i < file.length; i++) {
                if(imageName.equals(file[i].getName())) {
                    // se il nome dell'immagine e' gia' presente nella cartella restituisce false
                    nameOk = false;
                }
            }
        }

        // recupero il database
        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
        // recupero il cursore
        Cursor c = databaseHelper.getImageInfo();
        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            String databaseName = c.getString(1)+".jpg";

            if(imageName.equals(databaseName)) {
                // se il nome dell'immagine e' gia' presente nel database restituisce false
                nameOk = false;
            }
        }

        return nameOk;
    }

    // funzione del checkbox
    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();

        if(checked) {
            seekbarActive = false;
            direzioneSogg.setText("Soggetto fermo");
            DirezSogg = "Soggetto fermo";
            DirezSoggeDegree = 0.0f;
        } else {
            seekbarActive = true;
        }
    }

    public void changeText(Boolean result) {
        String testoDialogo = null;
        if(result) {
            testoDialogo = "FOTO SALVATA";
        } else{
            testoDialogo = "ERRORE: RISCATTARE LA FOTO!";
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        // i testi possono essere cambiati in values/styles.xml
        dialog.setMessage(testoDialogo);

        dialog.setNegativeButton(getActivity().getResources().getString
                (R.string.accetta), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                globals isFromNotifica = (globals) getActivity().getApplicationContext();
                if(isFromNotifica.getIsFromNotifica()!=null){
                    if(isFromNotifica.getIsFromNotifica()) {
                        isFromNotifica.setIsFromNotifica(false);
                        FragmentManager myFragment = getActivity().getSupportFragmentManager();
                        myFragment.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        buttonsFragment newFragment = new buttonsFragment();
                        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                        ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                                R.anim.slide_in_left, R.anim.slide_out_right);
                        ft.replace(R.id.fragment_container, newFragment, "recallButtons");
                        ft.addToBackStack("recallButtons");
                        ft.commit();
                    }
                }
                else {
                    // forza il back button fino al fragment del menu iniziale
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    int count = fm.getBackStackEntryCount();
                    while (count > 1) {
                        fm.popBackStackImmediate();
                        count--;
                    }
                }

            }
        });

        /*
        dialog.setPositiveButton(getActivity().getString(R.string.annulla), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                // forza il back button fino al fragment della camera
                FragmentManager fm = getActivity().getSupportFragmentManager();
                int count = fm.getBackStackEntryCount();
                while (count > 2) {
                    fm.popBackStackImmediate();
                    count--;
                }
            }
        });
        */

        dialog.show();
    }


    // recupera la posizione e la direzione cardinale del dispositivo al momento dello scatto
    public void GetPosition(android.location.Location mPosition, float degree) {
        mLatitude = 0;
        mLongitude = 0;
        mLatitude = mPosition.getLatitude();
        mLongitude = mPosition.getLongitude();
        Rotation = degree;
        mDegree = 360-degree;

        if((mDegree)>360) {
            mDegree = mDegree-360;
        }

        DirezTelef = setCardinalDirection(mDegree);
        Latitude = Location.convert(mPosition.getLatitude(), Location.FORMAT_SECONDS);
        if(mPosition.getLatitude()>0) {
            Latitude = Latitude+" N";
        } else {
            Latitude = Latitude+" S";
        }

        Longitude = Location.convert(mPosition.getLongitude(), Location.FORMAT_SECONDS);
        if(mPosition.getLongitude()>0) {
            Longitude = Longitude+" E";
        } else {
            Longitude = Longitude + " W";
        }
    }

    public String setCardinalDirection(float myDegree) {
        String compass = null;

        if(myDegree>=0 && myDegree<=23) {
            compass = "Nord";
        } else if(myDegree>23 && myDegree<=68) {
            compass = "Nord-Est";
        } else if(myDegree>68 && myDegree<=113) {
            compass = "Est";
        } else if(myDegree>113 && myDegree<=158) {
            compass = "Sud-Est";
        } else if(myDegree>158 && myDegree<=203) {
            compass = "Sud";
        } else if(myDegree>203 && myDegree<=248) {
            compass = "Sud-Ovest";
        } else if(myDegree>248 && myDegree<=293) {
            compass = "Ovest";
        } else if(myDegree>293 && myDegree<=338) {
            compass = "Nord-Ovest";
        } else {
            compass = "Nord";
        }

        return compass;
    }

    // sezione dedicata alla mappa google
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // recupero le mappe di google maps
        initilizeMap();
    }

    private void initilizeMap() {
        if (googleMap == null) {
            ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map))
                    .getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        // controlla se la creazione delle mappe sullo schermo è avvenuta con successo
        if (googleMap == null) {
            Toast.makeText(getActivity().getApplicationContext(),
                    "Errore! Impossibile creare mappa!", Toast.LENGTH_SHORT)
                    .show();
        }

        CameraUpdate center =
                CameraUpdateFactory.newLatLng(new LatLng(mLatitude,
                        mLongitude));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

        // setto la posizione iniziale sulla mappa mondiale e stabilisco il livello di zoom iniziale
        googleMap.moveCamera(center);
        googleMap.animateCamera(zoom);
        googleMap.getUiSettings().setAllGesturesEnabled(false);

        googleMap.addMarker(new MarkerOptions().position(new LatLng(mLatitude, mLongitude)));

    }

    public class CircleSeekBarListener implements OnCircularSeekBarChangeListener {
        // funzioni di default della circular seekbar
        @Override
        public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
            if(seekbarActive) {
                DirezSoggeDegree = progress;
                DirezSogg = setCardinalDirection(DirezSoggeDegree);

                direzioneSogg.setText(DirezSoggeDegree + " gradi " + DirezSogg);
            }
        }

        @Override
        public void onStopTrackingTouch(CircularSeekBar seekBar) {

        }

        @Override
        public void onStartTrackingTouch(CircularSeekBar seekBar) {

        }
    }


}
