package com.caitoo0o.filler.activities.loggedoutactivites;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.caitoo0o.filler.R;
import com.caitoo0o.filler.activities.loggedinactivities.PicSourceActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    private ProgressBar progressBar;

    private FirebaseAuth auth;

    // Email and password login vars
    private EditText emailEditText, passwordEditText;
    private TextView loginButtonTextView;
    private Button signupButtonLink, resetButtonLink;

    // Google login vars
    private static final int RC_GOOGLE_SIGN_IN = 9001;
    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, PicSourceActivity.class));
            finish();
        }

        // set the view now
        setContentView(R.layout.activity_login);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        setupEmailAndPasswordLogin();

        setupGoogleLogin();

    }

    // Email and password login code begin

    private void setupEmailAndPasswordLogin() {

        emailEditText = (EditText) findViewById(R.id.email_edit_text);
        passwordEditText = (EditText) findViewById(R.id.password_edit_text);
        loginButtonTextView = (TextView) findViewById(R.id.login_button);
        signupButtonLink = (Button) findViewById(R.id.signup_button_link);
        resetButtonLink = (Button) findViewById(R.id.reset_password_button_link);

        // Make sure font in password text edit is the default
        passwordEditText.setTypeface(Typeface.DEFAULT);
        passwordEditText.setTransformationMethod(new PasswordTransformationMethod());

        loginButtonTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailLogin();
            }
        });

        signupButtonLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

        resetButtonLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });
    }

    private void emailLogin() {
        String email = emailEditText.getText().toString();
        final String password = passwordEditText.getText().toString();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.empty_email_input));
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.empty_password_input));
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        //authenticate user
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        progressBar.setVisibility(View.GONE);
                        if (!task.isSuccessful()) {
                            // there was an error
                            if (password.length() < 6) {
                                passwordEditText.setError(getString(R.string.password_too_short));
                            } else {
                                // TODO: Put this alert in a fragment so orientation change can be handled
                                // TODO: See https://developer.android.com/guide/topics/ui/dialogs.html
                                new AlertDialog.Builder(LoginActivity.this)
                                        .setMessage(getString(R.string.login_failed))
                                        .setPositiveButton(getString(R.string.login_try_again),
                                                new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // Do nothing, just close the dialog
                                            }
                                        }).show();
                            }
                        } else {
                            Intent intent = new Intent(LoginActivity.this, PicSourceActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }

    // Email and password login code end

    // Google login code begin

    private void setupGoogleLogin() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* Activity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Setup button text and add handler
        ViewGroup googleSignInButton = (ViewGroup) findViewById(R.id.google_login_button);
        setGoogleSignInButtonText(googleSignInButton);
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
            }
        });
    }

    private void setGoogleSignInButtonText(ViewGroup signInButton) {
        // Set text
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(R.string.login_with_google_button);
                return;
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An error occurred that cannot be resolved. Cannot connect to Google APIs
        Log.d(this.getClass().getName(), "GoogleAPIClient onConnectionFailed:" + connectionResult);
        // TODO: Put this alert in a fragment so orientation change can be handled
        // TODO: See https://developer.android.com/guide/topics/ui/dialogs.html
        new AlertDialog.Builder(LoginActivity.this)
                .setMessage(getString(R.string.google_play_services_error))
                .setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing, just close the dialog
                            }
                        }).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Show dialog to notify user that Google login failed
                // TODO: Put this alert in a fragment so orientation change can be handled
                // TODO: See https://developer.android.com/guide/topics/ui/dialogs.html
                new AlertDialog.Builder(LoginActivity.this)
                        .setMessage(getString(R.string.google_login_failed))
                        .setPositiveButton(getString(R.string.login_try_again),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Do nothing, just close the dialog
                                    }
                                }).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        progressBar.setVisibility(View.VISIBLE);

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(this.getClass().getName(), "Google AuthCredential signInWithCredential succeeded");
                            startActivity(new Intent(LoginActivity.this, PicSourceActivity.class));
                        } else {
                            Log.w(this.getClass().getName(), "Google AuthCredential signInWithCredential failed: ", task.getException());
                            // Show dialog to notify user that Google login failed
                            // TODO: Put this alert in a fragment so orientation change can be handled
                            // TODO: See https://developer.android.com/guide/topics/ui/dialogs.html
                            new AlertDialog.Builder(LoginActivity.this)
                                    .setMessage(getString(R.string.google_login_authentication_failed))
                                    .setPositiveButton(getString(R.string.login_try_again),
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // Do nothing, just close the dialog
                                                }
                                            }).show();
                        }

                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    // Google login code end
}

