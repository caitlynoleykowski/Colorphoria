package com.caitoo0o.filler;

import android.graphics.Bitmap;

/**
 * Created by Cait on 8/11/2016.
 */
public class BitmapWrapper {

    private static final int MAX_PIXELS = 2000000;

    Bitmap source;
    Bitmap mod;
    int [] pixels;
    PixelNode [][] pixelNodes;
    boolean [] processed;

    public BitmapWrapper(Bitmap bm) {
        // TODO: Should this go in its own function?
        int numPixels = bm.getWidth() * bm.getHeight();
        if (numPixels >= MAX_PIXELS) {
            double scaleFactor = (double)MAX_PIXELS / (double)numPixels;
            int width = (int) (bm.getWidth() * scaleFactor);
            int height = (int) (bm.getHeight() * scaleFactor);
            setSource(Bitmap.createScaledBitmap(bm, width, height, true));
        } else {
            setSource(bm);
        }
    }

    // TODO: Can get rid of this if I make BitmapWrapper serializable or parcelable
    public Bitmap getSourceBitmap() {
        return source;
    }
    public void setModifiedBitmap(Bitmap bm) {
        // TODO: check if mod is mutable?
        mod = bm;
        pixels = new int[mod.getWidth() * mod.getHeight()];
        mod.getPixels(pixels, 0, mod.getWidth(), 0, 0, mod.getWidth(), mod.getHeight());
        pixelNodes = new PixelNode[mod.getWidth()][mod.getHeight()];
        processed = new boolean[mod.getWidth() * mod.getHeight()];
    }

    public Bitmap getModifiedBitmap() { return mod; }

    public PixelNode getPixel(int x, int y) {
        if (pixelNodes[x][y] == null) {
            pixelNodes[x][y] = new PixelNode(x, y, mod.getWidth(), mod.getHeight(), pixels, processed);
        }
        return pixelNodes[x][y];
    }

    public int getWidth() {
        return mod.getWidth();
    }

    public int getHeight() {
        return mod.getHeight();
    }

    public void doneProcessing() {
        mod.setPixels(pixels, 0, mod.getWidth(), 0, 0, mod.getWidth(), mod.getHeight());
        processed = new boolean[mod.getWidth() * mod.getHeight()];
    }

    public void setSource(Bitmap bm) {
        source = bm;
        setModifiedBitmap(source.copy(source.getConfig(), true));
    }

    public void resetFromSource() {
        setModifiedBitmap(source.copy(source.getConfig(), true));
    }
}
