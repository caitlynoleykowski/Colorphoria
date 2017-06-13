package com.caitoo0o.filler.activities.loggedoutactivites;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.caitoo0o.filler.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private TextView resetButtonTextView;
    private Button backButtonLink;
    private ProgressBar progressBar;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        auth = FirebaseAuth.getInstance();

        emailEditText = (EditText) findViewById(R.id.email_edit_text);
        resetButtonTextView = (TextView) findViewById(R.id.reset_password_button);
        backButtonLink = (Button) findViewById(R.id.back_button_link);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        resetButtonTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = emailEditText.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    emailEditText.setError(getString(R.string.empty_email_input));
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // TODO: Put this alert in a fragment so orientation change can be handled
                                    // TODO: See https://developer.android.com/guide/topics/ui/dialogs.html
                                    new AlertDialog.Builder(ResetPasswordActivity.this)
                                            .setMessage(getString(R.string.reset_password_success))
                                            .setPositiveButton(getString(R.string.ok),
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            // Do nothing, just close the dialog
                                                        }
                                                    }).show();
                                } else {
                                    // TODO: Put this alert in a fragment so orientation change can be handled
                                    // TODO: See https://developer.android.com/guide/topics/ui/dialogs.html
                                    new AlertDialog.Builder(ResetPasswordActivity.this)
                                            .setMessage(getString(R.string.reset_password_failed))
                                            .setPositiveButton(getString(R.string.ok),
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
        });

        backButtonLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
