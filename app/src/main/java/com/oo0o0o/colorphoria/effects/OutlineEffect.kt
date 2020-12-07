package com.oo0o0o.colorphoria.effects

import android.graphics.Bitmap

import com.oo0o0o.colorphoria.effecthelpers.ColorHelper
import com.oo0o0o.colorphoria.effecthelpers.PixelNode

import java.util.ArrayList
import java.util.LinkedList
import java.util.Random

/**
 * Created by oo0o0o on 4/12/2018.
 */

class OutlineEffect(internal var dark: Boolean) : BitmapEffect {

    override fun applyEffect(bm: Bitmap, ctrl: Double, randomSeed: Int, colorPalette: ArrayList<Int>?, rotationAngle: Int): Bitmap {
        // goal : amplify ctrl ( which is a number less than 1) and keep it less than 1
        // position on slider = user input between 0 and 100
        // ctrl = 1 - positionOver100
        val positionOver100 = 1 - ctrl
        val delta = 1 - positionOver100
        val partialDelta = delta / 2
        // amplifiedCtrl = 1 - (positionOver100 + partialDelta)
        // amplifiedCtrl = 1 - (( 1 - ctrl) + partialDelta)
        // amplifiedCtrl = 1 - 1 + ctrl - partialDelta
        val amplifiedCtrl = ctrl - partialDelta

        val original = bm.copy(bm.config, true)
        val processed = Array(bm.width) { BooleanArray(bm.height) }
        val r = Random(randomSeed.toLong())
        for (i in 0 until bm.width) {
            for (j in 0 until bm.height) {
                val currNode = PixelNode(i, j)
                if (!currNode.isPixelNodeProcessed(processed)) {
                    val targetColor = ColorHelper.getPixelColor(original, currNode.x(), currNode.y())
                    val nextColor = ColorHelper.getNextColor(r, colorPalette)
                    bleedFill(original, bm, currNode, targetColor, nextColor, amplifiedCtrl, processed, dark)
                }
            }
        }
        return bm
    }

