package com.caitoo0o.filler.effecthelpers;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.widget.ImageView;

import com.caitoo0o.filler.activities.loggedinactivities.PicEditActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Cait on 9/22/2016.
 */
public class BitmapEffectPreviewAsyncTask extends BitmapEffectAsyncTask {

    WeakReference<Bitmap> sourceBitmapWeakReference;

    public BitmapEffectPreviewAsyncTask(PicEditActivity activity, ImageView imageView,
                                        EffectTypes effect, Bitmap sourceBitmap,
                                        double ctrl, int randomSeed, ArrayList<Integer> colorPalette) {
        super(activity, imageView, effect, null, ctrl, randomSeed, colorPalette);
        sourceBitmapWeakReference = new WeakReference<Bitmap>(sourceBitmap);
    }

    // Create the thumbnail bitmap and use that to apply the effect to instead of the large bitmap
    @Override
    protected Bitmap doInBackground(Integer... params) {
        // Don't bother doing this stuff if the imageView isn't there anymore
        if (imageViewWeakReference != null && imageViewWeakReference.get() != null) {
            if (sourceBitmapWeakReference != null && sourceBitmapWeakReference.get() != null) {
                Bitmap sourceBitmap = sourceBitmapWeakReference.get();
                // Create scaled down thumbnail (not the final one which will be square) so that
                // the effect preview will fill in the same way the larger image will
                // (With the square part of the image could be cut off and so the effect would apply
                // differently)
                double scaleFactor;
                Bitmap imgThumbnail;
                if (sourceBitmap.getWidth() > sourceBitmap.getHeight()) {
                    scaleFactor = (double) 100 / (double) sourceBitmap.getHeight();
                    imgThumbnail = ThumbnailUtils.extractThumbnail(sourceBitmap, (int) (sourceBitmap.getWidth() * scaleFactor), 100);
                } else {
                    scaleFactor = (double) 100 / (double) sourceBitmap.getWidth();
                    imgThumbnail = ThumbnailUtils.extractThumbnail(sourceBitmap, 100, (int) (sourceBitmap.getHeight() * scaleFactor));
                }
                BitmapWrapper thumbnailBmw = new BitmapWrapper();
                thumbnailBmw.setSource(imgThumbnail);
                super.setBmwWeakReference(thumbnailBmw);
                return super.doInBackground();
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
        super.onPostExecute(imgThumbnail);
    }
}