package com.caitoo0o.filler.effecthelpers;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import com.caitoo0o.filler.effects.BitmapEffect;
import com.caitoo0o.filler.effects.ResetEffect;
import com.caitoo0o.filler.effects.RotateEffect;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Cait on 8/11/2016.
 */
public class BitmapWrapper {

    protected static final int MAX_PIXELS = 1000000; // Protected for unit test
    public static final int MAX_CUMULATIVE_DELTA_OF_ALL_COLORS = (0xff + 0xff + 0xff); // Protected for unit test
    private static final int ROTATION_ANGLE = 90;
    private static final int MAX_COLOR = 0xffffff;

    Bitmap source;
    Bitmap mod;

    int currRotationAngle;

    public BitmapWrapper(Bitmap bm) {
        currRotationAngle = 0;
        double scaleFactor = getScaleFactor(bm.getWidth(), bm.getHeight());
        // could use scaleFactor to multiply by the image dimensions regardless
        // but it's an optimization to not create a new bitmap if you don't have to,
        // so just set the source bm to the existing bitmap if the scaleFactor is 1
        if (scaleFactor == 1) {
            setSource(bm);
        } else {
            int width = (int) (bm.getWidth() * scaleFactor);
            int height = (int) (bm.getHeight() * scaleFactor);
            setSource(Bitmap.createScaledBitmap(bm, width, height, true));
        }
    }

    public BitmapWrapper() {} // Used when reloading asyncronously from saved state

    public static double getScaleFactor(int bitmapWidth, int bitmapHeight) {
        int numPixels = bitmapWidth * bitmapHeight;
        if (numPixels >= MAX_PIXELS) {
            double scaleFactor = Math.sqrt((double) MAX_PIXELS / (double) numPixels);
            return scaleFactor;
        } else {
            return 1;
        }
    }

    public Bitmap getSourceBitmap() {
        return source;
    }

    public void setModifiedBitmap(Bitmap bm) {
        // TODO: throw exception if mod is not mutable
        if (!bm.isMutable()) {
            Log.e("bitmapwrapper", "error!! bitmap not mutable");
        }
        mod = bm;
    }

    public Bitmap getModifiedBitmap() { return mod; }

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

    public int getRotationAngle() {
        return currRotationAngle;
    }

    public void setRotationAngle(int rotationAngle) {
        currRotationAngle = rotationAngle;
    }

    public void setSource(Bitmap bm) {
        source = bm;
        setModifiedBitmap(source.copy(source.getConfig(), true));
    }

    protected Bitmap getRotatedSource() { // protected for unit tests
        if (currRotationAngle != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(currRotationAngle);
            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        } else {
            return source.copy(source.getConfig(), true);
        }
    }

    // effect helpers

    public static int getNextColor(Random r, ArrayList<Integer> colorPalette) {
        int nextColor;
        if (colorPalette != null && colorPalette.size() > 0) {
            nextColor = colorPalette.get(r.nextInt(colorPalette.size()));
        } else {
            nextColor = r.nextInt(MAX_COLOR);
        }
        return nextColor;
    }

    // Start effects

    public void applyEffect(BitmapEffect effect, double ctrl, int randomSeed, ArrayList<Integer> colorPalette) {
        Bitmap temp = getRotatedSource();
        effect.applyEffect(temp, ctrl, randomSeed, colorPalette, 0);
        mod = temp;
    }

    public void reset(double ctrl, int randomSeed, ArrayList<Integer> colorPalette) {
        currRotationAngle = 0;
        Bitmap temp = getRotatedSource();
        (new ResetEffect()).applyEffect(temp, ctrl, randomSeed, colorPalette, 0);
        mod = temp;
    }

    public void rotateRight(double ctrl, int randomSeed, ArrayList<Integer> colorPalette) {
        Bitmap temp = getRotatedSource();

        int rotationAngle = ROTATION_ANGLE;
        // calculate the net rotation angle
        currRotationAngle += rotationAngle;
        currRotationAngle %= 360;
        (new RotateEffect()).applyEffect(temp, ctrl, randomSeed, colorPalette, rotationAngle);
        mod = temp;
    }

    public void rotateLeft(double ctrl, int randomSeed, ArrayList<Integer> colorPalette) {
        int rotationAngle = -1 * ROTATION_ANGLE;
        // calculate the net rotation angle
        currRotationAngle += rotationAngle;
        currRotationAngle %= 360;

        Bitmap temp = getRotatedSource();
        (new RotateEffect()).applyEffect(temp, ctrl, randomSeed, colorPalette, rotationAngle);
        mod = temp;
    }

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
}
