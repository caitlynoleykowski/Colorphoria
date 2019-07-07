package com.oo0o0o.colorphoria.activities.loggedinactivities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.oo0o0o.colorphoria.effecthelpers.BitmapEffectAsyncTask;
import com.oo0o0o.colorphoria.effecthelpers.BitmapEffectPreviewAsyncTask;
import com.oo0o0o.colorphoria.effecthelpers.PicEditBitmapLoaderAsyncTask;
import com.oo0o0o.colorphoria.effecthelpers.BitmapWrapper;
import com.oo0o0o.colorphoria.R;
import com.oo0o0o.colorphoria.effects.BitmapEffect;
import com.oo0o0o.colorphoria.effects.InvertEffect;
import com.oo0o0o.colorphoria.effects.OutlineEffect;
import com.oo0o0o.colorphoria.effects.RandomBleedFillEffect;
import com.oo0o0o.colorphoria.effects.RandomFloodFillEffect;
import com.oo0o0o.colorphoria.effects.RandomLineFillEffect;
import com.oo0o0o.colorphoria.effects.ResetEffect;
import com.oo0o0o.colorphoria.effects.RotateEffect;
import com.oo0o0o.colorphoria.effects.ThresholdEffect;
import com.oo0o0o.colorphoria.reusableuserinterface.PaletteChooserDialog;
import com.oo0o0o.colorphoria.utils.Utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PicEditActivity extends LoggedInActivity {

    private static final String TAG = PicEditActivity.class.getName();

    public static final String SOURCE_URI = "SOURCE_URI";

    private static final String SOURCE_URI_STATE_KEY = "SOURCE_STATE_KEY";
    private static final String MOD_URI_STATE_KEY = "MOD_STATE_KEY";
    private static final String ROTATION_ANGLE_STATE_KEY = "ROTATION_ANGLE_STATE_KEY";

    public static final int REQUEST_PERMISSIONS_CALLBACK_SAVE_WRITE_EXTERNAL_STORAGE = 0;

    private static final int MAX_UNDO_QUEUE_SIZE = 2;

    private BitmapWrapper bmW;
    private Deque<BitmapWrapper> undoQueue;
    private Deque<BitmapWrapper> redoQueue;
    private boolean processingImageDone = false;

    private Uri sourceUri;

    private int randomSeed;

    final private ArrayList<Integer> colorPalette = new ArrayList<Integer>();

    AdView adView;

    InterstitialAd interstitialAd;

    private FirebaseAnalytics firebaseAnalytics;

    // Begin activity lifecycle handlers

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_edit);

        Utils.setupcolorphoriaRecentAppsBar(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_24dp);

        undoQueue = new ArrayDeque<>();
        redoQueue = new ArrayDeque<>();

        // So that the effects aren't the same every time the activity is restarted,
        // the random seed itself needs to be random
        Random randSeedGen = new Random();
        randomSeed = randSeedGen.nextInt();

        setUIforImageDoneProcessing(false);

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getResources().getString(R.string.admob_ad_unit_id_interstitial_share));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Log.w(TAG, "Interstitial ad failed to load, error code: " + errorCode);
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                interstitialAd.loadAd(new AdRequest.Builder().build());
                // Code to be executed when when the interstitial ad is closed.
                // This function call below assumes that permission has already been procured
                // for storage access, because the ad would not have been shown without it
                saveImageAndStartShareActivity();
            }
        });
        interstitialAd.loadAd(new AdRequest.Builder().build());

        if (savedInstanceState != null) {
            sourceUri = savedInstanceState.getParcelable(SOURCE_URI_STATE_KEY);
            Uri modImageUri = savedInstanceState.getParcelable(MOD_URI_STATE_KEY);
            int rotationAngle = savedInstanceState.getInt(ROTATION_ANGLE_STATE_KEY);
            // MAJOR TODO: Need to save and read modified bitmap from a uri and do the subsequent steps here
//            if (sourceImage != null && modImage != null) {
//                bmW = new BitmapWrapper(sourceImage);
//                bmW.setRotationAngle(rotationAngle);
//                bmW.setModifiedBitmap(modImage);
//                setUIforImageDoneProcessing(true);
//                ((ImageView) findViewById(R.id.theImage)).setImageBitmap(bmW.getModifiedBitmap());
//
//                refreshEffectThumbnails(randomSeed);
//            } else {
//                // TODO: Error/bug, shouldn't get here
//                setUIforImageDoneProcessing(false); // Shouldn't get here
//            }
            // Have to do this in place of the to do above. Same steps as new intent below
            ImageView iv = (ImageView) findViewById(R.id.theImage);
            bmW = new BitmapWrapper();
            PicEditBitmapLoaderAsyncTask bmwAsync = new PicEditBitmapLoaderAsyncTask(this, getApplicationContext(), iv, sourceUri, bmW,
                    (ImageButton) findViewById(R.id.original_button),
                    (ImageButton) findViewById(R.id.random_bleed_button),
                    (ImageButton) findViewById(R.id.threshold_button),
                    (ImageButton) findViewById(R.id.random_line_button),
                    (ImageButton) findViewById(R.id.random_flood_button),
                    (ImageButton) findViewById(R.id.invert_button),
                    (ImageButton) findViewById(R.id.outline_button),
                    getEffectCtrl(), randomSeed);
            bmwAsync.execute();
        } else if (getIntent().hasExtra(SOURCE_URI)) {
            sourceUri = (Uri)getIntent().getParcelableExtra(SOURCE_URI);
            ImageView iv = (ImageView) findViewById(R.id.theImage);
            bmW = new BitmapWrapper();
            PicEditBitmapLoaderAsyncTask bmwAsync = new PicEditBitmapLoaderAsyncTask(this, getApplicationContext(), iv, sourceUri, bmW,
                    (ImageButton) findViewById(R.id.original_button),
                    (ImageButton) findViewById(R.id.random_bleed_button),
                    (ImageButton) findViewById(R.id.threshold_button),
                    (ImageButton) findViewById(R.id.random_line_button),
                    (ImageButton) findViewById(R.id.random_flood_button),
                    (ImageButton) findViewById(R.id.invert_button),
                    (ImageButton) findViewById(R.id.outline_button),
                    getEffectCtrl(), randomSeed);
            bmwAsync.execute();
        } else {
            // TODO: Error/bug, shouldn't get here
            setUIforImageDoneProcessing(false);
            LinearLayout ctrlLayout = (LinearLayout) findViewById(R.id.effect_ctrl_layout);
            ctrlLayout.setVisibility(View.GONE);
        }

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

        ((ImageButton) findViewById(R.id.original_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (processingImageDone) {
                    onOriginal(view);
                }
            }
        });
        ((ImageButton) findViewById(R.id.random_bleed_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (processingImageDone) {
                    onRandomBleedFill(view);
                }
            }
        });
        ((ImageButton) findViewById(R.id.threshold_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (processingImageDone) {
                    onThreshold(view);
                }
            }
        });
        ((ImageButton) findViewById(R.id.random_line_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (processingImageDone) {
                    onRandomLineFill(view);
                }
            }
        });
        ((ImageButton) findViewById(R.id.random_flood_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (processingImageDone) {
                    onRandomFloodFill(view);
                }
            }
        });
        ((ImageButton) findViewById(R.id.invert_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (processingImageDone) {
                    onInvert(view);
                }
            }
        });
        ((ImageButton) findViewById(R.id.outline_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (processingImageDone) {
                    onOutlineEffect(view);
                }
            }
        });

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, PicEditActivity.class.getName());
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    /** Called when leaving the activity */
    @Override
    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }
    /** Called when returning to the activity */
    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }
    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        if (bmW != null) {
            outState.putParcelable(SOURCE_URI_STATE_KEY, sourceUri);
//            outState.putParcelable(MOD_URI_STATE_KEY, modUri);
            outState.putInt(ROTATION_ANGLE_STATE_KEY, bmW.getRotationAngle());
        }
        else {
            outState.putParcelable(SOURCE_URI_STATE_KEY, null);
//            outState.putParcelable(MOD_URI_STATE_KEY, modUri);
            outState.putInt(ROTATION_ANGLE_STATE_KEY, 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.pic_edit_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Only enable the menu items (which act on the image) when the image is done processing
        boolean enableMenuItems = processingImageDone;
        for (int i = 0; i < menu.size(); i++) {
            if (menu.getItem(i).getItemId() == R.id.home) {
                // do nothing
            } else {
                menu.getItem(i).setEnabled(enableMenuItems);
            }
        }
        if (enableMenuItems) {
            if (undoQueue.size() > 0) {
                MenuItem undoItem = menu.findItem(R.id.pic_edit_menu_undo);
                undoItem.setEnabled(true);
            } else {
                MenuItem undoItem = menu.findItem(R.id.pic_edit_menu_undo);
                undoItem.setEnabled(false);
            }
            if (redoQueue.size() > 0) {
                MenuItem undoItem = menu.findItem(R.id.pic_edit_menu_redo);
                undoItem.setEnabled(true);
            } else {
                MenuItem undoItem = menu.findItem(R.id.pic_edit_menu_redo);
                undoItem.setEnabled(false);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.pic_edit_menu_undo:
                onUndo();
                return true;
            case R.id.pic_edit_menu_redo:
                onRedo();
                return true;
            case R.id.pic_edit_menu_save:
                onSave();
                return true;
            case R.id.pic_edit_menu_share:
                onShare();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    // End activity lifecycle handlers

    private void setUIforImageDoneProcessing(boolean processingImageDone) {
        this.processingImageDone = processingImageDone;
        invalidateOptionsMenu();

        if (processingImageDone) {
            ((ProgressBar) findViewById(R.id.progress_bar)).setVisibility(View.GONE);
        } else {
            ((ProgressBar) findViewById(R.id.progress_bar)).setVisibility(View.VISIBLE);
        }

        // Disable buttons in bottom control panel (effects buttons/rotate button/palette button)
        LinearLayout ll2 = (LinearLayout) findViewById(R.id.icon_linear_layout);
        ll2.getChildCount();
        for (int i = 0; i < ll2.getChildCount(); i++) {
            (ll2.getChildAt(i)).setClickable(processingImageDone);
        }
        findViewById(R.id.choose_palette).setClickable(processingImageDone);
        findViewById(R.id.rotate_right).setClickable(processingImageDone);
        findViewById(R.id.effect_ctrl_seekbar).setEnabled(processingImageDone);
    }

    // Begin UI handlers
    public void onReset() {
        applyEffect(new ResetEffect());
    }

    public void onRedo() {
        undoEnqueue();
        bmW = redoQueue.removeLast();
        if (redoQueue.isEmpty()) {
            invalidateOptionsMenu();
        }
        invalidateOptionsMenu();
        ((ImageView) findViewById(R.id.theImage)).setImageBitmap(bmW.getModifiedBitmap());
    }

    public void onUndo() {
        redoEnqueue();
        bmW = undoQueue.removeLast();
        if (undoQueue.isEmpty()) {
            invalidateOptionsMenu();
        }
        ((ImageView) findViewById(R.id.theImage)).setImageBitmap(bmW.getModifiedBitmap());
    }

    public void onSave() {
        saveCurrImageStoragePermission();
    }

    public void onShare() {
        if (firebaseAnalytics != null) {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, new Bundle());
        }
        shareCurrImageStoragePermission();
    }

    public void onRotateRight(View v) {
        applyEffect(new RotateEffect());
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

    public void onOriginal(View v) {
        if (firebaseAnalytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, RandomBleedFillEffect.class.getName());
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
        }
        applyEffect(new ResetEffect());
    }

    public void onRandomBleedFill(View v) {
        if (firebaseAnalytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, RandomBleedFillEffect.class.getName());
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
        }
        applyEffect(new RandomBleedFillEffect());
    }

    public void onThreshold(View v) {
        if (firebaseAnalytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, ThresholdEffect.class.getName());
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
        }
        applyEffect(new ThresholdEffect());
    }

    public void onRandomLineFill(View v) {
        if (firebaseAnalytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, RandomLineFillEffect.class.getName());
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
        }
        applyEffect(new RandomLineFillEffect());
    }

    public void onRandomFloodFill(View v) {
        if (firebaseAnalytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, RandomFloodFillEffect.class.getName());
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
        }
        applyEffect(new RandomFloodFillEffect());
    }

    public void onInvert(View v) {
        if (firebaseAnalytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, InvertEffect.class.getName());
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
        }
        applyEffect(new InvertEffect());
    }

    public void onOutlineEffect(View v) {
        if (firebaseAnalytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, OutlineEffect.class.getName());
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
        }
        applyEffect(new OutlineEffect());
    }

    // End UI Handlers

    // Begin UI Helpers

    private void refreshEffectThumbnails(int randomSeed) {
        // Populate the effect preview buttons
        new BitmapEffectPreviewAsyncTask(this,
                (ImageButton) findViewById(R.id.original_button),
                new ResetEffect(), bmW.getSourceBitmap(), getEffectCtrl(),
                randomSeed, colorPalette).execute();
        new BitmapEffectPreviewAsyncTask(this,
                (ImageButton) findViewById(R.id.random_bleed_button),
                new RandomBleedFillEffect(), bmW.getSourceBitmap(), getEffectCtrl(),
                randomSeed, colorPalette).execute();
        new BitmapEffectPreviewAsyncTask(this,
                (ImageButton) findViewById(R.id.random_flood_button),
                new RandomFloodFillEffect(), bmW.getSourceBitmap(), getEffectCtrl(),
                randomSeed, colorPalette).execute();
        new BitmapEffectPreviewAsyncTask(this,
                (ImageButton) findViewById(R.id.random_line_button),
                new RandomLineFillEffect(), bmW.getSourceBitmap(), getEffectCtrl(),
                randomSeed, colorPalette).execute();
        new BitmapEffectPreviewAsyncTask(this,
                (ImageButton) findViewById(R.id.invert_button),
                new InvertEffect(), bmW.getSourceBitmap(), getEffectCtrl(),
                randomSeed, colorPalette).execute();
        new BitmapEffectPreviewAsyncTask(this,
                (ImageButton) findViewById(R.id.outline_button),
                new OutlineEffect(), bmW.getSourceBitmap(), getEffectCtrl(),
                randomSeed, colorPalette).execute();
        new BitmapEffectPreviewAsyncTask(this,
                (ImageButton) findViewById(R.id.threshold_button),
                new ThresholdEffect(), bmW.getSourceBitmap(), getEffectCtrl(),
                randomSeed, colorPalette).execute();
    }

    // public to be used by BitmapEffectAsyncTask
    public void doneProgress() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);
        setUIforImageDoneProcessing(true);
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
        invalidateOptionsMenu();
    }

    private void redoEnqueue() {
        // TODO: Do we need to check if the size gets exceeded? think its handled by undo checking
        BitmapWrapper redoCopy = new BitmapWrapper(bmW.getSourceBitmap().copy(bmW.getSourceBitmap().getConfig(), true));
        redoCopy.setModifiedBitmap(bmW.getModifiedBitmap().copy(bmW.getModifiedBitmap().getConfig(), true));
        redoQueue.addLast(redoCopy);
        invalidateOptionsMenu();
    }

    private void updateUndoRedoQueues() {
        redoQueue.clear();
        invalidateOptionsMenu();
        undoEnqueue();
    }

    private void applyEffect(BitmapEffect effect) {
        ((ProgressBar) findViewById(R.id.progress_bar)).setVisibility(View.VISIBLE);
        updateUndoRedoQueues();
        setUIforImageDoneProcessing(false);
        double ctrl = getEffectCtrl();
        ImageView iv = (ImageView) findViewById(R.id.theImage);
        ProgressBar pb = (ProgressBar) findViewById(R.id.progress_bar);
        new BitmapEffectAsyncTask(this,iv,effect,bmW,ctrl,randomSeed,colorPalette).execute();
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
                        Utils.showCustomToast(PicEditActivity.this, getString(R.string.unable_to_save_image));
                    }
                })) {
            saveCurrImage();
        }
    }

    private void saveCurrImage() {
        Utils.savecolorphoriaBitmap(bmW.getModifiedBitmap(), this, new Utils.SavecolorphoriaBitmapHandler() {
            public void onSuccess(Uri uri) {
                Utils.showCustomToast(PicEditActivity.this, getString(R.string.image_saved));
            }
            public void onFailure() {
                // do nothing
                Utils.showCustomToast(PicEditActivity.this, "Save failed.");
            }
        });
    }

    private void shareCurrImageStoragePermission() {
        if (Utils.getPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                getString(R.string.permission_write_external_storage_explanation),
                REQUEST_PERMISSIONS_CALLBACK_SAVE_WRITE_EXTERNAL_STORAGE,
                new Utils.OnExplanationDeclinedHandler() {
                    @Override
                    public void onExplanationDeclined() {
                        Utils.showCustomToast(PicEditActivity.this, getString(R.string.unable_to_save_image));
                    }
                })) {
            showInterstitial();
        }
    }

    private void showInterstitial() {
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
            Log.d(TAG, "The interstitial wasn't loaded yet.");
            saveImageAndStartShareActivity();
        }
    }

    private void saveImageAndStartShareActivity() {
        Utils.savecolorphoriaBitmap(bmW.getModifiedBitmap(), this, new Utils.SavecolorphoriaBitmapHandler() {
            public void onSuccess(Uri uri) {
                startShareActivity(uri);
            }
            public void onFailure() {
                // do nothing
                Utils.showCustomToast(PicEditActivity.this, "Save failed.");
            }
        });
    }

    private void startShareActivity(Uri uri) {
        Intent intent = new Intent(this, UploadActivity.class);
        intent.putExtra(UploadActivity.IMAGE_URI, uri);
        startActivity(intent);
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
                    Utils.showCustomToast(PicEditActivity.this, getString(R.string.unable_to_save_image));
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