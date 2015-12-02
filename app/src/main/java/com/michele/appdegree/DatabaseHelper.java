package com.michele.appdegree;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.text.MessageFormat;


public class DatabaseHelper extends SQLiteOpenHelper {

    // classe che gestisce il database SQLite

    // nome database
    private static final String DATABASE_NAME = "dataAD.db";

    // primo update
    private static final int SCHEMA_VERSION = 1;

    // creazione database
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA_VERSION);
    }

    // onCreate viene inizializzato solo se il database non esiste e il suo argomento e' il
    // database appena creato
    public void onCreate(SQLiteDatabase db) {
        // string da passare a sql per la creazione della tabella
        String sql = "CREATE TABLE {0} ({1} INTEGER PRIMARY KEY AUTOINCREMENT," +
                " {2} TEXT NOT NULL,{3} INTEGER NOT NULL,{4} INTEGER NOT NULL," +
                " {5} TEXT NOT NULL,{6} TEXT NOT NULL,{7} TEXT NOT NULL,{8} TEXT NOT NULL," +
                " {9} FLOAT NOT NULL,{10} TEXT NOT NULL, {11} FLOAT NOT NULL," +
                " {12} INTEGER NOT NULL,{13} BOOLEAN NOT NULL );";
        // creazione della tabella fornendo String, nome tabella e nome colonne (_ID e' di default)
        db.execSQL(MessageFormat.format(sql, ImageTable.TABLE_NAME, ImageTable._ID,
                ImageTable.NOME, ImageTable.WIDTH, ImageTable.HEIGHT, ImageTable.LATITUDE,
                ImageTable.LONGITUDE, ImageTable.ADDRESS, ImageTable.COMPASSOBS,
                ImageTable.DEGREEOBS, ImageTable.DIRECTIONSUB, ImageTable.DEGREESUB,
                ImageTable.DISTANCE, ImageTable.TRANSFERRED));

    }

    // la versione inserita SCHEMA_VERSION differisce dalla precedente viene avviato subito onUpdate
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // aggiorno stato immagine trasferita
    public void updateTransferred(Long idFoto, boolean transferred) {
        // recupero database
        SQLiteDatabase db = this.getWritableDatabase();
        // creo il contenitore della nuove informazioni
        ContentValues v = new ContentValues();
        // where
        String where="_ID="+idFoto;
        v.put(ImageTable.TRANSFERRED, transferred);
        db.update(ImageTable.TABLE_NAME,v,where,null);
        db.close();
    }


    // void per l'inerimento dei dati
    public long insertImageData(String nome, Integer width, Integer height, String latitude,
                                 String longitude, String address, String compass, Float degree,
                                 String direction, Float directDegree, Integer distanza,
                                 Boolean tranferred) {
        // ritorno id
        long id;
        // recupero database
        SQLiteDatabase db = this.getWritableDatabase();
        // creo il contenitore della nuove informazioni
        ContentValues v = new ContentValues();
        // inserisco le informazioni nei loro campi
        v.put(ImageTable.NOME, nome);
        v.put(ImageTable.WIDTH, width);
        v.put(ImageTable.HEIGHT, height);
        v.put(ImageTable.LATITUDE, latitude);
        v.put(ImageTable.LONGITUDE, longitude);
        v.put(ImageTable.ADDRESS, address);
        v.put(ImageTable.COMPASSOBS, compass);
        v.put(ImageTable.DEGREEOBS, degree);
        v.put(ImageTable.DIRECTIONSUB, direction);
        v.put(ImageTable.DEGREESUB, directDegree);
        v.put(ImageTable.DISTANCE, distanza);
        v.put(ImageTable.TRANSFERRED, tranferred);
        // inserisco le informazioni del contenitore nel database
        id=db.insert(ImageTable.TABLE_NAME, null, v);
        db.close();

        return id;
    }

    // cursore per selezionare gli elementi della tabella durante le operazioni sulla stessa
    public Cursor getImageInfo() {

        return (getReadableDatabase().query(
                ImageTable.TABLE_NAME,
                ImageTable.COLUMNS,
                null,
                null,
                null,
                null,
                ImageTable.NOME));
    }

    // interfaccia per creare le varie colonne della tabella
    public interface ImageTable extends BaseColumns {
        String TABLE_NAME = "InfoImages";

        String NOME = "nome";
        String HEIGHT = "height";
        String WIDTH = "width";
        String LATITUDE = "latitude";
        String LONGITUDE = "longitude";
        String ADDRESS = "indirizzo";
        String COMPASSOBS = "bussolaObs";
        String DEGREEOBS = "gradiObs";
        String DIRECTIONSUB = "directionSub";
        String DEGREESUB = "gradiSub";
        String DISTANCE = "distance";
        String TRANSFERRED = "transferred";

        String[] COLUMNS = new String[]
                { _ID, NOME, WIDTH, HEIGHT, LATITUDE, LONGITUDE, ADDRESS, COMPASSOBS, DEGREEOBS,
                        DIRECTIONSUB, DEGREESUB, DISTANCE, TRANSFERRED };
    }

}
