package com.michele.appdegree;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;

import com.michele.fragmentexample.R;

import java.io.File;

public class mainActivity extends FragmentActivity implements
        buttonsFragment.ToolbarListener, CameraFragment.ToolbarListener,
        ImageCaptured.ToolbarListener, ImageDeTails.ToolbarListener, mainGallery.ToolbarListener,
        loginFragment.ToolbarListener, mainNotification.ToolbarListener,
        notificationDetails.ToolbarListener{

    // Activity principale che gestisce l'interazione tra tutti di Fragment dell'applicazione e
    // conserva i dati raccolti per il loro inserimento all'interno del database

    static android.location.Location posizioneAttuale = null;
    static String nameImage = null;
    static String mDirezioneSoggCard = null;
    static Float mDirezioneSoggDegree = 0.0f;
    static String mDirezioneDispCard = null;
    static Float mDirezioneDispDegree = 0.0f;
    static Integer widthImage;
    static Integer heightImage;
    static String imLatitude;
    static String imLongitude;
    static Integer mDistanza = 0;

    static Integer result=0;
    static String stringDialog="";

    public static final String MY_PREFS_NAME = "ricordamiLogin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        setContentView(R.layout.main);

        //getActionBar().setDisplayHomeAsUpEnabled(true);

        // registra il tokenid
        GcmPushNotifications registration = new GcmPushNotifications();
        registration.registrationDevice(this);

        // valuta se riempire il fragment container con il primo fragment o se questo e' gia' in uso
        if (findViewById(R.id.fragment_container) != null) {
            // ma se contiene gia' un fragment allora ignora
            if (savedInstanceState != null) {
                return;
            }

            Intent intent = getIntent();
            if(intent.hasExtra("idN") && intent.hasExtra("case")){
                Bundle b = getIntent().getExtras();
                int value = b.getInt("case");
                String idN = b.getString("idN");
                if(value==0){
                    loginFragment firstFragment = new loginFragment();
                    changingFragment(firstFragment, "recallLogin", false, true);
                }
                else if(value==1){
                    // virtual reality
                }
                else if(value==2){
                    globals isFromNotifica = (globals) getApplicationContext();
                    isFromNotifica.setIsFromNotifica(true);
                    onNotificationContinueSelected(idN);
                }
                else if(value==3){
                    onNotificationCloseSelected(idN);
                }
            }
            else {
                // setto a 0 l'id notifica
                globals idNotifica = (globals) getApplicationContext();
                idNotifica.setIdNotifica("0");

                // ricordami Login
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
                        // crea il primo fragment
                        loginFragment firstFragment = new loginFragment();

                        // inizializza fragment menu iniziale
                        changingFragment(firstFragment, "recallLogin", false, true);

                        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                        dialog.setMessage(stringDialog).setTitle("Errore!");
                        dialog.setMessage(stringDialog).setMessage("Inserire username e password corretti!");
                        dialog.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        AlertDialog alertDialog = dialog.create();
                        alertDialog.show();
                    } else {
                        globals idUtente = (globals) getApplicationContext();
                        idUtente.setId(idU);

                        buttonsFragment btnFragment = new buttonsFragment();
                        changingFragment(btnFragment, "recallButtons", true, false);

                        // abilita il servizio di localizzazione automatico
                        LocationUpdate start = new LocationUpdate();
                        start.startServiceLocation(this);
                    }
                } else {
                    // crea il primo fragment
                    loginFragment firstFragment = new loginFragment();

                    // inizializza fragment menu iniziale
                    changingFragment(firstFragment, "recallLogin", false, true);
                }
            }
        }
    }

    // FRAGMENT LOGIN
    public void onButtonLogin() {

        EditText username = (EditText)findViewById(R.id.loginUsername);
        EditText password = (EditText)findViewById(R.id.loginPassword);
        CheckBox ricordami = (CheckBox)findViewById(R.id.loginCheckbox);
        String usernameText = username.getText().toString();
        String passwordText = password.getText().toString();

        Log.d("username", usernameText);
        Log.d("password", passwordText);

        String idU;

        ServerConnection sendLogin = new ServerConnection();
        idU=sendLogin.sendLogin(usernameText, passwordText, this);

        Log.d("id", idU);

        if(idU.equals("0")){
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage(stringDialog).setTitle("Errore!");
            dialog.setMessage(stringDialog).setMessage("Inserire username e password corretti!");
            dialog.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog alertDialog = dialog.create();
            alertDialog.show();
        }
        else{
            globals idUtente = (globals) getApplicationContext();
            idUtente.setId(idU);

            if(ricordami.isChecked()){
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("username", Base64.encodeToString(usernameText.getBytes(), Base64.DEFAULT));
                editor.putString("password", Base64.encodeToString(passwordText.getBytes(), Base64.DEFAULT));
                editor.commit();
            }
            else{
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.remove("username");
                editor.remove("password");
                editor.commit();
            }

            Log.d("entra", "si");

            FragmentManager myFragment = getSupportFragmentManager();
            myFragment.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            buttonsFragment newFragment = new buttonsFragment();
            changingFragment(newFragment, "recallButton", true, false);

            // abilita il servizio di localizzazione automatico
            LocationUpdate start = new LocationUpdate();
            start.startServiceLocation(this);
        }
    }

    // SEGUONO I QUATTRO PULSANTI DEL MENU INIZIALE
    // inizializza fragment della camera
    public void onButtonClick() {
        CameraFragment newFragment = new CameraFragment();

        changingFragment(newFragment, "recallCamera", true, false);
    }

    // inizializza galleria immagini
    public void onButtonClick02() {
        mainGallery newFragment = new mainGallery();

        changingFragment(newFragment, "recallGallery", true, false);
    }

    // inizializza la mappa di google
    public void onButtonClick04() {
        mapFragment newFragment = new mapFragment();

        changingFragment(newFragment, "recallGoogleMaps", true, false);
    }

    // inizializza la calibrazione della bussola
    public void onButtonClick05() {
        calibrationFragment newFragment = new calibrationFragment();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        changingFragment(newFragment, "recallCalibration", true, false);
    }

    // invio foto mancanti
    public void onButtonClick06() {

        final ProgressDialog progressDialog = ProgressDialog.show(this,
                "Attendere", "Sto caricando le foto sul server!", false, true);

        result=updateOtherPhoto();

        switch(result){
            case 0:
                stringDialog="Nessuna foto da caricare!";
                break;
            case 1:
                stringDialog="Non sono state caricate tutte le foto!";
                break;
            case 2:
                stringDialog="Caricamento completato!";
                break;
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(stringDialog).setTitle("Upload Foto");
        dialog.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }

    // inizializza la mappa di google
    public void onButtonClick07() {
        mainNotification newFragment = new mainNotification();

        changingFragment(newFragment, "recallNotification", true, false);
    }

    public int updateOtherPhoto(){

        int result=0, i=0;

        // attivazione della classe e creazione/recupero del database
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        // connetto al database
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        // recupero il cursore ed eseguo query
        Cursor c = db.rawQuery("SELECT * FROM InfoImages WHERE transferred = 0", null);
        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            // invio json server delle foto mancanti
            ServerConnection sendData = new ServerConnection();
            if(sendData.sendData(c.getLong(0), c.getString(1), c.getString(4), c.getString(5),
                    c.getString(6), c.getString(7), c.getFloat(8), c.getString(9), c.getFloat(10),
                    c.getInt(11), this)){
                databaseHelper.updateTransferred(c.getLong(0), true);

                result++;
            }
            i++;
        }
        db.close();

        if(i==0){
            result=0;
        }
        else if(result!=i){
            result=1;
        }
        else{
            result=2;
        }

        return result;
    }


    // inizializza fragment "risultato scatto" - richiamato dalla camera
    public void onPictureSaved(Bitmap mBitmap, android.location.Location mPosition, float degree) {
        widthImage = null;
        heightImage = null;
        widthImage = mBitmap.getWidth();
        heightImage = mBitmap.getHeight();
        mDirezioneDispDegree = 0.0f;
        mDirezioneDispDegree = degree;
        posizioneAttuale = null;
        if(mPosition!=null) {
            // recupero la posizione gps della foto
            posizioneAttuale = mPosition;
        }

        ImageCaptured newFragment = new ImageCaptured();
        newFragment.displayImage(mBitmap);

        changingFragment(newFragment, "onPictureSavedRecall", true, false);
    }

    // inizializza fragment d'inserimento dei dettagli riguardanti una foto
    public void onPictureAccepted() {
        ImageDeTails newFragment = new ImageDeTails();

        // invio la posizione gps della foto
        newFragment.GetPosition(posizioneAttuale, mDirezioneDispDegree);

        changingFragment(newFragment, "deTailsRecall", true, false);
    }

    // conferma il salvataggio della foto al fragment camera
    public Boolean onSendName(String name, Float direzioneDispDegree, String direzioneDispCard,
                           Float direzioneSoggDegree, String direzioneSoggCard, Integer distanza) {
        nameImage = null;
        nameImage = name;
        mDirezioneDispDegree = 0.0f;
        mDirezioneDispDegree = direzioneDispDegree;
        mDirezioneDispCard = null;
        mDirezioneDispCard = direzioneDispCard;
        mDirezioneSoggDegree = 0.0f;
        mDirezioneSoggDegree = direzioneSoggDegree;
        mDirezioneSoggCard = null;
        mDirezioneSoggCard = direzioneSoggCard;
        mDistanza = distanza;
        // salvo il file immagine con il nome scelto nel fragment precedente
        CameraFragment finalsave =
                (CameraFragment) getSupportFragmentManager().findFragmentByTag("recallCamera");
        finalsave.SaveFile(nameImage);

        // creato il file e scelto il nome adesso aggiorno il database
        upDateDatabase();

        Boolean result;

        if(controlloDatabase() && controlloCartella()){
            result=true;
        }
        else{
            result=false;
        }

        return result;

        /*
        Boolean cDatabase = controlloDatabase();
        Boolean cCartella = controlloCartella();

        // infine comunico la riuscita dell'operazione
        ImageDeTails avviso =
                (ImageDeTails) getSupportFragmentManager().findFragmentByTag("deTailsRecall");
        avviso.changeText(cCartella, cDatabase);
        */
    }

    public Boolean controlloCartella() {
        Boolean saveOK = false;

        String imageName = nameImage+".jpg";

        String path = Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_PICTURES).toString()+"/AppDegreeTrial/";

        File f = new File(path);
        if(f.isDirectory()) {
            // recupero dei file contenuti nella cartella
            File file[] = f.listFiles();
            for (int i = 0; i < file.length; i++) {
                if(imageName.equals(file[i].getName())) {
                    // se il nome dell'immagine e' presente nella cartella restituisce true
                    saveOK = true;
                }
            }
        }

        return saveOK;
    }

    public Boolean controlloDatabase() {
        Boolean databaseOk = false;

        String imageName = nameImage;

        // recupero il database
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        // recupero il cursore
        Cursor c = databaseHelper.getImageInfo();
        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            String databaseName = c.getString(1);

            if(imageName.equals(databaseName)) {
                // se il nome dell'immagine e' presente nel database restituisce true
                databaseOk = true;
            }
        }

        return databaseOk;
    }


    // inizializza l'apertura dei dettagli riguardanti una foto quando viene cliccata sul listener
    // del fragment gallery
    /*public void onPictureSelected(String imagePath) {
        galleryPhotoDeTails newFragment = new galleryPhotoDeTails();
        // invio il percorso dell'immagine da visualizzare nel nuovo fragment
        newFragment.displayImage(imagePath);

        changingFragment(newFragment, "galleryImageDeTails", true, false);
    }*/

    public void onPhotoSelected(String idF){
        photoDetails newFragment = new photoDetails();
        // invio l'id della notifica da visualizzare nel nuovo fragment
        newFragment.photoID(idF);

        changingFragment(newFragment, "photoDetails", true, false);
    }

    public void onNotificationSelected(String idN){
        notificationDetails newFragment = new notificationDetails();
        // invio l'id della notifica da visualizzare nel nuovo fragment
        newFragment.notificationID(idN);

        changingFragment(newFragment, "notificationDetails", true, false);
    }

    public void onNotificationCloseSelected(String idN){
        notificationClose newFragment = new notificationClose();
        // invio l'id della notifica da visualizzare nel nuovo fragment
        newFragment.notificationID(idN);

        changingFragment(newFragment, "notificationClose", false, true);
    }

    public void onNotificationContinueSelected(String idN){
        globals idNotifica = (globals) getApplicationContext();
        idNotifica.setIdNotifica(idN);

        CameraFragment newFragment = new CameraFragment();

        changingFragment(newFragment, "recallCamera", true, false);

        Log.d("id notifica", idNotifica.getIdNotifica());
    }


    // ogni volta che bisogna inizializzare un nuovo fragment viene avviato questo metodo
    public void changingFragment(android.support.v4.app.Fragment newFragment, String Tag,
                                 Boolean saveState, Boolean removeState) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                R.anim.slide_in_left, R.anim.slide_out_right);

        if(removeState) {
            transaction.remove(getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container));
        }
        transaction.replace(R.id.fragment_container, newFragment, Tag);
        if(saveState) {
            transaction.addToBackStack(Tag);
        }
        else{
            transaction.addToBackStack(null);
        }
        // Tag e' il tag con il quale sara' poi richiamato il fragment sucessivamente
        transaction.commit();
    }


    public void upDateDatabase() {
        // attivazione della classe e creazione/recupero del database
        DatabaseHelper databaseHelper = new DatabaseHelper(this);

        // trasformo valori double in valori string
        imLatitude = String.valueOf(posizioneAttuale.getLatitude());
        imLongitude = String.valueOf(posizioneAttuale.getLongitude());

        // recupero l'indirizzo tramite le coordinate geografiche
        AddressCatcher getAddress = new AddressCatcher();
        String Address = null;
        Address = getAddress.Address(this, imLatitude, imLongitude);
        // in caso di risultato nullo stampo questa stringa
        if(Address==null) {
            Address = "Indirizzo non trovato!";
        }

        // id foto inserita
        long id;
        // inserisco nel database una nuova riga con tutte le informazioni riguardanti l'immagine
        id=databaseHelper.insertImageData(nameImage, widthImage, heightImage, imLatitude,
                imLongitude, Address, mDirezioneDispCard, mDirezioneDispDegree, mDirezioneSoggCard,
                mDirezioneSoggDegree, mDistanza, false);

        // invio json server
        ServerConnection sendData = new ServerConnection();
        if(sendData.sendData(id, nameImage, imLatitude, imLongitude, Address, mDirezioneDispCard,
                mDirezioneDispDegree, mDirezioneSoggCard, mDirezioneSoggDegree, mDistanza, this)){
            databaseHelper.updateTransferred(id, true);
        }


        // PER OPERAZIONI DI DEBUG ATTIVARE QUESTE RIGHE DI COMANDO - STAMPANO LE INFORMAZIONI
        // INSERITE DALL'ACTIVITY NEL DATABASE

        // recupero le info stabilite dal Cursor getImageInfo()
        Cursor c = databaseHelper.getImageInfo();

        // in questo caso richiamare il database serve solo a stampare il suo contenuto per intero
        // (in questo caso il risultato e' visibile solo al programmatore)
        try {
            // moveToNext sposta il cursore di una posizione ad ogni ciclo fino al termine dei dati
            while (c.moveToNext()) {
                // long data type is a 64-bit two's complement integer
                Log.d("DATABASE TRIAL", "_id: " + c.getLong(0) + " name: " +
                        c.getString(1) + " width: " + c.getInt(2) + " height: " + c.getInt(3) +
                        " latitude: " + c.getString(4) + " longitude: " + c.getString(5) +
                        " indirizzo: " + c.getString(6) + " direzione iniziale: " + c.getString(7) +
                        " a " + c.getString(8) + " gradi " + " direction del soggetto: " +
                        c.getString(9) + " a " + c.getString(10) + " gradi " + "distanza: " +
                        c.getString(11) + " metri " + " trasferito? " + c.getString(12));
            }
        }
        // finally viene invocato che try abbia successo o meno per chiudere il Cursor
        finally {
            c.close();
        }

    }


    // azioni previste per il back button
    @Override
    public void onBackPressed() {
        // recupero i frammenti interessati e permetto solo a questi di tornare al fragment
        // precedente tramite il back button
        FragmentManager myFragment = getSupportFragmentManager();
        if(myFragment.findFragmentByTag("recallButtons") != null &&
                myFragment.findFragmentByTag("recallButtons").isVisible()) {
            // in tutti gli altri casi il back button attiva una finestra di dialogo che
            // chiede conferma per l'uscita dal programma
            new AlertDialog.Builder(this)
                    .setMessage("Vuoi chiudere il programma?")
                    .setNegativeButton("Conferma", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            // permette al backbutton il comportamento di default
                            //mainActivity.super.onBackPressed();
                            // chiude l'applicazione ma la lascia lavorare in background
                            //finish();
                            // termina il programma chiudendo tutti i processi in background
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    })
                    .setPositiveButton("Annulla", null).create().show();
        } else if(myFragment.findFragmentByTag("onPictureSavedRecall") != null &&
                myFragment.findFragmentByTag("onPictureSavedRecall").isVisible()) {
            CameraFragment camera = new CameraFragment();
            camera.Recycle();
            ImageCaptured imageCaptured = new ImageCaptured();
            imageCaptured.recycle();
            mainActivity.super.onBackPressed();
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0){
            getSupportFragmentManager().popBackStack();
        } else {
            mainActivity.super.onBackPressed();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // gestisce il menu
        menu.add(Menu.NONE, R.id.menu_action1, Menu.NONE, R.string.menu_action1);
        menu.add(Menu.NONE, R.id.menu_action2, Menu.NONE, R.string.menu_action2);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action1:
                Log.d("btn","hai cliccato il btn 1");
                return true;
            case R.id.menu_action2:
                Log.d("btn","hai cliccato il btn 2");

                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.remove("username");
                editor.remove("password");
                editor.commit();

                FragmentManager myFragment = getSupportFragmentManager();
                myFragment.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                loginFragment firstFragment = new loginFragment();
                changingFragment(firstFragment, "recallLogin", false, true);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
