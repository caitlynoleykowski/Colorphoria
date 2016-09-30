package com.caitoo0o.filler;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Created by Cait on 8/11/2016.
 */
public class BitmapWrapper {

    private static final int MAX_PIXELS = 2000000;
    private static final int ROTATION_ANGLE = 90;

    Bitmap source;
    Bitmap mod;
    int currRotationAngle;
    boolean [][] processed;

    public BitmapWrapper(Bitmap bm) {
        currRotationAngle = 0;
        double scaleFactor = getScaleFactor(bm.getWidth(), bm.getHeight());
        if (scaleFactor == 1) {
            setSource(bm);
        } else {
            int width = (int) (bm.getWidth() * scaleFactor);
            int height = (int) (bm.getHeight() * scaleFactor);
            setSource(Bitmap.createScaledBitmap(bm, width, height, true));
        }
    }

    public BitmapWrapper() {}

    public static double getScaleFactor(int bitmapWidth, int bitmapHeight) {
        int numPixels = bitmapWidth * bitmapHeight;
        if (numPixels >= MAX_PIXELS) {
            double scaleFactor = (double) MAX_PIXELS / (double) numPixels;
            return scaleFactor;
        } else {
            return 1;
        }
    }

    // TODO: Can get rid of this if I make BitmapWrapper serializable or parcelable
    public Bitmap getSourceBitmap() {
        return source;
    }
    public void setModifiedBitmap(Bitmap bm) {
        // TODO: throw exception if mod is not mutable
        if (!bm.isMutable()) {
            Log.e("bitmapwrapper", "error!! bitmap not mutable");
        }
        mod = bm;
        processed = new boolean[mod.getWidth()][mod.getHeight()];
    }

    public Bitmap getModifiedBitmap() { return mod; }

    private int getPixelColor(int x, int y) {
        return mod.getPixel(x, y) & 0x00FFFFFF;
    }
    private void setPixelColor(int x, int y, int color) {
        mod.setPixel(x, y, color);
    }
    public boolean isPixelNodeProcessed(PixelNode n) {
        return processed[n.x()][n.y()];
    }
    public void markPixelNodeProcessed(PixelNode n) {
        processed[n.x()][n.y()] = true;
    }
    public PixelNode getNorth(PixelNode n) {
        PixelNode node = new PixelNode(n.x(), n.y() - 1);
        return isWithinBounds(node) ? node : null;
    }
    public PixelNode getSouth(PixelNode n) {
        PixelNode node = new PixelNode(n.x(), n.y() + 1);
        return isWithinBounds(node) ? node : null;
    }
    public PixelNode getEast(PixelNode n) {
        PixelNode node = new PixelNode(n.x() + 1, n.y());
        return isWithinBounds(node) ? node : null;
    }
    public PixelNode getWest(PixelNode n) {
        PixelNode node = new PixelNode(n.x() - 1, n.y());
        return isWithinBounds(node) ? node : null;
    }
    private boolean isWithinBounds(PixelNode n) {
        if (n.x() < getWidth() && n.x() >= 0 && n.y() < getHeight() && n.y() >= 0) {
            return true;
        } else {
            return false;
        }
    }

    public int getWidth() {
        return mod.getWidth();
    }

    public int getHeight() {
        return mod.getHeight();
    }

    public int getRotationAngle() {
        return currRotationAngle;
    }

    public void setRotationAngle(int rotationAngle) {
        currRotationAngle = rotationAngle;
    }

    public void doneProcessing() {
        processed = new boolean[mod.getWidth()][mod.getHeight()];
    }

    public void setSource(Bitmap bm) {
        source = bm;
        setModifiedBitmap(source.copy(source.getConfig(), true));
    }

