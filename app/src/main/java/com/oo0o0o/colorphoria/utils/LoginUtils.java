package com.oo0o0o.colorphoria.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;

import com.oo0o0o.colorphoria.R;
import com.oo0o0o.colorphoria.activities.loggedinactivities.BottomNavActivity;
import com.oo0o0o.colorphoria.activities.loggedoutactivites.LoginActivity;
import com.oo0o0o.colorphoria.backgroundtasks.RestHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Date;

/**
 * Created by oo0o0o on 10/25/2017.
 */

public class LoginUtils {

    public static void recordNewUserAfterFirebaseSignupSuccess(final Activity activity,
                                                               String fullName, Date birthdayDate) {
        Utils.showBlockingProgressBar(activity);

        // Get firebase auth instance
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();

        RestHelper.recordNewUser(
                activity,
                firebaseUser,
                fullName,
                "", // TODO: username
                String.valueOf(birthdayDate.getTime()),
                new RestHelper.HttpCallbacks() {
                    @Override
                    public void onSuccess(String response) {
                        signupSuccess(activity);
                    }
                    @Override
                    public void onFailure(String error) {
                        signupFailure(activity);
                    }
                }
        );
    }

    public static void signupSuccess(final Activity activity) {
        Utils.showAlertDialog(
                activity,
                activity.getString(R.string.signup_success),
                activity.getString(R.string.ok),
                new Utils.AlertDialogHandler() {
                    public void onPositiveButton(Dialog confirmDialog) {
                        // Close the dialog and go to login screen
                        confirmDialog.dismiss();
                        activity.startActivity(new Intent(activity, BottomNavActivity.class));
                    }
                },
                false);
        Utils.removeBlockingProgressBar(activity);
    }

    public static void signupFailure(final Activity activity) {
        // TODO: Delete user or don't display signup failed when rest endpoint call failed just try again???
        Utils.showAlertDialog(
                activity,
                activity.getString(R.string.signup_failed),
                activity.getString(R.string.ok),
                new Utils.AlertDialogHandler() {
                    public void onPositiveButton(Dialog confirmDialog) {
                        // Close the dialog and go to login screen
                        confirmDialog.dismiss();
                        activity.startActivity(new Intent(activity, LoginActivity.class));
                    }
                },
                false);
        Utils.removeBlockingProgressBar(activity);
    }
}
