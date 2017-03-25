package com.caitoo0o.filler;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Created by Cait on 9/22/2016.
 */
public class BitmapEffectAsyncTask extends AsyncTask<Integer, Void, Bitmap> {
    private WeakReference<PicEditActivity> activityWeakReference;
    private WeakReference<ImageView> imageViewWeakReference;
    private WeakReference<BitmapWrapper> bmwWeakReference;
    private WeakReference<PicEditActivity.Effect> effectWeakReference;
    private final double ctrl;

    public BitmapEffectAsyncTask(PicEditActivity activity, ImageView imageView,
                                 PicEditActivity.Effect effect, BitmapWrapper bmw, double ctrl) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        activityWeakReference = new WeakReference<PicEditActivity>(activity);
        imageViewWeakReference = new WeakReference<ImageView>(imageView);
        bmwWeakReference = new WeakReference<BitmapWrapper>(bmw);
        effectWeakReference = new WeakReference<PicEditActivity.Effect>(effect);
        this.ctrl = ctrl;
    }

    protected void setBmwWeakReference(BitmapWrapper bmW) {
        bmwWeakReference = new WeakReference<BitmapWrapper>(bmW);
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
        if (imageViewWeakReference != null && bitmap != null) {
            final ImageView imageView = imageViewWeakReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
        if (activityWeakReference != null && activityWeakReference.get() != null) {
            activityWeakReference.get().doneProgress();
        }
    }
}