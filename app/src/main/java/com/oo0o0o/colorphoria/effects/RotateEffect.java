package com.oo0o0o.colorphoria.effects;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.oo0o0o.colorphoria.effecthelpers.BitmapWrapper;
import com.oo0o0o.colorphoria.effecthelpers.EffectTypes;

import java.util.ArrayList;

/**
 * Created by oo0o0o on 4/12/2018.
 */

public class RotateEffect implements BitmapEffect {

    public Bitmap applyEffect(Bitmap bm, double ctrl, int randomSeed, ArrayList<Integer> colorPalette, int rotationAngle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationAngle);
        return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
    }
}
