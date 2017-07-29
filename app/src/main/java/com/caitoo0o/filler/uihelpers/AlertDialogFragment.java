package com.caitoo0o.filler.uihelpers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.IdRes;

import com.caitoo0o.filler.R;

/**
 * Created by caitoo0o on 7/13/2017.
 */

public class AlertDialogFragment extends DialogFragment {

    public interface AlertDialogHandler {
        public void onPositiveButtonClick();
        public void onNegativeButtonClick();
    }

    public AlertDialogFragment() { }

    public void setAlertDialogHandler(AlertDialogHandler handler) {

    }

    public void setPositiveButtonText() {

    }

    public void setNegativeButtonText() {

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_fire_missiles)
                .setPositiveButton(R.string.fire, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
