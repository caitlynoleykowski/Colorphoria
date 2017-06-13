package com.caitoo0o.filler.activities.loggedinactivities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.caitoo0o.filler.R;
import com.caitoo0o.filler.activities.loggedoutactivites.LoginActivity;
import com.caitoo0o.filler.activities.loggedoutactivites.SignupActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class AccountActivity extends LoggedInActivity {

    private TextView changeEmailButton, changePasswordButton, resetPasswordButton, deleteAccountButton,
            signOutButton;
    private EditText newEmailEditText, newPasswordEditText;
    private ProgressBar progressBar;

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    // Google login vars (used for logout in this activity)
    private GoogleApiClient googleApiClient;

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
                    startActivity(new Intent(AccountActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        changeEmailButton = (TextView) findViewById(R.id.change_email_button);
        changePasswordButton = (TextView) findViewById(R.id.change_password_button);
        resetPasswordButton = (TextView) findViewById(R.id.account_reset_password_button);
        deleteAccountButton = (TextView) findViewById(R.id.delete_account_button);
        signOutButton = (TextView) findViewById(R.id.sign_out_button);

        newEmailEditText = (EditText) findViewById(R.id.new_email_edit_text);
        newPasswordEditText = (EditText) findViewById(R.id.new_password_edit_text);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        changeEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newEmailEditText.getVisibility() == View.GONE) {
                    hideInputs();
                    newEmailEditText.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    if (user != null && !newEmailEditText.getText().toString().trim().equals("")) {
                        user.updateEmail(newEmailEditText.getText().toString().trim())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            progressBar.setVisibility(View.GONE);
                                            // TODO: Put this alert in a fragment so orientation change can be handled
                                            // TODO: See https://developer.android.com/guide/topics/ui/dialogs.html
                                            new AlertDialog.Builder(AccountActivity.this)
                                                    .setMessage(getString(R.string.change_email_success))
                                                    .setPositiveButton(getString(R.string.ok),
                                                            new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    signOut();
                                                                }
                                                            }).show();
                                        } else {
                                            progressBar.setVisibility(View.GONE);
                                            String failedMessage;
                                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                                failedMessage = getString(R.string.change_email_failed_existing_user);
                                            } else if (task.getException() instanceof FirebaseAuthRecentLoginRequiredException) {
                                                failedMessage = getString(R.string.change_email_failed_recent_login);
                                            } else {
                                                failedMessage = getString(R.string.change_email_failed);
                                            }
                                            // TODO: Put this alert in a fragment so orientation change can be handled
                                            // TODO: See https://developer.android.com/guide/topics/ui/dialogs.html
                                            new AlertDialog.Builder(AccountActivity.this)
                                                    .setMessage(failedMessage)
                                                    .setPositiveButton(getString(R.string.ok),
                                                            new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    // Do nothing, just close the dialog
                                                                }
                                                            }).show();
                                        }
                                    }
                                });
                    } else if (newEmailEditText.getText().toString().trim().equals("")) {
                        newEmailEditText.setError(getString(R.string.empty_email_input));
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }
        });

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newPasswordEditText.getVisibility() == View.GONE) {
                    hideInputs();
                    newPasswordEditText.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    if (user != null && !newPasswordEditText.getText().toString().trim().equals("")) {
                        if (newPasswordEditText.getText().toString().trim().length() < 6) {
                            newPasswordEditText.setError(getString(R.string.password_too_short));
                            progressBar.setVisibility(View.GONE);
                        } else {
                            user.updatePassword(newPasswordEditText.getText().toString().trim())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                progressBar.setVisibility(View.GONE);
                                                // TODO: Put this alert in a fragment so orientation change can be handled
                                                // TODO: See https://developer.android.com/guide/topics/ui/dialogs.html
                                                new AlertDialog.Builder(AccountActivity.this)
                                                        .setMessage(getString(R.string.change_password_success))
                                                        .setPositiveButton(getString(R.string.ok),
                                                                new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        signOut();
                                                                    }
                                                                }).show();
                                            } else {
                                                progressBar.setVisibility(View.GONE);
                                                // TODO: Put this alert in a fragment so orientation change can be handled
                                                // TODO: See https://developer.android.com/guide/topics/ui/dialogs.html
                                                new AlertDialog.Builder(AccountActivity.this)
                                                        .setMessage(getString(R.string.change_password_failed))
                                                        .setPositiveButton(getString(R.string.ok),
                                                                new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        // Do nothing, just close the dialog
                                                                    }
                                                                }).show();
                                            }
                                        }
                                    });
                        }
                    } else if (newPasswordEditText.getText().toString().trim().equals("")) {
                        newPasswordEditText.setError(getString(R.string.empty_password_input));
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }
        });

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideInputs();
                progressBar.setVisibility(View.VISIBLE);
                if (!auth.getCurrentUser().getEmail().trim().equals("")) {
                    auth.sendPasswordResetEmail(auth.getCurrentUser().getEmail().trim())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        progressBar.setVisibility(View.GONE);
                                        // TODO: Put this alert in a fragment so orientation change can be handled
                                        // TODO: See https://developer.android.com/guide/topics/ui/dialogs.html
                                        new AlertDialog.Builder(AccountActivity.this)
                                                .setMessage(getString(R.string.reset_password_success))
                                                .setPositiveButton(getString(R.string.ok),
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                signOut();
                                                            }
                                                        }).show();
                                    } else {
                                        progressBar.setVisibility(View.GONE);
                                        // TODO: Put this alert in a fragment so orientation change can be handled
                                        // TODO: See https://developer.android.com/guide/topics/ui/dialogs.html
                                        new AlertDialog.Builder(AccountActivity.this)
                                                .setMessage(getString(R.string.reset_password_failed))
                                                .setPositiveButton(getString(R.string.ok),
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                // Do nothing, just close the dialog
                                                            }
                                                        }).show();
                                    }
                                }
                            });
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideInputs();
                // TODO: Put this alert in a fragment so orientation change can be handled
                // TODO: See https://developer.android.com/guide/topics/ui/dialogs.html
                new AlertDialog.Builder(AccountActivity.this)
                        .setMessage(getString(R.string.delete_account_confirmation))
                        .setPositiveButton(getString(R.string.yes),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        deleteUser(user);
                                    }
                                })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // do nothing, just close the dialog
                            }
                        }).show();

            }
        });

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideInputs();
                signOut();
            }
        });

    }

    private void hideInputs() {
        // Hide the inputs until the user selects the option that requires them
        newEmailEditText.setVisibility(View.GONE);
        newPasswordEditText.setVisibility(View.GONE);
    }

    public void deleteUser(FirebaseUser user) {
        progressBar.setVisibility(View.VISIBLE);
        if (user != null) {
            user.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                googleSignOut(); // Have to do this when user is deleted in case it was a google sign in
                                progressBar.setVisibility(View.GONE);
                                // TODO: Put this alert in a fragment so orientation change can be handled
                                // TODO: See https://developer.android.com/guide/topics/ui/dialogs.html
                                new AlertDialog.Builder(AccountActivity.this)
                                        .setMessage(getString(R.string.delete_account_success))
                                        .setPositiveButton(getString(R.string.ok),
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        // Go to signup activity
                                                        startActivity(new Intent(AccountActivity.this, SignupActivity.class));
                                                        finish();
                                                    }
                                                }).show();
                            } else {
                                progressBar.setVisibility(View.GONE);
                                // TODO: Put this alert in a fragment so orientation change can be handled
                                // TODO: See https://developer.android.com/guide/topics/ui/dialogs.html
                                new AlertDialog.Builder(AccountActivity.this)
                                        .setMessage(getString(R.string.delete_account_failed))
                                        .setPositiveButton(getString(R.string.ok),
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        // Do nothing, just close the dialog
                                                    }
                                                }).show();
                            }
                        }
                    });
        }
    }
}