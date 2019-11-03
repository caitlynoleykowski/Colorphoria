package com.oo0o0o.colorphoria.effects;

import android.graphics.Bitmap;
import android.os.Environment;

import com.oo0o0o.colorphoria.effecthelpers.BitmapWrapper;
import com.oo0o0o.colorphoria.effecthelpers.ColorHelper;
import com.oo0o0o.colorphoria.effecthelpers.PixelNode;
import com.oo0o0o.colorphoria.utils.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Created by oo0o0o on 4/12/2018.
 */

public class RandomBleedFillEffect implements BitmapEffect {

    public Bitmap applyEffect(Bitmap bm, double ctrl, int randomSeed, ArrayList<Integer> colorPalette, int rotationAngle) {

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyyhhmmss");
        String formattedDate = sdf.format(date);
        String baseFilename = "colorphoriaImgGen" + formattedDate;

        ArrayList<File> generatedImgFiles = new ArrayList<>();

        Bitmap original = bm.copy(bm.getConfig(), true);
        boolean [][] processed = new boolean[bm.getWidth()][bm.getHeight()];
        Random r = new Random(randomSeed);
        int count = 0;
        for (int i = 0; i < bm.getWidth(); i++) {
            for (int j = 0; j < bm.getHeight(); j++) {
                PixelNode currNode = new PixelNode(i, j);
                if (!currNode.isPixelNodeProcessed(processed)) {
                    int targetColor = ColorHelper.getPixelColor(original, currNode.x(), currNode.y());
                    int nextColor = ColorHelper.getNextColor(r, colorPalette);
                    bleedFill(original, bm, currNode, targetColor, nextColor, ctrl, processed);
                }
                // TODO Video gen
//                if ((count % (BitmapWrapper.MAX_PIXELS/10)) == 0) { // Divisor is the number of image files we'll generate
//                    String filename = baseFilename + "-" + i + "-" + j + ".png";
//                    File file = Utils.savecolorphoriaBitmap(bm, null, filename, null);
//                    generatedImgFiles.add(file);
//                }
                count++;
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


        return bm;
    }

    // Flood-fill (node, target-color, replacement-color):
    private static void bleedFill(Bitmap original, Bitmap bm, PixelNode node, int targetColor, int replacementColor, double ctrl, boolean [][] processed) {
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
            if (!sigChange) {
                // 8.    Set the color of n to adjusted replacement-color and mark "n" as processed.
                int adjustedReplacementColor;
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
                ColorHelper.setPixelColor(bm, n.x(), n.y(), adjustedReplacementColor);
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
