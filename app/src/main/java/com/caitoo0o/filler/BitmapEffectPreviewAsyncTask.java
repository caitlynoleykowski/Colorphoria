package com.caitoo0o.filler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * Created by Cait on 9/22/2016.
 */
public class BitmapEffectPreviewAsyncTask extends BitmapEffectAsyncTask {

    WeakReference<Bitmap> sourceBitmapWeakReference;

    public BitmapEffectPreviewAsyncTask(PicEditActivity activity, ImageView imageView,
                                        PicEditActivity.Effect effect, Bitmap sourceBitmap,
                                        double ctrl, int randomSeed) {
        super(activity, imageView, effect, null, ctrl, randomSeed);
        sourceBitmapWeakReference = new WeakReference<Bitmap>(sourceBitmap);
    }

    // Create the thumbnail bitmap and use that to apply the effect to instead of the large bitmap
    @Override
    protected Bitmap doInBackground(Integer... params) {
        if (imageViewWeakReference != null && imageViewWeakReference.get() != null) {
            ImageView imageView = imageViewWeakReference.get();
            int width = imageView.getMeasuredWidth();

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