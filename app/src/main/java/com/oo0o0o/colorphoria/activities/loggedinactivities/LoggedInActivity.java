package com.oo0o0o.colorphoria.activities.loggedinactivities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.oo0o0o.colorphoria.R;
import com.oo0o0o.colorphoria.activities.BaseColorphoriaActivity;
import com.oo0o0o.colorphoria.activities.loggedoutactivites.LoginActivity;
import com.oo0o0o.colorphoria.utils.Utils;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by oo0o0o on 6/11/2017.
 */

public class LoggedInActivity extends BaseColorphoriaActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    protected FirebaseAuth.AuthStateListener authListener;
    protected FirebaseAuth auth;
    private FirebaseAnalytics firebaseAnalytics;

    // Google login vars (used for logout in this activity)
    protected GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get firebase auth instance
        auth = FirebaseAuth.getInstance();

        // Get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(LoggedInActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, new Bundle());
    }

    // Google sign out stuff
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An error occurred that cannot be resolved. Cannot connect to Google APIs
        Log.d(this.getClass().getName(), "GoogleAPIClient onConnectionFailed:" + connectionResult);
        Utils.showAlertDialog(
                LoggedInActivity.this,
                getString(R.string.google_play_services_error),
                getString(R.string.ok),
                new Utils.AlertDialogHandler() {
                    public void onPositiveButton(Dialog confirmDialog) {
                        confirmDialog.dismiss();
                    }
                },
                false);
    }

    //sign out method
    protected void signOut() {
        auth.getCurrentUser();

        googleSignOut();

        auth.signOut();
    }

    protected void googleSignOut() {
        // Google revoke access - could also just sign out but this is more complete
        Auth.GoogleSignInApi.revokeAccess(googleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        // do nothing since we're not sure we were signed in to google
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        googleApiClient.connect();

        super.onStart();


        // TODO: Do i need to be adding this onstart/onstop auth stuff everywhere??
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }
}
