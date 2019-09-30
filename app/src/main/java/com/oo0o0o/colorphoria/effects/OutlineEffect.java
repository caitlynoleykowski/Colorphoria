package com.oo0o0o.colorphoria.effects;

import android.graphics.Bitmap;
import android.util.Log;

import com.oo0o0o.colorphoria.effecthelpers.BitmapWrapper;
import com.oo0o0o.colorphoria.effecthelpers.ColorHelper;
import com.oo0o0o.colorphoria.effecthelpers.PixelNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Created by oo0o0o on 4/12/2018.
 */

public class OutlineEffect implements BitmapEffect {

    boolean dark;

    public OutlineEffect(boolean dark) {
        this.dark = dark;
    }

    public Bitmap applyEffect(Bitmap bm, double ctrl, int randomSeed, ArrayList<Integer> colorPalette, int rotationAngle) {
        // goal : amplify ctrl ( which is a number less than 1) and keep it less than 1
        // position on slider = user input between 0 and 100
        // ctrl = 1 - positionOver100
        double positionOver100 = 1 - ctrl;
        double delta = 1 - positionOver100;
        double partialDelta = delta / 2;
        // amplifiedCtrl = 1 - (positionOver100 + partialDelta)
        // amplifiedCtrl = 1 - (( 1 - ctrl) + partialDelta)
        // amplifiedCtrl = 1 - 1 + ctrl - partialDelta
        double amplifiedCtrl = ctrl - partialDelta;

        Bitmap original = bm.copy(bm.getConfig(), true);
        boolean [][] processed = new boolean[bm.getWidth()][bm.getHeight()];
        Random r = new Random(randomSeed);
        for (int i = 0; i < bm.getWidth(); i++) {
            for (int j = 0; j < bm.getHeight(); j++) {
                PixelNode currNode = new PixelNode(i, j);
                if (!currNode.isPixelNodeProcessed(processed)) {
                    int targetColor = ColorHelper.getPixelColor(original, currNode.x(), currNode.y());
                    int nextColor = ColorHelper.getNextColor(r, colorPalette);
                    bleedFill(original, bm, currNode, targetColor, nextColor, amplifiedCtrl, processed, dark);
                }
            }
        }
        return bm;
    }

    // Flood-fill (node, target-color, replacement-color):
    private static void bleedFill(Bitmap original, Bitmap bm, PixelNode node, int targetColor, int replacementColor, double ctrl, boolean [][] processed, boolean darkBg) {
        int bgColor;
        int adjustedReplacementColor;
        if (darkBg) {
            // with dark bg we want light outline colors
            bgColor = 0x000000;
            adjustedReplacementColor = getLightColor(replacementColor);
        } else {
            // with light bg we want dark outline colors
            bgColor = 0xffffff;
            adjustedReplacementColor = getDarkColor(replacementColor);
        }

        Queue<PixelNode> queue = new LinkedList<PixelNode>();
        queue.add(node);

        while (!queue.isEmpty()) {
            //Log.e("picsource", "The queue:" + queue.toString());
            // 5.    Set n equal to the first element of Q.
            // 6.    Remove first element from Q.
            PixelNode n = queue.remove();
            // 7.     If the color of n is not significantly different than the target-color:
            boolean sigChange = false;
            int pixelCurrColor = ColorHelper.getPixelColor(original, n.x(), n.y());
            int changeInRed = Math.abs(ColorHelper.getRed(pixelCurrColor) - ColorHelper.getRed(targetColor));
            int changeInGreen = Math.abs(ColorHelper.getGreen(pixelCurrColor) - ColorHelper.getGreen(targetColor));
            int changeInBlue = Math.abs(ColorHelper.getBlue(pixelCurrColor) - ColorHelper.getBlue(targetColor));
            int overallChange = changeInRed + changeInGreen + changeInBlue;
            if (overallChange > (ColorHelper.MAX_CUMULATIVE_DELTA_OF_ALL_COLORS * ctrl)) {
                sigChange = true;
            }
            if (sigChange) {
                // 8.    Set the color of n to replacement-color and mark "n" as processed.
                ColorHelper.setPixelColor(bm, n.x(), n.y(), adjustedReplacementColor);
                n.markPixelNodeProcessed(processed);
                changeSurroundingPixels(bm, n, adjustedReplacementColor, processed, 0, 3);
            } else {
                // 8.    Set the color of n to replacement-color and mark "n" as processed.
                ColorHelper.setPixelColor(bm, n.x(), n.y(), bgColor);
                n.markPixelNodeProcessed(processed);
                // 9.    Add west node to end of Q if west has not been processed yet.
                PixelNode west = n.getWest(bm);
                if (west != null && !west.isPixelNodeProcessed(processed)) {
                    west.markPixelNodeProcessed(processed);
                    queue.add(west);
                }
                // 10.   Add east node to end of Q if east has not been processed yet.
                PixelNode east = n.getEast(bm);
                if (east != null && !east.isPixelNodeProcessed(processed)) {
                    east.markPixelNodeProcessed(processed);
                    queue.add(east);
                }
                // 11.   Add north node to end of Q if north has not been processed yet.
                PixelNode north = n.getNorth(bm);
                if (north != null && !north.isPixelNodeProcessed(processed)) {
                    north.markPixelNodeProcessed(processed);
                    queue.add(north);
                }
                // 12.   Add south node to end of Q if south has not been processed yet.
                PixelNode south = n.getSouth(bm);
                if (south != null && !south.isPixelNodeProcessed(processed)) {
                    south.markPixelNodeProcessed(processed);
                    queue.add(south);
                }
//                PixelNode northwest = n.getNorthWest(bm);
//                if (northwest != null && !northwest.isPixelNodeProcessed(processed)) {
//                    northwest.markPixelNodeProcessed(processed);
//                    queue.add(northwest);
//                }
//                PixelNode southwest = n.getSouthWest(bm);
//                if (southwest != null && !southwest.isPixelNodeProcessed(processed)) {
//                    southwest.markPixelNodeProcessed(processed);
//                    queue.add(southwest);
//                }
//                PixelNode northeast = n.getNorthEast(bm);
//                if (northeast != null && !northeast.isPixelNodeProcessed(processed)) {
//                    northeast.markPixelNodeProcessed(processed);
//                    queue.add(northeast);
//                }
//                PixelNode southeast = n.getSouthEast(bm);
//                if (southeast != null && !southeast.isPixelNodeProcessed(processed)) {
//                    southeast.markPixelNodeProcessed(processed);
//                    queue.add(southeast);
//                }
            }
        }
        //13. Return.
        return;
    }

