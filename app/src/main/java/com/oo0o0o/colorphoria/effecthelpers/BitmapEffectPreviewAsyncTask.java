package com.oo0o0o.colorphoria.effecthelpers;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.widget.ImageView;

import com.oo0o0o.colorphoria.activities.loggedinactivities.PicEditActivity;
import com.oo0o0o.colorphoria.effects.BitmapEffect;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Cait on 9/22/2016.
 */
public class BitmapEffectPreviewAsyncTask extends BitmapEffectAsyncTask {

    private static final int previewDimen = 200; // 200 pixels width 200 pixels height for preview

    WeakReference<Bitmap> sourceBitmapWeakReference;

    BitmapEffect effect;

    public BitmapEffectPreviewAsyncTask(PicEditActivity activity, ImageView imageView,
                                        BitmapEffect effect, Bitmap sourceBitmap,
                                        double ctrl, int randomSeed, ArrayList<Integer> colorPalette) {
        super(activity, imageView, effect, null, ctrl, randomSeed, colorPalette);
        sourceBitmapWeakReference = new WeakReference<Bitmap>(sourceBitmap);
        this.effect = effect;
    }

    // Create the thumbnail bitmap and use that to applyEffect the effect to instead of the large bitmap
    @Override
    protected Bitmap doInBackground(Integer... params) {
        // Don't bother doing this stuff if the imageView isn't there anymore
        if (imageViewWeakReference != null && imageViewWeakReference.get() != null) {
            if (sourceBitmapWeakReference != null && sourceBitmapWeakReference.get() != null) {
                Bitmap sourceBitmap = sourceBitmapWeakReference.get();
                // Create scaled down thumbnail (not the final one which will be square) so that
                // the effect preview will fill in the same way the larger image will
                // (With the square part of the image could be cut off and so the effect would applyEffect
                // differently)
                double scaleFactor;
                Bitmap imgThumbnail;
                if (sourceBitmap.getWidth() > sourceBitmap.getHeight()) {
                    scaleFactor = (double) previewDimen / (double) sourceBitmap.getHeight();
                    imgThumbnail = ThumbnailUtils.extractThumbnail(sourceBitmap, (int) (sourceBitmap.getWidth() * scaleFactor), previewDimen);
                } else {
                    scaleFactor = (double) previewDimen / (double) sourceBitmap.getWidth();
                    imgThumbnail = ThumbnailUtils.extractThumbnail(sourceBitmap, previewDimen, (int) (sourceBitmap.getHeight() * scaleFactor));
                }
                effect.applyEffect(imgThumbnail, ctrl, randomSeed, colorPalette, 0);
                return imgThumbnail;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        // Cut the preview down to the final square thumbnail
        Bitmap imgThumbnail = ThumbnailUtils.extractThumbnail(bitmap, 100, 100);
        super.onPostExecute(bitmap);
    }
}