    // Flood-fill (node, target-color, replacement-color):
    private fun bleedFill(original: Bitmap, bm: Bitmap, node: PixelNode, targetColor: Int, replacementColor: Int, ctrl: Double, processed: Array<BooleanArray>, darkBg: Boolean) {
        val bgColor: Int
        val adjustedReplacementColor: Int
        if (darkBg) {
            // with dark bg we want light outline colors
            bgColor = 0x000000
            adjustedReplacementColor = getLightColor(replacementColor)
        } else {
            // with light bg we want dark outline colors
            bgColor = 0xffffff
            adjustedReplacementColor = getDarkColor(replacementColor)
        }

        val queue = LinkedList<PixelNode>()
        queue.add(node)

        while (!queue.isEmpty()) {
            //Log.e("picsource", "The queue:" + queue.toString());
            // 5.    Set n equal to the first element of Q.
            // 6.    Remove first element from Q.
            val n = queue.remove()
            // 7.     If the color of n is not significantly different than the target-color:
            var sigChange = false
            val pixelCurrColor = ColorHelper.getPixelColor(original, n.x(), n.y())
            val changeInRed = Math.abs(ColorHelper.getRed(pixelCurrColor) - ColorHelper.getRed(targetColor))
            val changeInGreen = Math.abs(ColorHelper.getGreen(pixelCurrColor) - ColorHelper.getGreen(targetColor))
            val changeInBlue = Math.abs(ColorHelper.getBlue(pixelCurrColor) - ColorHelper.getBlue(targetColor))
            val overallChange = changeInRed + changeInGreen + changeInBlue
            if (overallChange > ColorHelper.MAX_CUMULATIVE_DELTA_OF_ALL_COLORS * ctrl) {
                sigChange = true
            }
            if (sigChange) {
                // 8.    Set the color of n to replacement-color and mark "n" as processed.
                ColorHelper.setPixelColor(bm, n.x(), n.y(), adjustedReplacementColor)
                n.markPixelNodeProcessed(processed)
                changeSurroundingPixels(bm, n, adjustedReplacementColor, processed, 0, 3)
            } else {
                // 8.    Set the color of n to replacement-color and mark "n" as processed.
                ColorHelper.setPixelColor(bm, n.x(), n.y(), bgColor)
                n.markPixelNodeProcessed(processed)
                // 9.    Add west node to end of Q if west has not been processed yet.
                val west = n.getWest(bm)
                if (west != null && !west.isPixelNodeProcessed(processed)) {
                    west.markPixelNodeProcessed(processed)
                    queue.add(west)
                }
                // 10.   Add east node to end of Q if east has not been processed yet.
                val east = n.getEast(bm)
                if (east != null && !east.isPixelNodeProcessed(processed)) {
                    east.markPixelNodeProcessed(processed)
                    queue.add(east)
                }
                // 11.   Add north node to end of Q if north has not been processed yet.
                val north = n.getNorth(bm)
                if (north != null && !north.isPixelNodeProcessed(processed)) {
                    north.markPixelNodeProcessed(processed)
                    queue.add(north)
                }
                // 12.   Add south node to end of Q if south has not been processed yet.
                val south = n.getSouth(bm)
                if (south != null && !south.isPixelNodeProcessed(processed)) {
                    south.markPixelNodeProcessed(processed)
                    queue.add(south)
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
        return
    }

    private fun changeSurroundingPixels(bm: Bitmap, n: PixelNode, replacementColor: Int, processed: Array<BooleanArray>, currDepth: Int, maxDepth: Int) {
        // Detect base case, else perform algorithm at current depth
        if (currDepth == maxDepth) {
            return
        } else {
            // Check if west node exists and if so
            // set the color of it to replacement-color and mark it as processed
            val west = n.getWest(bm)
            if (west != null) {
                ColorHelper.setPixelColor(bm, west.x(), west.y(), replacementColor)
                changeSurroundingPixels(bm, west, replacementColor, processed, currDepth + 1, maxDepth)
            }
            // Check if east node exists and if so
            // set the color of it to replacement-color and mark it as processed
            val east = n.getEast(bm)
            if (east != null) {
                ColorHelper.setPixelColor(bm, east.x(), east.y(), replacementColor)
                changeSurroundingPixels(bm, east, replacementColor, processed, currDepth + 1, maxDepth)
            }
            // Check if north node exists and if so
            // set the color of it to replacement-color and mark it as processed
            val north = n.getNorth(bm)
            if (north != null) {
                ColorHelper.setPixelColor(bm, north.x(), north.y(), replacementColor)
                changeSurroundingPixels(bm, north, replacementColor, processed, currDepth + 1, maxDepth)
            }
            // Check if south node exists and if so
            // set the color of it to replacement-color and mark it as processed
            val south = n.getSouth(bm)
            if (south != null) {
                ColorHelper.setPixelColor(bm, south.x(), south.y(), replacementColor)
                changeSurroundingPixels(bm, south, replacementColor, processed, currDepth + 1, maxDepth)
            }
        }
    }

    private fun getLightColor(replacementColor: Int): Int {
        val red = ColorHelper.getRed(replacementColor)
        val green = ColorHelper.getGreen(replacementColor)
        val blue = ColorHelper.getBlue(replacementColor)

        val minLight = 255.0 * 2.0 / 3.0 * 3.0
        val lightInRepColor = (red + green + blue).toDouble()

        var adjustedRed: Int
        var adjustedBlue: Int
        var adjustedGreen: Int
        if (lightInRepColor < minLight) {
            // goal: adjustedRed + adjustedGreen + adjustedBlue ~= minLight

            val lightDelta = minLight - lightInRepColor
            val dividedDelta = lightDelta / 3.0
            adjustedRed = red + dividedDelta.toInt()
            adjustedGreen = green + dividedDelta.toInt()
            adjustedBlue = blue + dividedDelta.toInt()

            adjustedRed = if (adjustedRed > 255) 255 else adjustedRed
            adjustedGreen = if (adjustedGreen > 255) 255 else adjustedGreen
            adjustedBlue = if (adjustedBlue > 255) 255 else adjustedBlue
        } else {
            adjustedRed = red
            adjustedGreen = green
            adjustedBlue = blue
        }

        return (adjustedRed shl 16) + (adjustedGreen shl 8) + adjustedBlue
    }

    private fun getDarkColor(replacementColor: Int): Int {
        val red = ColorHelper.getRed(replacementColor)
        val green = ColorHelper.getGreen(replacementColor)
        val blue = ColorHelper.getBlue(replacementColor)

        val maxLight = 255.0 * 2.0 / 3.0 * 3.0 // 66% the light of full white ... iow, 33% darker than white
        val lightInRepColor = (red + green + blue).toDouble()

        var adjustedRed: Int
        var adjustedBlue: Int
        var adjustedGreen: Int
        if (lightInRepColor > maxLight) {
            // goal: adjustedRed + adjustedGreen + adjustedBlue ~= maxLight
            val lightDelta = lightInRepColor - maxLight
            val dividedDelta = lightDelta / 3.0
            adjustedRed = red - dividedDelta.toInt()
            adjustedGreen = green - dividedDelta.toInt()
            adjustedBlue = blue - dividedDelta.toInt()

            adjustedRed = if (adjustedRed < 0) 0 else adjustedRed
            adjustedGreen = if (adjustedGreen < 0) 0 else adjustedGreen
            adjustedBlue = if (adjustedBlue < 0) 0 else adjustedBlue
        } else {
            adjustedRed = red
            adjustedGreen = green
            adjustedBlue = blue
        }

        return (adjustedRed shl 16) + (adjustedGreen shl 8) + adjustedBlue
    }
}