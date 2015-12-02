package com.michele.appdegree;

import android.app.Application;

/**
 * Created by mattia on 01/12/15.
 */
public class globals extends Application {

    private String id;
    private final static String baseServerUrl = "http://esamiuniud.altervista.org/";
    private final static String homeServerUrl = "encimg/";
    private final static String passwordServerUrl = "dim-pass.php";
    private final static String uploadServerUrl = "inserita-foto.php";

    public String getId() {
        return id;
    }

    public void setId(String idR) {
        id = idR;
    }

    /*
    public static String getHomeServerUrl() {
        return baseServerUrl + homeServerUrl;
    }
    public static String getPasswordServerUrl() {
        return baseServerUrl + homeServerUrl + passwordServerUrl;
    }
    public static String getUploadServerUrl() {
        return globals.getHomeServerUrl() + "/" + uploadServerUrl;
    }
    */

}
