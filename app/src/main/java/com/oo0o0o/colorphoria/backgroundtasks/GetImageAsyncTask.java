package com.oo0o0o.colorphoria.backgroundtasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Cait on 9/22/2016.
 *
 * This task gets an image with a specified url. It first checks the internal app cache
 * to see if the image has already been downloaded. If it has it gets it from there. If not,
 * it gets it from the url.
 */
public class GetImageAsyncTask extends AsyncTask<Integer, Void, Drawable> {
    private static final String TAG = GetImageAsyncTask.class.getName();

    private WeakReference<Context> contextWeakReference;
    private WeakReference<ImageView> imageViewWeakReference;
    private WeakReference<ProgressBar> progressBarWeakReference;
    private String urlString;

    public GetImageAsyncTask(Context context, ImageView imageView, ProgressBar progressBar, String url) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        contextWeakReference = new WeakReference<Context>(context);
        imageViewWeakReference = new WeakReference<ImageView>(imageView);
        progressBarWeakReference = new WeakReference<ProgressBar>(progressBar);
        urlString = url;
    }

    // Apply effect to image in background
    @Override
    protected Drawable doInBackground(Integer... params) {
        // Turn the url into a string that can be used as a filename so it can be stored
        // in the cache directory if it was just fetched from the internet, and fetched from
        // the cache directory if it was already downloaded.
        String urlFileName = urlString.replaceAll("/", "-");
        urlFileName = urlFileName.replaceAll("\\.", "_");

        // If the image already is already downloaded (exists in the cache dir), just return it (as a Drawable)
        if (contextWeakReference != null) {
            Context context = contextWeakReference.get();
            if (context != null) {
                String filepath = context.getCacheDir().getAbsolutePath() + "/" + urlFileName;
                File f = new File(filepath);
                if (f.exists()) {
                    Drawable d = Drawable.createFromPath(filepath);
                    return d;
                }
            }
        }

        // At this point, image wasn't found, so it has to be downloaded, cached, and returned
        URL url;
        InputStream content;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            Log.e(this.getClass().getName(), "Malformed url: " + urlString, e);
            return null;
        }
        try {
            content = (InputStream) url.getContent();
        } catch (IOException e) {
            Log.e(TAG, "Could not create InputStream from url content.", e);
            return null;
        }

        Drawable drawable = Drawable.createFromStream(content , "src");

        try {
            content.close();
        } catch (IOException e) {
            // TODO
            Log.e(TAG, "Error closing InputStream after downloading image", e);
        }

        // Cache image
        // TODO: return the drawable before this is finished? Start another task for this
        if (contextWeakReference != null) {
            Context context = contextWeakReference.get();
            if (context != null) {

                try {
                    Bitmap bm = ((BitmapDrawable) drawable).getBitmap();
                    File file = new File(context.getCacheDir(), urlFileName);
                    FileOutputStream outStream = new FileOutputStream(file);
                    bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                    outStream.flush();
                    outStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error caching downloaded image", e);
                }
            }
        }

        return drawable;
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Drawable drawable) {
        if (imageViewWeakReference != null && drawable != null) {
            final ImageView imageView = imageViewWeakReference.get();
            if (imageView != null) {
                imageView.setImageDrawable(drawable);
                if (progressBarWeakReference != null) {
                    ProgressBar progressBar = progressBarWeakReference.get();
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }
        }
    }
}