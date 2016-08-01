package com.caitoo0o.filler;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.graphics.Bitmap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.io.File;

public class PicSourceActivity extends AppCompatActivity {

    Bitmap source;
    Bitmap mod;
    int [] pixels;

    private static final int ROTATION_ANGLE = 90;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_source);

        // TODO: put this in a function or something
        final int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    private File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e("picsource", "Directory not created");
        }
        return file;
    }

    private void reloadModImage() {
        LinearLayout ll = (LinearLayout) findViewById(R.id.theLinearLayout);
        ll.removeAllViews();
        ImageView img = new ImageView(getApplicationContext());
        img.setImageBitmap(mod);
        img.setScaleType(ScaleType.FIT_CENTER);
        img.setPadding(0, 4, 4, 0);
        img.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        ll.addView(img);
    }

    public void onRotate(View v) {
        Matrix matrix = new Matrix();
        matrix.postRotate(ROTATION_ANGLE);
        mod = Bitmap.createBitmap(mod, 0, 0, mod.getWidth(), mod.getHeight(), matrix, true);
        pixels = new int[mod.getWidth() * mod.getHeight()];
        mod.getPixels(pixels, 0, mod.getWidth(), 0, 0, mod.getWidth(), mod.getHeight());

        reloadModImage();
    }

    public void onReset(View v) {
        mod = source.copy(source.getConfig(), true);
        pixels = new int[mod.getWidth() * mod.getHeight()];
        mod.getPixels(pixels, 0, mod.getWidth(), 0, 0, mod.getWidth(), mod.getHeight());
        reloadModImage();
    }

    public void onThreshold(View v) {
        pixels = applyThreshold(pixels, mod.getWidth(), mod.getHeight());
        mod.setPixels(pixels, 0, mod.getWidth(), 0, 0, mod.getWidth(), mod.getHeight());
        reloadModImage();
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
        pixels = applyRandomLineFill(pixels, mod.getWidth(), mod.getHeight());
        mod.setPixels(pixels, 0, mod.getWidth(), 0, 0, mod.getWidth(), mod.getHeight());
        reloadModImage();
    }

    int[] applyRandomLineFill(int [] pixels, int width, int height) {
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

    public void onRandomFloodFill(View v) {
        applyRandomFill(pixels, mod.getWidth(), mod.getHeight());
        mod.setPixels(pixels, 0, mod.getWidth(), 0, 0, mod.getWidth(), mod.getHeight());
        reloadModImage();
    }

    public void onSave(View v) {
        if (isExternalStorageWritable()) {
            //File albumStorageDir = getAlbumStorageDir("FillerSaves");
            File albumStorageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
            File picFile = new File(albumStorageDir, "fillerPic.png");
            Log.e("picsource", "picfile: " + picFile.getAbsolutePath());
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(picFile);
                mod.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored
            } catch (Exception e) {
                // TODO: notify user
                Log.e("picsource", "failed save.");
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    // TODO: change this from e to i and notify user
                    Log.e("picsource", "file written to: " + picFile.getAbsolutePath());
                } catch (IOException e) {
                    // TODO: notify user
                    Log.e("picsource", "failed save.");
                    e.printStackTrace();
                }
            }
        }
        else {
            // TODO: notify user
            Log.e("picsource", "failed save. external storage not writeable");
        }
    }

    public void onOpen(View v) {
        if (isExternalStorageReadable()) {
            try {
                //File albumStorageDir = getAlbumStorageDir("FillerSaves");
                File albumStorageDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES);
                File picFile = new File(albumStorageDir, "fillerPic.jpg");
                Log.e("picsource", "picfile: " + picFile.getAbsolutePath());

                source = BitmapFactory.decodeFile(picFile.getAbsolutePath());
                source = Bitmap.createScaledBitmap(source,source.getWidth()/2,source.getHeight()/2,true);
                mod = source.copy(source.getConfig(), true);
                pixels = new int[mod.getWidth() * mod.getHeight()];
                mod.getPixels(pixels, 0, mod.getWidth(), 0, 0, mod.getWidth(), mod.getHeight());

                LinearLayout ll = (LinearLayout) findViewById(R.id.buttonLinearLayout);
                ll.getChildCount();
                for (int i = 0; i < ll.getChildCount(); i++) {
                    ll.getChildAt(i).setEnabled(true);
                }
                findViewById(R.id.saveButton).setEnabled(true);

                reloadModImage();
            }
            catch (Exception e) {
                // TODO: notify user
                Log.e("picsource", "open failed.");
            }
        }
        else {
            // TODO: notify user
            Log.e("picsource", "failed open. external storage not readable");
        }
    }

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
        Queue<PixelNode> queue = new LinkedList<PixelNode>();
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
