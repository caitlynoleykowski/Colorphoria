package com.caitoo0o.filler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * Created by Cait on 9/22/2016.
 */
public class BitmapEffectAsyncTask extends AsyncTask<Integer, Void, Bitmap> {
    private final WeakReference<PicEditActivity> activityWeakReference;
    private final WeakReference<Context> contextWeakReference;
    private final WeakReference<ImageView> imageViewReference;
    private final WeakReference<ProgressBar> progressBarWeakReference;
    private final WeakReference<BitmapWrapper> bmwWeakReference;
    private final WeakReference<PicEditActivity.Effect> effectWeakReference;
    private final double ctrl;
    private int data = 0;

    public BitmapEffectAsyncTask(PicEditActivity activity, Context context, ImageView imageView, ProgressBar pb, PicEditActivity.Effect effect, BitmapWrapper bmw, double ctrl) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        activityWeakReference = new WeakReference<PicEditActivity>(activity);
        contextWeakReference = new WeakReference<Context>(context);
        imageViewReference = new WeakReference<ImageView>(imageView);
        progressBarWeakReference = new WeakReference<ProgressBar>(pb);
        bmwWeakReference = new WeakReference<BitmapWrapper>(bmw);
        effectWeakReference = new WeakReference<PicEditActivity.Effect>(effect);
        this.ctrl = ctrl;
    }

    // Apply effect to image in background
    @Override
    protected Bitmap doInBackground(Integer... params) {
        if (effectWeakReference.get() != null && bmwWeakReference.get() != null) {
            switch (effectWeakReference.get()) {
                case NONE:
                    // do nothing
                    break;
                case RESET:
                    bmwWeakReference.get().reset();
                    break;
                case ROTATE_RIGHT:
                    bmwWeakReference.get().rotateRight();
                    break;
                case ROTATE_LEFT:
                    bmwWeakReference.get().rotateLeft();
                    break;
                case RANDOM_BLEED_FILL:
                    bmwWeakReference.get().randomBleedFill(ctrl);
                    break;
                case THRESHOLD:
                    bmwWeakReference.get().threshold(ctrl);
                    break;
                case RANDOM_LINE_FILL:
                    bmwWeakReference.get().randomLineFill(ctrl);
                    break;
                case RANDOM_FLOOD_FILL:
                    bmwWeakReference.get().randomFloodFill(ctrl);
                    break;
            }
        }
        return bmwWeakReference.get().getModifiedBitmap();
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (imageViewReference != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
        if (activityWeakReference != null && activityWeakReference.get() != null) {
            activityWeakReference.get().doneProgress();
        }
    }
}