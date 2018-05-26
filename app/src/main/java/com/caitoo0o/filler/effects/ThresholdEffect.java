package com.caitoo0o.filler.effects;

import android.graphics.Bitmap;

import com.caitoo0o.filler.effecthelpers.ColorHelper;
import com.caitoo0o.filler.effecthelpers.EffectTypes;

import java.util.ArrayList;

/**
 * Created by caitoo0o on 4/12/2018.
 */

public class ThresholdEffect implements BitmapEffect {

    public Bitmap applyEffect(Bitmap bm, double ctrl, int randomSeed, ArrayList<Integer> colorPalette, int rotationAngle) {
        for (int y = 0; y < bm.getHeight(); y++) {
            for (int x = 0; x < bm.getWidth(); x++) {
                int color = ColorHelper.getPixelColor(bm, x, y);
                int red = (color >> 16) & 0xFF;
                int green = (color >> 8) & 0xFF;
                int blue = (color >> 0) & 0xFF;
                int average = (red + blue + green) / 3;
                if (average <= (255 * ctrl)) {
                    ColorHelper.setPixelColor(bm, x, y, 0x000000);
                } else {
                    ColorHelper.setPixelColor(bm, x, y, 0xffffff);
                }
            }
        }
        return bm;
    }
}
