package com.caitoo0o.filler.activities.loggedinactivities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.caitoo0o.filler.R;
import com.caitoo0o.filler.activities.loggedoutactivites.LoginActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by caitoo0o on 6/11/2017.
 */

public class LoggedInActivity extends AppCompatActivity  implements
        GoogleApiClient.OnConnectionFailedListener {

    protected FirebaseAuth.AuthStateListener authListener;
    protected FirebaseAuth auth;

    // Google login vars (used for logout in this activity)
    protected GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

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

    }

    // Google sign out stuff
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An error occurred that cannot be resolved. Cannot connect to Google APIs
        Log.d(this.getClass().getName(), "GoogleAPIClient onConnectionFailed:" + connectionResult);
        // TODO: Put this alert in a fragment so orientation change can be handled
        // TODO: See https://developer.android.com/guide/topics/ui/dialogs.html
        new AlertDialog.Builder(LoggedInActivity.this)
                .setMessage(getString(R.string.google_play_services_error))
                .setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing, just close the dialog
                            }
                        }).show();    }

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
