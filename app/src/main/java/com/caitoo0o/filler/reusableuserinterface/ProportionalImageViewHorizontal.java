package com.caitoo0o.filler.reusableuserinterface;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * Created by caitoo0o on 11/24/2017.
 */
public class ProportionalImageViewHorizontal extends AppCompatImageView {

    public ProportionalImageViewHorizontal(Context context) {
        super(context);
    }

    public ProportionalImageViewHorizontal(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProportionalImageViewHorizontal(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable d = getDrawable();
        if (d != null) {
            int h = MeasureSpec.getSize(heightMeasureSpec);
            int w = h * d.getIntrinsicWidth() / d.getIntrinsicHeight();
            setMeasuredDimension(w, h);
        }
        else super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
