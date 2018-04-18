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
    private WeakReference<PicEditActivity> activityWeakReference;
    protected WeakReference<ImageView> imageViewWeakReference;
    private WeakReference<BitmapWrapper> bmwWeakReference;
    private WeakReference<BitmapEffect> effectWeakReference;
    private final double ctrl;
    private final int randomSeed;
    private final ArrayList<Integer> colorPalette;

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
            if (effect.getEffectType() == EffectTypes.RESET)
                bmwWeakReference.get().reset(ctrl, randomSeed, colorPalette);
            else if (effect.getEffectType() == EffectTypes.ROTATE_RIGHT)
                bmwWeakReference.get().rotateRight(ctrl, randomSeed, colorPalette);
            else
                bmwWeakReference.get().applyEffect(effect, ctrl, randomSeed, colorPalette);
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