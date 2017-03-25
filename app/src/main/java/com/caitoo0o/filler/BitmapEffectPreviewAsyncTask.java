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
                                        double ctrl) {
        super(activity, imageView, effect, null, ctrl);
        sourceBitmapWeakReference = new WeakReference<Bitmap>(sourceBitmap);
    }

    // Create the thumbnail bitmap and use that to apply the effect to instead of the large bitmap
    @Override
    protected Bitmap doInBackground(Integer... params) {
        if (sourceBitmapWeakReference != null && sourceBitmapWeakReference.get() != null) {
            Bitmap sourceBitmap = sourceBitmapWeakReference.get();
            Bitmap imgThumbnail = ThumbnailUtils.extractThumbnail(sourceBitmap, 100, 100);
            BitmapWrapper thumbnailBmw = new BitmapWrapper();
            thumbnailBmw.setSource(imgThumbnail);
            super.setBmwWeakReference(thumbnailBmw);
            return super.doInBackground();
        } else {
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
    }
}