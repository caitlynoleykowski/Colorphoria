package com.caitoo0o.filler.effects;

import android.graphics.Bitmap;

import com.caitoo0o.filler.effecthelpers.BitmapWrapper;
import com.caitoo0o.filler.effecthelpers.EffectTypes;

import java.util.ArrayList;

/**
 * Created by caitoo0o on 4/12/2018.
 */

public class ResetEffect implements BitmapEffect {

    public Bitmap applyEffect(Bitmap bm, double ctrl, int randomSeed, ArrayList<Integer> colorPalette, int rotationAngle) {
        return bm; // Yeah this is weird but it's because the rotation angle is in the BitmapWrapper
    }
}
