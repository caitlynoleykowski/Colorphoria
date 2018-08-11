package com.oo0o0o.colorphoria.reusableuserinterface;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;

/**
 * Created by oo0o0o on 12/28/2017.
 */

public class SmallerSquareImageButton extends AppCompatImageButton {
    public SmallerSquareImageButton(Context context) {
        super(context);
    }

    public SmallerSquareImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SmallerSquareImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        // Pick the smaller of the two dimensions
        int size = width > height ? height : width;
        setMeasuredDimension(size, size);
    }
}