package com.oo0o0o.colorphoria.effects

import android.graphics.Bitmap

import com.oo0o0o.colorphoria.effecthelpers.ColorHelper
import com.oo0o0o.colorphoria.effecthelpers.EffectTypes
import com.oo0o0o.colorphoria.effecthelpers.PixelNode

import java.util.ArrayList
import java.util.LinkedList
import java.util.Queue
import java.util.Random

/**
 * Created by oo0o0o on 4/12/2018.
 */

class RandomFloodFillEffect : BitmapEffect {

    companion object {
        val effectType = EffectTypes.RANDOM_FLOOD_FILL
    }

    override val effectType: EffectTypes = RandomFloodFillEffect.effectType

    override fun applyEffect(bm: Bitmap, ctrl: Double, randomSeed: Int, colorPalette: ArrayList<Int>?, rotationAngle: Int): Bitmap {
        ThresholdEffect().applyEffect(bm, ctrl, randomSeed, colorPalette, rotationAngle)
        applyRandomFill(bm, ctrl, randomSeed, colorPalette)
        return bm
    }

    private fun applyRandomFill(bm: Bitmap, ctrl: Double, randomSeed: Int, colorPalette: ArrayList<Int>?) {
        val processed = Array(bm.width) { BooleanArray(bm.height) }
        val r = Random(randomSeed.toLong())
        for (i in 0 until bm.width) {
            for (j in 0 until bm.height) {
                val currNode = PixelNode(i, j)
                if (!currNode.isPixelNodeProcessed(processed) && ColorHelper.getPixelColor(bm, currNode.x(), currNode.y()) == 0xffffff) {
                    val nextColor = ColorHelper.getNextColor(r, colorPalette)
                    floodFill(bm, currNode, 0xffffff, nextColor, processed)
                }
            }
        }
    }

    // Flood-fill (node, target-color, replacement-color):
    private fun floodFill(bm: Bitmap, node: PixelNode, targetColor: Int, replacementColor: Int, processed: Array<BooleanArray>) {
        val queue = LinkedList<PixelNode>()
        queue.add(node)

        while (!queue.isEmpty()) {
            //Log.e("picsource", "The queue:" + queue.toString());
            // 5.    Set n equal to the first element of Q.
            // 6.    Remove first element from Q.
            val n = queue.remove()
            // 7.     If the color of n is equal to target-color:
            if (ColorHelper.getPixelColor(bm, n.x(), n.y()) == targetColor) {
                // 8.    Set the color of n to replacement-color and mark "n" as processed.
                ColorHelper.setPixelColor(bm, n.x(), n.y(), replacementColor)
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
            }
        }
        //13. Return.
        return
    }
}