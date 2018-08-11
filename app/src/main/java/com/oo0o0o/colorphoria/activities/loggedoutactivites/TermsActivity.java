package com.oo0o0o.colorphoria.activities.loggedoutactivites;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.oo0o0o.colorphoria.R;
import com.oo0o0o.colorphoria.activities.BaseColorphoriaActivity;
import com.oo0o0o.colorphoria.utils.LoginUtils;
import com.oo0o0o.colorphoria.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Date;

public class TermsActivity extends BaseColorphoriaActivity {

    public static final String FULLNAME_STATE_KEY = "FULLNAME_STATE_KEY";

    private Date birthdayDate;
    private boolean startedFromSignup = true;
    private boolean startingAnotherActivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        if (savedInstanceState != null) {
            String fullname = savedInstanceState.getString(FULLNAME_STATE_KEY);
            if (fullname != null) {
                EditText nameEditText = (EditText) findViewById(R.id.name_edit_text);
                nameEditText.setText(fullname);
            }
            birthdayDate = (Date) savedInstanceState.getSerializable(BirthdayActivity.BIRTHDAY_STATE_KEY);
            startedFromSignup = savedInstanceState.getBoolean(BirthdayActivity.STARTED_FROM_SIGNUP);
        } else if (getIntent().hasExtra(BirthdayActivity.BIRTHDAY_STATE_KEY)) {
            birthdayDate = (Date) getIntent().getSerializableExtra(BirthdayActivity.BIRTHDAY_STATE_KEY);
            startedFromSignup = getIntent().getBooleanExtra(BirthdayActivity.STARTED_FROM_SIGNUP, true);
        }

        TextView getapp =(TextView) findViewById(R.id.terms_agreement_text_view);
        getapp.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onResume() {
        startingAnotherActivity = false;

        // If started from a google sign in but has since been signed out, go back to login
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (!startedFromSignup && auth.getCurrentUser() == null) {
            startActivity(new Intent(TermsActivity.this, LoginActivity.class));
            finish();
        }

        super.onResume();
    }

    @Override
    public void onStop() {
        if (!startingAnotherActivity) {
            signOut();
        }
        super.onStop();
    }

    private void signOut() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            auth.signOut();
        }
    }

    public void onAccept(View v) {
        // Only continue forward to the SignupActivity if the terms form is filled out (name is entered)
        EditText nameEditText = (EditText) findViewById(R.id.name_edit_text);
        if (!nameEditText.getText().toString().isEmpty()) {
            String fullName = nameEditText.getText().toString();
            startingAnotherActivity = true;
            if (startedFromSignup) {
                // Start the Signup activity with birthday and fullname as extras
                Intent signupActivityIntent = new Intent(TermsActivity.this, SignupActivity.class);
                signupActivityIntent.putExtra(BirthdayActivity.BIRTHDAY_STATE_KEY, birthdayDate);
                signupActivityIntent.putExtra(TermsActivity.FULLNAME_STATE_KEY, fullName);
                startActivity(signupActivityIntent);
            } else {
                LoginUtils.recordNewUserAfterFirebaseSignupSuccess(this, fullName, birthdayDate);
            }
        } else {
            Utils.showAlertDialog(
                    this,
                    getString(R.string.terms_error_enter_name),
                    getString(android.R.string.ok),
                    new Utils.AlertDialogHandler() {
                        @Override
                        public void onPositiveButton(Dialog confirmDialog) {
                            confirmDialog.dismiss();
                        }
                    },
                    false);
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        EditText nameEditText = (EditText) findViewById(R.id.name_edit_text);
        String fullName = nameEditText.getText().toString();
        outState.putString(FULLNAME_STATE_KEY, fullName);
        outState.putSerializable(BirthdayActivity.BIRTHDAY_STATE_KEY, birthdayDate);
    }
}
