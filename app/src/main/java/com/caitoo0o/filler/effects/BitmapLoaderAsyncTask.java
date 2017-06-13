package com.caitoo0o.filler.effects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.caitoo0o.filler.activities.loggedinactivities.PicEditActivity;

import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * Created by Cait on 9/22/2016.
 */
public class BitmapLoaderAsyncTask extends AsyncTask<Integer, Void, Bitmap> {
    private final WeakReference<PicEditActivity> activityWeakReference;
    private final WeakReference<Context> contextWeakReference;
    private final WeakReference<ImageView> imageViewReference;
    private final WeakReference<Uri> uriWeakReference;
    private final WeakReference<BitmapWrapper> bmwWeakReference;
    private final WeakReference<ImageButton> bleedImgBtnWeakReference;
    private final WeakReference<ImageButton> thresholdImgBtnWeakReference;
    private final WeakReference<ImageButton> lineImgBtnWeakReference;
    private final WeakReference<ImageButton> floodImgBtnWeakReference;
    private final double ctrl;
    private final int randomSeed;

    public BitmapLoaderAsyncTask(PicEditActivity activity, Context context, ImageView imageView, Uri uri, BitmapWrapper bmw,
                                 ImageButton bleedImageBtn, ImageButton thresholdImageBtn,
                                 ImageButton lineImageBtn, ImageButton floodImageButton, double ctrl,
                                 int randomSeed) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        activityWeakReference = new WeakReference<PicEditActivity>(activity);
        contextWeakReference = new WeakReference<Context>(context);
        imageViewReference = new WeakReference<ImageView>(imageView);
        uriWeakReference = new WeakReference<Uri>(uri);
        bmwWeakReference = new WeakReference<BitmapWrapper>(bmw);
        bleedImgBtnWeakReference = new WeakReference<ImageButton>(bleedImageBtn);
        thresholdImgBtnWeakReference = new WeakReference<ImageButton>(thresholdImageBtn);
        lineImgBtnWeakReference = new WeakReference<ImageButton>(lineImageBtn);
        floodImgBtnWeakReference = new WeakReference<ImageButton>(floodImageButton);
        this.ctrl = ctrl;
        this.randomSeed = randomSeed;
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
        if (bmwWeakReference != null && bmwWeakReference.get() != null) {
            BitmapWrapper bmw = bmwWeakReference.get();
            bmw.setSource(bitmap);
        }
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }

        // Don't need a colorPalette here because there will never be one yet
        if (activityWeakReference != null && activityWeakReference.get() != null && bitmap != null) {
            if (bleedImgBtnWeakReference != null && bleedImgBtnWeakReference.get() != null) {
                new BitmapEffectPreviewAsyncTask(activityWeakReference.get(),
                        bleedImgBtnWeakReference.get(), PicEditActivity.Effect.RANDOM_BLEED_FILL,
                        bitmap, ctrl, randomSeed, null).execute();
            }
            if (thresholdImgBtnWeakReference != null && thresholdImgBtnWeakReference.get() != null) {
                new BitmapEffectPreviewAsyncTask(activityWeakReference.get(),
                        thresholdImgBtnWeakReference.get(), PicEditActivity.Effect.THRESHOLD,
                        bitmap, ctrl, randomSeed, null).execute();
            }
            if (lineImgBtnWeakReference != null && lineImgBtnWeakReference.get() != null) {
                new BitmapEffectPreviewAsyncTask(activityWeakReference.get(),
                        lineImgBtnWeakReference.get(), PicEditActivity.Effect.RANDOM_LINE_FILL,
                        bitmap, ctrl, randomSeed, null).execute();
            }
            if (floodImgBtnWeakReference != null && floodImgBtnWeakReference.get() != null) {
                new BitmapEffectPreviewAsyncTask(activityWeakReference.get(),
                        floodImgBtnWeakReference.get(), PicEditActivity.Effect.RANDOM_FLOOD_FILL,
                        bitmap, ctrl, randomSeed, null).execute();
            }
        }
    }
}