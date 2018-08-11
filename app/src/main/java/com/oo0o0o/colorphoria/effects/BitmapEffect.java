package com.oo0o0o.colorphoria.effects;

import android.graphics.Bitmap;

import com.oo0o0o.colorphoria.effecthelpers.EffectTypes;

import java.util.ArrayList;

/**
 * Created by oo0o0o on 4/14/2018.
 */

public interface BitmapEffect {
    Bitmap applyEffect(Bitmap bm, double ctrl, int randomSeed, ArrayList<Integer> colorPalette, int rotationAngle);
}
