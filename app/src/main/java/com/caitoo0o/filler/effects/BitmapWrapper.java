package com.caitoo0o.filler.effects;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Created by Cait on 8/11/2016.
 */
public class BitmapWrapper {

    private static final int MAX_PIXELS = 1000000;
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

    public BitmapWrapper() {}

    public static double getScaleFactor(int bitmapWidth, int bitmapHeight) {
        int numPixels = bitmapWidth * bitmapHeight;
        if (numPixels >= MAX_PIXELS) {
            double scaleFactor = Math.sqrt((double) MAX_PIXELS / (double) numPixels);
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
    }

    public Bitmap getModifiedBitmap() { return mod; }

    private int getPixelColor(Bitmap bm, int x, int y) {
        return bm.getPixel(x, y) & 0x00FFFFFF;
    }
    private void setPixelColor(Bitmap bm, int x, int y, int color) {
        bm.setPixel(x, y, color);
    }
    public boolean isPixelNodeProcessed(PixelNode n, boolean [][] processed) {
        return processed[n.x()][n.y()];
    }
    public void markPixelNodeProcessed(PixelNode n, boolean [][]processed) {
        processed[n.x()][n.y()] = true;
    }
    public PixelNode getNorth(Bitmap bm, PixelNode n) {
        PixelNode node = new PixelNode(n.x(), n.y() - 1);
        return isWithinBounds(bm, node) ? node : null;
    }
    public PixelNode getSouth(Bitmap bm, PixelNode n) {
        PixelNode node = new PixelNode(n.x(), n.y() + 1);
        return isWithinBounds(bm, node) ? node : null;
    }
    public PixelNode getEast(Bitmap bm, PixelNode n) {
        PixelNode node = new PixelNode(n.x() + 1, n.y());
        return isWithinBounds(bm, node) ? node : null;
    }
    public PixelNode getWest(Bitmap bm, PixelNode n) {
        PixelNode node = new PixelNode(n.x() - 1, n.y());
        return isWithinBounds(bm, node) ? node : null;
    }
    private boolean isWithinBounds(Bitmap bm, PixelNode n) {
        if (n.x() < bm.getWidth() && n.x() >= 0 && n.y() < bm.getHeight() && n.y() >= 0) {
            return true;
        } else {
            return false;
        }
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

    private Bitmap getRotatedSource() {
        if (currRotationAngle != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(currRotationAngle);
            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        } else {
            return source.copy(source.getConfig(), true);
        }
    }

    // effect helpers

    private int getNextColor(Random r, ArrayList<Integer> colorPalette) {
        int nextColor;
        if (colorPalette != null && colorPalette.size() > 0) {
            nextColor = colorPalette.get(r.nextInt(colorPalette.size()));
        } else {
            nextColor = r.nextInt(MAX_COLOR);
        }
        return nextColor;
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

    public void randomBleedFill(double ctrl, int randomSeed, ArrayList<Integer> colorPalette) {
        Bitmap temp = getRotatedSource();
        applyRandomBleedFill(temp, ctrl, randomSeed, colorPalette);
        mod = temp;
    }

    public void threshold(double ctrl) {
        Bitmap temp = getRotatedSource();
        applyThreshold(temp, ctrl);
        mod = temp;
    }

    public void randomLineFill(double ctrl, int randomSeed, ArrayList<Integer> colorPalette) {
        Bitmap temp = getRotatedSource();
        applyThreshold(temp, ctrl);
        applyRandomLineFill(temp, randomSeed, colorPalette);
        mod = temp;
    }

    public void randomFloodFill(double ctrl, int randomSeed, ArrayList<Integer> colorPalette) {
        Bitmap temp = getRotatedSource();
        applyThreshold(temp, ctrl);
        applyRandomFill(temp, randomSeed, colorPalette);
        mod = temp;
    }

    private void applyRotate(int rotationAngle) {
        // calculate the net rotation angle
        currRotationAngle += rotationAngle;
        currRotationAngle %= 360;
        // apply the rotation
        // TODO: combine this with the applyRotate function by passing a bitmap to applyRotate?
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationAngle);
        setModifiedBitmap(Bitmap.createBitmap(mod, 0, 0, mod.getWidth(), mod.getHeight(), matrix, true));
    }

    void applyThreshold(Bitmap bm, double ctrl) {
        for (int y = 0; y < bm.getHeight(); y++) {
            for (int x = 0; x < bm.getWidth(); x++) {
                int color = getPixelColor(bm, x, y);
                int red = (color >> 16) & 0xFF;
                int green = (color >> 8) & 0xFF;
                int blue = (color >> 0) & 0xFF;
                int average = (red + blue + green) / 3;
                if (average <= (255 * ctrl)) {
                    setPixelColor(bm, x, y, 0x000000);
                } else {
                    setPixelColor(bm, x, y, 0xffffff);
                }
            }
        }
    }

    void applyRandomLineFill(Bitmap bm, int randomSeed, ArrayList<Integer> colorPalette) {
        int prevPixelColor = 0x000000;
        int currColor = 0;
        Random r = new Random(randomSeed);
        for (int y = 0; y < bm.getHeight(); y++) {
            for (int x = 0; x < bm.getWidth(); x++) {
                int color = getPixelColor(bm, x, y);
                if (color == 0xffffff && prevPixelColor == 0x000000) {
                    currColor = getNextColor(r, colorPalette);
                    prevPixelColor = color;
                    setPixelColor(bm, x, y, currColor);
                } else if (color == 0xffffff && prevPixelColor == 0xffffff) {
                    prevPixelColor = color;
                    setPixelColor(bm, x, y, currColor);
                } else if (color == 0x000000) {
                    prevPixelColor = color;
                } else {
                    // this condition is only hit if the current pixel is a different color
                    prevPixelColor = color;
                }
            }
        }
    }

    public void applyRandomFill(Bitmap bm, int randomSeed, ArrayList<Integer> colorPalette) {
        boolean [][] processed = new boolean[mod.getWidth()][mod.getHeight()];
        Random r = new Random(randomSeed);
        for (int i = 0; i < bm.getWidth(); i++) {
            for (int j = 0; j < bm.getHeight(); j++) {
                PixelNode currNode = new PixelNode(i, j);
                if (!isPixelNodeProcessed(currNode, processed) &&
                        getPixelColor(bm, currNode.x(), currNode.y()) == 0xffffff) {
                    int nextColor = getNextColor(r, colorPalette);
                    floodFill(bm, currNode, 0xffffff, nextColor, processed);
                }
            }
        }
    }

    // Flood-fill (node, target-color, replacement-color):
    private void floodFill(Bitmap bm, PixelNode node, int targetColor, int replacementColor, boolean [][] processed) {
        Queue<PixelNode> queue = new LinkedList<PixelNode>();
        queue.add(node);

        while (!queue.isEmpty()) {
            //Log.e("picsource", "The queue:" + queue.toString());
            // 5.    Set n equal to the first element of Q.
            // 6.    Remove first element from Q.
            PixelNode n = queue.remove();
            // 7.     If the color of n is equal to target-color:
            if (getPixelColor(bm, n.x(), n.y()) == targetColor) {
                // 8.    Set the color of n to replacement-color and mark "n" as processed.
                setPixelColor(bm, n.x(), n.y(), replacementColor);
                markPixelNodeProcessed(n, processed);
                // 9.    Add west node to end of Q if west has not been processed yet.
                PixelNode west = getWest(bm, n);
                if (west != null && !isPixelNodeProcessed(west, processed)) {
                    markPixelNodeProcessed(west, processed);
                    queue.add(west);
                }
                // 10.   Add east node to end of Q if east has not been processed yet.
                PixelNode east = getEast(bm, n);
                if (east != null && !isPixelNodeProcessed(east, processed)) {
                    markPixelNodeProcessed(east, processed);
                    queue.add(east);
                }
                // 11.   Add north node to end of Q if north has not been processed yet.
                PixelNode north = getNorth(bm, n);
                if (north != null && !isPixelNodeProcessed(north, processed)) {
                    markPixelNodeProcessed(north, processed);
                    queue.add(north);
                }
                // 12.   Add south node to end of Q if south has not been processed yet.
                PixelNode south = getSouth(bm, n);
                if (south != null && !isPixelNodeProcessed(south, processed)) {
                    markPixelNodeProcessed(south, processed);
                    queue.add(south);
                }
            }
        }
        //13. Return.
        return;
    }

    public void applyRandomBleedFill(Bitmap bm, double ctrl, int randomSeed, ArrayList<Integer> colorPalette) {
        boolean [][] processed = new boolean[mod.getWidth()][mod.getHeight()];
        Random r = new Random(randomSeed);
        for (int i = 0; i < bm.getWidth(); i++) {
            for (int j = 0; j < bm.getHeight(); j++) {
                PixelNode currNode = new PixelNode(i, j);
                if (!isPixelNodeProcessed(currNode, processed)) {
                    int targetColor = getPixelColor(bm, currNode.x(), currNode.y());
                    int nextColor = getNextColor(r, colorPalette);
                    bleedFill(bm, currNode, targetColor, nextColor, ctrl, processed);
                }
            }
        }
    }

    // Flood-fill (node, target-color, replacement-color):
    void bleedFill(Bitmap bm, PixelNode node, int targetColor, int replacementColor, double ctrl, boolean [][] processed) {
        Queue<PixelNode> queue = new LinkedList<PixelNode>();
        queue.add(node);

        while (!queue.isEmpty()) {
            //Log.e("picsource", "The queue:" + queue.toString());
            // 5.    Set n equal to the first element of Q.
            // 6.    Remove first element from Q.
            PixelNode n = queue.remove();
            // 7.     If the color of n is not significantly different than the target-color:
            boolean sigChange = false;
            int pixelCurrColor = getPixelColor(bm, n.x(), n.y());
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
                setPixelColor(bm, n.x(), n.y(), adjustedReplacementColor);
                markPixelNodeProcessed(n, processed);
                // 9.    Add west node to end of Q if west has not been processed yet.
                PixelNode west = getWest(bm, n);
                if (west != null && !isPixelNodeProcessed(west, processed)) {
                    markPixelNodeProcessed(west, processed);
                    queue.add(west);
                }
                // 10.   Add east node to end of Q if east has not been processed yet.
                PixelNode east = getEast(bm, n);
                if (east != null && !isPixelNodeProcessed(east, processed)) {
                    markPixelNodeProcessed(east, processed);
                    queue.add(east);
                }
                // 11.   Add north node to end of Q if north has not been processed yet.
                PixelNode north = getNorth(bm, n);
                if (north != null && !isPixelNodeProcessed(north, processed)) {
                    markPixelNodeProcessed(north, processed);
                    queue.add(north);
                }
                // 12.   Add south node to end of Q if south has not been processed yet.
                PixelNode south = getSouth(bm, n);
                if (south != null && !isPixelNodeProcessed(south, processed)) {
                    markPixelNodeProcessed(south, processed);
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
