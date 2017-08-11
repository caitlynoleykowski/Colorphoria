package com.caitoo0o.filler.activities.loggedinactivities;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Cait on 9/22/2016.
 */
public class GetImageAsyncTask extends AsyncTask<Integer, Void, Drawable> {
    private WeakReference<ImageView> imageViewWeakReference;
    private WeakReference<ProgressBar> progressBarWeakReference;
    private String urlString;

    public GetImageAsyncTask(ImageView imageView, ProgressBar progressBar, String url) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewWeakReference = new WeakReference<ImageView>(imageView);
        progressBarWeakReference = new WeakReference<ProgressBar>(progressBar);
        urlString = url;
    }

    // Apply effect to image in background
    @Override
    protected Drawable doInBackground(Integer... params) {
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
            Log.e(this.getClass().getName(), "Could not create InputStream from url content.", e);
            return null;
        }
        Drawable d = Drawable.createFromStream(content , "src");
        return d;
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