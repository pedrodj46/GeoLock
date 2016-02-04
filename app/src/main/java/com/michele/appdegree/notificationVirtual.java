package com.michele.appdegree;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.michele.fragmentexample.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.List;

/**
 * Created by mattia on 19/01/16.
 */
public class notificationVirtual extends Fragment implements SensorEventListener {

    // Questo Fragment si occupa della gestione della camera e dell'anteprima che mostra il flusso
    // video acquisito

    private Camera mCamera;

    // View per visualizzare l'anteprima della camera
    private CameraPreview mPreview;

    // booleano per selezionare il tipo di riquadro (statico o mobile)
    static Boolean drawBallsOk = true;

    // View contenitore di tutti gli elementi riguardanti la camera
    private View mCameraView;

    // incognita booleana esternata per attivare la rotazione dell'immagine dopo lo scatto
    // nel caso il cellulare sia in posizione 'portrait'
    int ruotaImmagine = 0;

    // variabile bitmap esternata per effettuare un salvataggio ex-tempore
    static Bitmap croppedBitmap;
    static Bitmap bitmap;

    // variabile statica necessaria al funzionamento di COLORBALL, che non puo' contenere variabili
    // statiche
    static int count = 0;

    // variabili necessarie a recuperare la posizione dei punti ed effettuare il taglio
    // dell'immagine post-scatto
    float UPcut = 0;
    float DOWNcut = 0;
    float LEFTcut = 0;
    float RIGHTcut = 0;

    // incognita GeoTag per la posizione
    MyLocation myLocation = new MyLocation();
    // contenitore della posizione trovata
    static android.location.Location mPosition;
    // pulsante geotag
    static ImageView geoButton;
    static Boolean buttonOk;

    Double latitudine, longitudine;
    Integer gradi;

    // barra di caricamento
    protected ProgressDialog mLoadingProgressDialog;

    // disattiva il pulsante CAPTURE se il gps non non ha ancora recuperato delle coordinate
    Boolean buttonOn;

    // variabili bussola
    // contenitore dell'immagine della bussola
    private ImageView mPointer;
    // variabile di rotazione della bussola
    private float currentDegree = 0f;
    // manager per accedere ai sensori necessari all'uso della bussola
    private SensorManager mSensorManager;

    static ImageView freccia;
    static ImageView frecciaSx;
    static ImageView frecciaDx;

    String idN;
    float distanza;

