package com.caitoo0o.filler.activities.loggedinactivities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.graphics.Bitmap;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.caitoo0o.filler.effects.BitmapEffectAsyncTask;
import com.caitoo0o.filler.effects.BitmapEffectPreviewAsyncTask;
import com.caitoo0o.filler.effects.BitmapLoaderAsyncTask;
import com.caitoo0o.filler.effects.BitmapWrapper;
import com.caitoo0o.filler.uihelpers.OnSwipeTouchListener;
import com.caitoo0o.filler.R;
import com.caitoo0o.filler.uihelpers.PaletteChooserDialog;
import com.caitoo0o.filler.utils.Utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PicEditActivity extends AppCompatActivity {

    public static final String SOURCE_URI = "SOURCE_URI";

    private static final String SOURCE_STATE_KEY = "SOURCE_STATE_KEY";
    private static final String ROTATION_ANGLE_STATE_KEY = "ROTATION_ANGLE_STATE_KEY";
    private static final String MOD_STATE_KEY = "MOD_STATE_KEY";
    private static final String EFFECT_SELECTED_STATE_KEY = "EFFECT_SELECTED_STATE_KEY";

    public static final int REQUEST_PERMISSIONS_CALLBACK_SAVE_WRITE_EXTERNAL_STORAGE = 0;
    public static final int REQUEST_PERMISSIONS_CALLBACK_SHARE_WRITE_EXTERNAL_STORAGE = 1;

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

    private int randomSeed;

    final private ArrayList<Integer> colorPalette = new ArrayList<Integer>();

    // Begin activity lifecycle handlers

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_edit);

        Utils.setupFillerRecentAppsBar(this);

        undoQueue = new ArrayDeque<>();
        redoQueue = new ArrayDeque<>();

        effectSelected = Effect.NONE;

        // So that the effects aren't the same every time the activity is restarted,
        // the random seed itself needs to be random
        Random randSeedGen = new Random();
        randomSeed = randSeedGen.nextInt();

        if (savedInstanceState != null) {
            sourceUri = savedInstanceState.getParcelable(SOURCE_URI);
            Bitmap sourceImage = (Bitmap) savedInstanceState.getParcelable(SOURCE_STATE_KEY);
            int rotationAngle = savedInstanceState.getInt(ROTATION_ANGLE_STATE_KEY);
            Bitmap modImage = (Bitmap) savedInstanceState.getParcelable(MOD_STATE_KEY);
            if (sourceImage != null && modImage != null) {
                bmW = new BitmapWrapper(sourceImage);
                bmW.setRotationAngle(rotationAngle);
                bmW.setModifiedBitmap(modImage);
                setUIforImageDoneProcessing(true);
                ((ImageView) findViewById(R.id.theImage)).setImageBitmap(bmW.getModifiedBitmap());

                refreshEffectThumbnails(randomSeed);
            } else {
                // TODO: Error/bug, shouldn't get here
                setUIforImageDoneProcessing(false); // Shouldn't get here
            }
        } else if (getIntent().hasExtra(SOURCE_URI)) {
            sourceUri = (Uri)getIntent().getParcelableExtra(SOURCE_URI);
            Uri uri = sourceUri;
            ImageView iv = (ImageView) findViewById(R.id.theImage);
            bmW = new BitmapWrapper();
            BitmapLoaderAsyncTask bmwAsync = new BitmapLoaderAsyncTask(this, getApplicationContext(), iv, uri, bmW,
                    (ImageButton) findViewById(R.id.icon_square_button),
                    (ImageButton) findViewById(R.id.threshold_button),
                    (ImageButton) findViewById(R.id.random_line_button),
                    (ImageButton) findViewById(R.id.random_flood_button),
                    getEffectCtrl(), randomSeed);
            bmwAsync.execute();
            setUIforImageDoneProcessing(true);
        } else {
            // TODO: Error/bug, shouldn't get here
            setUIforImageDoneProcessing(false);
            LinearLayout ctrlLayout = (LinearLayout) findViewById(R.id.effect_ctrl_layout);
            ctrlLayout.setVisibility(View.GONE);
        }

        View outerLinearLayout = findViewById(R.id.outer_layout);
        outerLinearLayout.setOnTouchListener(new OnSwipeTouchListener(PicEditActivity.this) {
            public void onSwipeTop() {
                // Swipe bottom to top
            }

            public void onSwipeRight() {
                // Swipe left to right
            }

            public void onSwipeLeft() {
                // Swipe right to left
            }

            public void onSwipeBottom() {
                // Swipe top to bottom
                goToPicSourceActivity(sourceUri);
            }

        });

        SeekBar pb = (SeekBar) findViewById(R.id.effect_ctrl_seekbar);
        pb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                refreshEffectThumbnails(randomSeed);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                // TODO Auto-generated method stub
            }
        });
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
            outState.putInt(ROTATION_ANGLE_STATE_KEY, 0);
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

    // End device helpers

    // Begin UI handlers

    public void onDiscard(View v) {
        goToPicSourceActivity(sourceUri);
    }

    public void onReset(View v) {
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
        saveCurrImageStoragePermission();
    }

    public void onShare(View v) {
        shareCurrImageStoragePermission();
    }

    public void onRotateRight(View v) {
        effectSelected = Effect.ROTATE_RIGHT;
        applyEffect();
    }

    public void onRotateLeft(View v) {
        effectSelected = Effect.ROTATE_LEFT;
        applyEffect();
    }

    public void onChoosePalette(View v) {
        final PaletteChooserDialog colorPaletteDialog = new PaletteChooserDialog(this, colorPalette);
        // TODO: Make an onPaletteChange callback that is called in the ondismiss in case of a change
        // inside of the PaletteChooserDialog class
        colorPaletteDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (colorPaletteDialog.isColorPaletteChanged()) {
                    refreshEffectThumbnails(randomSeed);
                }
            }
        });
        colorPaletteDialog.show();
    }

    public void onRandomBleedFill(View v) {
        effectSelected = Effect.RANDOM_BLEED_FILL;
        applyEffect();
    }

    public void onThreshold(View v) {
        effectSelected = Effect.THRESHOLD;
        applyEffect();
    }

    public void onRandomLineFill(View v) {
        effectSelected = Effect.RANDOM_LINE_FILL;
        applyEffect();
    }

    public void onRandomFloodFill(View v) {
        effectSelected = Effect.RANDOM_FLOOD_FILL;
        applyEffect();
    }

    // End UI Handlers

    // Begin UI Helpers

    private void refreshEffectThumbnails(int randomSeed) {
        // Populate the effect preview buttons
        new BitmapEffectPreviewAsyncTask(this,
                (ImageButton) findViewById(R.id.icon_square_button),
                PicEditActivity.Effect.RANDOM_BLEED_FILL, bmW.getSourceBitmap(), getEffectCtrl(),
                randomSeed, colorPalette).execute();
        new BitmapEffectPreviewAsyncTask(this,
                (ImageButton) findViewById(R.id.threshold_button),
                PicEditActivity.Effect.THRESHOLD, bmW.getSourceBitmap(), getEffectCtrl(),
                randomSeed, colorPalette).execute();
        new BitmapEffectPreviewAsyncTask(this,
                (ImageButton) findViewById(R.id.random_line_button),
                PicEditActivity.Effect.RANDOM_LINE_FILL, bmW.getSourceBitmap(), getEffectCtrl(),
                randomSeed, colorPalette).execute();
        new BitmapEffectPreviewAsyncTask(this,
                (ImageButton) findViewById(R.id.random_flood_button),
                PicEditActivity.Effect.RANDOM_FLOOD_FILL, bmW.getSourceBitmap(), getEffectCtrl(),
                randomSeed, colorPalette).execute();
    }

    private void setImageButtonClickable(View view, boolean clickable) {
        // TODO: Maybe change this to a selector in the layout.xml??
        // http://stackoverflow.com/questions/7228985/android-imagebutton-with-disabled-ui-feel
        ImageButton button = (ImageButton) view;
        button.setClickable(clickable);
        if (clickable) {
            button.getDrawable().setColorFilter(null);
        } else {
            button.getDrawable().setColorFilter(getColor(R.color.colorHighlight), PorterDuff.Mode.SRC_IN);
        }
    }

    // public to be used by BitmapEffectAsyncTask
    public void doneProgress() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);
        setUIforImageDoneProcessing(true);
    }

    private void setUIforImageDoneProcessing(boolean processingImageDone) {
        LinearLayout ll1 = (LinearLayout) findViewById(R.id.firstButtonLinearLayout);
        ll1.getChildCount();
        for (int i = 0; i < ll1.getChildCount(); i++) {
            setImageButtonClickable(ll1.getChildAt(i), processingImageDone);
        }
        LinearLayout ll2 = (LinearLayout) findViewById(R.id.icon_linear_layout);
        ll2.getChildCount();
        for (int i = 0; i < ll2.getChildCount(); i++) {
            ((ImageButton) ll2.getChildAt(i)).setClickable(processingImageDone);
        }
        setImageButtonClickable(findViewById(R.id.choose_palette), processingImageDone);
        setImageButtonClickable(findViewById(R.id.rotate_right), processingImageDone);
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
        overridePendingTransition(R.anim.slide_top_to_center, R.anim.slide_center_to_bottom);
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
        ((ProgressBar) findViewById(R.id.progress_bar)).setVisibility(View.VISIBLE);
        updateUndoRedoQueues();
        setUIforImageDoneProcessing(false);
        double ctrl = getEffectCtrl();
        ImageView iv = (ImageView) findViewById(R.id.theImage);
        ProgressBar pb = (ProgressBar) findViewById(R.id.progress_bar);
        new BitmapEffectAsyncTask(this,iv,effectSelected,bmW,ctrl,randomSeed,colorPalette).execute();
        Random randSeedGen = new Random();
        randomSeed = randSeedGen.nextInt();
        refreshEffectThumbnails(randomSeed);
    }

    private double getEffectCtrl() {
        SeekBar ctrlSeekbar = (SeekBar) findViewById(R.id.effect_ctrl_seekbar);
        double progress = ctrlSeekbar.getProgress();
        double max = ctrlSeekbar.getMax();
        double ctrl = 1 - progress/max;
        return ctrl;
    }

    private void saveCurrImageStoragePermission() {
        if (Utils.getPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                getString(R.string.permission_write_external_storage_explanation),
                REQUEST_PERMISSIONS_CALLBACK_SAVE_WRITE_EXTERNAL_STORAGE,
                new Utils.OnExplanationDeclinedHandler() {
                    @Override
                    public void onExplanationDeclined() {
                        Toast.makeText(PicEditActivity.this, R.string.unable_to_save_image, Toast.LENGTH_LONG);
                    }
                })) {
            saveCurrImage();
        }
    }

    private void saveCurrImage() {
        Utils.saveFillerBitmap(bmW.getModifiedBitmap(), this, new Utils.SaveFillerBitmapHandler() {
            public void onSuccess() {
                Toast.makeText(PicEditActivity.this.getApplicationContext(), "Image saved.", Toast.LENGTH_LONG).show();
            }
            public void onFailure() {
                // do nothing
                Toast.makeText(PicEditActivity.this.getApplicationContext(), "Save failed.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void shareCurrImageStoragePermission() {
        if (Utils.getPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                getString(R.string.permission_write_external_storage_explanation),
                REQUEST_PERMISSIONS_CALLBACK_SHARE_WRITE_EXTERNAL_STORAGE,
                new Utils.OnExplanationDeclinedHandler() {
                    @Override
                    public void onExplanationDeclined() {
                        Toast.makeText(PicEditActivity.this, R.string.unable_to_save_image, Toast.LENGTH_LONG);
                    }
                })) {
            shareCurrImage();
        }
    }

    private void shareCurrImage() {
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bmW.getModifiedBitmap(), "Export from Filler", null);
        Uri uri = Uri.parse(path);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, "Share Image"));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_CALLBACK_SAVE_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, so continue.
                    saveCurrImage();
                } else {
                    Toast.makeText(PicEditActivity.this, R.string.unable_to_save_image, Toast.LENGTH_LONG);
                }
                return;
            }
            case REQUEST_PERMISSIONS_CALLBACK_SHARE_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, so continue.
                    shareCurrImage();
                } else {
                    Toast.makeText(PicEditActivity.this, R.string.unable_to_share_image, Toast.LENGTH_LONG);
                }
                return;
            }
        }
    }

    // End business logic

    // Begin network calls

    private void getData() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://black-obelisk-161316.appspot.com/_ah/api/echo/v1/echo";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.i(PicEditActivity.this.getClass().getName(), "Response is: " +
                                response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(PicEditActivity.this.getClass().getName(), "Error when calling endpoint: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("message", "data");
                params.put("n", "9");

                return params;
            }
        };;
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    // End network calls
}