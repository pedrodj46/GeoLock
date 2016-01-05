package com.michele.appdegree;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.michele.fragmentexample.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraFragment extends Fragment implements SensorEventListener {

    // Questo Fragment si occupa della gestione della camera e dell'anteprima che mostra il flusso
    // video acquisito

    private Camera mCamera;

    // View per visualizzare l'anteprima della camera
    private CameraPreview mPreview;

    // Riquadro di ritaglio
    private DrawView mRitaglio;
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

    // segue l'interfaccia per lo scambio di dati tra il camera_fragment e imageCapture_fragment
    ToolbarListener activityCallback;

    public interface ToolbarListener {
        public void onPictureSaved(Bitmap mBitmap, android.location.Location mPosition,
                                   float degree);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            activityCallback = (ToolbarListener) activity;
            // mostra una finestra di caricamento
            mLoadingProgressDialog = new ProgressDialog(getActivity());
            mLoadingProgressDialog.setMessage("Salvataggio foto...");
            mLoadingProgressDialog.setCancelable(true);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ToolbarListener");
        }
    }

    public void pictureSaved(Bitmap bitmap, android.location.Location mPosition, float degree) {
        activityCallback.onPictureSaved(bitmap, mPosition, degree);
    }
    // termine delle righe necessarie a impostare il trasferimento dati tramite interfaccia


    public CameraFragment(){
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.camera_fragment, container, false);

        buttonOn = false;
        buttonOk = true;

        mPosition = null;
        // richiamo per la locazione geotag
        myLocation.stopLocation();
        myLocation.getLocation(getActivity().getApplicationContext(), locationResult);
        // pulsante geotag
        geoButton = (ImageView) view.findViewById(R.id.imgGeoTag);
        resolveGeoButton();

        // bussola
        // imageview per visualizzare la bussola
        mPointer = (ImageView) view.findViewById(R.id.compass_camera);
        // sensore per l'utilizzo della bussola
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        // crea la preview della camera e la imposta nel container dell'activity
        boolean opened = safeCameraOpenInView(view);

        if(opened == false){
            // errore la preview non e' stata avviata
            return view;
        }

        Button back = (Button) view.findViewById(R.id.button_backToMenu);
        back.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // fermo la ricerca gps-network
                        myLocation.stopLocation();

                        // back button forzato - riapre il menu iniziale
                        getFragmentManager().popBackStack();
                    }
                });

        // pulsante di cattura e acquisizione dell'immagine
        Button captureButton = (Button) view.findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(buttonOn) {
                            // attiva barra di caricamento
                            mLoadingProgressDialog.show();

                            // acquisisce l'immagine dalla camera
                            mCamera.takePicture(null, null, mPicture);
                        } else {
                            Toast.makeText(getActivity(), "Wait for GPS connection",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        final Button mSwitch = (Button) view.findViewById(R.id.button_switch);
        mSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(drawBallsOk) {
                    drawBallsOk = false;
                    // aggiornamento dell'drawView
                    mRitaglio.invalidate();
                    // testo presente sul pulsante - cambia in base al tipo di ritaglio selezionato
                    mSwitch.setText("CURSORI");
                } else {
                    drawBallsOk = true;
                    mRitaglio.invalidate();
                    mSwitch.setText("MIRINO");
                }
            }
        });

        return view;
    }


    // default void per la bussola
    @Override
    public void onSensorChanged(SensorEvent event) {
        // calcola i gradi di rotazione dell'immagine
        float degree = Math.round(event.values[0]);

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
        currentDegree = -degree;

    }

    // default void per la bussola
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    // funzione per disattivare la barra di caricamento
    private void cancelProgressDialog(){
        if(mLoadingProgressDialog != null){
            if(mLoadingProgressDialog.isShowing()){
                mLoadingProgressDialog.cancel();
            }
        }
    }


    // contiene una serie di comandi per avviare la comunicazione con la camera e avviare lo
    // stream video necessario e visualizzare l'anteprima ed effettuare lo scatto
    private boolean safeCameraOpenInView(View view) {
        boolean qOpened = false;
        releaseCameraAndPreview();
        mCamera = getCameraInstance();
        mCameraView = view;
        qOpened = (mCamera != null);

        if(qOpened == true){
            mPreview = new CameraPreview(getActivity().getBaseContext(), mCamera,view);
            FrameLayout preview = (FrameLayout) view.findViewById(R.id.camera_preview);
            preview.addView(mPreview);

            mRitaglio = new DrawView(getActivity().getBaseContext());
            preview.addView(mRitaglio);

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

            // trasferisci dati alla main activity
            pictureSaved(croppedBitmap, mPosition, currentDegree);

            // disattiva la barra di caricamento
            cancelProgressDialog();

            // disattiva gps e network
            myLocation.stopLocation();

        }
    };

    // salvataggio definitivo dell'immagine (una volta inseriti tutti i dettagli da parte
    // dell'utente nel Fragment ImageDeTails)
    public void SaveFile(String name) {
        File pictureFile = getOutputMediaFile(name);
        if (pictureFile == null){
            Toast.makeText(getActivity(), "Image retrieval failed.", Toast.LENGTH_SHORT)
                    .show();
            return;
        }


        Bitmap finalImage = croppedBitmap;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(pictureFile);
            finalImage.compress(Bitmap.CompressFormat.JPEG, 100, out);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // una volta salvata l'immagine croppedBitmap viene riciclato e la memoria svuotata
        Recycle();
        if (finalImage != null && !finalImage.isRecycled()) {
            finalImage.recycle();
        }
    }

    // recupera l'output della camera
    private File getOutputMediaFile(String timeStamp){

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "AppDegreeTrial");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }

        // crea il nome del file in base a quello inserito dall'utente
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                timeStamp + ".jpg");

        return mediaFile;
    }

    public void Recycle() {
        if (croppedBitmap != null && !croppedBitmap.isRecycled()) {
            croppedBitmap.recycle();
        }
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }

        ImageCaptured imageCaptured = new ImageCaptured();
        imageCaptured.recycle();
    }

    // void necessario a richiamare la classe MyLocation e recuperare Longitudine e Latitudine
    // al momento dello scatto
    private MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
        @Override public void gotLocation(Location location) {
            // qui viene stabilito cosa fare con le info recuperate
            if(location!=null) {
                mPosition = null;
                mPosition = location;
            }

            if(buttonOk) {
                resolveGeoButton();
            }
        }
    };

    protected void resolveGeoButton() {
        // rallenta l'operazione di mezzo secondo
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }

        // controlla se l'activity e' disponibile
        if(getActivity() == null)
            return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // se e' stata trovata la posizione gps
                if(mPosition != null) {
                    // se e' pieno viene reso invisibile e si passa alla visualizzazione delle foto
                    // trovate
                    geoButton.setImageResource(R.mipmap.geolog_on);
                    buttonOn = true;
                    buttonOk = false;
                } else {
                    // se e' vuoto viene reso visibile il pulsante geotag
                    geoButton.setImageResource(R.mipmap.geolog_off);
                    buttonOn = false;
                    buttonOk = true;
                }
            }
        });
    }


    class DrawView extends View {

        // array dei 4 punti che formano il rettangolo
        Point[] points = new Point[4];
        int width;
        int height;

        // point1 e point3 sono nel gruppo 1 - point2 e point4 nel gruppo 2
        int groupId = 1;
        // questo array contiene i cursori cerchio
        private ArrayList<ColorBall> colorballs = new ArrayList<ColorBall>();
        // variabile per identificare il cursore mosso in quel momento
        private int balID = 2;

        Paint paint;
        Canvas canvas;

        int halfballsize = 0;

        // centro riquadro
        Boolean centerClick = false;
        int actualX = 0;
        int actualY = 0;

        public DrawView(Context context) {
            super(context);
            init(context);
        }

        public DrawView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        public DrawView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init(context);
        }

        private void init(Context context) {
            paint = new Paint();
            // attiva la possibilita' di interagire con il tocco dell'utente sullo schermo
            setFocusable(true);
            canvas = new Canvas();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            width = canvas.getWidth();
            height = canvas.getHeight();

            // recupero la grandezza di una delle sfere
            halfballsize = Math.round((BitmapFactory.decodeResource(getContext().getResources(),
                    R.mipmap.gray_circle)).getWidth() / 2);

            // seleziona riquadro di ritaglio mobile o mirino statico
            if(drawBallsOk) {
                drawBalls(canvas);
            } else {
                drawMirino(canvas);
            }
        }

        // funzione per l'utilizzo del mirino e il relativo ritaglio applicato all'immagine
        protected void drawMirino(Canvas canvas) {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);

            int left, top, right, bottom;
            // valuta la rotazione dello schermo
            if(width<height) {
                left = width / 5;
                right = width * 4 / 5;
                top = (height / 2) - ((right - left) / 2);
                bottom = (height / 2) + ((right - left) / 2);
            } else {
                top = height / 5;
                bottom = height * 4 / 5;
                left = (width / 2) - ((bottom - top) / 2);
                right = (width / 2) + ((bottom - top) / 2);
            }

            // gestione aspetti grafici
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setStrokeJoin(Paint.Join.ROUND);

            // disegna la linea e le fornisce un colore (rosso - trasparente) e uno spessore
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.parseColor("#55DB1255"));
            paint.setStrokeWidth(15);
            // riquadro designato come mirino
            canvas.drawRect(left, top, right, bottom, paint);

            // linee per decorare i bordi del mirino
            int bMir = 35;
            canvas.drawLine(right, height / 2, right + bMir, height / 2, paint);
            canvas.drawLine(left, height / 2, left - bMir, height / 2, paint);
            canvas.drawLine(width / 2, top, width / 2, top - bMir, paint);
            canvas.drawLine(width / 2, bottom, width / 2, bottom + bMir, paint);

            // il calcolo trasforma le dimensioni raccolte dall'anteprima in proporzioni
            // necessarie a ritagliare l'immagine finale
            UPcut = top;
            DOWNcut = bottom;
            LEFTcut = left;
            RIGHTcut = right;

            LEFTcut = LEFTcut/width;
            RIGHTcut = RIGHTcut/width;
            UPcut = UPcut/height;
            DOWNcut = DOWNcut/height;
        }

        // funzione per il ritaglio dell'immagine personalizzata tramite cursori
        protected void drawBalls(Canvas canvas) {
            // ripulisco lo schermo
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            // azzera il count per l'ID dei cursori
            count = 0;

            // rettangolo iniziale
            if(points[0] == null) {
                int X = (width/5)-halfballsize;
                int Y = (height/5)-halfballsize;
                int X2 = (width*4/5)-halfballsize;
                int Y2 = (height*4/5)-halfballsize;

                points[0] = new Point();
                points[0].x = X;
                points[0].y = Y;

                points[1] = new Point();
                points[1].x = X;
                points[1].y = Y2;

                points[2] = new Point();
                points[2].x = X2;
                points[2].y = Y2;

                points[3] = new Point();
                points[3].x = X2;
                points[3].y = Y;

                UPcut = Y;
                DOWNcut = Y2;
                LEFTcut = X;
                RIGHTcut = X2;

                // il calcolo trasforma le dimensioni raccolte dall'anteprima in proporzioni
                // necessarie a ritagliare l'immagine finale
                LEFTcut = LEFTcut/width;
                RIGHTcut = RIGHTcut/width;
                UPcut = UPcut/height;
                DOWNcut = DOWNcut/height;

                // ogni cursore cerchio viene dichiarato come ColorBall
                for (Point pt : points) {
                    colorballs.add(new ColorBall(getContext(), R.mipmap.gray_circle, pt));
                }

            }

            // point4 ritorna null quando non viene toccato lo schermo
            if(points[3]==null)
                return;
            int left, top, right, bottom;
            left = points[0].x;
            top = points[0].y;
            right = points[0].x;
            bottom = points[0].y;
            // sposta i lati del rettangolo a seconda della posizione dei punti
            for (int i = 1; i < points.length; i++) {
                left = left > points[i].x ? points[i].x:left;
                top = top > points[i].y ? points[i].y:top;
                right = right < points[i].x ? points[i].x:right;
                bottom = bottom < points[i].y ? points[i].y:bottom;
            }

            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setStrokeJoin(Paint.Join.ROUND);

            // disegna la linea e le fornisce un colore (rosso - trasparente) e uno spessore
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.parseColor("#55DB1255"));
            paint.setStrokeWidth(5);
            canvas.drawRect(
                    left + colorballs.get(0).getWidthOfBall() / 2,
                    top + colorballs.get(0).getWidthOfBall() / 2,
                    right + colorballs.get(2).getWidthOfBall() / 2,
                    bottom + colorballs.get(2).getWidthOfBall() / 2, paint);

            // disegno i cursori cerchio sul canvas
            BitmapDrawable bitmap = new BitmapDrawable();
            // disegna i cursori e i numeri blu
            // colore testo. evita inoltre la trasparenza delle balls dovuta alle linee rosse
            paint.setColor(Color.BLUE);
            paint.setTextSize(18);
            paint.setStrokeWidth(0);
            for (int i =0; i < colorballs.size(); i ++) {
                ColorBall ball = colorballs.get(i);
                canvas.drawBitmap(ball.getBitmap(), ball.getX(), ball.getY(),
                        paint);

                // scrive il numero della pallina accanto alla pallina stessa
                canvas.drawText("" + (i+1), ball.getX(), ball.getY(), paint);
            }
        }

        // eventi attivati dall'interazione dell'utente con lo schermo
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            // se e' selezionato il riquadro di ritaglio mobile le interazioni touch sono attive
            if(drawBallsOk) {
                int eventaction = event.getAction();

                int X = (int) event.getX();
                int Y = (int) event.getY();
                // spazio di lasco tra il detect touch delle sfere e del centro del riquadro
                ColorBall balls = colorballs.get(0);
                int lasco = balls.getWidthOfBall();

                switch (eventaction) {
                    // quando viene toccato lo schermo viene valutata la posizione del dito
                    case MotionEvent.ACTION_DOWN:
                        // valuta se il dito si trova sopra uno dei cerchi
                        balID = -1;
                        groupId = -1;
                        for (int i = colorballs.size() - 1; i >= 0; i--) {
                            ColorBall ball = colorballs.get(i);
                            // per far questo devo valutare se si trova all'interno dell'area
                            // dell'immagine scelta per fare da cursore del cerchio
                            int centerX = ball.getX() + ball.getWidthOfBall();
                            int centerY = ball.getY() + ball.getHeightOfBall();
                            paint.setColor(Color.CYAN);

                            // questo e' il calcolo effettivo basato su un cerchio di raggio pari
                            // a meta' della grandezza dell'immagine cursore
                            double radCircle = Math
                                    .sqrt((double) (((centerX - X) * (centerX - X)) + (centerY - Y)
                                            * (centerY - Y)));

                            if (radCircle < ball.getWidthOfBall()) {

                                balID = ball.getID();
                                if (balID == 1 || balID == 3) {
                                    groupId = 2;
                                    // annulla la possibilita' di muovere il riquadro intero
                                    centerClick = false;
                                } else {
                                    groupId = 1;
                                    // annulla la possibilita' di muovere il riquadro intero
                                    centerClick = false;
                                }
                                invalidate();
                                break;
                            }
                            invalidate();
                        }

                        int limitSx = 0;
                        int limitDx = 0;
                        int limitUp = 0;
                        int limitDw = 0;
                        // identifica gli estremi delle 4 sfere - tiene conto della loro inversione
                        if (colorballs.get(0).getX() > colorballs.get(3).getX()) {
                            limitDx = colorballs.get(0).getX();
                            limitSx = colorballs.get(3).getX();
                        } else {
                            limitDx = colorballs.get(3).getX();
                            limitSx = colorballs.get(0).getX();
                        }
                        if (colorballs.get(0).getY() > colorballs.get(1).getY()) {
                            limitUp = colorballs.get(1).getY();
                            limitDw = colorballs.get(0).getY();
                        } else {
                            limitUp = colorballs.get(0).getY();
                            limitDw = colorballs.get(1).getY();
                        }

                        // valuta se sta toccando all'interno del riquadro senza toccare le sfere
                        if (groupId == -1) {
                            if (X > (limitSx + lasco)) {
                                if (X < (limitDx - lasco)) {
                                    if (Y > (limitUp + lasco)) {
                                        if (Y < (limitDw - lasco)) {
                                            // attiva spostamento riquadro e segna la posizione
                                            // attuale del dito
                                            centerClick = true;
                                            actualX = X;
                                            actualY = Y;
                                        }
                                    }
                                }
                            }
                        }

                        break;

                    // valuta se il dito (una volta toccato lo schermo) si muove senza staccarsi
                    case MotionEvent.ACTION_MOVE:

                        if (balID > -1) {
                            // muove il cerchio cursore dove si muove il dito
                            colorballs.get(balID).setX(X);
                            colorballs.get(balID).setY(Y);

                            paint.setColor(Color.CYAN);

                            // e le altre sfere di conseguenza (per tenere il riquadro rettangolare)
                            if (groupId == 1) {
                                colorballs.get(1).setX(colorballs.get(0).getX());
                                colorballs.get(1).setY(colorballs.get(2).getY());
                                colorballs.get(3).setX(colorballs.get(2).getX());
                                colorballs.get(3).setY(colorballs.get(0).getY());
                            } else {
                                colorballs.get(0).setX(colorballs.get(1).getX());
                                colorballs.get(0).setY(colorballs.get(3).getY());
                                colorballs.get(2).setX(colorballs.get(3).getX());
                                colorballs.get(2).setY(colorballs.get(1).getY());
                            }

                            invalidate();
                        }

                        // muove tutte le sfere assieme se viene toccato il centro del riquadro
                        if (centerClick) {
                            // calcolo di quanto muovere ciscuna sfera
                            int moveX = X - actualX;
                            int moveY = Y - actualY;

                            // muovo ogni sfera del valore calcolato
                            for (int i = colorballs.size() - 1; i >= 0; i--) {
                                int ballId = i;
                                colorballs.get(ballId).setX(colorballs.get(ballId).getX() + moveX);
                                colorballs.get(ballId).setY(colorballs.get(ballId).getY() + moveY);
                            }
                            // aggiorno la posizione attuale del dito
                            actualX = X;
                            actualY = Y;

                        }

                        // evita che le palle escano dai lati dello schermo
                        for (int i = colorballs.size() - 1; i >= 0; i--) {
                            ColorBall ball = colorballs.get(i);
                            if (colorballs.get(i).getX() > width - ball.getWidthOfBall()) {
                                colorballs.get(i).setX(width - ball.getWidthOfBall());
                            } else if (colorballs.get(i).getX() < 0) {
                                colorballs.get(i).setX(0);
                            }
                            if (colorballs.get(i).getY() > height - ball.getHeightOfBall()) {
                                colorballs.get(i).setY(height - ball.getHeightOfBall());
                            } else if (colorballs.get(i).getY() < 0) {
                                colorballs.get(i).setY(0);
                            }
                        }

                        break;

                    // operazioni che vengono eseguite quando il dito si stacca dallo schermo
                    case MotionEvent.ACTION_UP:

                        // sistema per assegnare sempre il valore giusto al lato giusto anche
                        // se si incrociano e sostituiscono i pallini sullo schermo
                        RIGHTcut = 0;
                        LEFTcut = colorballs.get(0).getX();
                        UPcut = colorballs.get(0).getY();
                        DOWNcut = 0;
                        for (int i = colorballs.size() - 1; i >= 0; i--) {
                            if (colorballs.get(i).getX() > RIGHTcut) {
                                RIGHTcut = colorballs.get(i).getX();
                            }
                            if (colorballs.get(i).getX() < LEFTcut) {
                                LEFTcut = colorballs.get(i).getX();
                            }
                            if (colorballs.get(i).getY() > DOWNcut) {
                                DOWNcut = colorballs.get(i).getY();
                            }
                            if (colorballs.get(i).getY() < UPcut) {
                                UPcut = colorballs.get(i).getY();
                            }
                        }

                        // il calcolo trasforma le dimensioni raccolte dall'anteprima in proporzioni
                        // necessarie a ritagliare l'immagine finale
                        LEFTcut = (LEFTcut+halfballsize) / width;
                        RIGHTcut = (RIGHTcut+halfballsize) / width;
                        UPcut = (UPcut+halfballsize) / height;
                        DOWNcut = (DOWNcut+halfballsize) / height;

                        // disattiva un'eventuale focus sul riquadro di selezione per il movimento
                        // di massa di tutti i cursori - disattivando questo false si puo'
                        // cliccare prima l'interno del riquadro e poi un punto casuale al suo
                        // esterno per spostarlo senza doverlo trascinare - attualmente e'
                        // impostato per muoversi solo per trascinamento
                        centerClick = false;

                        break;
                }

                // aggiorna il canvas
                invalidate();
                return true;
            } else {
                // se e' selezionato il mirino le interazioni touch sono disattivate
                return false;
            }

        }

    }


    // classe interna per la gestione dei cursori
    public class ColorBall {

        Bitmap bitmap;
        Context mContext;
        Point point;
        int id;


        public ColorBall(Context context, int resourceId, Point point) {
            this.id = count++;
            bitmap = BitmapFactory.decodeResource(context.getResources(),
                    resourceId);
            mContext = context;
            this.point = point;
        }

        public int getWidthOfBall() {
            return bitmap.getWidth();
        }

        public int getHeightOfBall() {
            return bitmap.getHeight();
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public int getX() {
            return point.x;
        }

        public int getY() {
            return point.y;
        }

        public int getID() {
            return id;
        }

        public void setX(int x) {
            point.x = x;
        }

        public void setY(int y) {
            point.y = y;
        }
    }

}