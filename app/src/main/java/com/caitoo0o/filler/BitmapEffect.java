package com.caitoo0o.filler;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Button;

/**
 * Created by Cait on 7/31/2016.
 */
public abstract class BitmapEffect {

    BitmapEffect() {}

    public abstract boolean resetRequired();

    public abstract Bitmap applyEffect(Bitmap bm);


}
