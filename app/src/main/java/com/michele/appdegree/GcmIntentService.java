package com.michele.appdegree;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.michele.fragmentexample.R;

import org.json.JSONObject;

/**
 * Created by mattia on 03/12/15.
 */
public class GcmIntentService extends IntentService {

    public GcmIntentService() {
        super("GcmIntentService");
    }
    public static final String TAG = "GeoLock";

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {

            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {

            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {

            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                try{
                    JSONObject json;
                    json=new JSONObject(extras.getString("message"));
                    sendNotification(json);

                    Log.d(TAG, "Received: " + extras.toString());
                }
                catch (Exception e){
                    Log.d("Errore", "errore "+e);
                }
            }
        }

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(JSONObject msg) {

        try{
            NotificationManager mNotificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);

            Intent intent = new Intent(this, afterNotification.class);

            intent.putExtra("id_foto", msg.getString("idFoto"));
            intent.putExtra("id_notifica", msg.getString("idNotifica"));
            intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

            //PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
            //        intent, PendingIntent.FLAG_UPDATE_CURRENT);

            PendingIntent contentIntent = TaskStackBuilder.create(this)
                    .addParentStack(afterNotification.class)
                    .addNextIntent(intent)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder mBuilder =
                    (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle(msg.getString("titolo"))
                            .setContentText(msg.getString("testo"));


            // cancella dopo click
            mBuilder.setAutoCancel(true);

            // vibrazione
            mBuilder.setVibrate(new long[] { 500, 300, 500, 300, 500 });

            // led
            mBuilder.setLights(Color.RED, 3000, 3000);

            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(1, mBuilder.build());

        }
        catch (Exception e){
            Log.d("Errore", "errore "+e);
        }
    }

}
