package com.caitoo0o.filler.effecthelpers;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.caitoo0o.filler.activities.loggedinactivities.PicEditActivity;
import com.caitoo0o.filler.effects.BitmapEffect;
import com.caitoo0o.filler.effects.InvertEffect;
import com.caitoo0o.filler.effects.RandomBleedFillEffect;
import com.caitoo0o.filler.effects.RandomFloodFillEffect;
import com.caitoo0o.filler.effects.RandomLineFillEffect;
import com.caitoo0o.filler.effects.ThresholdEffect;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Cait on 9/22/2016.
 */
public class BitmapEffectAsyncTask extends AsyncTask<Integer, Void, Bitmap> {
    protected WeakReference<PicEditActivity> activityWeakReference;
    protected WeakReference<ImageView> imageViewWeakReference;
    protected WeakReference<BitmapWrapper> bmwWeakReference;
    protected WeakReference<BitmapEffect> effectWeakReference;
    protected final double ctrl;
    protected final int randomSeed;
    protected final ArrayList<Integer> colorPalette;

    public BitmapEffectAsyncTask(PicEditActivity activity, ImageView imageView,
                                 BitmapEffect effect, BitmapWrapper bmw, double ctrl,
                                 int randomSeed, ArrayList<Integer> colorPalette) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        activityWeakReference = new WeakReference<PicEditActivity>(activity);
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
            bmwWeakReference.get().applyEffect(effect, ctrl, randomSeed, colorPalette);
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