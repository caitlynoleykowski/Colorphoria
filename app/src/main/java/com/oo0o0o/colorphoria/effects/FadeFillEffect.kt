package com.oo0o0o.colorphoria.effects

import android.graphics.Bitmap
import com.oo0o0o.colorphoria.effecthelpers.ColorHelper
import com.oo0o0o.colorphoria.effecthelpers.EffectTypes
import com.oo0o0o.colorphoria.effecthelpers.PixelNode
import java.util.Random
import java.util.LinkedList

abstract class FadeFillEffect(internal val tintFill: Boolean = false) : BitmapEffect {
    private enum class RGB {
        RED,
        GREEN,
        BLUE
    }

    override fun applyEffect(bm: Bitmap, ctrl: Double, randomSeed: Int, colorPalette: ArrayList<Int>?, rotationAngle: Int): Bitmap? {
        val original = bm.copy(bm.config, true)
        val processed = Array(bm.width) { BooleanArray(bm.height) }
        val r = Random(randomSeed.toLong())
        for (i in 0 until bm.width) {
            for (j in 0 until bm.height) {
                val currNode = PixelNode(i, j)
                if (!currNode.isPixelNodeProcessed(processed)) {
                    val targetColor = ColorHelper.getPixelColor(original, currNode.x(), currNode.y())
                    val nextColor = ColorHelper.getNextColor(r, colorPalette)
                    val nextRed = ColorHelper.getRed(nextColor)
                    val nextGreen = ColorHelper.getRed(nextColor)
                    val nextBlue = ColorHelper.getRed(nextColor)
                    val fadeTo: RGB
                    if (nextRed < nextGreen && nextRed < nextBlue) {
                        fadeTo = RGB.RED
                    } else if (nextGreen < nextRed && nextGreen < nextBlue) {
                        fadeTo = RGB.GREEN
                    } else {
                        fadeTo = RGB.BLUE
                    }
                    fadeFill(original, bm, currNode, targetColor, nextColor, ctrl, processed, fadeTo, tintFill)
                }
            }
        }
        return bm
    }

    // Flood-fill (node, target-color, replacement-color):
    private fun fadeFill(original: Bitmap, bm: Bitmap, node: PixelNode, targetColor: Int,
                         replacementColor: Int, ctrl: Double, processed: Array<BooleanArray>,
                         fadeTo: RGB, tintFill: Boolean) {
        val queue = LinkedList<PixelNode>()
        queue.add(node)

        var count = 0
        var fadedReplacementColor = replacementColor
        while (!queue.isEmpty()) {
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
            if (!sigChange) {
                // 8.    Set the color of n to adjusted replacement-color and mark "n" as processed.
                val adjustedReplacementColor: Int
                if (tintFill) {
                    // If tint fill, use the original bitmap pixel color as a basis for color
                    val pixelDarkness = ColorHelper.getRed(pixelCurrColor) + ColorHelper.getGreen(pixelCurrColor) + ColorHelper.getBlue(pixelCurrColor)
                    val darknessInRepColor = ColorHelper.getRed(replacementColor) + ColorHelper.getGreen(replacementColor) + ColorHelper.getBlue(replacementColor)
                    val redRatioInRepColor: Double
                    val greenRatioInRepColor: Double
                    val blueRatioInRepColor: Double
                    if (darknessInRepColor > 0) {
                        redRatioInRepColor = ColorHelper.getRed(replacementColor).toDouble() / darknessInRepColor.toDouble()
                        greenRatioInRepColor = ColorHelper.getGreen(replacementColor).toDouble() / darknessInRepColor.toDouble()
                        blueRatioInRepColor = ColorHelper.getBlue(replacementColor).toDouble() / darknessInRepColor.toDouble()
                    } else {
                        redRatioInRepColor = 0.0
                        greenRatioInRepColor = 0.0
                        blueRatioInRepColor = 0.0
                    }
                    val redInAdjRepColor = (redRatioInRepColor * pixelDarkness).toInt()
                    val greenInAdjRepColor = (greenRatioInRepColor * pixelDarkness).toInt()
                    val blueInAdjRepColor = (blueRatioInRepColor * pixelDarkness).toInt()
                    adjustedReplacementColor = (redInAdjRepColor shl 16) + (greenInAdjRepColor shl 8) + blueInAdjRepColor
                } else {
                    adjustedReplacementColor = fadedReplacementColor
                }
                // Set the pixel color
                ColorHelper.setPixelColor(bm, n.x(), n.y(), adjustedReplacementColor)
                n.markPixelNodeProcessed(processed)
                // Handle fading
                val pixelCtrl = (2000.0 * ctrl).toInt()
                val fadeCondition = count % pixelCtrl
                if (fadeCondition == 0) {
                    val redInFadedRepColor = ColorHelper.getRed(fadedReplacementColor)
                    val greenInFadedRepColor = ColorHelper.getGreen(fadedReplacementColor)
                    val blueInFadedRepColor = ColorHelper.getBlue(fadedReplacementColor)

                    when (fadeTo) {
                        FadeFillEffect.RGB.RED -> if (redInFadedRepColor < 255) {
                            fadedReplacementColor = (redInFadedRepColor + 1 shl 16) + (greenInFadedRepColor shl 8) + blueInFadedRepColor
                        }
                        FadeFillEffect.RGB.GREEN -> if (greenInFadedRepColor < 255) {
                            fadedReplacementColor = (redInFadedRepColor shl 16) + (greenInFadedRepColor + 1 shl 8) + blueInFadedRepColor
                        }
                        FadeFillEffect.RGB.BLUE -> if (blueInFadedRepColor < 255) {
                            fadedReplacementColor = (redInFadedRepColor shl 16) + (greenInFadedRepColor shl 8) + blueInFadedRepColor + 1
                        }
                    }
                }
                count++
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