package com.michele.appdegree.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import com.michele.appdegree.mainNotification;
import com.michele.fragmentexample.R;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

/**
 * Created by mattia on 29/12/15.
 */
public class JsonAdapter extends SimpleAdapter {

    final mainNotification mContext;

    public JsonAdapter(Context context, mainNotification fragment, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to){
        super(context, data, resource, from, to);

        this.mContext=fragment;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        // here you let SimpleAdapter built the view normally.
        View v = super.getView(position, convertView, parent);

        final int pos = position;

        // Then we get reference for Picasso
        ImageView img = (ImageView) v.getTag();
        img = null;
        if(img == null){
            img = (ImageView) v.findViewById(R.id.icon);
            v.setTag(img); // <<< THIS LINE !!!!
        }

        // get the url from the data you passed to the `Map`
        String url = "http://esamiuniud.altervista.org/tesi/img/"+((Map<String, String>)getItem(position)).get("nome")+".jpg";
        // do Picasso
        Picasso.with(v.getContext()).cancelRequest(img);
        Picasso.with(v.getContext()).load(url).placeholder(R.drawable.picasso_loading).fit().centerCrop().into(img);

        String letta = ((Map<String, String>) getItem(position)).get("letta");

        if (letta.equals("0")) {
            v.setBackgroundColor(v.getResources().getColor(R.color.notification_unread_light));
        } else {
            v.setBackgroundColor(v.getResources().getColor(R.color.background_white));
        }

        String aperta = ((Map<String, String>) getItem(position)).get("aperta");

        // get the url from the data you passed to the `Map`
        String url_icon = "http://esamiuniud.altervista.org/tesi/icon/";

        ImageView icon = (ImageView) v.getTag();
        icon = (ImageView) v.findViewById(R.id.icon2);
        v.setTag(icon);

        if(aperta.equals("1")){
            // do Picasso
            Picasso.with(v.getContext()).load(url_icon+"check_new.png").fit().centerCrop().into(icon);
        }
        else{
            // do Picasso
            Picasso.with(v.getContext()).load(url_icon+"times_new.png").fit().centerCrop().into(icon);
        }

        Button info = (Button) v.findViewById(R.id.btnInfo);
        Button continua = (Button) v.findViewById(R.id.btnContinua);
        Button chiudi = (Button) v.findViewById(R.id.btnChiudi);

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContext.notificationInfo(pos);
            }
        });

        continua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContext.notificationContinua(pos);
            }
        });

        chiudi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContext.notificationChiudi(pos);
            }
        });

        // return the view
        return v;
    }
}
