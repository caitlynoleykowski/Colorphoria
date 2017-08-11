package com.caitoo0o.filler.activities.loggedinactivities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.caitoo0o.filler.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by caitoo0o on 7/3/2017.
 */

public class ImageFeedActivity extends LoggedInActivity {

    private ProgressDialog mProgressDialog;

    private ImageAdapter adapter;
    private ArrayList<ImageInfo> imageInfoArrayList;

    private int currentImageIdx = 0;

    private static final int MAX_IMAGE_ROW_PIXELS = 900000;

    private Pattern pattern = Pattern.compile(".*/photos/(.*?)");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_feed);

        imageInfoArrayList = new ArrayList<ImageInfo>();
        // Create the adapter to convert the array to views
        adapter = new ImageAdapter(getApplicationContext(), imageInfoArrayList);

        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.image_list_view);
        listView.setAdapter(adapter);

        getMyImages();
    }

    private void getMyImages() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        RestService.HttpCallbacks callbacks = new RestService.HttpCallbacks() {
            public void onSuccess(String response) {
                Log.i(this.getClass().getName(), response);

                try {
                    JSONObject responseJson = new JSONObject(response);
                    JSONArray urlArrayJson = responseJson.getJSONArray("urls");
                    for (int i = 0; i < urlArrayJson.length(); i++) {
                            String url = (String) urlArrayJson.get(i);
                            imageInfoArrayList.add(new ImageInfo(url));
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    // TODO: Handle - this will cause an inconsistency b/w imageInfoArrayList and currentImageIdx
                    Log.i(this.getClass().getName(), "Error parsing json.", e);
                }

                // Download ihmag-izz
                //beginDownload(imageInfoArrayList.get(currentImageIdx).getUrl());
            }
            public void onFailure(String error) {
                // If get images fails notify the user
                new android.app.AlertDialog.Builder(ImageFeedActivity.this)
                        .setMessage(getString(R.string.download_failure))
                        .setPositiveButton(getString(android.R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Do nothing, just close
                                    }
                                }).show();
            }
        };
        RestService.myImages(getApplicationContext(), user, callbacks);

    }

    // Image Adapter
    // Adapter for Grid View
    private class ImageAdapter extends BaseAdapter {

        private Context context;
        private ArrayList<ImageInfo> imageInfoList;

        public ImageAdapter(Context context, ArrayList<ImageInfo> files) {
            this.context = context;
            this.imageInfoList = files;
        }

        public int getCount() {
            return imageInfoList.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = new ViewHolder();

            if (convertView == null) {
                holder.picturesView = new ImageView(context);
                //Converting the Row Layout to be used in Grid View
                convertView = getLayoutInflater().inflate(R.layout.image_row, parent, false);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.image_row_progress_bar);
            progressBar.setVisibility(View.VISIBLE);

            holder.picturesView = (ImageView) convertView.findViewById(R.id.imageview);
            //if (imageInfoList.get(position).isDownloaded()) {
            if (position < imageInfoList.size()) {
                // TODO: Is is possible this will wind up with pictures out of order if one async task finishes later than one that started later?
                new GetImageAsyncTask(holder.picturesView, progressBar, imageInfoList.get(position).getUrl()).execute();
            }

            return convertView;
        }
        // View Holder pattern used for Smooth Scrolling. As View Holder pattern recycle the findViewById() object.
        class ViewHolder {
            private ImageView picturesView;
        }


        // Helper function
        private double getScaleFactor(int bitmapWidth, int bitmapHeight) {
            int numPixels = bitmapWidth * bitmapHeight;
            if (numPixels >= MAX_IMAGE_ROW_PIXELS) {
                double scaleFactor = Math.sqrt((double) MAX_IMAGE_ROW_PIXELS / (double) numPixels);
                return scaleFactor;
            } else {
                return 1;
            }
        }
    }

    private class ImageInfo {
        private String url;
        private boolean downloaded;

        ImageInfo(String url) {
            this.url = url;
            downloaded=false;
        }

        public String getUrl() {
            return url;
        }

        public void setDownloaded(boolean downloaded) {
            this.downloaded = downloaded;
        }

        public boolean isDownloaded() {
            return downloaded;
        }
    }
}
