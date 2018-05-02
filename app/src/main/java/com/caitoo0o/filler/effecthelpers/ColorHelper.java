package com.caitoo0o.filler.effecthelpers;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by caitoo0o on 4/17/2018.
 */

public class ColorHelper {

    public static final int MAX_CUMULATIVE_DELTA_OF_ALL_COLORS = (0xff + 0xff + 0xff);
    private static final int MAX_COLOR = 0xffffff;

    // protected for unit tests
    public static int getRed(int color) {
        return (color & 0x00ff0000)>> 16;
    }

    // protected for unit tests
    public static int getGreen(int color) {
        return (color &  0x0000ff00) >> 8;
    }

    // protected for unit tests
    public static int getBlue(int color) {
        return (color & 0x000000ff) >> 0;
    }

    public static int getNextColor(Random r, ArrayList<Integer> colorSet) {
        int nextColor;
        if (colorSet != null && colorSet.size() > 0) {
            nextColor = colorSet.get(r.nextInt(colorSet.size()));
        } else {
            nextColor = r.nextInt(MAX_COLOR);
        }
        return nextColor;
    }

    public static int getPixelColor(Bitmap bm, int x, int y) {
        return bm.getPixel(x, y) & 0x00FFFFFF;
    }

    public static void setPixelColor(Bitmap bm, int x, int y, int color) {
        int alphaChannelBitMask = (0xFF000000 & bm.getPixel(x,y));
        int argbBitMask = alphaChannelBitMask | 0x00FFFFFF;
        int argbColorBitMask = 0xFF000000 | color;
        int argbColor = argbColorBitMask & argbBitMask;
        bm.setPixel(x, y, argbColor);
    }
}
