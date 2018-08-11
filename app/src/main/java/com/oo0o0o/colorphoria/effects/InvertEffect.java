package com.oo0o0o.colorphoria.effects;

import android.graphics.Bitmap;

import com.oo0o0o.colorphoria.effecthelpers.ColorHelper;
import com.oo0o0o.colorphoria.effecthelpers.EffectTypes;

import java.util.ArrayList;

/**
 * Created by oo0o0o on 4/12/2018.
 */

public class InvertEffect implements BitmapEffect {

    public Bitmap applyEffect(Bitmap bm, double ctrl, int randomSeed, ArrayList<Integer> colorPalette, int rotationAngle) {
        for (int y = 0; y < bm.getHeight(); y++) {
            for (int x = 0; x < bm.getWidth(); x++) {
                int color = ColorHelper.getPixelColor(bm, x, y);

                int red = (color >> 16) & 0xFF;
                int green = (color >> 8) & 0xFF;
                int blue = (color >> 0) & 0xFF;

                // Only invert if below the threshold (ctrl is inverted, i.e. ctrl = 1 - seekbar)
                if ((red + green + blue) / 3 <= (ctrl * 0xff)) {
                    int invertedRed = 0xff - red;
                    int invertedGreen = 0xff - green;
                    int invertedBlue = 0xff - blue;

                    int invertedColor = (invertedRed << 16) + (invertedGreen << 8) + invertedBlue;

                    ColorHelper.setPixelColor(bm, x, y, invertedColor);
                }
            }
        }
        return bm;
    }
}
