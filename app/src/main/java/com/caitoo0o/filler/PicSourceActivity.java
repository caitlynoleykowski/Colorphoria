package com.caitoo0o.filler;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
    Queue<int []> undoQueue;
    Queue<int []> redoQueue;

    private static final int MAX_IMAGE_DIMENSION = 1000;
    private static final int ROTATION_ANGLE = 90;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GET = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_source);

        getPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        getPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        getPermission(Manifest.permission.CAMERA);

        undoQueue = new LinkedList<int []>();
        redoQueue = new LinkedList<int []>();
    }

    void getPermission(String permissionKind) {
        final int REQUEST = 1;
        String[] PERMISSIONS_STORAGE = {
                permissionKind,
        };
        int permission = ActivityCompat.checkSelfPermission(this, permissionKind);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST
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

    private void removeCurrImage() {
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.imageAreaLayout);
        //rl.removeAllViews();
        ImageView currImg = (ImageView) findViewById(R.id.theImage);
        rl.removeView(currImg);
    }

    private void reloadCurrImage() {
        removeCurrImage();
        ImageView img = new ImageView(getApplicationContext());
        img.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                Log.e("picsource", "Hey!");
                return true;
            }
        });
        img.setId(R.id.theImage);
        img.setImageBitmap(mod);
        img.setScaleType(ScaleType.FIT_CENTER);
        img.setPadding(0, 4, 4, 0);
        img.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.imageAreaLayout);
        rl.addView(img);
    }

    public void onRotate(View v) {
        Matrix matrix = new Matrix();
        matrix.postRotate(ROTATION_ANGLE);
        mod = Bitmap.createBitmap(mod, 0, 0, mod.getWidth(), mod.getHeight(), matrix, true);
        pixels = new int[mod.getWidth() * mod.getHeight()];
        mod.getPixels(pixels, 0, mod.getWidth(), 0, 0, mod.getWidth(), mod.getHeight());

        reloadCurrImage();
    }

    public void onReset(View v) {
        mod = source.copy(source.getConfig(), true);
        pixels = new int[mod.getWidth() * mod.getHeight()];
        mod.getPixels(pixels, 0, mod.getWidth(), 0, 0, mod.getWidth(), mod.getHeight());
        reloadCurrImage();
    }

    public void onThreshold(View v) {
        pixels = applyThreshold(pixels, mod.getWidth(), mod.getHeight());
        mod.setPixels(pixels, 0, mod.getWidth(), 0, 0, mod.getWidth(), mod.getHeight());
        reloadCurrImage();
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
        reloadCurrImage();
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
        reloadCurrImage();
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
                Toast.makeText(getApplicationContext(), "Save failed.", Toast.LENGTH_LONG).show();
                Log.e("picsource", "failed save.");
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    Toast.makeText(getApplicationContext(), "Image saved.", Toast.LENGTH_LONG).show();
                    Log.i("picsource", "file written to: " + picFile.getAbsolutePath());
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Save failed.", Toast.LENGTH_LONG).show();
                    Log.e("picsource", "failed save.");
                    e.printStackTrace();
                }
            }
        }
        else {
            // TODO: move these strings to strings file
            Toast.makeText(getApplicationContext(), "Save failed. External storage not writeable.", Toast.LENGTH_LONG).show();
            Log.e("picsource", "failed save. external storage not writeable");
        }
    }

    private void addImageFromNewSource() {
        // TODO: Fix this
//        if (source.getWidth() >= MAX_IMAGE_DIMENSION &&  source.getHeight() >= MAX_IMAGE_DIMENSION && source.getWidth() > source.getHeight()) {
//            int height = MAX_IMAGE_DIMENSION / source.getWidth() * source.getHeight();
//            source = Bitmap.createScaledBitmap(source, MAX_IMAGE_DIMENSION, height,true);
//        } else if (source.getWidth() >= MAX_IMAGE_DIMENSION && source.getHeight() >= MAX_IMAGE_DIMENSION && source.getHeight() > source.getWidth()) {
//            int width = MAX_IMAGE_DIMENSION/source.getHeight() * source.getWidth();
//            source = Bitmap.createScaledBitmap(source, width, MAX_IMAGE_DIMENSION, true);
//        }

        // TODO: Fix this logic and remove hardcoded numbersageFromNewSource() {
        if (source.getWidth() >= 1000 &&  source.getHeight() >= 1000 && source.getWidth() > source.getHeight()) {
            source = Bitmap.createScaledBitmap(source, source.getWidth()/2, source.getHeight()/2,true);
        }

        mod = source.copy(source.getConfig(), true);
        pixels = new int[mod.getWidth() * mod.getHeight()];
        mod.getPixels(pixels, 0, mod.getWidth(), 0, 0, mod.getWidth(), mod.getHeight());

        LinearLayout ll1 = (LinearLayout) findViewById(R.id.firstButtonLinearLayout);
        ll1.getChildCount();
        for (int i = 0; i < ll1.getChildCount(); i++) {
            ll1.getChildAt(i).setEnabled(true);
        }
        LinearLayout ll2 = (LinearLayout) findViewById(R.id.buttonLinearLayout);
        ll2.getChildCount();
        for (int i = 0; i < ll2.getChildCount(); i++) {
            ll2.getChildAt(i).setEnabled(true);
        }
        findViewById(R.id.cameraButton).setVisibility(View.GONE);
        findViewById(R.id.openExistingButton).setVisibility(View.GONE);
        reloadCurrImage();
    }

    public void onOpen(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        intent.putExtra("outputX", 256);
        intent.putExtra("outputY", 256);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("return-data", true);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET);
        }
    }

    public void onDiscard(View v) {
        source = null;
        mod = null;

        LinearLayout ll1 = (LinearLayout) findViewById(R.id.firstButtonLinearLayout);
        ll1.getChildCount();
        for (int i = 0; i < ll1.getChildCount(); i++) {
            ll1.getChildAt(i).setEnabled(false);
        }
        LinearLayout ll2 = (LinearLayout) findViewById(R.id.buttonLinearLayout);
        ll2.getChildCount();
        for (int i = 0; i < ll2.getChildCount(); i++) {
            ll2.getChildAt(i).setEnabled(false);
        }
        findViewById(R.id.cameraButton).setVisibility(View.VISIBLE);
        findViewById(R.id.openExistingButton).setVisibility(View.VISIBLE);
        removeCurrImage();
    }

    public void onCamera(View v) {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
            else {
                Toast.makeText(getApplicationContext(), "No known app to handle camera use.", Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "Device has no camera.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            Log.e("picsource", "No Image Selected.");
            Toast.makeText(getApplicationContext(), "No Image Selected", Toast.LENGTH_LONG).show();
            return;
        }

        if (requestCode == REQUEST_IMAGE_GET) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();

                try {
                    source = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    addImageFromNewSource();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "No image selection found.", Toast.LENGTH_LONG).show();
                    Log.e("picsource", "Failed getting bitmap from uri from image picker.");
                    e.printStackTrace();
                }
            } else {
                Log.e("picsource", "data or data.getData from image picker intent is null.");
                Toast.makeText(getApplicationContext(), "No image selection found.", Toast.LENGTH_LONG).show();
            }
        }
        else if (requestCode == REQUEST_IMAGE_CAPTURE) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                source = (Bitmap) extras.get("data");
                addImageFromNewSource();
            } else {
                Log.e("picsource", "Extras return from camera intent is null.");
                Toast.makeText(getApplicationContext(), "No image capture found.", Toast.LENGTH_LONG).show();
            }
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
