package com.oo0o0o.colorphoria.activities.loggedoutactivites;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputLayout;
import com.oo0o0o.colorphoria.R;
import com.oo0o0o.colorphoria.activities.BaseColorphoriaActivity;
import com.oo0o0o.colorphoria.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.apache.commons.validator.routines.EmailValidator;

import java.util.Date;

import static com.oo0o0o.colorphoria.utils.LoginUtils.recordNewUserAfterFirebaseSignupSuccess;
import static com.oo0o0o.colorphoria.utils.LoginUtils.signupFailure;

public class SignupActivity extends BaseColorphoriaActivity {

    private EditText emailEditText, passwordEditText, confirmPasswordEditText;
    private TextInputLayout emailTextInputLayout, passwordTextInputLayout, confirmPasswordTextInputLayout;
    private Button signUpButton;
    private Button signInButtonLink;

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
        confirmPasswordEditText = (EditText) findViewById(R.id.confirm_password_edit_text);
        emailTextInputLayout = (TextInputLayout) findViewById(R.id.emailTextInputLayout);
        passwordTextInputLayout = (TextInputLayout) findViewById(R.id.passwordTextInputLayout);
        confirmPasswordTextInputLayout = (TextInputLayout) findViewById(R.id.confirmPasswordTextInputLayout);
        signUpButton = (Button) findViewById(R.id.sign_up_button);
        signInButtonLink = (Button) findViewById(R.id.sign_in_button);

        // Make sure font in password text edit is the default
        passwordEditText.setTypeface(Typeface.DEFAULT);
        passwordEditText.setTransformationMethod(new PasswordTransformationMethod());
        confirmPasswordEditText.setTypeface(Typeface.DEFAULT);
        confirmPasswordEditText.setTransformationMethod(new PasswordTransformationMethod());

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fullName == null || birthdayDate == null) {
                    Utils.showCustomToast(SignupActivity.this, "fullName or birthday date null");
                    return;
                }

                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String confirmPassword = confirmPasswordEditText.getText().toString().trim();

                emailTextInputLayout.setError("");
                passwordTextInputLayout.setError("");
                confirmPasswordTextInputLayout.setError("");

                if (TextUtils.isEmpty(email)) {
                    emailTextInputLayout.setError(getString(R.string.empty_email_input));
                    return;
                }

                if (!EmailValidator.getInstance().isValid(email)) {
                    emailTextInputLayout.setError(getString(R.string.invalid_email_input));
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    passwordTextInputLayout.setError(getString(R.string.empty_password_input));
                    return;
                }

                if (!confirmPassword.equals(password)) {
                    confirmPasswordTextInputLayout.setError(getString(R.string.password_input_mismatch));
                    return;
                }

                if (password.length() < 6) {
                    passwordTextInputLayout.setError(getString(R.string.password_too_short));
                    return;
                }

                Utils.showBlockingProgressBar(SignupActivity.this);

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
        Utils.removeBlockingProgressBar(SignupActivity.this);
    }

    private void signupCompletionCallback(Task<AuthResult> task) {
        if (!task.isSuccessful()) {
            Log.d(this.getClass().getName(), "Signup failed: " + task.getException());
            signupFailure(this);
        } else {
            recordNewUserAfterFirebaseSignupSuccess(this, fullName, birthdayDate);
        }
    }
}