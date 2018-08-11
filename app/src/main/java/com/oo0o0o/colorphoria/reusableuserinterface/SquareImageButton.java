package com.oo0o0o.colorphoria.reusableuserinterface;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.widget.ImageButton;

/**
 * Created by oo0o0o on 3/26/2017.
 */

public class SquareImageButton extends AppCompatImageButton {
    public SquareImageButton(Context context) {
        super(context);
    }

    public SquareImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
//        // Pick the smaller of the two dimensions
//        int size = width > height ? height : width;
        // Always use the height. If wanting to change this use above commented code
        setMeasuredDimension(height, height);

    }
}