package com.michele.appdegree.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import com.michele.fragmentexample.R;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

/**
 * Created by mattia on 29/12/15.
 */
public class JsonAdapter extends SimpleAdapter {

    public JsonAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to){
        super(context, data, resource, from, to);
    }

    public View getView(int position, View convertView, ViewGroup parent){
        // here you let SimpleAdapter built the view normally.
        View v = super.getView(position, convertView, parent);

        // Then we get reference for Picasso
        ImageView img = (ImageView) v.getTag();
        if(img == null){
            img = (ImageView) v.findViewById(R.id.icon);
            v.setTag(img); // <<< THIS LINE !!!!
        }

        // get the url from the data you passed to the `Map`
        String url = "http://esamiuniud.altervista.org/tesi/img/"+((Map<String, String>)getItem(position)).get("nome")+".jpg";
        // do Picasso
        Picasso.with(v.getContext()).load(url).fit().centerCrop().into(img);

        String fragment = ((Map<String, String>)getItem(position)).get("fragment");

        if(fragment.equals("notification")) {
            String letta = ((Map<String, String>) getItem(position)).get("letta");

            if (letta.equals("0")) {
                v.setBackgroundColor(Color.RED);
            } else {
                v.setBackgroundColor(Color.GRAY);
            }

            String aperta = ((Map<String, String>) getItem(position)).get("aperta");

            // get the url from the data you passed to the `Map`
            String url_icon = "http://esamiuniud.altervista.org/tesi/icon/";

            ImageView icon = (ImageView) v.getTag();
            icon = (ImageView) v.findViewById(R.id.icon2);
            v.setTag(icon);

            if(aperta.equals("1")){
                // do Picasso
                Picasso.with(v.getContext()).load(url_icon+"check.png").fit().centerCrop().into(icon);
            }
            else{
                // do Picasso
                Picasso.with(v.getContext()).load(url_icon+"times.png").fit().centerCrop().into(icon);
            }
        }

        // return the view
        return v;
    }
}
