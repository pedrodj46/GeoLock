package com.michele.appdegree;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.michele.fragmentexample.R;

public class buttonsFragment extends Fragment {

    // questo Fragment si occupa di percepire le interazioni tra l'utente e i tasti del menu
    // iniziale indicando all'Activity quale Fragment attivare

    Boolean GpsEnabled = false;

    // inizio interfaccia di collegamento con la main activity
    ToolbarListener activityCallback;

    public interface ToolbarListener {
        public void onButtonClick();
        public void onButtonClick02();
        public void onButtonClick04();
        public void onButtonClick05();
        public void onButtonClick06();
        public void onButtonClick07();
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.buttons,
                container, false);

        // pulsanti dell'interfaccia
        final LinearLayout button =
                (LinearLayout) view.findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkLocationEnabled(getActivity());
                // impedisce l'avvio della camera se il GPS non e' attivo
                if (GpsEnabled) {
                    buttonClicked(v);
                }
            }
        });

        final LinearLayout button02 =
                (LinearLayout) view.findViewById(R.id.button2);
        button02.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buttonClicked02(v);
            }
        });

        final LinearLayout button04 =
                (LinearLayout) view.findViewById(R.id.button4);
        button04.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buttonClicked04(v);
            }
        });

        final LinearLayout button05 =
                (LinearLayout) view.findViewById(R.id.button5);
        button05.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buttonClicked05(v);
            }
        });

        final LinearLayout button06 =
                (LinearLayout) view.findViewById(R.id.button06);
        button06.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buttonClicked06(v);
            }
        });

        final LinearLayout button07 =
                (LinearLayout) view.findViewById(R.id.button07);
        button07.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buttonClicked07(v);
            }
        });

        return view;
    }

    // onClickListener attivati dall'onCreateView
    public void buttonClicked (View view) {
        activityCallback.onButtonClick();
    }

    public void buttonClicked02 (View view) {
        activityCallback.onButtonClick02();
    }

    public void buttonClicked04 (View view) {
        activityCallback.onButtonClick04();
    }

    public void buttonClicked05 (View view) {
        activityCallback.onButtonClick05();
    }

    public void buttonClicked06 (View view) {
        activityCallback.onButtonClick06();
    }

    public void buttonClicked07 (View view) {
        activityCallback.onButtonClick07();
    }

    // verifica se nel telefono e' attivo il segnale GPS
    // il controllo non viene fatto anche per il segnale NETWORK per permettere l'uso
    // della camera anche in assenza di segnale Wifi o GSM
    public void checkLocationEnabled(Context context) {
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        // se il GPS non e' attivato restituisce una finestra di dialogo
        if(!gps_enabled) {
            GpsEnabled = false;
            // finestra di notifica
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            // i testi possono essere cambiati in velues/styles.xml
            dialog.setMessage(context.getResources().getString(R.string.gps_network_not_enabled));

            dialog.setNegativeButton(context.getResources().getString
                    (R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent =
                            new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    getActivity().startActivity(myIntent);
                }
            });

            dialog.setPositiveButton(context.getString(R.string.Cancel),
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                }
            });

            dialog.show();
        } else if(gps_enabled) {
            GpsEnabled = true;
        }
    }

}