package com.oo0o0o.colorphoria.activities.loggedinactivities;

import android.Manifest;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.oo0o0o.colorphoria.backgroundtasks.GetImageAsyncTask;
import com.oo0o0o.colorphoria.backgroundtasks.RestHelper;
import com.oo0o0o.colorphoria.data.AppDatabase;
import com.oo0o0o.colorphoria.data.MediaEntity;
import com.oo0o0o.colorphoria.reusableuserinterface.ListRefreshView;
import com.oo0o0o.colorphoria.R;
import com.oo0o0o.colorphoria.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class BottomNavActivity extends LoggedInActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_feed);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.outerLayout, ImageFeedFragment.newInstance("", ""))
                .commit();
    }

    @Override
    public void onStart() {
        super.onStart();

        ((BottomNavigationView) findViewById(R.id.bottomNavigation))
                .setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.image_feed_nav:
                                if (!item.isChecked()) {
                                    item.setChecked(true);
                                    onHome();
                                }
                                break;
                            case R.id.create_nav:
                                onAdd();
                                break;
                            case R.id.account_nav:
                                if (!item.isChecked()) {
                                    item.setChecked(true);
                                    onAccount();
                                }
                                break;
                            default:
                                // Do nothing
                        }
                        return false;
                    }
                });

    }

    public void onAdd() {
        Intent intent = new Intent(this, SourceActivity.class);
        startActivity(intent);
    }

    public void onHome() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.outerLayout, ImageFeedFragment.newInstance("", ""))
                .commit();
    }

    public void onAccount() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.outerLayout, AccountFragment.newInstance("", ""))
                .commit();
    }
}
