package com.caitoo0o.filler.effects;

import android.graphics.Bitmap;

import com.caitoo0o.filler.effecthelpers.EffectTypes;

import java.util.ArrayList;

/**
 * Created by caitoo0o on 4/14/2018.
 */

public interface BitmapEffect {
    void applyEffect(Bitmap bm, double ctrl, int randomSeed, ArrayList<Integer> colorPalette, int rotationAngle);
}
