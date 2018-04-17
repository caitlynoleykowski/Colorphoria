package com.caitoo0o.filler.effects;

import android.graphics.Bitmap;

import com.caitoo0o.filler.effecthelpers.BitmapWrapper;
import com.caitoo0o.filler.effecthelpers.EffectTypes;

import java.util.ArrayList;

/**
 * Created by caitoo0o on 4/12/2018.
 */

public class ThresholdEffect implements BitmapEffect {

    EffectTypes type = EffectTypes.THRESHOLD;

    public void applyEffect(Bitmap bm, double ctrl, int randomSeed, ArrayList<Integer> colorPalette, int rotationAngle) {
        for (int y = 0; y < bm.getHeight(); y++) {
            for (int x = 0; x < bm.getWidth(); x++) {
                int color = BitmapWrapper.getPixelColor(bm, x, y);
                int red = (color >> 16) & 0xFF;
                int green = (color >> 8) & 0xFF;
                int blue = (color >> 0) & 0xFF;
                int average = (red + blue + green) / 3;
                if (average <= (255 * ctrl)) {
                    BitmapWrapper.setPixelColor(bm, x, y, 0x000000);
                } else {
                    BitmapWrapper.setPixelColor(bm, x, y, 0xffffff);
                }
            }
        }
    }
}
