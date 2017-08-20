package com.caitoo0o.filler.activities.loggedoutactivites;

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.caitoo0o.filler.R;
import com.caitoo0o.filler.activities.loggedinactivities.PicSourceActivity;
import com.caitoo0o.filler.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private TextView signUpButtonTextView;
    private Button signInButtonLink;
    private ProgressBar progressBar;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        emailEditText = (EditText) findViewById(R.id.email_edit_text);
        passwordEditText = (EditText) findViewById(R.id.password_edit_text);
        signUpButtonTextView = (TextView) findViewById(R.id.sign_up_button);
        signInButtonLink = (Button) findViewById(R.id.sign_in_button);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        // Make sure font in password text edit is the default
        passwordEditText.setTypeface(Typeface.DEFAULT);
        passwordEditText.setTransformationMethod(new PasswordTransformationMethod());

        signUpButtonTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    emailEditText.setError(getString(R.string.empty_email_input));
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    passwordEditText.setError(getString(R.string.empty_password_input));
                    return;
                }

                if (password.length() < 6) {
                    passwordEditText.setError(getString(R.string.password_too_short));
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                //create user
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Log.d(this.getClass().getName(), "Authentication failed: " + task.getException());
                                    Utils.showAlertDialog(
                                            SignupActivity.this,
                                            getString(R.string.signup_success),
                                            getString(R.string.ok),
                                            new Utils.AlertDialogHandler() {
                                                public void onPositiveButton(Dialog confirmDialog) {
                                                    // Do nothing, just close the dialog
                                                    confirmDialog.dismiss();
                                                }
                                            },
                                            false);
                                } else {
                                    Utils.showAlertDialog(
                                            SignupActivity.this,
                                            getString(R.string.signup_failed),
                                            getString(R.string.ok),
                                            new Utils.AlertDialogHandler() {
                                                public void onPositiveButton(Dialog confirmDialog) {
                                                    // Do nothing, just close the dialog
                                                    confirmDialog.dismiss();
                                                }
                                            },
                                            false);
                                    startActivity(new Intent(SignupActivity.this, PicSourceActivity.class));
                                    finish();
                                }
                            }
                        });

            }
        });

        signInButtonLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }
}