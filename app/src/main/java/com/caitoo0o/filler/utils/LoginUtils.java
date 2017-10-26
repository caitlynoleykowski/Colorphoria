package com.caitoo0o.filler.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;

import com.caitoo0o.filler.R;
import com.caitoo0o.filler.activities.loggedinactivities.PicSourceActivity;
import com.caitoo0o.filler.activities.loggedoutactivites.LoginActivity;
import com.caitoo0o.filler.activities.loggedoutactivites.SignupActivity;
import com.caitoo0o.filler.backgroundtasks.RestHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Date;

/**
 * Created by caitoo0o on 10/25/2017.
 */

public class LoginUtils {

    public static void recordNewUserAfterFirebaseSignupSuccess(final Context context,
                                                               String fullName, Date birthdayDate) {
        // Get firebase auth instance
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();

        RestHelper.recordNewUser(
                context,
                firebaseUser,
                fullName,
                "", // TODO: username
                String.valueOf(birthdayDate.getTime()),
                new RestHelper.HttpCallbacks() {
                    @Override
                    public void onSuccess(String response) {
                        signupSuccess(context);
                    }
                    @Override
                    public void onFailure(String error) {
                        signupFailure(context);
                    }
                }
        );
    }

    public static void signupSuccess(final Context context) {
        Utils.showAlertDialog(
                context,
                context.getString(R.string.signup_success),
                context.getString(R.string.ok),
                new Utils.AlertDialogHandler() {
                    public void onPositiveButton(Dialog confirmDialog) {
                        // Close the dialog and go to login screen
                        confirmDialog.dismiss();
                        context.startActivity(new Intent(context, PicSourceActivity.class));
                    }
                },
                false);
    }

    public static void signupFailure(final Context context) {
        // TODO: Delete user or don't display signup failed when rest endpoint call failed just try again???
        Utils.showAlertDialog(
                context,
                context.getString(R.string.signup_failed),
                context.getString(R.string.ok),
                new Utils.AlertDialogHandler() {
                    public void onPositiveButton(Dialog confirmDialog) {
                        // Close the dialog and go to login screen
                        confirmDialog.dismiss();
                        context.startActivity(new Intent(context, LoginActivity.class));
                    }
                },
                false);
    }
}
