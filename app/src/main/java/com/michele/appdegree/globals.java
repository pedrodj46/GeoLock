package com.michele.appdegree;

import android.app.Application;

/**
 * Created by mattia on 01/12/15.
 */
public class globals extends Application {

    private String id;
    private String idNotifica;
    private Boolean bool;
    //private final static String baseServerUrl = "http://esamiuniud.altervista.org/";

    public String getId() {
        return id;
    }

    public void setId(String idR) {
        id = idR;
    }

    public String getIdNotifica() {
        return idNotifica;
    }

    public void setIdNotifica(String idN) {
        idNotifica = idN;
    }

    public Boolean getIsFromNotifica() {
        return bool;
    }

    public void setIsFromNotifica(Boolean isFromNotifica) {
        bool = isFromNotifica;
    }

    /*
    public static String getServerUrl() {
        return baseServerUrl;
    }
    */

}
