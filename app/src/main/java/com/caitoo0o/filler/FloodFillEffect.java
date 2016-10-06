package com.caitoo0o.filler;

import android.graphics.Bitmap;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Created by Cait on 10/1/2016.
 */
public class FloodFillEffect implements BitmapEffect {

    public boolean resetRequired() { return true; }

    public Bitmap applyEffect(Bitmap bm) {
        boolean [][] processed = new boolean[mod.getWidth()][mod.getHeight()];
        Random r = new Random();
        int maxColor = 0xffffff;
        for (int i = 0; i < bm.getWidth(); i++) {
            for (int j = 0; j < bm.getHeight(); j++) {
                PixelNode currNode = new PixelNode(i, j);
                if (!isPixelNodeProcessed(currNode, processed) &&
                        getPixelColor(bm, currNode.x(), currNode.y()) == 0xffffff) {
                    floodFill(bm, currNode, 0xffffff, r.nextInt(maxColor), processed);
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
}
