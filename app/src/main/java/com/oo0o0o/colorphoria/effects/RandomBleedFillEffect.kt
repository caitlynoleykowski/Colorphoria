package com.oo0o0o.colorphoria.effects

import android.graphics.Bitmap
import android.os.Environment

import com.oo0o0o.colorphoria.effecthelpers.BitmapWrapper
import com.oo0o0o.colorphoria.effecthelpers.ColorHelper
import com.oo0o0o.colorphoria.effecthelpers.PixelNode
import com.oo0o0o.colorphoria.utils.Utils

import java.io.File
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.LinkedList
import java.util.Queue
import java.util.Random

/**
 * Created by oo0o0o on 4/12/2018.
 */

class RandomBleedFillEffect : BitmapEffect {

    override fun applyEffect(bm: Bitmap, ctrl: Double, randomSeed: Int, colorPalette: ArrayList<Int>?, rotationAngle: Int): Bitmap {

        val date = Date()
        val sdf = SimpleDateFormat("MMddyyyyhhmmss")
        val formattedDate = sdf.format(date)
        val baseFilename = "colorphoriaImgGen$formattedDate"

        val generatedImgFiles = ArrayList<File>()

        val original = bm.copy(bm.config, true)
        val processed = Array(bm.width) { BooleanArray(bm.height) }
        val r = Random(randomSeed.toLong())
        var count = 0
        for (i in 0 until bm.width) {
            for (j in 0 until bm.height) {
                val currNode = PixelNode(i, j)
                if (!currNode.isPixelNodeProcessed(processed)) {
                    val targetColor = ColorHelper.getPixelColor(original, currNode.x(), currNode.y())
                    val nextColor = ColorHelper.getNextColor(r, colorPalette)
                    bleedFill(original, bm, currNode, targetColor, nextColor, ctrl, processed)
                }
                // TODO Video gen
                //                if ((count % (BitmapWrapper.MAX_PIXELS/10)) == 0) { // Divisor is the number of image files we'll generate
                //                    String filename = baseFilename + "-" + i + "-" + j + ".png";
                //                    File file = Utils.savecolorphoriaBitmap(bm, null, filename, null);
                //                    generatedImgFiles.add(file);
                //                }
                count++
            }
        }

        // TODO Video gen
        // After img gen try creating an mp4 from the imgs
        //        File pictureStorageDir = Environment.getExternalStoragePublicDirectory(
        //                Environment.DIRECTORY_PICTURES);
        //        File file = new File(pictureStorageDir, baseFilename + ".mp4");
        //        try
        //        {
        //            EncodeDecode encodeDecoder = new EncodeDecode(generatedImgFiles, file);
        //            encodeDecoder.encodeDecodeVideoFromBufferToSurface(bm.getWidth(), bm.getHeight(),
        //                    2000000);
        //        } catch (Throwable e)
        //        {
        //            e.printStackTrace();
        //        }


        return bm
    }

    // Flood-fill (node, target-color, replacement-color):
    private fun bleedFill(original: Bitmap, bm: Bitmap, node: PixelNode, targetColor: Int, replacementColor: Int, ctrl: Double, processed: Array<BooleanArray>) {
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
            if (!sigChange) {
                // 8.    Set the color of n to adjusted replacement-color and mark "n" as processed.
                val adjustedReplacementColor: Int
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
                ColorHelper.setPixelColor(bm, n.x(), n.y(), adjustedReplacementColor)
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
