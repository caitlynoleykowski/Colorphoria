package com.caitoo0o.filler.activities.loggedoutactivites;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.caitoo0o.filler.R;
import com.caitoo0o.filler.activities.BaseFillerActivity;
import com.caitoo0o.filler.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.Date;

public class FillerEolActivity extends BaseFillerActivity {

    private static final String TAG = FillerEolActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filler_eol);
    }

    public void openColorphoriaOnGooglePlay(View v) {
        Intent browserIntent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=com.oo0o0o.colorphoria"));
        startActivity(browserIntent);
    }
}
