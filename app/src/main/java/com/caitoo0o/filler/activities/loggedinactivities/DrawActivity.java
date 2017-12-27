package com.caitoo0o.filler.activities.loggedinactivities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.caitoo0o.filler.effects.BitmapWrapper;
import com.caitoo0o.filler.reusableuserinterface.DrawView;
import com.caitoo0o.filler.reusableuserinterface.OnSwipeTouchListener;
import com.caitoo0o.filler.R;
import com.caitoo0o.filler.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class DrawActivity extends LoggedInActivity {

    private static final int REQUEST_PERMISSIONS_CALLBACK_SAVE_DRAWING_WRITE_EXTERNAL_STORAGE = 0;
    private static final int REQUEST_PERMISSIONS_CALLBACK_START_PIC_EDIT_WRITE_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_PERMISSIONS_CALLBACK_START_PIC_SOURCE_WRITE_EXTERNAL_STORAGE = 2;
    private static final int REQUEST_PERMISSIONS_CALLBACK_START_BROWSE_WRITE_EXTERNAL_STORAGE = 3;
    private static final int REQUEST_PERMISSIONS_CALLBACK_START_CAMERA_WRITE_EXTERNAL_STORAGE = 4;

    private static final String IMAGE_STATE_KEY = "IMAGE_STATE_KEY";
    public static final String SOURCE_BITMAP = "SOURCE_BITMAP";
    public static final String SOURCE_URI = "SOURCE_URI";

    private static final String TAG = DrawActivity.class.getName();

    private DrawView drawView;

    private float extraSmallBrush;
    private float smallBrush;
    private float mediumBrush;
    private float largeBrush;

    private ImageButton currColorButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        Utils.setupFillerRecentAppsBar(this);

        ((ImageButton) findViewById(R.id.drawButton)).setImageResource(R.drawable.ic_brush_white_24dp);
        ((ImageButton) findViewById(R.id.cameraButton)).setImageResource(R.drawable.ic_camera_alt_dark_24dp);
        ((ImageButton) findViewById(R.id.homeButton)).setImageResource(R.drawable.ic_home_dark_24dp);
        ((ImageButton) findViewById(R.id.openExistingButton)).setImageResource(R.drawable.ic_collections_dark_24dp);

        drawView = (DrawView)findViewById(R.id.drawing);

        if (savedInstanceState != null) {
            if (savedInstanceState.getParcelable(IMAGE_STATE_KEY) != null) {
                Bitmap bm = savedInstanceState.getParcelable(IMAGE_STATE_KEY);
                drawView.setCanvasBitmap(bm);
            }
        } else if (getIntent().hasExtra(SOURCE_URI)) {
        //} else if (getIntent().hasExtra(SOURCE_BITMAP)) {
//            if (getIntent().getParcelableExtra(SOURCE_BITMAP) != null) {
//                Bitmap bm = (Bitmap) getIntent().getParcelableExtra(SOURCE_BITMAP);
//                drawView.setCanvasBitmap(bm);
//            }


            if (getIntent().getParcelableExtra(SOURCE_URI) != null) {
                Uri uri = (Uri) getIntent().getParcelableExtra(SOURCE_URI);

                try {
                    InputStream is = getContentResolver().openInputStream(uri);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(is, null, options);
                    // TODO: Maybe can move the getScaleFactor function somewhere else or create a generic one?
                    double scaleFactor = BitmapWrapper.getScaleFactor(options.outWidth, options.outHeight);
                    BitmapFactory.Options finalOptions = new BitmapFactory.Options();
                    finalOptions.inSampleSize = (int) Math.ceil(1 / scaleFactor);
                    InputStream finalInputStream = getContentResolver().openInputStream(uri);
                    Bitmap bm = BitmapFactory.decodeStream(finalInputStream, null, finalOptions);
                    drawView.setCanvasBitmap(bm);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "File with uri provided in Intent can not be found.");
                    // TODO: Handle this error
                }


            }


//
//            Uri uri = sourceUri;
//            ImageView iv = (ImageView) findViewById(R.id.theImage);
//            bmW = new BitmapWrapper();
//            PicEditBitmapLoaderAsyncTask bmwAsync = new PicEditBitmapLoaderAsyncTask(this, getApplicationContext(), iv, uri, bmW,
//                    (ImageButton) findViewById(R.id.icon_square_button),
//                    (ImageButton) findViewById(R.id.threshold_button),
//                    (ImageButton) findViewById(R.id.random_line_button),
//                    (ImageButton) findViewById(R.id.random_flood_button),
//                    getEffectCtrl(), randomSeed);
//            bmwAsync.execute();
//            setUIforImageDoneProcessing(true);
        }

        // Setup button refs and onClick listeners
        LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
        currColorButton = (ImageButton)paintLayout.getChildAt(0);
        currColorButton.setImageDrawable(getResources().getDrawable(R.drawable.paint_selected_bg));

        // instantiate brushes
        extraSmallBrush = getResources().getInteger(R.integer.extra_small_size);
        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);

        drawView.setPaintbrushSize(extraSmallBrush);
    }

    public void onDraw(View v) {
        // Do nothing
    }

    public void onCamera(View v) {
        startPicSourceStoragePermission(false, true);
    }

    public void onHome(View v) {
        startPicSourceStoragePermission(false, false);
    }

    public void onBrowse(View v) {
        startPicSourceStoragePermission(true, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.draw_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.draw_menu_new:
                onNewButton();
                return true;
            case R.id.draw_menu_draw:
                onDrawButton();
                return true;
            case R.id.draw_menu_eraser:
                onEraseButton();
                return true;
            case R.id.draw_menu_save:
                onSaveButton();
                return true;
            case R.id.draw_menu_done:
                onDoneButton();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    // onClick for paint color buttons
    public void onPaintColorClicked(View view){
        // Update if the selected color is different than the currently selected color
        if (view != currColorButton){
            drawView.setEraseMode(false);
            drawView.setPaintbrushSize(drawView.getPrevBrushSize());
            ImageButton imgView = (ImageButton)view;
            String color = view.getTag().toString();
            drawView.setPaintColor(color);
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_selected_bg));
            currColorButton.setImageDrawable(getResources().getDrawable(R.drawable.paint_color_bg));
            currColorButton =(ImageButton)view;
        }
    }

    // onClick for paintbrush button
    public void onDrawButton(){
        final Dialog brushDialog = new Dialog(this);
        brushDialog.setContentView(R.layout.paintbrush_options);
        ImageButton extraSmallBtn = (ImageButton) brushDialog.findViewById(R.id.extra_small_brush);
        extraSmallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setPaintbrushSize(extraSmallBrush);
                drawView.setPrevBrushSize(extraSmallBrush);
                drawView.setEraseMode(false);
                brushDialog.dismiss();
            }
        });
        ImageButton smallBtn = (ImageButton) brushDialog.findViewById(R.id.small_brush);
        smallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setPaintbrushSize(smallBrush);
                drawView.setPrevBrushSize(smallBrush);
                drawView.setEraseMode(false);
                brushDialog.dismiss();
            }
        });
        ImageButton mediumBtn = (ImageButton) brushDialog.findViewById(R.id.medium_brush);
        mediumBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setPaintbrushSize(mediumBrush);
                drawView.setPrevBrushSize(mediumBrush);
                drawView.setEraseMode(false);
                brushDialog.dismiss();
            }
        });

        ImageButton largeBtn = (ImageButton) brushDialog.findViewById(R.id.large_brush);
        largeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setPaintbrushSize(largeBrush);
                drawView.setPrevBrushSize(largeBrush);
                drawView.setEraseMode(false);
                brushDialog.dismiss();
            }
        });
        brushDialog.show();
    }

    // onClick for erase button
    public void onEraseButton(){
        final Dialog eraseDialog = new Dialog(this);
        eraseDialog.setContentView(R.layout.paintbrush_options);
        ImageButton smallBtn = (ImageButton)eraseDialog.findViewById(R.id.small_brush);
        smallBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                drawView.setEraseMode(true);
                drawView.setPaintbrushSize(smallBrush);
                eraseDialog.dismiss();
            }
        });
        ImageButton mediumBtn = (ImageButton)eraseDialog.findViewById(R.id.medium_brush);
        mediumBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                drawView.setEraseMode(true);
                drawView.setPaintbrushSize(mediumBrush);
                eraseDialog.dismiss();
            }
        });
        ImageButton largeBtn = (ImageButton)eraseDialog.findViewById(R.id.large_brush);
        largeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                drawView.setEraseMode(true);
                drawView.setPaintbrushSize(largeBrush);
                eraseDialog.dismiss();
            }
        });
        eraseDialog.show();
    }

    // onClick for new image button
    public void onNewButton(){
        Utils.showConfirmDialog(this,
                getString(R.string.are_you_sure),
                getString(R.string.yes),
                getString(R.string.no),
                new Utils.ConfirmDialogHandler() {
                    public void onNegativeButton(Dialog confirmDialog) {
                        confirmDialog.dismiss();
                    }
                    public void onPositiveButton(Dialog confirmDialog) {
                        drawView.startNew();
                        confirmDialog.dismiss();
                    }
                },
                true);
    }

    // onClick for save image button
    public void onSaveButton(){
            saveDrawingStoragePermission();
    }

    // onClick for done button
    public void onDoneButton(){
        if (drawView.isEdited()) {
            startPicEditStoragePermission();
        } else {
            Utils.showCustomToast(this, "Draw an image to edit :)");
        }
    }

    private void saveDrawingStoragePermission() {
        if (Utils.getPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                getString(R.string.permission_write_external_storage_explanation),
                REQUEST_PERMISSIONS_CALLBACK_SAVE_DRAWING_WRITE_EXTERNAL_STORAGE,
                new Utils.OnExplanationDeclinedHandler() {
                    @Override
                    public void onExplanationDeclined() {
                        Utils.showCustomToast(DrawActivity.this, getString(R.string.unable_to_save_image));
                    }
                })) {
            saveDrawing();
        }
    }

    private File saveDrawing() {
        //save drawing
        drawView.setDrawingCacheEnabled(true);
        File imageFile = Utils.saveFillerBitmap(drawView.getDrawingCache(), this, new Utils.SaveFillerBitmapHandler() {
            public void onSuccess(Uri uri) {
                Utils.showCustomToast(DrawActivity.this, getString(R.string.image_saved));
            }
            public void onFailure() {
                Utils.showCustomToast(DrawActivity.this, "Save failed.");
            }
        });
        drawView.destroyDrawingCache();
        return imageFile;
    }

    private void startPicSourceStoragePermission(final boolean startBrowse, final boolean startCamera) {
        int callbackId;
        if (!startBrowse && !startCamera) {
            callbackId = REQUEST_PERMISSIONS_CALLBACK_START_PIC_SOURCE_WRITE_EXTERNAL_STORAGE;
        } else if (startBrowse){
            callbackId = REQUEST_PERMISSIONS_CALLBACK_START_BROWSE_WRITE_EXTERNAL_STORAGE;
        } else {
            callbackId = REQUEST_PERMISSIONS_CALLBACK_START_CAMERA_WRITE_EXTERNAL_STORAGE;
        }
        if (Utils.getPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                getString(R.string.permission_write_external_storage_explanation),
                callbackId,
                new Utils.OnExplanationDeclinedHandler() {
                    @Override
                    public void onExplanationDeclined() {
                        Utils.showCustomToast(DrawActivity.this, getString(R.string.unable_to_save_image));
                        startPicSourceActivityWithoutSaving(startBrowse, startCamera);
                    }
                })) {
            startPicSourceActivity(startBrowse, startCamera);
        }
    }

    private void startPicSourceActivity(boolean startBrowse, boolean startCamera) {
        Intent intent;
        if (startBrowse) {
            intent = new Intent(this, BrowseActivity.class);
        } else if (startCamera) {
            intent = new Intent(this, PicSourceActivity.class);
            intent.putExtra(PicSourceActivity.START_CAMERA, true);
        } else {
            intent = new Intent(this, PicSourceActivity.class);
        }
        if (drawView.isEdited()) {
            File imageFile = saveDrawing();
            // Load intent with uri
            intent.putExtra(PicSourceActivity.IMAGE_URI_STATE_KEY, Uri.fromFile(imageFile));
        }
        startActivity(intent);
    }

    private void startPicSourceActivityWithoutSaving(boolean startBrowse, boolean startCamera) {
        Intent intent;
        if (startBrowse) {
            intent = new Intent(this, BrowseActivity.class);
        } else if (startCamera) {
            intent = new Intent(this, PicSourceActivity.class);
            intent.putExtra(PicSourceActivity.START_CAMERA, true);
        } else {
            intent = new Intent(this, PicSourceActivity.class);
        }
        startActivity(intent);
    }

    private void startPicEditStoragePermission() {
        if (Utils.getPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                getString(R.string.permission_write_external_storage_explanation),
                REQUEST_PERMISSIONS_CALLBACK_START_PIC_EDIT_WRITE_EXTERNAL_STORAGE,
                new Utils.OnExplanationDeclinedHandler() {
                    @Override
                    public void onExplanationDeclined() {
                        Utils.showCustomToast(DrawActivity.this, getString(R.string.unable_to_save_image));
                    }
                })) {
            startPicEditActivity();
        }
    }

    private void startPicEditActivity() {
        if (drawView.isEdited()) {
            File imageFile = saveDrawing();

            Intent intent = new Intent(this, PicEditActivity.class);
            intent.putExtra(PicEditActivity.SOURCE_URI, Uri.fromFile(imageFile)); // Load with uri
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_CALLBACK_SAVE_DRAWING_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, so continue.
                    saveDrawing();
                } else {
                    Utils.showCustomToast(DrawActivity.this, getString(R.string.unable_to_save_image));
                }
                return;
            }
            case REQUEST_PERMISSIONS_CALLBACK_START_PIC_EDIT_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, so continue.
                    startPicEditActivity();
                } else {
                    Utils.showCustomToast(DrawActivity.this, getString(R.string.unable_to_save_image));
                }
                return;
            }
            case REQUEST_PERMISSIONS_CALLBACK_START_PIC_SOURCE_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, so continue.
                    startPicSourceActivity(false, false);
                } else {
                    Utils.showCustomToast(DrawActivity.this, getString(R.string.unable_to_save_image));
                    startPicSourceActivityWithoutSaving(false, false);
                }
                return;
            }
            case REQUEST_PERMISSIONS_CALLBACK_START_BROWSE_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, so continue.
                    startPicSourceActivity(true, false);
                } else {
                    Utils.showCustomToast(DrawActivity.this, getString(R.string.unable_to_save_image));
                    startPicSourceActivityWithoutSaving(true, false);
                }
                return;
            }
            case REQUEST_PERMISSIONS_CALLBACK_START_CAMERA_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, so continue.
                    startPicSourceActivity(false, true);
                } else {
                    Utils.showCustomToast(DrawActivity.this, getString(R.string.unable_to_save_image));
                    startPicSourceActivityWithoutSaving(false, true);
                }
                return;
            }
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        drawView.setDrawingCacheEnabled(true);
        Bitmap bm = drawView.getDrawingCache();
        if (bm != null) {
            Bitmap bm2 = bm.copy(bm.getConfig(), true);
            outState.putParcelable(IMAGE_STATE_KEY, bm2);
        }
        drawView.setDrawingCacheEnabled(false);
    }
}
