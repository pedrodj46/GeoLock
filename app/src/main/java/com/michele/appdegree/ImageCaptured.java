package com.michele.appdegree;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.michele.fragmentexample.R;


public class ImageCaptured extends android.support.v4.app.Fragment {

    // questo fragment gestisce l'immagine acquisita dalla camera e chiede all'utente se confermarla
    // o procedere con un nuovo scatto eliminato quello attuale

    View rootView;
    ImageView image;
    Button button;
    Button button2;

    static Bitmap mImage;


    // interfaccia di collegamento con la main activity
    ToolbarListener activityCallback;

    public interface ToolbarListener {
        public void onPictureAccepted();
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

    public void pictureAccepted() {
        activityCallback.onPictureAccepted();
    }
    // fine interfaccia di collegamento con la main activity



    public ImageCaptured() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_image_captured, container, false);

        addListenerOnButton();

        return rootView;
    }

    public void addListenerOnButton() {
        image = (ImageView) rootView.findViewById(R.id.image_preview);
        image.setImageBitmap(mImage);

        button = (Button) rootView.findViewById(R.id.done_button);

        button2 = (Button) rootView.findViewById(R.id.back_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pictureAccepted();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pictureRejected();
            }
        });

    }

    public void pictureRejected() {
        CameraFragment camera = new CameraFragment();
        recycle();
        camera.Recycle();
        // torna indietro - forza back button
        getFragmentManager().popBackStack();
    }


    public void displayImage(Bitmap mBitmap) {
        mImage = null;
        mImage = mBitmap;
    }

    public void recycle() {
        if (mImage != null && !mImage.isRecycled()) {
            mImage.recycle();
        }
    }

}
