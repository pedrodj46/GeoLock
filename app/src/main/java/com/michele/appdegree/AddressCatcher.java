package com.michele.appdegree;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AddressCatcher {

    // questa classe si occupa del calcolo dell'indirizzo in base alle coordinate rilevate
    // se le coordinate non sono nelle vicinanze di una strada segnata nel database di Google Maps
    // restituisce null

    // trasforma le coordinate geografiche in indirizzi
    public String Address (Context context, String latitude, String longitude) {
        String _Location = null;
        Double mLatitude = Double.parseDouble(latitude);
        Double mLongitude = Double.parseDouble(longitude);

        // attivo geocoder
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            // recupera un indirizzo dalle coordinate fornite
            List<Address> listAddresses = geocoder.getFromLocation(mLatitude, mLongitude, 1);
            if(null!=listAddresses&&listAddresses.size()>0){
                String address = listAddresses.get(0).getAddressLine(0);
                String city = listAddresses.get(0).getLocality();
                String country = listAddresses.get(0).getCountryName();
                String postalCode = listAddresses.get(0).getPostalCode();

                _Location = address+" "+postalCode+" "+city+" "+country;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // ritorna una stringa con l'indirizzo trovato
        return _Location;
    }

}
