package com.caitoo0o.filler.effects;

import android.graphics.Bitmap;

import com.caitoo0o.filler.effecthelpers.BitmapWrapper;
import com.caitoo0o.filler.effecthelpers.EffectTypes;

import java.util.ArrayList;

/**
 * Created by caitoo0o on 4/12/2018.
 */

public class ResetEffect implements BitmapEffect {

    public void applyEffect(Bitmap bm, double ctrl, int randomSeed, ArrayList<Integer> colorPalette, int rotationAngle) {
        bm = bm.copy(bm.getConfig(), true);
    }
}
