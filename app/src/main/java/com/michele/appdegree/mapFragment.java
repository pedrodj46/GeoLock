package com.michele.appdegree;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.michele.fragmentexample.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


public class mapFragment extends Fragment implements OnMapReadyCallback {

    // Fragment che gestisce la visualizzazione delle mappe di google e i relativi marker delle
    // varie posizioni

    Float EarthR = 6378.1f; // Raggio della Terra

    private GoogleMap googleMap;
    private static View view;

    // incognite necessarie a generare una custom infoview
    private HashMap<Marker, MyMarker> mMarkersHashMap;
    private ArrayList<MyMarker> mMyMarkersArray = new ArrayList<MyMarker>();

    // incognita GeoTag per la posizione
    MyLocation myLocation = new MyLocation();
    // contenitore della posizione trovata
    static android.location.Location mPosition;
    static Boolean positionChanged;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // segue una procedura necessaria al fragment per essere ricaricato:
        // una volta visualizzati i dettagli di una foto, premendo il back button e' possibile
        // ricaricare la mappa - questa procedura controlla la presenza o meno di una mappa
        // evitando così che venga ricaricata ogni volta utilizzando quella caricata in precedenza
        if(view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
        try {
            view = inflater.inflate(R.layout.fragment_map, container, false);
            // ricerca della posizione geografica del dispositivo
            myLocation.getLocation(getActivity().getApplicationContext(), locationResult);

            positionChanged = false;
        } catch (InflateException e) {
            // mappa gia presente
        }

        return view;
    }


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


    // void inizializzato a seguito della ricerca delle mappe (che l'esito sia positivo o meno)
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        // controlla se la mappa e' stata generata con successo
        if (googleMap == null) {
            Toast.makeText(getActivity().getApplicationContext(),
                    "Errore! Impossibile recuperare le mappe!", Toast.LENGTH_SHORT)
                    .show();
        }

        CameraUpdate center =
                CameraUpdateFactory.newLatLngZoom(new LatLng(45.96263980000001,
                        12.655136200000015), 15f);

        // setto la posizione iniziale sulla mappa mondiale e stabilisco il livello di zoom iniziale
        googleMap.animateCamera(center);

        changePosition();

        // recupero le informazioni della varie immagini
        findDetails(getActivity());

        // genero i markers in base alle informazioni raccolte
        plotMarkers(mMyMarkersArray);

