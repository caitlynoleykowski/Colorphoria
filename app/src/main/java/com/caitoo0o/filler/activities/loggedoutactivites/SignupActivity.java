package com.caitoo0o.filler.activities.loggedoutactivites;

import android.app.Dialog;
import android.content.Context;
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
import android.widget.Toast;

import com.caitoo0o.filler.R;
import com.caitoo0o.filler.backgroundtasks.RestHelper;
import com.caitoo0o.filler.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Date;

import static com.caitoo0o.filler.utils.LoginUtils.recordNewUserAfterFirebaseSignupSuccess;
import static com.caitoo0o.filler.utils.LoginUtils.signupFailure;

public class SignupActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private TextView signUpButtonTextView;
    private Button signInButtonLink;
    private ProgressBar progressBar;

    private FirebaseAuth auth;

    String fullName;
    Date birthdayDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        if (savedInstanceState != null) {
            fullName = savedInstanceState.getString(TermsActivity.FULLNAME_STATE_KEY);
            birthdayDate = (Date) savedInstanceState.getSerializable(BirthdayActivity.BIRTHDAY_STATE_KEY);
        } else if (getIntent().hasExtra(BirthdayActivity.BIRTHDAY_STATE_KEY)) {
            fullName = getIntent().getStringExtra(TermsActivity.FULLNAME_STATE_KEY);
            birthdayDate = (Date) getIntent().getSerializableExtra(BirthdayActivity.BIRTHDAY_STATE_KEY);
        } else {
            // TODO: Error/bug, shouldn't get here
        }

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
                if (fullName == null || birthdayDate == null) {
                    Toast.makeText(SignupActivity.this, "fullName or birthday date null", Toast.LENGTH_LONG).show();
                    return;
                }

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
                                signupCompletionCallback(task);
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

    private void signupCompletionCallback(Task<AuthResult> task) {
        progressBar.setVisibility(View.GONE);
        if (!task.isSuccessful()) {
            Log.d(this.getClass().getName(), "Signup failed: " + task.getException());
            signupFailure(this);
        } else {
            recordNewUserAfterFirebaseSignupSuccess(this, fullName, birthdayDate);
        }
    }
}