    public void resetFromRotatedSource() {
        if (currRotationAngle != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(currRotationAngle);
            setModifiedBitmap(Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true));
        } else {
            setModifiedBitmap(source.copy(source.getConfig(), true));
        }
    }

    // Start effects

    public void reset() {
        currRotationAngle = 0;
        setModifiedBitmap(source.copy(source.getConfig(), true));
    }

    public void rotateRight() {
        applyRotate(ROTATION_ANGLE);
    }

    public void rotateLeft() {
        applyRotate(-1 * ROTATION_ANGLE);
    }

    public void randomBleedFill(double ctrl) {
        resetFromRotatedSource();
        applyRandomBleedFill(ctrl);
    }

    public void threshold(double ctrl) {
        resetFromRotatedSource();
        applyThreshold(ctrl);
    }

    public void randomLineFill(double ctrl) {
        resetFromRotatedSource();
        applyThreshold(ctrl);
        applyRandomLineFill();
    }

    public void randomFloodFill(double ctrl) {
        resetFromRotatedSource();
        applyThreshold(ctrl);
        applyRandomFill();
    }

    private void applyRotate(int rotationAngle) {
        // calculate the net rotation angle
        currRotationAngle += rotationAngle;
        currRotationAngle %= 360;
        // apply the rotation
        // TODO: combine this with the applyRotate function by passing a bitmap to applyRotate?
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationAngle);
        setModifiedBitmap(Bitmap.createBitmap(getModifiedBitmap(), 0, 0, getWidth(), getHeight(), matrix, true));
    }

    void applyThreshold(double ctrl) {
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                int color = getPixelColor(x, y);
                int red = (color >> 16) & 0xFF;
                int green = (color >> 8) & 0xFF;
                int blue = (color >> 0) & 0xFF;
                int average = (red + blue + green) / 3;
                if (average <= (255 * ctrl)) {
                    mod.setPixel(x, y, 0x000000);
                } else {
                    mod.setPixel(x, y, 0xffffff);
                }
            }
        }
        //doneProcessing();
    }

    void applyRandomLineFill() {
        int prevPixelColor = 0x000000;
        int currColor = 0;
        Random r = new Random();
        int maxColor = 0xffffff;
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                int color = getPixelColor(x, y);
                if (color == 0xffffff && prevPixelColor == 0x000000) {
                    currColor = r.nextInt(maxColor);
                    prevPixelColor = color;
                    mod.setPixel(x, y, currColor);
                } else if (color == 0xffffff && prevPixelColor == 0xffffff) {
                    prevPixelColor = color;
                    mod.setPixel(x, y, currColor);
                } else if (color == 0x000000) {
                    prevPixelColor = color;
                } else {
                    // this condition is only hit if the current pixel is a different color
                    prevPixelColor = color;
                }
            }
        }
        //doneProcessing();
    }

    public void applyRandomFill() {
        Random r = new Random();
        int maxColor = 0xffffff;
        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                PixelNode currNode = new PixelNode(i, j);
                floodFill(currNode, 0xffffff, r.nextInt(maxColor));
            }
        }
        doneProcessing();
    }

    // Flood-fill (node, target-color, replacement-color):
    private void floodFill(PixelNode node, int targetColor, int replacementColor) {
        Queue<PixelNode> queue = new LinkedList<PixelNode>();
        queue.add(node);

        while (!queue.isEmpty()) {
            //Log.e("picsource", "The queue:" + queue.toString());
            // 5.    Set n equal to the first element of Q.
            // 6.    Remove first element from Q.
            PixelNode n = queue.remove();
            // 7.     If the color of n is equal to target-color:
            if (getPixelColor(n.x(), n.y()) == targetColor) {
                // 8.    Set the color of n to replacement-color and mark "n" as processed.
                setPixelColor(n.x(), n.y(), replacementColor);
                markPixelNodeProcessed(n);
                // 9.    Add west node to end of Q if west has not been processed yet.
                PixelNode west = getWest(n);
                if (west != null && !isPixelNodeProcessed(west)) {
                    markPixelNodeProcessed(west);
                    queue.add(west);
                }
                // 10.   Add east node to end of Q if east has not been processed yet.
                PixelNode east = getEast(n);
                if (east != null && !isPixelNodeProcessed(east)) {
                    markPixelNodeProcessed(east);
                    queue.add(east);
                }
                // 11.   Add north node to end of Q if north has not been processed yet.
                PixelNode north = getNorth(n);
                if (north != null && !isPixelNodeProcessed(north)) {
                    markPixelNodeProcessed(north);
                    queue.add(north);
                }
                // 12.   Add south node to end of Q if south has not been processed yet.
                PixelNode south = getSouth(n);
                if (south != null && !isPixelNodeProcessed(south)) {
                    markPixelNodeProcessed(south);
                    queue.add(south);
                }
            }
        }
        //13. Return.
        return;
    }

    public void applyRandomBleedFill(double ctrl) {
        Random r = new Random();
        int maxColor = 0xffffff;
        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                PixelNode currNode = new PixelNode(i, j);
                if (!isPixelNodeProcessed(currNode)) {
                    int targetColor = getPixelColor(currNode.x(), currNode.y() );
                    bleedFill(currNode, targetColor, r.nextInt(maxColor), ctrl);
                }
            }
        }
        doneProcessing();
    }

    // Flood-fill (node, target-color, replacement-color):
    void bleedFill(PixelNode node, int targetColor, int replacementColor, double ctrl) {
        Queue<PixelNode> queue = new LinkedList<PixelNode>();
        queue.add(node);

        while (!queue.isEmpty()) {
            //Log.e("picsource", "The queue:" + queue.toString());
            // 5.    Set n equal to the first element of Q.
            // 6.    Remove first element from Q.
            PixelNode n = queue.remove();
            // 7.     If the color of n is not significantly different than the target-color:
            boolean sigChange = false;
            int pixelCurrColor = getPixelColor(n.x(), n.y());
            int changeInRed = Math.abs(getRed(pixelCurrColor) - getRed(targetColor));
            int changeInGreen = Math.abs(getGreen(pixelCurrColor) - getGreen(targetColor));
            int changeInBlue = Math.abs(getBlue(pixelCurrColor) - getBlue(targetColor));
            int overallChange = changeInRed + changeInGreen + changeInBlue;
            if (overallChange > ((0xff + 0xff + 0xff) * ctrl)) {
                sigChange = true;
            }
            if (!sigChange) {
                // 8.    Set the color of n to adjusted replacement-color and mark "n" as processed.
                int adjustedReplacementColor;
                int pixelDarkness = getRed(pixelCurrColor) + getGreen(pixelCurrColor) + getBlue(pixelCurrColor);
                int darknessInRepColor = getRed(replacementColor) + getGreen(replacementColor) + getBlue(replacementColor);
                double redRatioInRepColor, greenRatioInRepColor, blueRatioInRepColor;
                if (darknessInRepColor > 0) {
                    redRatioInRepColor = (double)getRed(replacementColor) / (double)darknessInRepColor;
                    greenRatioInRepColor = (double)getGreen(replacementColor) / (double)darknessInRepColor;
                    blueRatioInRepColor = (double)getBlue(replacementColor) / (double)darknessInRepColor;
                } else {
                    redRatioInRepColor = 0;
                    greenRatioInRepColor = 0;
                    blueRatioInRepColor = 0;
                }
                int redInAdjRepColor = (int) (redRatioInRepColor * pixelDarkness);
                int greenInAdjRepColor = (int) (greenRatioInRepColor * pixelDarkness);
                int blueInAdjRepColor = (int) (blueRatioInRepColor * pixelDarkness);
                adjustedReplacementColor = (redInAdjRepColor << 16) + (greenInAdjRepColor << 8) + blueInAdjRepColor;
                setPixelColor(n.x(), n.y(), adjustedReplacementColor);
                markPixelNodeProcessed(n);
                // 9.    Add west node to end of Q if west has not been processed yet.
                PixelNode west = getWest(n);
                if (west != null && !isPixelNodeProcessed(west)) {
                    markPixelNodeProcessed(west);
                    queue.add(west);
                }
                // 10.   Add east node to end of Q if east has not been processed yet.
                PixelNode east = getEast(n);
                if (east != null && !isPixelNodeProcessed(east)) {
                    markPixelNodeProcessed(east);
                    queue.add(east);
                }
                // 11.   Add north node to end of Q if north has not been processed yet.
                PixelNode north = getNorth(n);
                if (north != null && !isPixelNodeProcessed(north)) {
                    markPixelNodeProcessed(north);
                    queue.add(north);
                }
                // 12.   Add south node to end of Q if south has not been processed yet.
                PixelNode south = getSouth(n);
                if (south != null && !isPixelNodeProcessed(south)) {
                    markPixelNodeProcessed(south);
                    queue.add(south);
                }
            }
        }
        //13. Return.
        return;
    }

    private int getRed(int color) {
        return (color & 0xff0000)>> 16;
    }

    private int getGreen(int color) {
        return (color &  0x00ff00) >> 8;
    }

    private int getBlue(int color) {
        return (color & 0x0000ff) >> 0;
    }
}
