package com.oo0o0o.colorphoria.effecthelpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.oo0o0o.colorphoria.activities.loggedinactivities.edit.PicEditActivityKotlin;
import com.oo0o0o.colorphoria.effects.*;

import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * Created by Cait on 9/22/2016.
 */
public class PicEditBitmapLoaderAsyncTask extends AsyncTask<Integer, Void, Bitmap> {
    private final WeakReference<PicEditActivityKotlin> activityWeakReference;
    private final WeakReference<Context> contextWeakReference;
    private final WeakReference<ImageView> imageViewReference;
    private final WeakReference<Uri> uriWeakReference;
    private final WeakReference<BitmapWrapper> bmwWeakReference;
    private final WeakReference<ImageButton> originalImgBtnWeakReference;
    private final WeakReference<ImageButton> bleedImgBtnWeakReference;
    private final WeakReference<ImageButton> thresholdImgBtnWeakReference;
    private final WeakReference<ImageButton> lineImgBtnWeakReference;
    private final WeakReference<ImageButton> floodImgBtnWeakReference;
    private final WeakReference<ImageButton> invertImgBtnWeakReference;
    private final WeakReference<ImageButton> lightOutlineImgBtnWeakReference;
    private final WeakReference<ImageButton> outlineImgBtnWeakReference;
    private final WeakReference<ImageButton> fadeFillImgBtnWeakReference;
    private final WeakReference<ImageButton> fadeTintImgBtnWeakReference;
    private final double ctrl;
    private final int randomSeed;

    public PicEditBitmapLoaderAsyncTask(PicEditActivityKotlin activity, Context context, ImageView imageView, Uri uri, BitmapWrapper bmw,
                                        ImageButton originalImageBtn,
                                        ImageButton bleedImageBtn,
                                        ImageButton thresholdImageBtn,
                                        ImageButton lineImageBtn,
                                        ImageButton floodImageBtn,
                                        ImageButton invertImageBtn,
                                        ImageButton lightOutlineImgBtn,
                                        ImageButton outlineImageBtn,
                                        ImageButton fadeFillImageBtn,
                                        ImageButton fadeTintImageBtn,
                                        double ctrl,
                                        int randomSeed) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        activityWeakReference = new WeakReference<PicEditActivityKotlin>(activity);
        contextWeakReference = new WeakReference<Context>(context);
        imageViewReference = new WeakReference<ImageView>(imageView);
        uriWeakReference = new WeakReference<Uri>(uri);
        bmwWeakReference = new WeakReference<BitmapWrapper>(bmw);
        originalImgBtnWeakReference = new WeakReference<ImageButton>(originalImageBtn);
        bleedImgBtnWeakReference = new WeakReference<ImageButton>(bleedImageBtn);
        thresholdImgBtnWeakReference = new WeakReference<ImageButton>(thresholdImageBtn);
        lineImgBtnWeakReference = new WeakReference<ImageButton>(lineImageBtn);
        floodImgBtnWeakReference = new WeakReference<ImageButton>(floodImageBtn);
        invertImgBtnWeakReference = new WeakReference<ImageButton>(invertImageBtn);
        lightOutlineImgBtnWeakReference = new WeakReference<ImageButton>(lightOutlineImgBtn);
        outlineImgBtnWeakReference = new WeakReference<ImageButton>(outlineImageBtn);
        fadeFillImgBtnWeakReference = new WeakReference<ImageButton>(fadeFillImageBtn);
        fadeTintImgBtnWeakReference = new WeakReference<ImageButton>(fadeTintImageBtn);
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
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
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
        if (activityWeakReference != null && activityWeakReference.get() != null) {
            activityWeakReference.get().doneProgress();
        }

        // Don't need a colorPalette here because there will never be one yet
        if (activityWeakReference != null && activityWeakReference.get() != null && bitmap != null) {
            if (originalImgBtnWeakReference != null && originalImgBtnWeakReference.get() != null) {
                new BitmapEffectPreviewAsyncTask(activityWeakReference.get(),
                        originalImgBtnWeakReference.get(), new ResetEffect(),
                        bitmap, ctrl, randomSeed, null).execute();
            }
            if (bleedImgBtnWeakReference != null && bleedImgBtnWeakReference.get() != null) {
                new BitmapEffectPreviewAsyncTask(activityWeakReference.get(),
                        bleedImgBtnWeakReference.get(), new RandomBleedFillEffect(),
                        bitmap, ctrl, randomSeed, null).execute();
            }
            if (floodImgBtnWeakReference != null && floodImgBtnWeakReference.get() != null) {
                new BitmapEffectPreviewAsyncTask(activityWeakReference.get(),
                        floodImgBtnWeakReference.get(), new RandomFloodFillEffect(),
                        bitmap, ctrl, randomSeed, null).execute();
            }
            if (lineImgBtnWeakReference != null && lineImgBtnWeakReference.get() != null) {
                new BitmapEffectPreviewAsyncTask(activityWeakReference.get(),
                        lineImgBtnWeakReference.get(), new RandomLineFillEffect(),
                        bitmap, ctrl, randomSeed, null).execute();
            }
            if (invertImgBtnWeakReference != null && invertImgBtnWeakReference.get() != null) {
                new BitmapEffectPreviewAsyncTask(activityWeakReference.get(),
                        invertImgBtnWeakReference.get(), new InvertEffect(),
                        bitmap, ctrl, randomSeed, null).execute();
            }
            if (lightOutlineImgBtnWeakReference != null && lightOutlineImgBtnWeakReference.get() != null) {
                new BitmapEffectPreviewAsyncTask(activityWeakReference.get(),
                        lightOutlineImgBtnWeakReference.get(), new OutlineEffect(false),
                        bitmap, ctrl, randomSeed, null).execute();
            }
            if (outlineImgBtnWeakReference != null && outlineImgBtnWeakReference.get() != null) {
                new BitmapEffectPreviewAsyncTask(activityWeakReference.get(),
                        outlineImgBtnWeakReference.get(), new OutlineEffect(true),
                        bitmap, ctrl, randomSeed, null).execute();
            }
            if (thresholdImgBtnWeakReference != null && thresholdImgBtnWeakReference.get() != null) {
                new BitmapEffectPreviewAsyncTask(activityWeakReference.get(),
                        thresholdImgBtnWeakReference.get(), new ThresholdEffect(),
                        bitmap, ctrl, randomSeed, null).execute();
            }
            if (fadeFillImgBtnWeakReference != null && fadeFillImgBtnWeakReference.get() != null) {
                new BitmapEffectPreviewAsyncTask(activityWeakReference.get(),
                        fadeFillImgBtnWeakReference.get(), new FadeFillEffect(),
                        bitmap, ctrl, randomSeed, null).execute();
            }
            if (fadeTintImgBtnWeakReference != null && fadeTintImgBtnWeakReference.get() != null) {
                new BitmapEffectPreviewAsyncTask(activityWeakReference.get(),
                        fadeTintImgBtnWeakReference.get(), new FadeFillEffect(true),
                        bitmap, ctrl, randomSeed, null).execute();
            }
        }
    }
}