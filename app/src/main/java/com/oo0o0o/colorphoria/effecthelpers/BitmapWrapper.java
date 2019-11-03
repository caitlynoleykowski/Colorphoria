package com.oo0o0o.colorphoria.effecthelpers;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import com.oo0o0o.colorphoria.effects.BitmapEffect;
import com.oo0o0o.colorphoria.effects.ResetEffect;
import com.oo0o0o.colorphoria.effects.RotateEffect;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Cait on 8/11/2016.
 */
public class BitmapWrapper {

    private static final String TAG = "BitmapWrapper";

    public static final int MAX_PIXELS = 1000000; // Protected for unit test
    private static final int ROTATION_ANGLE = 90;

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
        return getScaleFactor(bitmapWidth, bitmapHeight, MAX_PIXELS);
    }

    public static double getScaleFactor(int bitmapWidth, int bitmapHeight, int maxPixels) {
        int numPixels = bitmapWidth * bitmapHeight;
        if (numPixels >= maxPixels) {
            double scaleFactor = Math.sqrt((double) maxPixels / (double) numPixels);
            return scaleFactor;
        } else {
            return 1;
        }
    }

    public void setSource(Bitmap bm) {
        source = bm;
        setModifiedBitmap(source.copy(source.getConfig(), true));
    }

    public Bitmap getSourceBitmap() {
        return source;
    }

    public void setModifiedBitmap(Bitmap bm) {
        if (!bm.isMutable()) {
            Log.e(TAG, "Error!! BitmapWrapper mod bitmap not mutable");
//            throw new IllegalArgumentException("The mod bitmap in BitmapWrapper must be mutable!");
        }
        mod = bm;
    }

    public Bitmap getModifiedBitmap() { return mod; }

    public int getRotationAngle() {
        return currRotationAngle;
    }

    public void setRotationAngle(int rotationAngle) {
        currRotationAngle = rotationAngle;
    }

    protected Bitmap getRotatedSourceCopy() { // protected for unit tests
        if (currRotationAngle != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(currRotationAngle);
            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        } else {
            return source.copy(source.getConfig(), true);
        }
    }

    public void applyEffect(BitmapEffect effect, double ctrl, int randomSeed, ArrayList<Integer> colorPalette) {
        // Special handling for reset
        if (effect instanceof ResetEffect) {
            currRotationAngle = 0;
        }

        Bitmap temp ;

        int rotationAngle = 0;
        // Special handling for rotate right
        if (effect instanceof RotateEffect) {
            temp = getModifiedBitmap();

            rotationAngle = ROTATION_ANGLE;
            // calculate the net rotation angle
            currRotationAngle += rotationAngle;
            currRotationAngle %= 360;
        } else {
            temp = getRotatedSourceCopy();
        }

        mod = effect.applyEffect(temp, ctrl, randomSeed, colorPalette, rotationAngle);
    }
}
