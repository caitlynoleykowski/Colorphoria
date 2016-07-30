package com.caitoo0o.filler;

import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class PicSourceActivity extends AppCompatActivity {

    Bitmap source;
    Bitmap mod;
    ImageView modImg;
    int clickCount = 0;
    Queue<PixelNode> queue;
    int [] pixels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_source);

        source = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.test);
        mod = source.copy(source.getConfig(), true);
        queue = new LinkedList<PixelNode>();
        pixels = new int[mod.getWidth() * mod.getHeight()];
        mod.getPixels(pixels, 0, mod.getWidth(), 0, 0, mod.getWidth(), mod.getHeight());
    }

    public void onThreshold(View v) {
        LinearLayout ll = (LinearLayout) findViewById(R.id.theLinearLayout);
        ll.removeAllViews();

        pixels = applyThreshold(pixels, mod.getWidth(), mod.getHeight());
        mod.setPixels(pixels, 0, mod.getWidth(), 0, 0, mod.getWidth(), mod.getHeight());

        ImageView img = new ImageView(getApplicationContext());
        img.setImageBitmap(mod);
        img.setScaleType(ScaleType.FIT_CENTER);
        img.setPadding(0, 4, 4, 0);
        img.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        ll.addView(img);
    }

    // The bitmap passed in must be mutable
    int[] applyThreshold(int [] pixels, int width, int height) {
        for (int i = 0; i < pixels.length; i++) {
            int red = (pixels[i] >> 16) & 0xFF;
            int green = (pixels[i] >> 8) & 0xFF;
            int blue = (pixels[i] >> 0) & 0xFF;
            int average = (red + blue + green) / 3;
            if (average >= (255/2)) {
                pixels[i] = 0xffffff;
            } else {
                pixels[i] = 0x000000;
            }
        }
        return pixels;
    }

    public void onRandomLineFill(View v) {
        LinearLayout ll = (LinearLayout) findViewById(R.id.theLinearLayout);
        ll.removeAllViews();

        pixels = applyRandomLineFill(pixels, mod.getWidth(), mod.getHeight());
        mod.setPixels(pixels, 0, mod.getWidth(), 0, 0, mod.getWidth(), mod.getHeight());

        ImageView img = new ImageView(getApplicationContext());
        img.setImageBitmap(mod);
        img.setScaleType(ScaleType.FIT_CENTER);
        img.setPadding(0, 4, 4, 0);
        img.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        ll.addView(img);
    }

    int[] applyRandomLineFill(int [] pixels, int width, int height) {
        // TODO: for some reason when i getPixels() again all pixels are -1 :/ figure out why
        //bm.getPixels(pixels, 0, bm.getWidth(), 0, 0, bm.getWidth(), bm.getHeight());
        int [] modPixels = new int[width * height];
        int prevPixelColor = 0x000000;
        int currColor = 0;
        Random r = new Random();
        int maxColor = 0xffffff;
        for (int i = 0; i < pixels.length; i++) {
        //for (int i = 0; i < 20; i++) {
            //Log.i("picsource", "pixels[i]: " + pixels[i]);
            if (pixels[i] == 0xffffff && prevPixelColor == 0x000000) {
                currColor = r.nextInt(maxColor);
                modPixels[i] = currColor;
                prevPixelColor = pixels[i];
            } else if (pixels[i] == 0xffffff && prevPixelColor == 0xffffff) {
                modPixels[i] = currColor;
                prevPixelColor = pixels[i];
            } else if (pixels[i] == 0x000000) {
                prevPixelColor = pixels[i];
                continue;
            } else {
                //Log.i("picsource", "pixel color = " + pixels[i]);
                //Log.i("picsource", "The Bitmap passed to applySearchAndFill() was not black and white.");
                continue;
                //return bm; // the color of every pixel must be black or white
                // this condition is only hit if the current pixel is a different color
            }
        }
        return modPixels;
    }

    public void onFloodFill(View v) {
        LinearLayout ll = (LinearLayout) findViewById(R.id.theLinearLayout);
        ll.removeAllViews();

        applyRandomFill(pixels, mod.getWidth(), mod.getHeight());
        mod.setPixels(pixels, 0, mod.getWidth(), 0, 0, mod.getWidth(), mod.getHeight());

        ImageView img = new ImageView(getApplicationContext());
        img.setImageBitmap(mod);
        img.setScaleType(ScaleType.FIT_CENTER);
        img.setPadding(0, 4, 4, 0);
        img.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        ll.addView(img);
    }

    // Random shitty code to call the flood fill??
    int [] applyRandomFill(int [] pixels, int width, int height) {
        boolean[] processed = new boolean[pixels.length];
        Arrays.fill(processed, false);
        Random r = new Random();
        int maxColor = 0xffffff;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                PixelNode currNode = new PixelNode(i, j, width, height, pixels, processed);
                floodFill(currNode, 0xffffff, r.nextInt(maxColor));
            }
        }
        return pixels;
    }

    // Flood-fill (node, target-color, replacement-color):
    void floodFill(PixelNode node, int targetColor, int replacementColor) {
        queue.add(node);

        while (!queue.isEmpty()) {
            //Log.e("picsource", "The queue:" + queue.toString());
            // 5.    Set n equal to the first element of Q.
            // 6.    Remove first element from Q.
            PixelNode n = queue.remove();
            // 7.     If the color of n is equal to target-color:
            if (n.getColor() == targetColor) {
                // 8.    Set the color of n to replacement-color and mark "n" as processed.
                n.setColor(replacementColor);
                n.markProcessed();
                // 9.    Add west node to end of Q if west has not been processed yet.
                PixelNode west = n.getWest();
                if (west != null && !west.isProcessed()) {
                    west.markProcessed();
                    queue.add(west);
                }
                // 10.   Add east node to end of Q if east has not been processed yet.
                PixelNode east = n.getEast();
                if (east != null && !east.isProcessed()) {
                    east.markProcessed();
                    queue.add(east);
                }
                // 11.   Add north node to end of Q if north has not been processed yet.
                PixelNode north = n.getNorth();
                if (north != null && !north.isProcessed()) {
                    north.markProcessed();
                    queue.add(north);
                }
                // 12.   Add south node to end of Q if south has not been processed yet.
                PixelNode south = n.getSouth();
                if (south != null && !south.isProcessed()) {
                    south.markProcessed();
                    queue.add(south);
                }
            }
        }
        //13. Return.
        return;
    }

    class PixelNode {
        int x;
        int y;
        int idx;
        int width;
        int height;
        int[] pixels;
        boolean [] processed;

        PixelNode(int x, int y, int width, int height, int[] pixels, boolean[] processed) {
            this.x = x;
            this.y = y;
            this.idx = x + (y * width);
            this.width = width;
            this.height = height;
            this.pixels = pixels;
            this.processed = processed;
        }
        public int getColor() {
            return pixels[idx];
        }
        public void setColor(int color) {
            pixels[idx] = color;
        }
        public boolean isProcessed() {
            return processed[idx];
        }
        public void markProcessed() {
            processed[idx] = true;
        }
        public PixelNode getNorth() {
            PixelNode node = new PixelNode(x, y - 1, width, height, pixels, processed);
            return node.isWithinBounds() ? node : null;
        }
        public PixelNode getSouth() {
            PixelNode node = new PixelNode(x, y + 1, width, height, pixels, processed);
            return node.isWithinBounds() ? node : null;
        }
        public PixelNode getEast() {
            PixelNode node = new PixelNode(x + 1, y, width, height, pixels, processed);
            return node.isWithinBounds() ? node : null;
        }
        public PixelNode getWest() {
            PixelNode node = new PixelNode(x - 1, y, width, height, pixels, processed);
            return node.isWithinBounds() ? node : null;
        }
        private boolean isWithinBounds() {
            if (x < width && x >= 0 && y < height && y >= 0) {
                return true;
            } else {
                return false;
            }
        }
        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }

    //public class CaitlynsBitmap extends Bitmap {
    //
    //}
}
