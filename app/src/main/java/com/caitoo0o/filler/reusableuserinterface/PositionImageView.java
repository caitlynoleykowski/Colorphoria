package com.caitoo0o.filler.reusableuserinterface;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by caitoo0o on 9/29/2017.
 */

public class PositionImageView extends ImageView {
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

    public PositionImageView(Context context, AttributeSet attrs, int defStyleAttr,
                             int defStyleRes){
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
