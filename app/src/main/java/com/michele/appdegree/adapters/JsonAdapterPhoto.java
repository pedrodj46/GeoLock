package com.michele.appdegree.adapters;

import android.content.Context;
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
public class JsonAdapterPhoto extends SimpleAdapter {

    public JsonAdapterPhoto(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to){
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

        // return the view
        return v;
    }
}
