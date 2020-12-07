package com.oo0o0o.colorphoria.effecthelpers;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.oo0o0o.colorphoria.activities.loggedinactivities.edit.PicEditActivityKotlin;
import com.oo0o0o.colorphoria.effects.BitmapEffect;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Cait on 9/22/2016.
 */
public class BitmapEffectAsyncTask extends AsyncTask<Integer, Void, Bitmap> {
    protected WeakReference<PicEditActivityKotlin> activityWeakReference;
    protected WeakReference<ImageView> imageViewWeakReference;
    protected WeakReference<BitmapWrapper> bmwWeakReference;
    protected WeakReference<BitmapEffect> effectWeakReference;
    protected final double ctrl;
    protected final int randomSeed;
    protected final ArrayList<Integer> colorPalette;

    public BitmapEffectAsyncTask(PicEditActivityKotlin activity, ImageView imageView,
                                 BitmapEffect effect, BitmapWrapper bmw, double ctrl,
                                 int randomSeed, ArrayList<Integer> colorPalette) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        activityWeakReference = new WeakReference<PicEditActivityKotlin>(activity);
        imageViewWeakReference = new WeakReference<ImageView>(imageView);
        bmwWeakReference = new WeakReference<BitmapWrapper>(bmw);
        effectWeakReference = new WeakReference<BitmapEffect>(effect);
        this.ctrl = ctrl;
        this.randomSeed = randomSeed;
        this.colorPalette = colorPalette;
    }

    protected void setBmwWeakReference(BitmapWrapper bmW) {
        bmwWeakReference = new WeakReference<BitmapWrapper>(bmW);
    }

    // Apply effect to image in background
    @Override
    protected Bitmap doInBackground(Integer... params) {
        if (effectWeakReference.get() != null && bmwWeakReference.get() != null) {
            BitmapEffect effect = effectWeakReference.get();
            BitmapWrapper bmW = bmwWeakReference.get();
            bmW.applyEffect(effect, ctrl, randomSeed, colorPalette);
            return bmW.getModifiedBitmap();
        } else {
            return null;
        }
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