        // click listener sulla info window dei marker
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                // cliccata la info window controlla l'immagine a cui e' legato
                for (int i = 0; i < mMyMarkersArray.size(); i++) {
                    if (marker.getTitle().equals(mMyMarkersArray.get(i).getmIcon())) {
                        // recuperate le informazioni riguardanti l'immagine a cui e' legato
                        // carica i dettagli rigurdanti la foto direttamente dalla gallery
                        galleryPhotoDeTails newFragment = new galleryPhotoDeTails();
                        // invio il percorso dell'immagine da visualizzare nel nuovo fragment
                        newFragment.displayImage(marker.getTitle());

                        FragmentTransaction transaction =
                                getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, newFragment,
                                "galleryImageDeTails2");
                        transaction.addToBackStack("galleryImageDeTails2");
                        transaction.commit();
                    }
                }
            }
        });
    }


    // void che serve a recuperare le varie immagini e le loro informazioni
    public void findDetails(Context context) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        // il cursore fornito dal class del database mi permette di selezionare gli elementi
        Cursor c = databaseHelper.getImageInfo();

        try {
            String path = Environment.getExternalStoragePublicDirectory
                    (Environment.DIRECTORY_PICTURES).toString()+"/AppDegreeTrial/";

            File f = new File(path);
            if(f.isDirectory()) {
                File file[] = f.listFiles();

                for (int i = 0; i < file.length; i++) {
                    String nomeImm = null;
                    Double latitude = null;
                    Double longitude = null;
                    Float angleDirectObserver = 0.0f;
                    Integer distanceObject = 0;
                    Boolean imHere = false;
                    // loop tra tutti gli elementi del database
                    for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                        String imageName = c.getString(1)+".jpg";

                        if(imageName.equals(file[i].getName())) {
                            // se l'immagine e' registrata allora viene attivato questo booleano
                            imHere = true;
                            nomeImm = c.getString(1);
                            latitude = c.getDouble(4);
                            longitude = c.getDouble(5);
                            angleDirectObserver = Float.parseFloat(c.getString(8));
                            distanceObject = Integer.parseInt(c.getString(11));
                        }
                    }
                    if(imHere==true) {

                        Double latitude2 = findLatitude2(latitude, angleDirectObserver,
                                distanceObject);
                        Double longitude2 = findLongitude2(latitude, longitude, angleDirectObserver,
                                distanceObject);

                        // salvo il marker appena creato dentro il mio array
                        mMarkersHashMap = new HashMap<Marker, MyMarker>();
                        mMyMarkersArray.add(new MyMarker(nomeImm, path+nomeImm+".jpg", latitude,
                                longitude, angleDirectObserver, distanceObject, latitude2,
                                longitude2));

                    }
                }
            }
        }
        finally {
            // infine chiudo il database (che l'estrazione sia riuscita o meno
            c.close();
        }

    }

    // aggiunge sulla mappa i custom markers
    private void plotMarkers(ArrayList<MyMarker> markers) {
        if(markers.size() > 0) {

            // genera le linee di collegamento tra i vari markers della stessa immagine
            for (MyMarker myMarker : markers) {
                Polyline line = googleMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(myMarker.getmLatitude(), myMarker.getmLongitude()),
                                new LatLng(myMarker.getmLatitude2(), myMarker.getmLongitude2()))
                        .width(2)
                        .color(Color.RED));
            }

            // genera i marker relativi alla posizione dell'oggetto fotografato
            for (MyMarker myMarker : markers) {
                MarkerOptions markerOption2 = new MarkerOptions()
                        .position(new LatLng(myMarker.getmLatitude2(), myMarker.getmLongitude2()));
                markerOption2.title(myMarker.getmIcon());
                markerOption2.icon(BitmapDescriptorFactory.fromResource(R.drawable.red_circle));
                markerOption2.anchor(0.5f, 0.5f);

                // inserisco ogni marker sulla mappa
                Marker currentMarker2 = googleMap.addMarker(markerOption2);
                mMarkersHashMap.put(currentMarker2, myMarker);
            }

            // genera i markers relativi alla posizione del dispositivo al momento dello scatto
            for (MyMarker myMarker : markers) {

                // genera le opzioni di personalizzazione del custom marker
                MarkerOptions markerOption = new MarkerOptions()
                        .position(new LatLng(myMarker.getmLatitude(), myMarker.getmLongitude()));
                markerOption.title(myMarker.getmIcon());
                markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_icon2));
                markerOption.anchor(0.5f, 0.95f);

                // inserisco ogni marker sulla mappa
                Marker currentMarker = googleMap.addMarker(markerOption);
                mMarkersHashMap.put(currentMarker, myMarker);

                // e imposto come infoview la custom view creata
                googleMap.setInfoWindowAdapter(new MarkerInfoWindowAdapter(getActivity()));
            }
        }
    }

    // trova la longitudine della posizione dell'oggetto fotografato
    private Double findLongitude2(Double latitude1, Double longitude1, Float degree,
                                    Integer Distance) {
        Double brng = Math.toRadians(degree);  // radianti della direzione cardinale in gradi
        String dis = String.valueOf(Distance);
        Float d = (Float.parseFloat(dis))/1000; // Distanza osservatore/oggetto in metri

        Double lat1 = Math.toRadians(latitude1);  // Latitudine convertita in radianti
        Double lon1 = Math.toRadians(longitude1); // Longitudine convertita in radianti

        Double lat2 = Math.asin(Math.sin(lat1) * Math.cos(d / EarthR) +
                Math.cos(lat1) * Math.sin(d / EarthR) * Math.cos(brng));

        Double lon2 = lon1 + Math.atan2(Math.sin(brng)*Math.sin(d/EarthR)*Math.cos(lat1),
                Math.cos(d/EarthR)-Math.sin(lat1)*Math.sin(lat2));

        lon2 = Math.toDegrees(lon2);

        return lon2;
    }

    // trova la latitudine della posizione dell'oggetto fotografato
    public Double findLatitude2(Double latitude1, Float degree,
                                Integer Distance) {

        Double brng = Math.toRadians(degree);  // radianti della direzione cardinale in gradi
        String dis = String.valueOf(Distance);
        Float d = (Float.parseFloat(dis))/1000; // Distanza osservatore/oggetto in metri

        Double lat1 = Math.toRadians(latitude1);  // Latitudine convertita in radianti

        Double lat2 = Math.asin(Math.sin(lat1) * Math.cos(d / EarthR) +
                Math.cos(lat1) * Math.sin(d / EarthR) * Math.cos(brng));

        lat2 = Math.toDegrees(lat2);

        return lat2;
    }

    // void necessario a richiamare la classe MyLocation e recuperare Longitudine e Latitudine
    private MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
        @Override public void gotLocation(Location location) {
            // qui viene stabilito cosa fare con le info recuperate
            if(location!=null) {
                mPosition = null;
                mPosition = location;

                // positionChanged evita che la posizione sia aggiornata ogni tot secondi
                // impedendo di navigare sulla mappa
                if(positionChanged == false) {
                    changePosition();
                    positionChanged = true;
                    // visto che gli aggiornamenti di posizione sono negati il servizio gps-network
                    // viene spento
                    myLocation.stopLocation();
                }
            }
        }
    };

    protected void changePosition() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // se e' stata trovata la posizione gps
                if (mPosition != null) {
                    // appena il gps trova la posizione la mappa viene centrata in quel punto
                    CameraUpdate center =
                            CameraUpdateFactory.newLatLngZoom(new LatLng(mPosition.getLatitude(),
                                    mPosition.getLongitude()), 15f);

                    googleMap.animateCamera(center);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Sto cercando la tua posizione...", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
    }


    // classe interna che modifica l'infoview dei markers
    public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        LayoutInflater inflater;
        MyMarker myMarker;

        public MarkerInfoWindowAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        // view che definisce il contenuto dell'infoview
        @Override
        public View getInfoContents(Marker marker) {
            View v  = inflater.inflate(R.layout.infowindow_layout, null);

            // recupero le info sui markers
            myMarker = mMarkersHashMap.get(marker);

            // inizializzo un'immagine e una casella di testo
            ImageView markerIcon = (ImageView) v.findViewById(R.id.marker_icon);
            TextView markerLabel = (TextView)v.findViewById(R.id.marker_label);


            // genero il thumbnail bitmap da visualizzare nell'infoview
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(myMarker.getmIcon(), options);

            int THUMBSIZE = 100;
            Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(bitmap, THUMBSIZE, THUMBSIZE);


            // setto l'immagine e il testo (il nome dell'immagine recuperato da MyMarker
            markerIcon.setImageBitmap(ThumbImage);
            markerLabel.setText(myMarker.getmLabel());

            return v;
        }
    }

}
