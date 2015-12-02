package com.michele.appdegree;


import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;

public class MyLocation {

    // classe che comunica con il localizzatore GPS e NETWORK per individuare la posizione del
    // dispositivo piu' accurata possibile

    Timer timer1;
    LocationManager lm;
    LocationResult locationResult;
    boolean gps_enabled=false;
    boolean network_enabled=false;

    public Location lastLocationReceived = null;

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    public boolean getLocation(Context context, LocationResult result) {
        locationResult=result;

        // cerca di attivare tutti i segnali disponibili
        if(lm==null) {
            lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }
        try{
            gps_enabled=lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex){
        }
        try{
            network_enabled=lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex){
        }

        // controlla quali segnali sono attivi
        if(!gps_enabled && !network_enabled) {
            return false;
        }
        if(gps_enabled) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
        }
        if(network_enabled) {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
        }

        timer1=new Timer();
        timer1.schedule(new GetLastLocation(), 10000);

        return true;
    }

    // listener che recupera tutti gli update di posizione ricevuti dal gps
    LocationListener locationListenerGps = new LocationListener() {
        // quando riceve update li confronta con i valori gia' in suo possesso (se ne ha) e valuta
        // se sono migliori dei precedenti - i valori migliori vengono passati, gli altri cancellati
        public void onLocationChanged(Location newlocation) {
            // se la nuova locazione rilevata non e' nulla
            if(newlocation != null) {
                if (lastLocationReceived == null) {
                    // se non ci sono valori iniziali prende i primi come buoni
                    lastLocationReceived = newlocation;
                    locationResult.gotLocation(lastLocationReceived);
                } else {
                    if (isBetterLocation(newlocation, lastLocationReceived)) {
                        locationResult.gotLocation(newlocation);
                    } else {
                        locationResult.gotLocation(lastLocationReceived);
                    }
                }
            }
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    // stesso sopra ma per quanto riguarda il network
    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location newlocation) {
            if(newlocation != null) {
                if (lastLocationReceived == null) {
                    lastLocationReceived = newlocation;
                    locationResult.gotLocation(lastLocationReceived);
                } else {
                    if (isBetterLocation(newlocation, lastLocationReceived)) {
                        locationResult.gotLocation(newlocation);
                    } else {
                        locationResult.gotLocation(lastLocationReceived);
                    }
                }
            }
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    // prende l'ultima locazione disponibile, sia essa gps o network, e funge da switch tra i due
    // valori se uno o entrambi non sono disponibili
    class GetLastLocation extends TimerTask {
        @Override public void run() {
            Location net_loc=null, gps_loc=null;
            if(gps_enabled) {
                gps_loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

            if(network_enabled) {
                net_loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            // se ci sono entrambi i valori prendi l'ultimo
            if(gps_loc!=null && net_loc!=null) {
                if(gps_loc.getTime()>net_loc.getTime()) {
                    locationResult.gotLocation(gps_loc);
                } else {
                    locationResult.gotLocation(net_loc);
                }
                return;
            }

            // se c'è n'è solo uno dei due prendi l'altro
            if(gps_loc!=null) {
                locationResult.gotLocation(gps_loc);
                return;
            }

            if(net_loc!=null) {
                locationResult.gotLocation(net_loc);
                return;
            }

            // altrimenti (se entrambi non ci sono) ritorna null
            locationResult.gotLocation(null);
        }

    }

    public static abstract class LocationResult {
        public abstract void gotLocation(Location location);
    }

    // azzera tutti i valori quando viene chiusa la ricerca della locazione
    // (fa le veci di onDestroy)
    public void stopLocation() {
        if(timer1 != null) {
            timer1.cancel();
        }
        if(lm != null && locationListenerNetwork != null) {
            lm.removeUpdates(locationListenerNetwork);
        }
        if(lm != null && locationListenerGps != null) {
            lm.removeUpdates(locationListenerGps);
        }
    }


    /* Determina quando la nuova locazione rilevata e' piu' affidabile di quella gia' in possesso
        - location: la nuova locazione rilevata da valutare
        - currentBestLocation: l'attuale locazione rilevata, da confrontare con la nuova
       A differenza di getLastLocation questo metodo agisce solo sugli update della posizione
       inizialmente rilevata
    */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // se nessuna locazione e' stata ancora stimata allora prendi quella nuova come buona
            return true;
        }

        // controlla che la locazione rilevata sia nuova rispetto a quella gia' in possesso
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // Se la nuova locazione e' stata rilevata piu' di due minuti dopo l'attuale prendi come
        // piu' affidabile (nel frattempo l'utente si è mosso)
        // altrimenti scartala
        if (isSignificantlyNewer) {
            return true;
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Controllo il livello di accuratezza della nuova informazione rispetto alla vecchia
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // e controllo se le due info provengono dallo stesso provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // determino la qualita' dell'informazione in base alla cronologia di acquisizione e
        // all'accuratezza dell'informazione disponibile
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    // booleano che controlla se le due info hanno stessa origine (provider)
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}