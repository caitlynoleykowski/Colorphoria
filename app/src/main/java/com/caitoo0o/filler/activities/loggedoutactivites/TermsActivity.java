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
import com.caitoo0o.filler.utils.Utils;

public class TermsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        TextView getapp =(TextView) findViewById(R.id.terms_agreement_text_view);
        getapp.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void onAccept(View v) {
        EditText nameEditText = (EditText) findViewById(R.id.name_edit_text);
        if (!nameEditText.getText().toString().isEmpty()) {
            startActivity(new Intent(TermsActivity.this, SignupActivity.class));
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
}
