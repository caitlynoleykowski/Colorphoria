package com.caitoo0o.filler.effects;

import android.graphics.Bitmap;

import com.caitoo0o.filler.effecthelpers.BitmapWrapper;
import com.caitoo0o.filler.effecthelpers.EffectTypes;
import com.caitoo0o.filler.effecthelpers.PixelNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Created by caitoo0o on 4/12/2018.
 */

public class RandomFloodFillEffect implements BitmapEffect {

    EffectTypes type = EffectTypes.RANDOM_FLOOD_FILL;

    public void applyEffect(Bitmap bm, double ctrl, int randomSeed, ArrayList<Integer> colorPalette, int rotationAngle) {
        (new ThresholdEffect()).applyEffect(bm, ctrl, randomSeed, colorPalette, rotationAngle);
        applyRandomFill(bm, ctrl, randomSeed, colorPalette);
    }

    private static void applyRandomFill(Bitmap bm, double ctrl, int randomSeed, ArrayList<Integer> colorPalette) {
        boolean [][] processed = new boolean[bm.getWidth()][bm.getHeight()];
        Random r = new Random(randomSeed);
        for (int i = 0; i < bm.getWidth(); i++) {
            for (int j = 0; j < bm.getHeight(); j++) {
                PixelNode currNode = new PixelNode(i, j);
                if (!currNode.isPixelNodeProcessed(processed) &&
                        BitmapWrapper.getPixelColor(bm, currNode.x(), currNode.y()) == 0xffffff) {
                    int nextColor = BitmapWrapper.getNextColor(r, colorPalette);
                    floodFill(bm, currNode, 0xffffff, nextColor, processed);
                }
            }
        }
    }

    // Flood-fill (node, target-color, replacement-color):
    private static void floodFill(Bitmap bm, PixelNode node, int targetColor, int replacementColor, boolean [][] processed) {
        Queue<PixelNode> queue = new LinkedList<PixelNode>();
        queue.add(node);

        while (!queue.isEmpty()) {
            //Log.e("picsource", "The queue:" + queue.toString());
            // 5.    Set n equal to the first element of Q.
            // 6.    Remove first element from Q.
            PixelNode n = queue.remove();
            // 7.     If the color of n is equal to target-color:
            if (BitmapWrapper.getPixelColor(bm, n.x(), n.y()) == targetColor) {
                // 8.    Set the color of n to replacement-color and mark "n" as processed.
                BitmapWrapper.setPixelColor(bm, n.x(), n.y(), replacementColor);
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
