package com.caitoo0o.filler;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.io.File;

public class PicEditActivity extends AppCompatActivity {

    public static final String SOURCE_URI = "SOURCE_URI";

    private static final String SOURCE_STATE_KEY = "SOURCE_STATE_KEY";
    private static final String ROTATED_SOURCE_STATE_KEY = "ROTATED_SOURCE_STATE_KEY";
    private static final String MOD_STATE_KEY = "MOD_STATE_KEY";
    private static final String EFFECT_SELECTED_STATE_KEY = "EFFECT_SELECTED_STATE_KEY";

    private static final int MAX_UNDO_QUEUE_SIZE = 2;
    private static final int ROTATION_ANGLE = 90;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GET = 0;

    public enum Effect {
        NONE,
        RANDOM_BLEED_FILL,
        THRESHOLD,
        RANDOM_LINE_FILL,
        RANDOM_FLOOD_FILL
    }

    private BitmapWrapper bmW;
    private Deque<BitmapWrapper> undoQueue;
    private Deque<BitmapWrapper> redoQueue;
    private Uri mImageUri;
    private Effect effectSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_edit);

        getPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        getPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        getPermission(Manifest.permission.CAMERA);

        undoQueue = new ArrayDeque<>();
        redoQueue = new ArrayDeque<>();

        effectSelected = Effect.NONE;

        if (savedInstanceState != null) {
            Bitmap sourceImage = (Bitmap) savedInstanceState.getParcelable(SOURCE_STATE_KEY);
            Bitmap rotatedSourceImage = (Bitmap) savedInstanceState.getParcelable(ROTATED_SOURCE_STATE_KEY);
            Bitmap modImage = (Bitmap) savedInstanceState.getParcelable(MOD_STATE_KEY);
            Effect effectSelected = (Effect) savedInstanceState.getSerializable(EFFECT_SELECTED_STATE_KEY);
            if (sourceImage != null && modImage != null && rotatedSourceImage != null) {
                bmW = new BitmapWrapper(sourceImage);
                bmW.setRotatedSource(rotatedSourceImage);
                bmW.setModifiedBitmap(modImage);
                setHasImageActivityState(true);
                ((ImageView) findViewById(R.id.theImage)).setImageBitmap(bmW.getModifiedBitmap());
            } else {
                setHasImageActivityState(false);
            }
            LinearLayout ctrlLayout = (LinearLayout) findViewById(R.id.effect_ctrl_layout);
            if (effectSelected == Effect.NONE) {
                ctrlLayout.setVisibility(View.GONE);
            } else {
                ctrlLayout.setVisibility(View.VISIBLE);
            }
        } else if (getIntent().hasExtra(SOURCE_URI)) {
            Uri uri = (Uri)getIntent().getParcelableExtra(SOURCE_URI);
            ImageView iv = (ImageView) findViewById(R.id.theImage);
            bmW = new BitmapWrapper();
            BitmapLoaderAsyncTask bmwAsync = new BitmapLoaderAsyncTask(getApplicationContext(), iv, uri, bmW);
            bmwAsync.execute();
            //Bitmap sourceBm = grabImage((Uri)intent.getParcelableExtra(SOURCE_URI));
            //bmW = new BitmapWrapper(sourceBm);
            setHasImageActivityState(true);
        } else {
            // TODO: Start the pic source activity
            setHasImageActivityState(false);
            LinearLayout ctrlLayout = (LinearLayout) findViewById(R.id.effect_ctrl_layout);
            ctrlLayout.setVisibility(View.GONE);
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

    private void newEffectApplied() {
        redoQueue.clear();
        ((ImageButton) findViewById(R.id.redoButton)).setClickable(false);
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
            button.getDrawable().setColorFilter(0xff00ffff, PorterDuff.Mode.SRC_IN);
        }
    }

    private void undoEnqueue() {
        if (undoQueue.size() >= MAX_UNDO_QUEUE_SIZE) {
            undoQueue.removeFirst();
        }
        BitmapWrapper undoCopy = new BitmapWrapper(bmW.getSourceBitmap().copy(bmW.getSourceBitmap().getConfig(), true));
        undoCopy.setRotatedSource(bmW.getRotatedSource().copy(bmW.getRotatedSource().getConfig(), true));
        undoCopy.setModifiedBitmap(bmW.getModifiedBitmap().copy(bmW.getModifiedBitmap().getConfig(), true));
        undoQueue.addLast(undoCopy);
        ((ImageButton) findViewById(R.id.undoButton)).setClickable(true);
    }

    private void redoEnqueue() {
        // TODO: Do we need to check if the size gets exceeded? think its handled by undo checking
        BitmapWrapper redoCopy = new BitmapWrapper(bmW.getSourceBitmap().copy(bmW.getSourceBitmap().getConfig(), true));
        redoCopy.setRotatedSource(bmW.getRotatedSource().copy(bmW.getRotatedSource().getConfig(), true));
        redoCopy.setModifiedBitmap(bmW.getModifiedBitmap().copy(bmW.getModifiedBitmap().getConfig(), true));
        redoQueue.addLast(redoCopy);
        ((ImageButton) findViewById(R.id.redoButton)).setClickable(true);

    }

    public void onRedo(View v) {
        undoEnqueue();
        bmW = redoQueue.removeLast();
        if (redoQueue.isEmpty()) {
            ((ImageButton) findViewById(R.id.redoButton)).setClickable(false);
        }
        ((ImageView) findViewById(R.id.theImage)).setImageBitmap(bmW.getModifiedBitmap());
    }

    public void onUndo(View v) {
        redoEnqueue();
        bmW = undoQueue.removeLast();
        if (undoQueue.isEmpty()) {
            ((ImageButton) findViewById(R.id.undoButton)).setClickable(false);
        }
        ((ImageView) findViewById(R.id.theImage)).setImageBitmap(bmW.getModifiedBitmap());
    }

    public void onRotate(View v) {
        newEffectApplied();
        Matrix matrix = new Matrix();
        matrix.postRotate(ROTATION_ANGLE);
        bmW.setRotatedSource(Bitmap.createBitmap(bmW.getRotatedSource(), 0, 0, bmW.getWidth(), bmW.getHeight(), matrix, true));
        bmW.setModifiedBitmap(Bitmap.createBitmap(bmW.getModifiedBitmap(), 0, 0, bmW.getWidth(), bmW.getHeight(), matrix, true));
        ((ImageView) findViewById(R.id.theImage)).setImageBitmap(bmW.getModifiedBitmap());
    }

    public void onRotateLeft(View v) {
        ((ProgressBar) findViewById(R.id.progressBar)).setVisibility(View.VISIBLE);
        newEffectApplied();
        Matrix matrix = new Matrix();
        matrix.postRotate(-1 * ROTATION_ANGLE);
        bmW.setRotatedSource(Bitmap.createBitmap(bmW.getRotatedSource(), 0, 0, bmW.getWidth(), bmW.getHeight(), matrix, true));
        bmW.setModifiedBitmap(Bitmap.createBitmap(bmW.getModifiedBitmap(), 0, 0, bmW.getWidth(), bmW.getHeight(), matrix, true));
        ((ImageView) findViewById(R.id.theImage)).setImageBitmap(bmW.getModifiedBitmap());
        ((ProgressBar) findViewById(R.id.progressBar)).setVisibility(View.GONE);
    }

    public void onReset(View v) {
        ((ProgressBar) findViewById(R.id.progressBar)).setVisibility(View.VISIBLE);
        bmW.resetFromSource();
        ((ImageView) findViewById(R.id.theImage)).setImageBitmap(bmW.getModifiedBitmap());
        ((ProgressBar) findViewById(R.id.progressBar)).setVisibility(View.GONE);
    }

    public void onRandomBleedFill(View v) {
        effectSelected = Effect.RANDOM_BLEED_FILL;
        applyEffect();
        ImageView ctrlImage = (ImageView) findViewById(R.id.effect_ctrl_image);
        ctrlImage.setImageResource(R.drawable.filler_rbleed_icon);
        LinearLayout ctrlLayout = (LinearLayout) findViewById(R.id.effect_ctrl_layout);
        ctrlLayout.setVisibility(View.VISIBLE);
    }

    public void onThreshold(View v) {
        effectSelected = Effect.THRESHOLD;
        applyEffect();
        ImageView ctrlImage = (ImageView) findViewById(R.id.effect_ctrl_image);
        ctrlImage.setImageResource(R.drawable.filler_threshold_icon);
        LinearLayout ctrlLayout = (LinearLayout) findViewById(R.id.effect_ctrl_layout);
        ctrlLayout.setVisibility(View.VISIBLE);
    }

    public void onRandomLineFill(View v) {
        effectSelected = Effect.RANDOM_LINE_FILL;
        applyEffect();
        ImageView ctrlImage = (ImageView) findViewById(R.id.effect_ctrl_image);
        ctrlImage.setImageResource(R.drawable.filler_rline_icon);
        LinearLayout ctrlLayout = (LinearLayout) findViewById(R.id.effect_ctrl_layout);
        ctrlLayout.setVisibility(View.VISIBLE);
    }

    public void onRandomFloodFill(View v) {
        effectSelected = Effect.RANDOM_FLOOD_FILL;
        applyEffect();
        ImageView ctrlImage = (ImageView) findViewById(R.id.effect_ctrl_image);
        ctrlImage.setImageResource(R.drawable.filler_rfill_icon);
        LinearLayout ctrlLayout = (LinearLayout) findViewById(R.id.effect_ctrl_layout);
        ctrlLayout.setVisibility(View.VISIBLE);
    }

    public void onEffectApplyClicked(View v) {
        applyEffect();
    }

    private void applyEffect() {
        ((ProgressBar) findViewById(R.id.progressBar)).setVisibility(View.VISIBLE);
        setHasImageActivityState(false);
        double ctrl = getEffectCtrl();
        newEffectApplied();
        ImageView iv = (ImageView) findViewById(R.id.theImage);
        ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
        new BitmapEffectAsyncTask(this,getApplicationContext(),iv,pb,effectSelected,bmW,ctrl).execute();
    }

    public void doneProgress() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        setHasImageActivityState(true);
    }

    private double getEffectCtrl() {
        SeekBar ctrlSeekbar = (SeekBar) findViewById(R.id.effect_ctrl_seekbar);
        double progress = ctrlSeekbar.getProgress();
        double max = ctrlSeekbar.getMax();
        double ctrl = 1 - progress/max;
        return ctrl;
    }

    public void onCtrlLayoutExit(View v) {
        exitCtrlLayout();
    }

    private void exitCtrlLayout() {
        LinearLayout ctrlLayout = (LinearLayout) findViewById(R.id.effect_ctrl_layout);
        ctrlLayout.setVisibility(View.GONE);
        effectSelected = Effect.NONE;
        //SeekBar ctrlSeekbar = (SeekBar) findViewById(R.id.effect_ctrl_seekbar);
        //ctrlSeekbar.setProgress(ctrlSeekbar.getMax()/2);
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
            ((ImageButton) ll1.getChildAt(i)).setClickable(hasImage);
        }
        LinearLayout ll2 = (LinearLayout) findViewById(R.id.buttonLinearLayout);
        ll2.getChildCount();
        for (int i = 0; i < ll2.getChildCount(); i++) {
            ((ImageButton) ll2.getChildAt(i)).setClickable(hasImage);
        }
        ((ImageButton) findViewById(R.id.effect_ctrl_apply_button)).setClickable(hasImage);
        ((ImageButton) findViewById(R.id.undoButton)).setClickable(false);
        ((ImageButton) findViewById(R.id.redoButton)).setClickable(false);
    }

    public void onDiscard(View v) {
//        bmW = null;
//        exitCtrlLayout();
//        setHasImageActivityState(false);
//        removeCurrImage();
        startActivity(new Intent(this, PicSourceActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public void onCamera(View v) {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {

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
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        if (bmW != null) {
            outState.putParcelable(SOURCE_STATE_KEY, bmW.getSourceBitmap());
            outState.putParcelable(ROTATED_SOURCE_STATE_KEY, bmW.getRotatedSource());
            outState.putParcelable(MOD_STATE_KEY, bmW.getModifiedBitmap());
            // TODO: Is there any possible reason i would need to save the URI?
            // it should only be needed on return from an intent in which case state is preserved
            // TODO: Handle saving the undo and redo queues
        }
        else {
            outState.putParcelable(SOURCE_STATE_KEY, null);
            outState.putParcelable(ROTATED_SOURCE_STATE_KEY, null);
            outState.putParcelable(MOD_STATE_KEY, null);
            // TODO: Is there any possible reason i would need to save the URI?
            // it should only be needed on return from an intent in which case state is preserved
            // TODO: Handle saving the undo and redo queues
        }
        outState.putSerializable(EFFECT_SELECTED_STATE_KEY, effectSelected);
    }
}