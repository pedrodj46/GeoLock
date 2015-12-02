package com.michele.appdegree.adapters;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class SquareLayout extends LinearLayout {
    // NON MODIFICARE: mScale gestisce il ratio del layout quadrato, se modificato viene deformato
    //                 l'aspetto di quadrato equilatero e di conseguenza l'aspetto del menu
    private final double mScale = 1.0;

    public SquareLayout(Context context) {
        super(context);
    }

    public SquareLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // funzione che gestice altezza e larghezza del layout in base alle misure dello schermo
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        /*if (width > (int)((mScale * height) + 0.5)) {
            width = (int)((mScale * height) + 0.5);
        } else {
            height = (int)((width / mScale) + 0.5);
        }*/

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        );
    }
}

