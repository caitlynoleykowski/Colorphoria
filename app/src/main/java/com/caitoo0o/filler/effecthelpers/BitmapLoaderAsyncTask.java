package com.caitoo0o.filler.effecthelpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * Created by Cait on 9/10/2017.
 */
public class BitmapLoaderAsyncTask extends AsyncTask<Integer, Void, Bitmap> {
    private final WeakReference<Context> contextWeakReference;
    private final WeakReference<ImageView> imageViewReference;
    private final WeakReference<Uri> uriWeakReference;

    public BitmapLoaderAsyncTask(Context context, ImageView imageView, Uri uri) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        contextWeakReference = new WeakReference<Context>(context);
        imageViewReference = new WeakReference<ImageView>(imageView);
        uriWeakReference = new WeakReference<Uri>(uri);
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(Integer... params) {
        if (uriWeakReference != null && uriWeakReference.get() != null) {
            return grabImage(uriWeakReference.get());
        } else {
            return null;
        }
    }

    // TODO: Move to utils? (and remove from other image async loader classes)
    public Bitmap grabImage(Uri uri)
    {
        try
        {
            if (contextWeakReference != null && contextWeakReference.get() != null) {
                InputStream is = contextWeakReference.get().getContentResolver().openInputStream(uri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(is, null, options);
                double scaleFactor = BitmapWrapper.getScaleFactor(options.outWidth, options.outHeight);
                BitmapFactory.Options finalOptions = new BitmapFactory.Options();
                finalOptions.inSampleSize = (int) Math.ceil(1 / scaleFactor);
                InputStream finalInputStream = contextWeakReference.get().getContentResolver().openInputStream(uri);
                Bitmap bm = BitmapFactory.decodeStream(finalInputStream, null, finalOptions);
                return bm;
            } else {
                return null;
            }
        }
        catch (Exception e)
        {
            Log.d("picsource", "Failed to load image", e);
            return null;
        }
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}