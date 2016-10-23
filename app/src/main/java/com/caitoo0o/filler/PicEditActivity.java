package com.caitoo0o.filler;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
    private static final String ROTATION_ANGLE_STATE_KEY = "ROTATION_ANGLE_STATE_KEY";
    private static final String MOD_STATE_KEY = "MOD_STATE_KEY";
    private static final String EFFECT_SELECTED_STATE_KEY = "EFFECT_SELECTED_STATE_KEY";

    private static final int MAX_UNDO_QUEUE_SIZE = 2;

    public enum Effect {
        NONE,
        RESET,
        ROTATE_LEFT,
        ROTATE_RIGHT,
        RANDOM_BLEED_FILL,
        THRESHOLD,
        RANDOM_LINE_FILL,
        RANDOM_FLOOD_FILL
    }

    private BitmapWrapper bmW;
    private Deque<BitmapWrapper> undoQueue;
    private Deque<BitmapWrapper> redoQueue;
    private Effect effectSelected;

    private Uri sourceUri;

    // Begin activity lifecycle handlers

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pic_edit);

        View outerLinearLayout = findViewById(R.id.outer_linear_layout);
        outerLinearLayout.setOnTouchListener(new OnSwipeTouchListener(PicEditActivity.this) {
            public void onSwipeTop() {
                Toast.makeText(PicEditActivity.this, "top", Toast.LENGTH_SHORT).show();
            }

            public void onSwipeRight() {
                Toast.makeText(PicEditActivity.this, "right", Toast.LENGTH_SHORT).show();
                goToPicSourceActivity(sourceUri);
            }

            public void onSwipeLeft() {
                Toast.makeText(PicEditActivity.this, "left", Toast.LENGTH_SHORT).show();
                goToBrowseActivity(sourceUri);
            }

            public void onSwipeBottom() {
                Toast.makeText(PicEditActivity.this, "bottom", Toast.LENGTH_SHORT).show();
            }

        });

        getPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        getPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        getPermission(Manifest.permission.CAMERA);

        undoQueue = new ArrayDeque<>();
        redoQueue = new ArrayDeque<>();

        effectSelected = Effect.NONE;

        if (savedInstanceState != null) {
            sourceUri = savedInstanceState.getParcelable(SOURCE_URI);
            Bitmap sourceImage = (Bitmap) savedInstanceState.getParcelable(SOURCE_STATE_KEY);
            int rotationAngle = savedInstanceState.getInt(ROTATION_ANGLE_STATE_KEY);
            Bitmap modImage = (Bitmap) savedInstanceState.getParcelable(MOD_STATE_KEY);
            Effect effectSelected = (Effect) savedInstanceState.getSerializable(EFFECT_SELECTED_STATE_KEY);
            if (sourceImage != null && modImage != null) {
                bmW = new BitmapWrapper(sourceImage);
                bmW.setRotationAngle(rotationAngle);
                bmW.setModifiedBitmap(modImage);
                setUIforImageDoneProcessing(true);
                ((ImageView) findViewById(R.id.theImage)).setImageBitmap(bmW.getModifiedBitmap());
                switch (effectSelected) {
                    case NONE:
                        // do nothing
                        break;
                    case RESET:
                        // do nothing
                        break;
                    case ROTATE_LEFT:
                        // do nothing
                        break;
                    case ROTATE_RIGHT:
                        // do nothing
                        break;
                    case RANDOM_BLEED_FILL:
                        showCtrlLayout(R.drawable.filler_rbleed_icon);
                        break;
                    case THRESHOLD:
                        showCtrlLayout(R.drawable.filler_threshold_icon);
                        break;
                    case RANDOM_LINE_FILL:
                        showCtrlLayout(R.drawable.filler_rline_icon);
                        break;
                    case RANDOM_FLOOD_FILL:
                        showCtrlLayout(R.drawable.filler_rfill_icon);
                        break;
                }
            } else {
                setUIforImageDoneProcessing(false);
            }
            LinearLayout ctrlLayout = (LinearLayout) findViewById(R.id.effect_ctrl_layout);
            if (effectSelected == Effect.NONE) {
                ctrlLayout.setVisibility(View.GONE);
            } else {
                ctrlLayout.setVisibility(View.VISIBLE);
            }
        } else if (getIntent().hasExtra(SOURCE_URI)) {
            sourceUri = (Uri)getIntent().getParcelableExtra(SOURCE_URI);
            Uri uri = sourceUri;
            ImageView iv = (ImageView) findViewById(R.id.theImage);
            bmW = new BitmapWrapper();
            BitmapLoaderAsyncTask bmwAsync = new BitmapLoaderAsyncTask(getApplicationContext(), iv, uri, bmW);
            bmwAsync.execute();
            setUIforImageDoneProcessing(true);
        } else {
            // TODO: Error/bug, shouldn't get here
            setUIforImageDoneProcessing(false);
            LinearLayout ctrlLayout = (LinearLayout) findViewById(R.id.effect_ctrl_layout);
            ctrlLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        if (bmW != null) {
            outState.putParcelable(SOURCE_STATE_KEY, bmW.getSourceBitmap());
            outState.putInt(ROTATION_ANGLE_STATE_KEY, bmW.getRotationAngle());
            outState.putParcelable(MOD_STATE_KEY, bmW.getModifiedBitmap());
            // TODO: Is there any possible reason i would need to save the URI?
            // it should only be needed on return from an intent in which case state is preserved
            // TODO: Handle saving the undo and redo queues
        }
        else {
            outState.putParcelable(SOURCE_STATE_KEY, null);
            outState.putInt(ROTATION_ANGLE_STATE_KEY, bmW.getRotationAngle());
            outState.putParcelable(MOD_STATE_KEY, null);
            // TODO: Is there any possible reason i would need to save the URI?
            // it should only be needed on return from an intent in which case state is preserved
            // TODO: Handle saving the undo and redo queues
        }
        outState.putSerializable(EFFECT_SELECTED_STATE_KEY, effectSelected);
        outState.putParcelable(SOURCE_URI, sourceUri);
    }

    // End activity lifecycle handlers

    // Begin device helpers

    private void getPermission(String permissionKind) {
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

    // End device helpers

    // Begin UI handlers

    public void onDiscard(View v) {
        goToPicSourceActivity(sourceUri);
    }

    public void onReset(View v) {
        showCtrlLayout(R.drawable.filler_rbleed_icon);
        effectSelected = Effect.RESET;
        applyEffect();
    }

    public void onRedo(View v) {
        undoEnqueue();
        bmW = redoQueue.removeLast();
        if (redoQueue.isEmpty()) {
            setImageButtonClickable(findViewById(R.id.redoButton), false);
        }
        ((ImageView) findViewById(R.id.theImage)).setImageBitmap(bmW.getModifiedBitmap());
    }

    public void onUndo(View v) {
        redoEnqueue();
        bmW = undoQueue.removeLast();
        if (undoQueue.isEmpty()) {
            setImageButtonClickable(findViewById(R.id.undoButton), false);
        }
        ((ImageView) findViewById(R.id.theImage)).setImageBitmap(bmW.getModifiedBitmap());
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

    public void onRotateRight(View v) {
        exitCtrlLayout();
        effectSelected = Effect.ROTATE_RIGHT;
        applyEffect();
    }

    public void onRotateLeft(View v) {
        exitCtrlLayout();
        effectSelected = Effect.ROTATE_LEFT;
        applyEffect();
    }

    public void onRandomBleedFill(View v) {
        effectSelected = Effect.RANDOM_BLEED_FILL;
        applyEffect();
        showCtrlLayout(R.drawable.filler_rbleed_icon);
    }

    public void onThreshold(View v) {
        effectSelected = Effect.THRESHOLD;
        applyEffect();
        showCtrlLayout(R.drawable.filler_threshold_icon);
    }

    public void onRandomLineFill(View v) {
        effectSelected = Effect.RANDOM_LINE_FILL;
        applyEffect();
        showCtrlLayout(R.drawable.filler_rline_icon);
    }

    public void onRandomFloodFill(View v) {
        effectSelected = Effect.RANDOM_FLOOD_FILL;
        applyEffect();
        showCtrlLayout(R.drawable.filler_rfill_icon);
    }

    public void onEffectApplyClicked(View v) {
        applyEffect();
    }

    public void onCtrlLayoutExit(View v) {
        exitCtrlLayout();
    }

    // End UI Handlers

    // Begin UI Helpers

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

    // public to be used by BitmapEffectAsyncTask
    public void doneProgress() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        setUIforImageDoneProcessing(true);
    }

    private void showCtrlLayout(int resDrawableId) {
        ImageView ctrlImage = (ImageView) findViewById(R.id.effect_ctrl_image);
        ctrlImage.setImageResource(resDrawableId);
        LinearLayout ctrlLayout = (LinearLayout) findViewById(R.id.effect_ctrl_layout);
        ctrlLayout.setVisibility(View.VISIBLE);
    }

    private void exitCtrlLayout() {
        LinearLayout ctrlLayout = (LinearLayout) findViewById(R.id.effect_ctrl_layout);
        ctrlLayout.setVisibility(View.GONE);
        effectSelected = Effect.NONE;
        //SeekBar ctrlSeekbar = (SeekBar) findViewById(R.id.effect_ctrl_seekbar);
        //ctrlSeekbar.setProgress(ctrlSeekbar.getMax()/2);
    }

    private void setUIforImageDoneProcessing(boolean processingImageDone) {
        LinearLayout ll1 = (LinearLayout) findViewById(R.id.firstButtonLinearLayout);
        ll1.getChildCount();
        for (int i = 0; i < ll1.getChildCount(); i++) {
            setImageButtonClickable(ll1.getChildAt(i), processingImageDone);
        }
        LinearLayout ll2 = (LinearLayout) findViewById(R.id.buttonLinearLayout);
        ll2.getChildCount();
        for (int i = 0; i < ll2.getChildCount(); i++) {
            ((ImageButton) ll2.getChildAt(i)).setClickable(processingImageDone);
        }
        setImageButtonClickable(findViewById(R.id.rotate_left), processingImageDone);
        setImageButtonClickable(findViewById(R.id.rotate_right), processingImageDone);
        setImageButtonClickable(findViewById(R.id.effect_ctrl_apply_button), processingImageDone);
        if (processingImageDone) {
            if (undoQueue.size() > 0) {
                setImageButtonClickable(findViewById(R.id.undoButton), true);
            } else {
                setImageButtonClickable(findViewById(R.id.undoButton), false);
            }
            if (redoQueue.size() > 0) {
                setImageButtonClickable(findViewById(R.id.redoButton), true);
            } else {
                setImageButtonClickable(findViewById(R.id.redoButton), false);
            }
        } else {
            setImageButtonClickable(findViewById(R.id.undoButton), false);
            setImageButtonClickable(findViewById(R.id.redoButton), false);
        }
    }

    private void goToPicSourceActivity(Uri uri) {
        Intent intent = new Intent(this, PicSourceActivity.class);
        intent.putExtra(PicSourceActivity.IMAGE_URI_STATE_KEY, uri);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void goToBrowseActivity(Uri uri) {
        Intent intent = new Intent(this, Browse.class);
        intent.putExtra(Browse.IMAGE_URI_STATE_KEY, uri);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    // End UI Helpers

    // Begin business logic

    private void undoEnqueue() {
        if (undoQueue.size() >= MAX_UNDO_QUEUE_SIZE) {
            undoQueue.removeFirst();
        }
        BitmapWrapper undoCopy = new BitmapWrapper(bmW.getSourceBitmap().copy(bmW.getSourceBitmap().getConfig(), true));
        undoCopy.setModifiedBitmap(bmW.getModifiedBitmap().copy(bmW.getModifiedBitmap().getConfig(), true));
        undoQueue.addLast(undoCopy);
        setImageButtonClickable(findViewById(R.id.undoButton), true);
    }

    private void redoEnqueue() {
        // TODO: Do we need to check if the size gets exceeded? think its handled by undo checking
        BitmapWrapper redoCopy = new BitmapWrapper(bmW.getSourceBitmap().copy(bmW.getSourceBitmap().getConfig(), true));
        redoCopy.setModifiedBitmap(bmW.getModifiedBitmap().copy(bmW.getModifiedBitmap().getConfig(), true));
        redoQueue.addLast(redoCopy);
        setImageButtonClickable(findViewById(R.id.redoButton), true);
    }

    private void updateUndoRedoQueues() {
        redoQueue.clear();
        ((ImageButton) findViewById(R.id.redoButton)).setClickable(false);
        undoEnqueue();
    }

    private void applyEffect() {
        ((ProgressBar) findViewById(R.id.progressBar)).setVisibility(View.VISIBLE);
        updateUndoRedoQueues();
        setUIforImageDoneProcessing(false);
        double ctrl = getEffectCtrl();
        ImageView iv = (ImageView) findViewById(R.id.theImage);
        ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
        new BitmapEffectAsyncTask(this,getApplicationContext(),iv,pb,effectSelected,bmW,ctrl).execute();
    }

    private double getEffectCtrl() {
        SeekBar ctrlSeekbar = (SeekBar) findViewById(R.id.effect_ctrl_seekbar);
        double progress = ctrlSeekbar.getProgress();
        double max = ctrlSeekbar.getMax();
        double ctrl = 1 - progress/max;
        return ctrl;
    }

    private void saveCurrImage() {
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

    // End business logic
}