package com.oo0o0o.colorphoria.reusableuserinterface;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * Created by oo0o0o on 9/29/2017.
 */

public class PositionImageView extends AppCompatImageView {
    private int position;

    public PositionImageView(Context context) {
        super(context);
    }

    public PositionImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PositionImageView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
