package com.oo0o0o.colorphoria.activities;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.oo0o0o.colorphoria.R;

/**
 * Created by oo0o0o on 12/26/2017.
 */

public class BaseColorphoriaActivity extends AppCompatActivity {

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