    private static void changeSurroundingPixels(Bitmap bm, PixelNode n, int replacementColor, boolean [][] processed, int currDepth, int maxDepth) {
        // Detect base case, else perform algorithm at current depth
        if (currDepth == maxDepth) {
            return;
        } else {
            // Check if west node exists and if so
            // set the color of it to replacement-color and mark it as processed
            PixelNode west = n.getWest(bm);
            if (west != null) {
                ColorHelper.setPixelColor(bm, west.x(), west.y(), replacementColor);
                changeSurroundingPixels(bm, west, replacementColor, processed, currDepth + 1, maxDepth);
            }
            // Check if east node exists and if so
            // set the color of it to replacement-color and mark it as processed
            PixelNode east = n.getEast(bm);
            if (east != null) {
                ColorHelper.setPixelColor(bm, east.x(), east.y(), replacementColor);
                changeSurroundingPixels(bm, east, replacementColor, processed, currDepth + 1, maxDepth);
            }
            // Check if north node exists and if so
            // set the color of it to replacement-color and mark it as processed
            PixelNode north = n.getNorth(bm);
            if (north != null) {
                ColorHelper.setPixelColor(bm, north.x(), north.y(), replacementColor);
                changeSurroundingPixels(bm, north, replacementColor, processed, currDepth + 1, maxDepth);
            }
            // Check if south node exists and if so
            // set the color of it to replacement-color and mark it as processed
            PixelNode south = n.getSouth(bm);
            if (south != null) {
                ColorHelper.setPixelColor(bm, south.x(), south.y(), replacementColor);
                changeSurroundingPixels(bm, south, replacementColor, processed, currDepth + 1, maxDepth);
            }
        }
    }

    private static int getLightColor(int replacementColor) {
        int red = ColorHelper.getRed(replacementColor);
        int green = ColorHelper.getGreen(replacementColor);
        int blue = ColorHelper.getBlue(replacementColor);

        double minLight = 255.0 * 2.0/3.0 * 3.0;
        double lightInRepColor = red + green + blue;

        int adjustedRed, adjustedBlue, adjustedGreen;
        if (lightInRepColor < minLight) {
            // goal: adjustedRed + adjustedGreen + adjustedBlue ~= minLight

            double lightDelta = minLight - lightInRepColor;
            double dividedDelta = lightDelta / 3.0;
            adjustedRed = red + (int) dividedDelta;
            adjustedGreen = green + (int) dividedDelta;
            adjustedBlue = blue + (int) dividedDelta;

            adjustedRed = adjustedRed > 255 ? 255 : adjustedRed;
            adjustedGreen = adjustedGreen > 255 ? 255 : adjustedGreen;
            adjustedBlue = adjustedBlue > 255 ? 255 : adjustedBlue;
        } else {
            adjustedRed = red;
            adjustedGreen = green;
            adjustedBlue = blue;
        }

        int adjustedReplacementColor = (adjustedRed << 16) + (adjustedGreen << 8) + adjustedBlue;
        return adjustedReplacementColor;
    }

    private static int getDarkColor(int replacementColor) {
        int red = ColorHelper.getRed(replacementColor);
        int green = ColorHelper.getGreen(replacementColor);
        int blue = ColorHelper.getBlue(replacementColor);

        double maxLight = 255.0 * 2.0/3.0 * 3.0; // 66% the light of full white ... iow, 33% darker than white
        double lightInRepColor = red + green + blue;

        int adjustedRed, adjustedBlue, adjustedGreen;
        if (lightInRepColor > maxLight) {
            // goal: adjustedRed + adjustedGreen + adjustedBlue ~= maxLight
            double lightDelta = lightInRepColor - maxLight;
            double dividedDelta = lightDelta / 3.0;
            adjustedRed = red - (int) dividedDelta;
            adjustedGreen = green - (int) dividedDelta;
            adjustedBlue = blue - (int) dividedDelta;

            adjustedRed = adjustedRed < 0 ? 0 : adjustedRed;
            adjustedGreen = adjustedGreen < 0 ? 0 : adjustedGreen;
            adjustedBlue = adjustedBlue < 0 ? 0 : adjustedBlue;
        } else {
            adjustedRed = red;
            adjustedGreen = green;
            adjustedBlue = blue;
        }

        int adjustedReplacementColor = (adjustedRed << 16) + (adjustedGreen << 8) + adjustedBlue;
        return adjustedReplacementColor;
    }
}
