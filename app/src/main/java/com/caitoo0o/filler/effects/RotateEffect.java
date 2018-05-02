package com.caitoo0o.filler.effects;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.caitoo0o.filler.effecthelpers.BitmapWrapper;
import com.caitoo0o.filler.effecthelpers.EffectTypes;

import java.util.ArrayList;

/**
 * Created by caitoo0o on 4/12/2018.
 */

public class RotateEffect implements BitmapEffect {

    public void applyEffect(Bitmap bm, double ctrl, int randomSeed, ArrayList<Integer> colorPalette, int rotationAngle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationAngle);
        bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
    }
}
