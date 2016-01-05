package com.michele.appdegree;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.michele.fragmentexample.R;

/**
 * Created by mattia on 04/01/16.
 */
public class notificationClose extends Fragment {

    String idN;
    String messaggio;

    ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View baseView = inflater.inflate(R.layout.close_notification, container, false);

        final EditText messaggioEdit = (EditText) baseView.findViewById(R.id.messaggio);
        //messaggio = messaggioEdit.getText().toString();
        Button btnInvia = (Button) baseView.findViewById((R.id.btnInvia));
        btnInvia.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                messaggio = messaggioEdit.getText().toString();
                btnInviaClicked();
            }
        });

        return baseView;
    }

    public void notificationID(String idNotification){
        idN = idNotification;
    }

    public void btnInviaClicked(){

        Log.d("messaggio", messaggio);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Attendere...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        new SendData().execute();
    }

    public class SendData extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            String result="0";

            if(!messaggio.equals("")) {
                ServerConnection closeNotification = new ServerConnection();
                result = closeNotification.closeNotification(idN, messaggio);
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();

            if(result.equals("0")){
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setTitle("Attenzione!");
                alertDialog.setMessage("Inserire una motivazione per chiudere la notifica!");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
            else{
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setTitle("Notifica chiusa");
                alertDialog.setMessage("Hai chiuso correttamente la notifica");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                getFragmentManager().popBackStack();
                            }
                        });
                alertDialog.show();
            }
        }
    }

}
