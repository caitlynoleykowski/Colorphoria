package com.caitoo0o.filler;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.graphics.Bitmap;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.io.File;

public class PicSourceActivity extends AppCompatActivity {

    private static final String SOURCE_STATE_KEY = "source";
    private static final String MOD_STATE_KEY = "mod";
    private static final String PIXELS_STATE_KEY = "pixels";
    private static final int MAX_UNDO_QUEUE_SIZE = 8;
    private static final int ROTATION_ANGLE = 90;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GET = 0;
    private static final int SIGNIFICANT_CHANGE_AMOUNT = 0xff;

    BitmapWrapper bmW;
    Deque<Bitmap> undoQueue;
    Deque<Bitmap> redoQueue;
    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_source);

        getPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        getPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        getPermission(Manifest.permission.CAMERA);

        undoQueue = new ArrayDeque<>();
        redoQueue = new ArrayDeque<>();
        if (savedInstanceState != null) {
            Bitmap sourceImage = (Bitmap) savedInstanceState.getParcelable(SOURCE_STATE_KEY);
            Bitmap modImage = (Bitmap) savedInstanceState.getParcelable(MOD_STATE_KEY);
            if (sourceImage != null && modImage != null) {
                bmW = new BitmapWrapper(sourceImage);
                bmW.setModifiedBitmap(modImage);
                setHasImageActivityState(true);
                reloadCurrImage();
            }
            else {
                setHasImageActivityState(false);
            }
        }
        else {
            setHasImageActivityState(false);
        }
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

    private void removeCurrImage() {
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.imageAreaLayout);
        //rl.removeAllViews();
        ImageView currImg = (ImageView) findViewById(R.id.theImage);
        rl.removeView(currImg);
    }

    private void reloadCurrImage() {
        removeCurrImage();
        ImageView img = new ImageView(getApplicationContext());
        // TODO: Remove this unless you want a longclick
        img.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                Log.e("picsource", "Hey!");
                return true;
            }
        });
        img.setId(R.id.theImage);
        if (bmW.getModifiedBitmap() != null) {
            img.setImageBitmap(bmW.getModifiedBitmap());
        }
        img.setScaleType(ScaleType.FIT_CENTER);
        img.setPadding(0, 4, 4, 0);
        img.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.imageAreaLayout);
        rl.addView(img);
    }

    private void newEffectApplied() {
        redoQueue.clear();
        setImageButtonClickable(findViewById(R.id.redoButton), false);
        undoEnqueue();
    }

    // TODO: If you make an imagebutton class this could be a function
    private void setImageButtonClickable(View view, boolean clickable) {
        // TODO: Maybe change this to a selector in the layout.xml??
        // http://stackoverflow.com/questions/7228985/android-imagebutton-with-disabled-ui-feel
        ImageButton button = (ImageButton) view;
        button.setClickable(clickable);
        if (clickable) {
            button.getDrawable().setColorFilter(null);
        } else {
            button.getDrawable().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        }
    }

    private void undoEnqueue() {
        if (undoQueue.size() >= MAX_UNDO_QUEUE_SIZE) {
            undoQueue.removeFirst();
        }
        undoQueue.addLast(bmW.getModifiedBitmap().copy(bmW.getModifiedBitmap().getConfig(), true));
        setImageButtonClickable(findViewById(R.id.undoButton), true);
    }

    private void redoEnqueue() {
        // TODO: Do we need to check if the size gets exceeded? think its handled by undo checking
        redoQueue.addLast(bmW.getModifiedBitmap().copy(bmW.getModifiedBitmap().getConfig(), true));
        setImageButtonClickable(findViewById(R.id.redoButton), true);
    }

    public void onRedo(View v) {
        undoEnqueue();
        bmW.setModifiedBitmap(redoQueue.removeLast());
        if (redoQueue.isEmpty()) {
            setImageButtonClickable(findViewById(R.id.redoButton), false);
        }
        reloadCurrImage();
    }

    public void onUndo(View v) {
        redoEnqueue();
        bmW.setModifiedBitmap(undoQueue.removeLast());
        if (undoQueue.isEmpty()) {
            setImageButtonClickable(findViewById(R.id.undoButton), false);
        }
        reloadCurrImage();
    }

    public void onRotate(View v) {
        newEffectApplied();
        Matrix matrix = new Matrix();
        matrix.postRotate(ROTATION_ANGLE);
        bmW.setModifiedBitmap(Bitmap.createBitmap(bmW.getModifiedBitmap(), 0, 0, bmW.getWidth(), bmW.getHeight(), matrix, true));
        reloadCurrImage();
    }

    public void onReset(View v) {
        bmW.resetFromSource();
        reloadCurrImage();
    }

    public void onThreshold(View v) {
        newEffectApplied();
        applyThreshold(bmW);
        reloadCurrImage();
    }

    void applyThreshold(BitmapWrapper bmW) {
        for (int y = 0; y < bmW.getHeight(); y++) {
            for (int x = 0; x < bmW.getWidth(); x++) {
                PixelNode pixel = bmW.getPixel(x, y);
                int red = (pixel.getColor() >> 16) & 0xFF;
                int green = (pixel.getColor() >> 8) & 0xFF;
                int blue = (pixel.getColor() >> 0) & 0xFF;
                int average = (red + blue + green) / 3;
                if (average >= (255 / 2)) {
                    pixel.setColor(0xffffff);
                } else {
                    pixel.setColor(0x000000);
                }
            }
        }
        bmW.doneProcessing();
    }

    public void onRandomLineFill(View v) {
        newEffectApplied();
        applyRandomLineFill(bmW);
        reloadCurrImage();
    }

    void applyRandomLineFill(BitmapWrapper bmW) {
        int prevPixelColor = 0x000000;
        int currColor = 0;
        Random r = new Random();
        int maxColor = 0xffffff;
        for (int y = 0; y < bmW.getHeight(); y++) {
            for (int x = 0; x < bmW.getWidth(); x++) {
                PixelNode pixel = bmW.getPixel(x, y);
                if (pixel.getColor() == 0xffffff && prevPixelColor == 0x000000) {
                    currColor = r.nextInt(maxColor);
                    prevPixelColor = pixel.getColor();
                    pixel.setColor(currColor);
                } else if (pixel.getColor() == 0xffffff && prevPixelColor == 0xffffff) {
                    prevPixelColor = pixel.getColor();
                    pixel.setColor(currColor);
                } else if (pixel.getColor() == 0x000000) {
                    prevPixelColor = pixel.getColor();
                } else {
                    // this condition is only hit if the current pixel is a different color
                    prevPixelColor = pixel.getColor();
                }
            }
        }
        bmW.doneProcessing();
    }

    public void onRandomFloodFill(View v) {
        newEffectApplied();
        applyRandomFill(bmW);
        reloadCurrImage();
    }

    public void applyRandomFill(BitmapWrapper bmW) {
        Random r = new Random();
        int maxColor = 0xffffff;
        for (int i = 0; i < bmW.getWidth(); i++) {
            for (int j = 0; j < bmW.getHeight(); j++) {
                PixelNode currNode = bmW.getPixel(i, j);
                floodFill(currNode, 0xffffff, r.nextInt(maxColor));
            }
        }
        bmW.doneProcessing();
    }

    // Flood-fill (node, target-color, replacement-color):
    private void floodFill(PixelNode node, int targetColor, int replacementColor) {
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

    public void onRandomBleedFill(View v) {
        newEffectApplied();
        applyRandomBleedFill(bmW);
        reloadCurrImage();
    }

    public void applyRandomBleedFill(BitmapWrapper bmW) {
        Random r = new Random();
        int maxColor = 0xffffff;
        for (int i = 0; i < bmW.getWidth(); i++) {
            for (int j = 0; j < bmW.getHeight(); j++) {
                PixelNode currNode = bmW.getPixel(i, j);
                if (!currNode.isProcessed()) {
                    int targetColor = currNode.getColor();
                    bleedFill(currNode, targetColor, r.nextInt(maxColor));
                }
            }
        }
        bmW.doneProcessing();
    }

    // Flood-fill (node, target-color, replacement-color):
    void bleedFill(PixelNode node, int targetColor, int replacementColor) {
        Queue<PixelNode> queue = new LinkedList<PixelNode>();
        queue.add(node);

        while (!queue.isEmpty()) {
            //Log.e("picsource", "The queue:" + queue.toString());
            // 5.    Set n equal to the first element of Q.
            // 6.    Remove first element from Q.
            PixelNode n = queue.remove();
            // 7.     If the color of n is not significantly different than the target-color:
            boolean sigChange = false;
            int pixelCurrColor = n.getColor();
            int changeInRed = Math.abs(getRed(pixelCurrColor) - getRed(targetColor));
            int changeInGreen = Math.abs(getGreen(pixelCurrColor) - getGreen(targetColor));
            int changeInBlue = Math.abs(getBlue(pixelCurrColor) - getBlue(targetColor));
            int overallChange = changeInRed + changeInGreen + changeInBlue;
            if (overallChange > SIGNIFICANT_CHANGE_AMOUNT) {
                sigChange = true;
            }
            if (!sigChange) {
                // 8.    Set the color of n to adjusted replacement-color and mark "n" as processed.
                int adjustedReplacementColor;
                int pixelDarkness = getRed(pixelCurrColor) + getGreen(pixelCurrColor) + getBlue(pixelCurrColor);
                int darknessInRepColor = getRed(replacementColor) + getGreen(replacementColor) + getBlue(replacementColor);
                double redRatioInRepColor, greenRatioInRepColor, blueRatioInRepColor;
                if (darknessInRepColor > 0) {
                    redRatioInRepColor = (double)getRed(replacementColor) / (double)darknessInRepColor;
                    greenRatioInRepColor = (double)getGreen(replacementColor) / (double)darknessInRepColor;
                    blueRatioInRepColor = (double)getBlue(replacementColor) / (double)darknessInRepColor;
                } else {
                    redRatioInRepColor = 0;
                    greenRatioInRepColor = 0;
                    blueRatioInRepColor = 0;
                }
                int redInAdjRepColor = (int) (redRatioInRepColor * pixelDarkness);
                int greenInAdjRepColor = (int) (greenRatioInRepColor * pixelDarkness);
                int blueInAdjRepColor = (int) (blueRatioInRepColor * pixelDarkness);
                adjustedReplacementColor = (redInAdjRepColor << 16) + (greenInAdjRepColor << 8) + blueInAdjRepColor;
                n.setColor(adjustedReplacementColor);
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

    private int getRed(int color) {
        return (color & 0xff0000)>> 16;
    }

    private int getGreen(int color) {
        return (color &  0x00ff00) >> 8;
    }

    private int getBlue(int color) {
        return (color & 0x0000ff) >> 0;
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

    public void saveCurrImage() {
        if (isExternalStorageWritable()) {
            //File albumStorageDir = getAlbumStorageDir("FillerSaves");
            File pictureStorageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
            File albumStorageDir = new File(pictureStorageDir, "Filler");
            try {
                albumStorageDir.mkdir();
            }
            catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Failed to create directory", Toast.LENGTH_LONG).show();
                Log.e("picsource", "Failed to create Filler directory.");
                e.printStackTrace();
            }
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyyhhmmss");
            String formattedDate = sdf.format(date);
            File picFile = new File(albumStorageDir, "Filler" + formattedDate + ".png");
            Log.e("picsource", "picfile: " + picFile.getAbsolutePath());
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(picFile);
                bmW.getModifiedBitmap().compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
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

    public void onSave(View v) {
        saveCurrImage();
    }

    public void onShare(View v) {
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bmW.getModifiedBitmap(), "Export from Filler", null);
        Uri uri = Uri.parse(path);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, "Share Image"));
        }
    }

    private void addImageFromNewSource(Bitmap bm) {
        bmW = new BitmapWrapper(bm);
        setHasImageActivityState(true);
        reloadCurrImage();
    }

    public void onOpen(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra("return-data", true);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET);
        }
    }

    private void setHasImageActivityState(boolean hasImage) {
        LinearLayout ll1 = (LinearLayout) findViewById(R.id.firstButtonLinearLayout);
        ll1.getChildCount();
        for (int i = 0; i < ll1.getChildCount(); i++) {
            setImageButtonClickable(ll1.getChildAt(i), hasImage);
        }
        LinearLayout ll2 = (LinearLayout) findViewById(R.id.buttonLinearLayout);
        ll2.getChildCount();
        for (int i = 0; i < ll2.getChildCount(); i++) {
            setImageButtonClickable(ll2.getChildAt(i), hasImage);
        }
        setImageButtonClickable(findViewById(R.id.undoButton), false);
        setImageButtonClickable(findViewById(R.id.redoButton), false);
        if (hasImage) {
            findViewById(R.id.cameraButton).setVisibility(View.GONE);
            findViewById(R.id.openExistingButton).setVisibility(View.GONE);
        } else {
            findViewById(R.id.cameraButton).setVisibility(View.VISIBLE);
            findViewById(R.id.openExistingButton).setVisibility(View.VISIBLE);
        }
    }

    public void onDiscard(View v) {
        bmW = null;

        setHasImageActivityState(false);
        removeCurrImage();
    }

    public void onCamera(View v) {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
//            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//            }
//            else {
//                Toast.makeText(getApplicationContext(), "No known app to handle camera use.", Toast.LENGTH_LONG).show();
//            }

            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            File photo;
            try
            {
                // place where to store camera taken picture
                photo = this.createTemporaryFile("picture", ".jpg");
                photo.delete();
            }
            catch(Exception e)
            {
                Log.v("picsource", "Can't create file to take picture!");
                Toast.makeText(this, "Please check SD card! Image shot is impossible!", Toast.LENGTH_LONG).show();
                return;
            }
            mImageUri = Uri.fromFile(photo);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
            //start camera intent
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
        else {
            Toast.makeText(getApplicationContext(), "Device has no camera.", Toast.LENGTH_LONG).show();
        }
    }

    private File createTemporaryFile(String part, String ext) throws Exception
    {
        File tempDir= Environment.getExternalStorageDirectory();
        tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
        if(!tempDir.exists())
        {
            tempDir.mkdirs();
        }
        return File.createTempFile(part, ext, tempDir);
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
                    addImageFromNewSource(MediaStore.Images.Media.getBitmap(getContentResolver(), uri));
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
            Bitmap image = grabImage();
            if (image != null) {
                addImageFromNewSource(grabImage());
            }
        }
    }

    public Bitmap grabImage()
    {
        getContentResolver().notifyChange(mImageUri, null);
        ContentResolver cr = this.getContentResolver();
        Bitmap bitmap;
        try
        {
            bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, mImageUri);
            return bitmap;
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            Log.d("picsource", "Failed to load image", e);
            return null;
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        if (bmW != null) {
            outState.putParcelable(SOURCE_STATE_KEY, bmW.getSourceBitmap());
            outState.putParcelable(MOD_STATE_KEY, bmW.getModifiedBitmap());
            // TODO: Is there any possible reason i would need to save the URI?
            // it should only be needed on return from an intent in which case state is preserved
            // TODO: Handle saving the undo and redo queues
        }
        else {
            outState.putParcelable(SOURCE_STATE_KEY, null);
            outState.putParcelable(MOD_STATE_KEY, null);
            // TODO: Is there any possible reason i would need to save the URI?
            // it should only be needed on return from an intent in which case state is preserved
            // TODO: Handle saving the undo and redo queues
        }
    }

    //public class CaitlynsBitmap extends Bitmap {
    //
    //}
}
