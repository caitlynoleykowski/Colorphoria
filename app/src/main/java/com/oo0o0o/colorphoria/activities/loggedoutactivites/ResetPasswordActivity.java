package com.oo0o0o.colorphoria.activities.loggedoutactivites;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.oo0o0o.colorphoria.R;
import com.oo0o0o.colorphoria.activities.BaseColorphoriaActivity;
import com.oo0o0o.colorphoria.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends BaseColorphoriaActivity {

    private EditText emailEditText;
    private Button resetButton;
    private Button backButtonLink;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        auth = FirebaseAuth.getInstance();

        emailEditText = (EditText) findViewById(R.id.email_edit_text);
        resetButton = (Button) findViewById(R.id.reset_password_button);
        backButtonLink = (Button) findViewById(R.id.back_button_link);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = emailEditText.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    emailEditText.setError(getString(R.string.empty_email_input));
                    return;
                }

                Utils.showBlockingProgressBar(ResetPasswordActivity.this);
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Utils.showAlertDialog(
                                            ResetPasswordActivity.this,
                                            getString(R.string.reset_password_success),
                                            getString(R.string.ok),
                                            new Utils.AlertDialogHandler() {
                                                public void onPositiveButton(Dialog confirmDialog) {
                                                    startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                                                    // Do nothing, just close the dialog
                                                    confirmDialog.dismiss();
                                                }
                                            },
                                            false);
                                } else {
                                    Utils.showAlertDialog(
                                            ResetPasswordActivity.this,
                                            getString(R.string.reset_password_failed),
                                            getString(R.string.ok),
                                            new Utils.AlertDialogHandler() {
                                                public void onPositiveButton(Dialog confirmDialog) {
                                                    // Do nothing, just close the dialog
                                                    confirmDialog.dismiss();
                                                }
                                            },
                                            false);
                                }

                                Utils.removeBlockingProgressBar(ResetPasswordActivity.this);
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
