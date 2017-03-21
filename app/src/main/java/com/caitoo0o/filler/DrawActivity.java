package com.caitoo0o.filler;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;

public class DrawActivity extends AppCompatActivity {

    private DrawView drawView;

    private float extraSmallBrush;
    private float smallBrush;
    private float mediumBrush;
    private float largeBrush;

    private ImageButton currColorButton;
    private ImageButton drawButton;
    private ImageButton eraseButton;
    private ImageButton newButton;
    private ImageButton saveButton;
    private ImageButton doneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utils.setupFillerRecentAppsBar(this);

        setContentView(R.layout.activity_draw);
        drawView = (DrawView)findViewById(R.id.drawing);
        // Setup button refs and onClick listeners
        LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
        currColorButton = (ImageButton)paintLayout.getChildAt(0);
        currColorButton.setImageDrawable(getResources().getDrawable(R.drawable.paint_selected_bg));

        drawButton = (ImageButton)findViewById(R.id.draw_btn);
        eraseButton = (ImageButton)findViewById(R.id.erase_btn);
        newButton = (ImageButton)findViewById(R.id.new_btn);
        saveButton = (ImageButton)findViewById(R.id.save_btn);
        doneButton = (ImageButton)findViewById(R.id.done_btn);

        // instantiate brushes
        extraSmallBrush = getResources().getInteger(R.integer.extra_small_size);
        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);

        drawView.setPaintbrushSize(extraSmallBrush);

        // setup swipe gestures
        View outerRelativeLayout = findViewById(R.id.outer_linear_layout);
        outerRelativeLayout.setOnTouchListener(new OnSwipeTouchListener(DrawActivity.this) {
            public void onSwipeTop() {
                //Toast.makeText(Browse.this, "top", Toast.LENGTH_SHORT).show();
            }

            public void onSwipeRight() {
                //Toast.makeText(Browse.this, "right", Toast.LENGTH_SHORT).show();
            }

            public void onSwipeLeft() {
                //Toast.makeText(Browse.this, "left", Toast.LENGTH_SHORT).show();
//                if (mImageUri != null) {
//                    startPicEditActivity(mImageUri);
//                }
                startPicSourceActivity();
            }

            public void onSwipeBottom() {
                //Toast.makeText(Browse.this, "bottom", Toast.LENGTH_SHORT).show();
            }

        });
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
    public void onDrawButton(View view){
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
    public void onEraseButton(View view){
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
    public void onNewButton(View view){
        final Dialog confirmDialog = new Dialog(this);
        confirmDialog.setContentView(R.layout.confirm);
        Button noButton = (Button) confirmDialog.findViewById(R.id.no_btn);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDialog.dismiss();
            }
        });
        Button yesButton = (Button) confirmDialog.findViewById(R.id.yes_btn);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.startNew();
                confirmDialog.dismiss();
            }
        });
        confirmDialog.show();
    }

    // onClick for save image button
    public void onSaveButton(View view){
            saveDrawing();
    }

    // onClick for done button
    public void onDoneButton(View view){
        if (drawView.isEdited()) {
            startPicEditActivity();
        } else {
            Toast.makeText(DrawActivity.this, "Draw an image to edit :)", Toast.LENGTH_LONG).show();
        }
    }

    private File saveDrawing() {
        //save drawing
        drawView.setDrawingCacheEnabled(true);
        File imageFile = Utils.saveFillerBitmap(drawView.getDrawingCache(), this);
        drawView.destroyDrawingCache();
        return imageFile;
    }

    private void startPicSourceActivity() {
        Intent intent = new Intent(this, PicSourceActivity.class);
        if (drawView.isEdited()) {
            File imageFile = saveDrawing();
            // Load intent with uri
            intent.putExtra(PicSourceActivity.IMAGE_URI_STATE_KEY, Uri.fromFile(imageFile));
        }
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void startPicEditActivity() {
        Intent intent = new Intent(this, PicEditActivity.class);
        if (drawView.isEdited()) {
            File imageFile = saveDrawing();
            // Load intent with uri
            intent.putExtra(PicEditActivity.SOURCE_URI, Uri.fromFile(imageFile));
        }
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
