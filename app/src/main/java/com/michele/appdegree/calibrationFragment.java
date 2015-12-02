package com.michele.appdegree;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.michele.fragmentexample.R;


public class calibrationFragment extends Fragment implements SensorEventListener {

    // questo Fragment si occupa della calibrazione della bussola

    // variabili bussola
    private ImageView mPointer;
    private float currentDegree = 0f;
    private SensorManager mSensorManager;


    public calibrationFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calibration, container, false);

        // bussola disegnata sullo schermo
        mPointer = (ImageView) view.findViewById(R.id.pointer);

        // sensore per l'utilizzo della bussola
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager
                .getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        if(getActivity() == null) {
            return;
        } else {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        if(getActivity() == null) {
            return;
        } else {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        }
    }

    // default void per la bussola - gestisce la rotazione della bussola visualizzata sullo schermo
    @Override
    public void onSensorChanged(SensorEvent event) {
        float degree = Math.round(event.values[0]);

        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
        ra.setDuration(210);
        ra.setFillAfter(true);
        mPointer.startAnimation(ra);
        currentDegree = -degree;
    }

    // default void per la bussola
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
