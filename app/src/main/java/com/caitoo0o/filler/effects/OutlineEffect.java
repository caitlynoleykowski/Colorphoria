package com.caitoo0o.filler.effects;

import android.graphics.Bitmap;

import com.caitoo0o.filler.effecthelpers.BitmapWrapper;
import com.caitoo0o.filler.effecthelpers.ColorHelper;
import com.caitoo0o.filler.effecthelpers.PixelNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Created by caitoo0o on 4/12/2018.
 */

public class OutlineEffect implements BitmapEffect {

    public Bitmap applyEffect(Bitmap bm, double ctrl, int randomSeed, ArrayList<Integer> colorPalette, int rotationAngle) {
        boolean [][] processed = new boolean[bm.getWidth()][bm.getHeight()];
        Random r = new Random(randomSeed);
        for (int i = 0; i < bm.getWidth(); i++) {
            for (int j = 0; j < bm.getHeight(); j++) {
                PixelNode currNode = new PixelNode(i, j);
                if (!currNode.isPixelNodeProcessed(processed)) {
                    int targetColor = ColorHelper.getPixelColor(bm, currNode.x(), currNode.y());
                    int nextColor = ColorHelper.getNextColor(r, colorPalette);
                    bleedFill(bm, currNode, targetColor, nextColor, ctrl, processed);
                }
            }
        }
        return bm;
    }

    // Flood-fill (node, target-color, replacement-color):
    private static void bleedFill(Bitmap bm, PixelNode node, int targetColor, int replacementColor, double ctrl, boolean [][] processed) {
        Queue<PixelNode> queue = new LinkedList<PixelNode>();
        queue.add(node);

        while (!queue.isEmpty()) {
            //Log.e("picsource", "The queue:" + queue.toString());
            // 5.    Set n equal to the first element of Q.
            // 6.    Remove first element from Q.
            PixelNode n = queue.remove();
            // 7.     If the color of n is not significantly different than the target-color:
            boolean sigChange = false;
            int pixelCurrColor = ColorHelper.getPixelColor(bm, n.x(), n.y());
            int changeInRed = Math.abs(ColorHelper.getRed(pixelCurrColor) - ColorHelper.getRed(targetColor));
            int changeInGreen = Math.abs(ColorHelper.getGreen(pixelCurrColor) - ColorHelper.getGreen(targetColor));
            int changeInBlue = Math.abs(ColorHelper.getBlue(pixelCurrColor) - ColorHelper.getBlue(targetColor));
            int overallChange = changeInRed + changeInGreen + changeInBlue;
            if (overallChange > (ColorHelper.MAX_CUMULATIVE_DELTA_OF_ALL_COLORS * ctrl)) {
                sigChange = true;
            }
            if (sigChange) {
                // 8.    Set the color of n to replacement-color and mark "n" as processed.
                ColorHelper.setPixelColor(bm, n.x(), n.y(), replacementColor);
                n.markPixelNodeProcessed(processed);
            } else {
                // 8.    Set the color of n to replacement-color and mark "n" as processed.
                ColorHelper.setPixelColor(bm, n.x(), n.y(), 0x000000);
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
            }
        }
        //13. Return.
        return;
    }
}
