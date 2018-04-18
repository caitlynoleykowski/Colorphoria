package com.caitoo0o.filler.effects;

import android.graphics.Bitmap;

import com.caitoo0o.filler.effecthelpers.BitmapWrapper;
import com.caitoo0o.filler.effecthelpers.EffectTypes;

import java.util.ArrayList;

/**
 * Created by caitoo0o on 4/12/2018.
 */

public class InvertEffect implements BitmapEffect {

    public EffectTypes getEffectType() {
        return EffectTypes.INVERT;
    }

    public void applyEffect(Bitmap bm, double ctrl, int randomSeed, ArrayList<Integer> colorPalette, int rotationAngle) {
        for (int y = 0; y < bm.getHeight(); y++) {
            for (int x = 0; x < bm.getWidth(); x++) {
                int color = BitmapWrapper.getPixelColor(bm, x, y);

                int red = (color >> 16) & 0xFF;
                int green = (color >> 8) & 0xFF;
                int blue = (color >> 0) & 0xFF;

                // Only invert if below the threshold (ctrl is inverted, i.e. ctrl = 1 - seekbar)
                if ((red + green + blue) / 3 <= (ctrl * 0xff)) {
                    int invertedRed = 0xff - red;
                    int invertedGreen = 0xff - green;
                    int invertedBlue = 0xff - blue;

                    int invertedColor = (invertedRed << 16) + (invertedGreen << 8) + invertedBlue;

                    BitmapWrapper.setPixelColor(bm, x, y, invertedColor);
                }
            }
        }
    }
}
