package com.oo0o0o.colorphoria.effects;

import android.graphics.Bitmap;

import com.oo0o0o.colorphoria.effecthelpers.ColorHelper;
import com.oo0o0o.colorphoria.effecthelpers.PixelNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Created by oo0o0o on 4/12/2018.
 */

public class FadeFillEffect implements BitmapEffect {

    private enum RGB {
        RED,
        GREEN,
        BLUE
    }

    boolean tintFill;

    public FadeFillEffect() {
        this.tintFill = false;
    }

    public FadeFillEffect(boolean tintFill) {
        this.tintFill = tintFill;
    }

    public Bitmap applyEffect(Bitmap bm, double ctrl, int randomSeed, ArrayList<Integer> colorPalette, int rotationAngle) {
        Bitmap original = bm.copy(bm.getConfig(), true);
        boolean [][] processed = new boolean[bm.getWidth()][bm.getHeight()];
        Random r = new Random(randomSeed);
        for (int i = 0; i < bm.getWidth(); i++) {
            for (int j = 0; j < bm.getHeight(); j++) {
                PixelNode currNode = new PixelNode(i, j);
                if (!currNode.isPixelNodeProcessed(processed)) {
                    int targetColor = ColorHelper.getPixelColor(original, currNode.x(), currNode.y());
                    int nextColor = ColorHelper.getNextColor(r, colorPalette);
                    int nextRed = ColorHelper.getRed(nextColor);
                    int nextGreen = ColorHelper.getRed(nextColor);
                    int nextBlue = ColorHelper.getRed(nextColor);
                    RGB fadeTo;
                    if (nextRed < nextGreen && nextRed < nextBlue) {
                        fadeTo = RGB.RED;
                    } else if (nextGreen < nextRed && nextGreen < nextBlue) {
                        fadeTo = RGB.GREEN;
                    } else {
                        fadeTo = RGB.BLUE;
                    }
                    fadeFill(original, bm, currNode, targetColor, nextColor, ctrl, processed, fadeTo, tintFill);
                }
            }
        }
        return bm;
    }

    // Flood-fill (node, target-color, replacement-color):
    private static void fadeFill(Bitmap original, Bitmap bm, PixelNode node, int targetColor,
                                 int replacementColor, double ctrl, boolean [][] processed,
                                 RGB fadeTo, boolean tintFill) {
        Queue<PixelNode> queue = new LinkedList<PixelNode>();
        queue.add(node);

        int count = 0;
        int fadedReplacementColor = replacementColor;
        while (!queue.isEmpty()) {
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
            if (!sigChange) {
                // 8.    Set the color of n to adjusted replacement-color and mark "n" as processed.
                int adjustedReplacementColor;
                if (tintFill) {
                    // If tint fill, use the original bitmap pixel color as a basis for color
                    int pixelDarkness = ColorHelper.getRed(pixelCurrColor) + ColorHelper.getGreen(pixelCurrColor) + ColorHelper.getBlue(pixelCurrColor);
                    int darknessInRepColor = ColorHelper.getRed(replacementColor) + ColorHelper.getGreen(replacementColor) + ColorHelper.getBlue(replacementColor);
                    double redRatioInRepColor, greenRatioInRepColor, blueRatioInRepColor;
                    if (darknessInRepColor > 0) {
                        redRatioInRepColor = (double) ColorHelper.getRed(replacementColor) / (double)darknessInRepColor;
                        greenRatioInRepColor = (double) ColorHelper.getGreen(replacementColor) / (double)darknessInRepColor;
                        blueRatioInRepColor = (double) ColorHelper.getBlue(replacementColor) / (double)darknessInRepColor;
                    } else {
                        redRatioInRepColor = 0;
                        greenRatioInRepColor = 0;
                        blueRatioInRepColor = 0;
                    }
                    int redInAdjRepColor = (int) (redRatioInRepColor * pixelDarkness);
                    int greenInAdjRepColor = (int) (greenRatioInRepColor * pixelDarkness);
                    int blueInAdjRepColor = (int) (blueRatioInRepColor * pixelDarkness);
                    adjustedReplacementColor = (redInAdjRepColor << 16) + (greenInAdjRepColor << 8) + blueInAdjRepColor;
                } else {
                    adjustedReplacementColor = fadedReplacementColor;
                }
                // Set the pixel color
                ColorHelper.setPixelColor(bm, n.x(), n.y(), adjustedReplacementColor);
                n.markPixelNodeProcessed(processed);
                // Handle fading
                int pixelCtrl = (int) (2000.0 * ctrl);
                int fadeCondition = count % pixelCtrl;
                if (fadeCondition == 0) {
                    int redInFadedRepColor = ColorHelper.getRed(fadedReplacementColor);
                    int greenInFadedRepColor = ColorHelper.getGreen(fadedReplacementColor);
                    int blueInFadedRepColor = ColorHelper.getBlue(fadedReplacementColor);

                    switch (fadeTo) {
                        case RED:
                            if (redInFadedRepColor < 255) {
                                fadedReplacementColor = ((redInFadedRepColor + 1) << 16) + (greenInFadedRepColor << 8) + blueInFadedRepColor;
                            }
                            break;
                        case GREEN:
                            if (greenInFadedRepColor < 255) {
                                fadedReplacementColor = ((redInFadedRepColor) << 16) + (greenInFadedRepColor + 1 << 8) + blueInFadedRepColor;
                            }
                            break;
                        case BLUE:
                            if (blueInFadedRepColor < 255) {
                                fadedReplacementColor = ((redInFadedRepColor) << 16) + (greenInFadedRepColor << 8) + blueInFadedRepColor + 1;
                            }
                            break;
                    }
                }
                count++;
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
