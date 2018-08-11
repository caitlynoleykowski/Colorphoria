package com.oo0o0o.colorphoria.reusableuserinterface;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.oo0o0o.colorphoria.R;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by oo0o0o on 4/1/2017.
 */

public class PaletteChooserDialog extends Dialog {

    Pattern p = Pattern.compile("[^a-fA-F0-9]");

    ArrayList<Integer> colorPalette;

    boolean colorPaletteChanged;
    int startingColorPaletteSize;

    public PaletteChooserDialog(Context context, final ArrayList<Integer> colorPalette) {
        super(context);

        // Remove title bar for pre api 21
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.effect_color_options);

        this.colorPalette = colorPalette;

        // Save starting state so we can know if color palette changed during this dialog usage
        colorPaletteChanged = false;
        startingColorPaletteSize = colorPalette.size();

        // Color palette grid view
        GridView colorPaletteGridView = (GridView) findViewById(R.id.color_palette_grid_view);
        // Create a new ArrayAdapter
        final ColorPaletteAdapter gridViewArrayAdapter = new ColorPaletteAdapter(context, colorPalette, colorPaletteGridView);
        // Data bind GridView with Adapter
        colorPaletteGridView.setAdapter(gridViewArrayAdapter);

        // Add button handler for the add color to palette button
        ImageButton addColorButton = (ImageButton) findViewById(R.id.add_color_button);
        addColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Deal with setting the edittext hex code
                EditText colorSelectedEditText = (EditText) findViewById(R.id.color_selected_edit_text);
                String colorString = colorSelectedEditText.getText().toString();
                colorString = colorString.replace("#", "");
                ImageView colorSelectedView = (ImageView) findViewById(R.id.color_selected_image_view);
                if (colorString.length() != 6 || p.matcher(colorString).find()) {
                    colorSelectedView.setBackgroundColor(0xFFFFFFFF);
                    colorSelectedEditText.setText(String.format("#%06X", 0xFFFFFF));
                } else {
                    int colorRGB = Integer.parseInt(colorString, 16);

                    // Set the color preview image view
                    int color = 0xFF000000 | colorRGB;
                    colorSelectedView.setBackgroundColor(color);

                    // Set the edittext with nice format
                    colorSelectedEditText.setText(String.format("#%06X", colorRGB));

                    // Add to the grid view adapter data
                    colorPaletteChanged = true;
                    colorPalette.add(color);
                    gridViewArrayAdapter.notifyDataSetChanged();
                }
            }
        });

        // Add button handler for the remove color from palette button
        ImageButton removeColorButton = (ImageButton) findViewById(R.id.remove_color_button);
        removeColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add to the grid view adapter data
                if (!colorPalette.isEmpty()) {
                    colorPaletteChanged = true;
                    colorPalette.remove(colorPalette.size() - 1);
                }
                gridViewArrayAdapter.notifyDataSetChanged();
            }
        });

        // Add touch handler to color spectrum image to set the color preview image view
        // and the edit text hex code to match the color being touched
        final ImageView colorsView = (ImageView) findViewById(R.id.color_spectrum_image_view);
        colorsView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // All this code is used to get the exact pixel on the image bitmap even
                // though the original image is stretched and scaled for the UI
                float eventX = motionEvent.getX();
                float eventY = motionEvent.getY();
                float[] eventXY = new float[] {eventX, eventY};

                Matrix invertMatrix = new Matrix();
                ((ImageView)view).getImageMatrix().invert(invertMatrix);

                invertMatrix.mapPoints(eventXY);
                int x = Integer.valueOf((int)eventXY[0]);
                int y = Integer.valueOf((int)eventXY[1]);

                Drawable imgDrawable = ((ImageView)view).getDrawable();
                Bitmap bitmap = ((BitmapDrawable)imgDrawable).getBitmap();

                //Limit x, y range within bitmap
                if(x < 0){
                    x = 0;
                }else if(x > bitmap.getWidth()-1){
                    x = bitmap.getWidth()-1;
                }

                if(y < 0){
                    y = 0;
                }else if(y > bitmap.getHeight()-1){
                    y = bitmap.getHeight()-1;
                }

                int touchedARGB = bitmap.getPixel(x, y);

                // Now that we have the ARGB of the selected color, go modify the UI to show the
                // color that has been selected (we only use RGB here so the user can't manually
                // enter an ARGB hex code cause that would make things more confusing)
                int touchedRGB = touchedARGB & 0xFFFFFF;
                ImageView colorSelectedView = (ImageView) findViewById(R.id.color_selected_image_view);
                colorSelectedView.setBackgroundColor(touchedARGB);
                EditText colorSelectedEditText = (EditText) findViewById(R.id.color_selected_edit_text);
                colorSelectedEditText.setText(String.format("#%06X", touchedRGB));
                return true;
            }
        });
    }

    // Helper function so that the activity using this dialog can know if it needs to do anything
    // when this dialog is dismissed
    public boolean isColorPaletteChanged() {
        if (startingColorPaletteSize == 0 && colorPalette.size() == 0) {
            return false;
        } else {
            return colorPaletteChanged;
        }
    }

    // Adapter for Grid View
    private class ColorPaletteAdapter extends BaseAdapter {

        private Context context;
        private ArrayList<Integer> colorPalette;
        private GridView parentGridView;

        public ColorPaletteAdapter(Context context, ArrayList<Integer> colorPalette, GridView parentGridView) {
            this.context = context;
            this.colorPalette = colorPalette;
            this.parentGridView = parentGridView;
        }

        public int getCount() {
            int numColumns = parentGridView.getNumColumns();
            // Show two rows by default
            int sizeOfTwoRows = numColumns*2;
            if (colorPalette.size() < sizeOfTwoRows) {
                return sizeOfTwoRows;
            }
            // Show the number of items in the color palette if that fills a complete last row
            else if (colorPalette.size() % numColumns == 0) {
                return colorPalette.size();
            }
            // Show complete rows even if the color palette doesn't fill the entire last row
            else {
                return colorPalette.size() + (numColumns - (colorPalette.size() % numColumns));
            }
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = new ViewHolder();

            if (convertView == null) {
                holder.colorView = new SquareImageButton(context);
                convertView = getLayoutInflater().inflate(R.layout.color_palette_button_item, parent, false);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            // Show the default color for an empty slot if there is not enough color palette colors
            // to fill all the slots
            int color;
            if (position < colorPalette.size()) {
                color = colorPalette.get(position);
            } else {
                color = ContextCompat.getColor(context, R.color.colorBackground);
            }
            holder.colorView = (SquareImageButton) convertView.findViewById(R.id.square_color_button);
            holder.colorView.setBackgroundColor(color);

            return convertView;
        }
        // View Holder pattern used for Smooth Scrolling. As View Holder pattern recycle the findViewById() object.
        class ViewHolder {
            private SquareImageButton colorView;
        }
    }
}
