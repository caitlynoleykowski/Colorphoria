package com.oo0o0o.colorphoria.activities.loggedinactivities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.transition.Fade;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.oo0o0o.colorphoria.R;

public class BottomNavActivity extends LoggedInActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        // inside your activity (if you did not enable transitions in your theme)
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        // set an exit and exit transition
//        getWindow().setExitTransition(new Fade());


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
//        overridePendingTransition(R.anim.slide_up_in, R.anim.slide_up_out);
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