    TextView metri;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }


    public notificationVirtual(){
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notification_virtual, container, false);

        buttonOn = false;
        buttonOk = true;

        mPosition = null;

        myLocation.getLocation(getActivity().getApplicationContext(), locationResult);

        // bussola
        // imageview per visualizzare la bussola
        // mPointer = (ImageView) view.findViewById(R.id.compass_camera);
        // sensore per l'utilizzo della bussola
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        freccia = (ImageView) view.findViewById(R.id.freccia);
        frecciaSx = (ImageView) view.findViewById(R.id.frecciasx);
        frecciaDx = (ImageView) view.findViewById(R.id.frecciadx);

        metri = (TextView) view.findViewById(R.id.metri);
        metri.setText("Calcolo...");

        // crea la preview della camera e la imposta nel container dell'activity
        boolean opened = safeCameraOpenInView(view);

        if(opened == false){
            // errore la preview non e' stata avviata
            return view;
        }

        String result = GetData();

        return view;
    }

    // void necessario a richiamare la classe MyLocation e recuperare Longitudine e Latitudine
    private MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
        @Override
        public void gotLocation(Location location) {
            // qui viene stabilito cosa fare con le info recuperate
            if(location!=null) {
                mPosition = null;
                mPosition = location;
            }
        }
    };


    // default void per la bussola
    @Override
    public void onSensorChanged(SensorEvent event) {
        // calcola i gradi di rotazione dell'immagine
        float degree = Math.round(event.values[0]);
        /*
        // animazione della rotazione della bussola
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
        // tempo entro il quale l'animazione deve iniziare e finire
        ra.setDuration(210);
        ra.setFillAfter(true);
        // inizia l'animazione
        mPointer.startAnimation(ra);
        */
        currentDegree = -degree;

        float sensore = Math.abs(currentDegree);
        float opposite = 0;
        float rotation = 0;
        int gradiMin = 0;
        int gradiMax = 0;
        boolean change = false;
        rotation=sensore-gradi;

        gradiMin=gradi-20;
        gradiMax=gradi+20;

        if(gradiMin<0 || gradiMin>360){
            gradiMin=Math.abs(Math.abs(gradiMin)-360);
            change=true;
        }
        if(gradiMax<0 || gradiMax>360){
            gradiMax=Math.abs(Math.abs(gradiMax)-360);
            change=true;
        }

        opposite=gradi+180;
        if(opposite>360){
            opposite-=360;
        }

        if(mPosition!=null) {
            //Log.d("coordinate", mPosition.getLatitude() + ", " + mPosition.getLongitude());
            Location loc1 = new Location("");
            loc1.setLatitude(latitudine);
            loc1.setLongitude(longitudine);

            Location loc2 = new Location("");
            loc2.setLatitude(mPosition.getLatitude());
            loc2.setLongitude(mPosition.getLongitude());

            distanza=loc1.distanceTo(loc2);

            int arrotondamento2=(int)distanza;
            if(arrotondamento2>=1000){
                float arrotondamento= arrotondamento2;
                arrotondamento/=1000;
                NumberFormat formatter = NumberFormat.getNumberInstance();
                formatter.setMinimumFractionDigits(2);
                formatter.setMaximumFractionDigits(2);
                metri.setText(formatter.format(arrotondamento)+" km");
            }
            else{
                metri.setText(arrotondamento2+" m");
            }
        }

        if(change) {
            if (sensore >= gradiMin || sensore <= gradiMax) {
                metri.setVisibility(View.VISIBLE);
                freccia.setVisibility(View.VISIBLE);
                frecciaSx.setVisibility(View.INVISIBLE);
                frecciaDx.setVisibility(View.INVISIBLE);
                freccia.setPivotX(freccia.getWidth() / 2);
                freccia.setPivotY(freccia.getHeight() / 2);
                freccia.setRotation(rotation);
            } else {
                metri.setVisibility(View.INVISIBLE);
                freccia.setVisibility(View.INVISIBLE);

                if(opposite<=180){
                    if(sensore>opposite && sensore<gradi){
                        frecciaSx.setVisibility(View.INVISIBLE);
                        frecciaDx.setVisibility(View.VISIBLE);
                    }
                    else{
                        frecciaSx.setVisibility(View.VISIBLE);
                        frecciaDx.setVisibility(View.INVISIBLE);
                    }
                }
                else{
                    if(sensore>gradi && sensore<opposite){
                        frecciaSx.setVisibility(View.VISIBLE);
                        frecciaDx.setVisibility(View.INVISIBLE);
                    }
                    else{
                        frecciaSx.setVisibility(View.INVISIBLE);
                        frecciaDx.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
        else{
            if (sensore >= gradiMin && sensore <= gradiMax) {
                metri.setVisibility(View.VISIBLE);
                freccia.setVisibility(View.VISIBLE);
                frecciaSx.setVisibility(View.INVISIBLE);
                frecciaDx.setVisibility(View.INVISIBLE);
                freccia.setPivotX(freccia.getWidth() / 2);
                freccia.setPivotY(freccia.getHeight() / 2);
                freccia.setRotation(rotation);
            } else {
                metri.setVisibility(View.INVISIBLE);
                freccia.setVisibility(View.INVISIBLE);

                if(opposite<=180){
                    if(sensore>opposite && sensore<gradi){
                        frecciaSx.setVisibility(View.INVISIBLE);
                        frecciaDx.setVisibility(View.VISIBLE);
                    }
                    else{
                        frecciaSx.setVisibility(View.VISIBLE);
                        frecciaDx.setVisibility(View.INVISIBLE);
                    }
                }
                else{
                    if(sensore>gradi && sensore<opposite){
                        frecciaSx.setVisibility(View.VISIBLE);
                        frecciaDx.setVisibility(View.INVISIBLE);
                    }
                    else{
                        frecciaSx.setVisibility(View.INVISIBLE);
                        frecciaDx.setVisibility(View.VISIBLE);
                    }
                }
            }
        }

    }

    // default void per la bussola
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void fadeOutAndHideImage(final ImageView img){
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(1000);

        fadeOut.setAnimationListener(new Animation.AnimationListener(){
            public void onAnimationEnd(Animation animation){
                img.setVisibility(View.GONE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });

        img.startAnimation(fadeOut);
    }

    private void fadeInAndShowImage(final ImageView img){
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new AccelerateInterpolator());
        fadeIn.setDuration(1000);

        fadeIn.setAnimationListener(new Animation.AnimationListener(){
            public void onAnimationEnd(Animation animation){
                img.setVisibility(View.VISIBLE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });

        img.startAnimation(fadeIn);
    }


    // contiene una serie di comandi per avviare la comunicazione con la camera e avviare lo
    // stream video necessario e visualizzare l'anteprima ed effettuare lo scatto
    private boolean safeCameraOpenInView(View view) {
        boolean qOpened = false;
        releaseCameraAndPreview();
        mCamera = getCameraInstance();
        mCameraView = view;
        qOpened = (mCamera != null);

        if(qOpened){
            mPreview = new CameraPreview(getActivity().getBaseContext(), mCamera,view);
            FrameLayout preview = (FrameLayout) view.findViewById(R.id.camera_preview);
            preview.addView(mPreview);

            mPreview.startCameraPreview();
        }
        return qOpened;
    }

    // metodo per inizializzare la camera
    public static Camera getCameraInstance(){
        // resetto la variabile c
        Camera c = null;
        try {
            // tentativo di contatto con la camera
            c = Camera.open();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        // ritorna null se la camera non e' disponibile
        return c;
    }

    @Override
    public void onPause() {
        super.onPause();

        // per la gestione del sensore bussola
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // per la gestione del sensore bussola
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    // reset parametri camera e geotag alla distruzione del Fragment
    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseCameraAndPreview();
        myLocation.stopLocation();
    }

    // richiamato da onDestroy per svuotare mCamera e mPreview
    private void releaseCameraAndPreview() {

        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if(mPreview != null){
            mPreview.destroyDrawingCache();
            mPreview.mCamera = null;
        }
    }

    // superficie utilizzata per mostrare l'anteprima della camera
    class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

        private SurfaceHolder mHolder;

        private Camera mCamera;

        private Context mContext;

        // altezza e larghezza della finestra di anteprima - viene modificata anche in caso di
        // rotazione dello schermo
        private Camera.Size mPreviewSize;

        // lista delle risoluzioni supportate dal dispositivo
        private List<Camera.Size> mSupportedPreviewSizes;

        // modalita' di flash supportate dal dispositivo
        private List<String> mSupportedFlashModes;

        // modalita' di messa a fuoco supportate dalla camera
        private List<String> mSupportedFocusModes;

        // View che contiene tutti gli elementi riguardanti l'anteprima
        private View mCameraView;

        public CameraPreview(Context context, Camera camera, View cameraView) {
            super(context);

            mCameraView = cameraView;
            mContext = context;
            setCamera(camera);

            // SurfaceHolder.Callback ha la funzione di controllare create e destroy della surface
            mHolder = getHolder();
            mHolder.addCallback(this);
            mHolder.setKeepScreenOn(true);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        // attiva e trasmette la preview
        public void startCameraPreview() {
            try{
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        // stabilisce le dimensioni della preview, i flash e le modalita' di messa a fuoco
        // supportate dalla camera in uso
        private void setCamera(Camera camera) {
            mCamera = camera;
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            mSupportedFlashModes = mCamera.getParameters().getSupportedFlashModes();
            mSupportedFocusModes = mCamera.getParameters().getSupportedFocusModes();

            // imposta la modalita' di flash della camera
            if (mSupportedFlashModes != null &&
                    mSupportedFlashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)){
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                mCamera.setParameters(parameters);
            }

            // imposta la modalita' di focus della camera
            if (mSupportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                mCamera.setParameters(parameters);
            } else if (mSupportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                mCamera.setParameters(parameters);
            }

            requestLayout();
        }

        // una volta che la surface contenente l'anteprima viene creata, viene inserita nella view
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // ferma l'anteprima e chiude la camera
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mCamera != null){
                mCamera.stopPreview();
            }
        }

        // reagisce quando la surface viene modificata (rotazione schermo)
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // se l'anteprima non esiste ancora
            if (mHolder.getSurface() == null){
                return;
            }

            // viene disattivata prima di apportare cambiamenti per evitare bug
            // il try impedisce che venga spenta una preview inesistente
            try {
                mCamera.stopPreview();
            } catch (Exception e){
            }

            // inizializza una nuova preview con nuove impostazioni
            try {
                Camera.Parameters parameters = mCamera.getParameters();

                // necessaria mPreviewSize
                if(mPreviewSize != null) {
                    Camera.Size previewSize = mPreviewSize;
                    parameters.setPreviewSize(previewSize.width, previewSize.height);
                }

                mCamera.setParameters(parameters);
                mCamera.startPreview();

            } catch (Exception e){
                e.printStackTrace();
            }
        }

        // gestisce le misure del layout
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
            setMeasuredDimension(width, height);

            if (mSupportedPreviewSizes != null){
                mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
            }
        }

        // stabilisce le misure dell'immagine sviluppata e le grandezza dell'anteprima in base alla
        // rotazione dello schermo
        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            if (changed) {
                final int width = right - left;
                final int height = bottom - top;

                int previewWidth = width;
                int previewHeight = height;

                float ratioJusto = 0;
                float ratioAttuale = 0;

                Display display = ((WindowManager)mContext.getSystemService
                        (Context.WINDOW_SERVICE)).getDefaultDisplay();

                if (mPreviewSize != null){
                    switch (display.getRotation()) {
                        case Surface.ROTATION_0:
                            previewWidth = mPreviewSize.width;
                            previewHeight = mPreviewSize.height;
                            if(previewWidth>previewHeight) {
                                previewWidth = mPreviewSize.height;
                                previewHeight = mPreviewSize.width;
                                mCamera.setDisplayOrientation(90);
                            }
                            break;
                        case Surface.ROTATION_90:
                            previewWidth = mPreviewSize.width;
                            previewHeight = mPreviewSize.height;
                            ruotaImmagine = 0;
                            break;
                        case Surface.ROTATION_180:
                            previewWidth = mPreviewSize.width;
                            previewHeight = mPreviewSize.height;
                            if(previewWidth>previewHeight) {
                                previewWidth = mPreviewSize.height;
                                previewHeight = mPreviewSize.width;
                                mCamera.setDisplayOrientation(270);
                                ruotaImmagine = 270;
                            }
                            break;
                        case Surface.ROTATION_270:
                            previewWidth = mPreviewSize.width;
                            previewHeight = mPreviewSize.height;
                            ruotaImmagine = 180;
                            mCamera.setDisplayOrientation(180);
                            break;
                    }
                }

                switch (display.getRotation()) {
                    case Surface.ROTATION_0:
                        mCamera.setDisplayOrientation(90);
                        ruotaImmagine = 90;
                        break;
                    case Surface.ROTATION_90:
                        ruotaImmagine = 0;
                        break;
                    case Surface.ROTATION_180:
                        if(previewWidth>previewHeight) {
                            mCamera.setDisplayOrientation(270);
                            ruotaImmagine = 270;
                        }
                        break;
                    case Surface.ROTATION_270:
                        ruotaImmagine = 180;
                        mCamera.setDisplayOrientation(180);
                        break;
                }

                final int scaledChildHeight = previewHeight * width / previewWidth;
                mCameraView.layout(0, height - scaledChildHeight, width, height);


                //SELEZIONE DELLA RISOLUZIONE DELL'IMMAGINE IN USCITA
                Camera.Parameters params = mCamera.getParameters();
                List<Camera.Size> sizes = params.getSupportedPictureSizes();
                // inizializzo sizeCapture
                Camera.Size sizeCapture = sizes.get(0);

                // creazione del ratio giusto a seconda del lato maggiore
                if(previewHeight > previewWidth) {
                    ratioJusto = (previewHeight*1000)/previewWidth;
                } else {
                    ratioJusto = (previewWidth*1000)/previewHeight;
                }

                for (int i = 0; i < sizes.size(); i++) {
                    // serve al confronto tra il ratio attuale e quello della preview
                    // se sono uguali verra' selezionata la risoluzione più alta con questo ratio
                    // se e' diverso verra' sostituito
                    ratioAttuale = (sizeCapture.width*1000)/sizeCapture.height;

                    if ((sizes.get(i).width*1000)/sizes.get(i).height == ratioJusto) {
                        if(ratioAttuale != ratioJusto) {
                            sizeCapture = sizes.get(i);
                        } else if (sizes.get(i).width > sizeCapture.width) {
                            sizeCapture = sizes.get(i);
                        }
                    }
                }

                params.setPictureSize(sizeCapture.width, sizeCapture.height);
                mCamera.setParameters(params);
            }
        }

        // gestisce le misure della preview
        private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int width, int height) {

            // Visualizzo le grandezze disponibili di preview
            Camera.Parameters parameters = mCamera.getParameters();

            Camera.Size optimalSize = null;

            // questa frazione di tolleranza garantisce la presenza di almeno una risoluzione
            // compatibile
            final double ASPECT_TOLERANCE = 0.1;
            double targetRatio = (double) height / width;

            // cerca tra le risoluzioni disponibili una che si adatti alla grandezza dello schermo
            for (Camera.Size size : sizes){

                if (size.height != width) continue;
                double ratio = (double) size.width / size.height;
                if (ratio <= targetRatio + ASPECT_TOLERANCE && ratio >= targetRatio - ASPECT_TOLERANCE){
                    optimalSize = size;
                }
            }

            // se non riesce a individuarne nessuna adatta ai pre-requisiti procede ignorandoli
            // l'anteprima viene visualizzata comunque ma potrebbe riportare un'immagine
            // deformata che compromette il funzionamento del riquadro di ritaglio
            if (optimalSize == null) {

            }

            return optimalSize;
        }
    }


    //Picture Callback per gestire la foto in output e salvarla
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            bitmap = null;
            // recupero l'immagine scattata
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

            // valuto larghezza e altezza dell'immagine
            int largX = bitmap.getWidth();
            int altY = bitmap.getHeight();

            // inverto altezza e larghezza nel caso lo schermo sia orrizzontale
            if(ruotaImmagine != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(ruotaImmagine);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, largX, altY, matrix, true);
                largX = bitmap.getWidth();
                altY = bitmap.getHeight();
            }

            // mathround trasforma i float in integer per il croppedBitmap
            // il calcolo trasforma le proporzioni dell'anteprima in proporzioni della picture
            // finale
            int mLEFTcut = Math.round(LEFTcut * largX);
            int mRIGHTcut = Math.round(RIGHTcut * largX);
            int mUPcut = Math.round(UPcut * altY);
            int mDOWNcut = Math.round(DOWNcut * altY);

            mRIGHTcut = largX-(largX-mRIGHTcut)-mLEFTcut;
            mDOWNcut = altY-(altY-mDOWNcut)-mUPcut;

            croppedBitmap = null;
            croppedBitmap = Bitmap.createBitmap(bitmap, mLEFTcut, mUPcut,
                    mRIGHTcut, mDOWNcut);

            // disattiva gps e network
            myLocation.stopLocation();

        }
    };

    public void notificationID(String idNotification){
        idN = idNotification;
    }

    public String GetData(){

        //initialize
        InputStream is = null;
        String result = "";
        JSONArray jArray = null;

        String url = "http://esamiuniud.altervista.org/tesi/getVR.php?idN=";
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
            latitudine = c.getDouble("lat");
            longitudine = c.getDouble("lon");
            gradi = c.getInt("gradi");
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return "1";
    }

}