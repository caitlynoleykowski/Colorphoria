package com.oo0o0o.colorphoria.effects;

import android.graphics.Bitmap;

import com.oo0o0o.colorphoria.effecthelpers.ColorHelper;
import com.oo0o0o.colorphoria.effecthelpers.EffectTypes;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by oo0o0o on 4/12/2018.
 */

public class RandomLineFillEffect implements BitmapEffect {

    public Bitmap applyEffect(Bitmap bm, double ctrl, int randomSeed, ArrayList<Integer> colorPalette, int rotationAngle) {
        (new ThresholdEffect()).applyEffect(bm, ctrl, randomSeed, colorPalette, rotationAngle);
        applyRandomLineFill(bm, ctrl, randomSeed, colorPalette);
        return bm;
    }

    private static void applyRandomLineFill(Bitmap bm, double ctrl, int randomSeed, ArrayList<Integer> colorPalette) {
        int prevPixelColor = 0x000000;
        int currColor = 0;
        Random r = new Random(randomSeed);
        for (int y = 0; y < bm.getHeight(); y++) {
            for (int x = 0; x < bm.getWidth(); x++) {
                int color = ColorHelper.getPixelColor(bm, x, y);
                if (color == 0xffffff && (prevPixelColor == 0x000000 || x == 0)) {
                    currColor = ColorHelper.getNextColor(r, colorPalette);
                    prevPixelColor = color;
                    ColorHelper.setPixelColor(bm, x, y, currColor);
                } else if (color == 0xffffff && prevPixelColor == 0xffffff) {
                    prevPixelColor = color;
                    ColorHelper.setPixelColor(bm, x, y, currColor);
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
