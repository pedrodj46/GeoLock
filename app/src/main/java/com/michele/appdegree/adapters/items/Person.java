package com.michele.appdegree.adapters.items;

// Created by Michele Zardetto

public class Person {

    // questa classe si occupa di contenere e gestire tutte le informazioni da inviare al server
    // per il loro storaggio e il loro successivo utilizzo

    private String name;
    private String latitude;
    private String longitude;
    private String address;
    private String direxObs;
    private String degree;
    private String direzSub;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDirexObs() {
        return direxObs;
    }

    public void setDirexObs(String direxObs) {
        this.direxObs = direxObs;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getDirezSub() {
        return direzSub;
    }

    public void setDirezSub(String direzSub) {
        this.direzSub = direzSub;
    }

    @Override
    public String toString() {
        return "Person [name=" + name + ", latitude=" + latitude + ", longitude="
                + longitude + ", address=" + address + ", direzioneUtente=" + direxObs +
                ", gradi=" + degree + ", direzioneSoggetto=" + direzSub + "]";
    }

}