package com.caitoo0o.filler.effects;

import android.graphics.Bitmap;

import com.caitoo0o.filler.effecthelpers.BitmapWrapper;
import com.caitoo0o.filler.effecthelpers.EffectTypes;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by caitoo0o on 4/12/2018.
 */

public class RandomLineFillEffect implements BitmapEffect {

    EffectTypes type = EffectTypes.RANDOM_LINE_FILL;

    public void applyEffect(Bitmap bm, double ctrl, int randomSeed, ArrayList<Integer> colorPalette, int rotationAngle) {
        (new ThresholdEffect()).applyEffect(bm, ctrl, randomSeed, colorPalette, rotationAngle);
        applyRandomLineFill(bm, ctrl, randomSeed, colorPalette);
    }

    private static void applyRandomLineFill(Bitmap bm, double ctrl, int randomSeed, ArrayList<Integer> colorPalette) {
        int prevPixelColor = 0x000000;
        int currColor = 0;
        Random r = new Random(randomSeed);
        for (int y = 0; y < bm.getHeight(); y++) {
            for (int x = 0; x < bm.getWidth(); x++) {
                int color = BitmapWrapper.getPixelColor(bm, x, y);
                if (color == 0xffffff && prevPixelColor == 0x000000) {
                    currColor = BitmapWrapper.getNextColor(r, colorPalette);
                    prevPixelColor = color;
                    BitmapWrapper.setPixelColor(bm, x, y, currColor);
                } else if (color == 0xffffff && prevPixelColor == 0xffffff) {
                    prevPixelColor = color;
                    BitmapWrapper.setPixelColor(bm, x, y, currColor);
                } else if (color == 0x000000) {
                    prevPixelColor = color;
                } else {
                    // this condition is only hit if the current pixel is a different color
                    prevPixelColor = color;
                }
            }
        }
    }
}
