package com.michele.appdegree;

import android.app.Application;

/**
 * Created by mattia on 01/12/15.
 */
public class globals extends Application {

    private String id;
    private String idFoto;
    //private final static String baseServerUrl = "http://esamiuniud.altervista.org/";

    public String getId() {
        return id;
    }

    public void setId(String idR) {
        id = idR;
    }

    public String getIdFoto() {
        return idFoto;
    }

    public void setIdFoto(String idF) {
        idFoto = idF;
    }

    /*
    public static String getServerUrl() {
        return baseServerUrl;
    }
    */

}
