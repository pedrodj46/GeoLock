package com.michele.appdegree.utilities;

// Created by Michele Zardetto

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import com.michele.appdegree.DatabaseHelper;
import com.michele.appdegree.adapters.items.PhotoItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class PhotoGalleryImageProvider {

    // questa classe compie l'effettiva azione di recupero delle immagini dalla memoria

    // recupera i percorsi alle immagini disponibili in memoria
    public static List<PhotoItem> getAlbumThumbnails(Context context){

        // arraylist necessario a immagazzinare le info riguardanti le foto
        ArrayList<PhotoItem> result = null;

        // percorso alla cartella dell'applicazione contente le foto scattate tramite l'app stessa
        String path = Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_PICTURES).toString()+"/AppDegreeTrial/";
        File f = new File(path);
        // controllo riguardo l'esistenza della cartella contente le foto
        if(f.isDirectory()) {
            // recupero dei file contenuti nella cartella
            File file[] = f.listFiles();

            int lunghezzaArray = 0;
            // recupero il database
            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            // recupero il cursore
            Cursor c = databaseHelper.getImageInfo();

            for (int i = 0; i < file.length; i++) {
                for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    String imageName = c.getString(1)+".jpg";

                    // confronto i file immagine con le info contenute nel database
                    if(imageName.equals(file[i].getName())) {
                        // se l'immagine e' registrata allora viene attivato questo booleano
                        lunghezzaArray++;
                    }

                }

            }

            // inizializzazione dell'arraylist a seconda del numero di immagini trovate
            result = new ArrayList<PhotoItem>(lunghezzaArray);
            for (int i = 0; i < file.length; i++) {
                for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    String imageName = c.getString(1) + ".jpg";

                    // confronto i file immagine con le info contenute nel database
                    if (imageName.equals(file[i].getName())) {
                        // per ogni file viene ricavato il percorso Uri
                        Uri fullImageUri = Uri.fromFile(file[i]);

                        PhotoItem newItem = new PhotoItem(fullImageUri);
                        // una volta processati da PhotoItem vengono caricati nell'arraylist
                        result.add(newItem);
                    }
                }
            }
        }

        return result;
    }

}