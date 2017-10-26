package com.caitoo0o.filler.activities.loggedoutactivites;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.caitoo0o.filler.R;
import com.caitoo0o.filler.utils.LoginUtils;
import com.caitoo0o.filler.utils.Utils;

import java.util.Date;

public class TermsActivity extends AppCompatActivity {

    public static final String FULLNAME_STATE_KEY = "FULLNAME_STATE_KEY";

    private Date birthdayDate;
    private boolean startedFromSignup = true;


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

    public void onAccept(View v) {
        // Only continue forward to the SignupActivity if the terms form is filled out (name is entered)
        EditText nameEditText = (EditText) findViewById(R.id.name_edit_text);
        if (!nameEditText.getText().toString().isEmpty()) {
            String fullName = nameEditText.getText().toString();
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
