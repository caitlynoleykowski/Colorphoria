package com.oo0o0o.colorphoria.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.oo0o0o.colorphoria.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by organizer on 3/9/17.
 */

public class Utils {

	public static void setupcolorphoriaRecentAppsBar(Activity activity) {
		// Setup recent apps bar with app color
//		Bitmap bm = BitmapFactory.decodeResource(activity.getResources(), R.mipmap.ic_launcher);
//		ActivityManager.TaskDescription taskDesc = new ActivityManager.TaskDescription(
//				activity.getString(R.string.app_name), bm, activity.getColor(R.color.colorPrimaryDark));
//		activity.setTaskDescription(taskDesc);
	}

	public interface OnExplanationDeclinedHandler {
		void onExplanationDeclined();
	}

	public static boolean getPermission(final Activity activity, final String permissionKind,
									 	final String permissionExplanation, final int callbackInt,
										final OnExplanationDeclinedHandler onExplanationDeclinedHandler) {
		// Here, thisActivity is the current activity
		if (ContextCompat.checkSelfPermission(activity, permissionKind) != PackageManager.PERMISSION_GRANTED) {

			// Should we show an explanation?
			if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
					permissionKind)) {

                // Show an explanation to the user
                //If the user would like to proceed from the explanation,
                // try again to request the permission.
                Utils.showConfirmDialog(activity,
                        permissionExplanation,
                        activity.getString(R.string.yes),
                        activity.getString(R.string.no),
                        new Utils.ConfirmDialogHandler() {
                            public void onNegativeButton(Dialog confirmDialog) {
                                onExplanationDeclinedHandler.onExplanationDeclined();
                                confirmDialog.dismiss();
                            }
                            public void onPositiveButton(Dialog confirmDialog) {
                                // Request the permission
                                ActivityCompat.requestPermissions(activity,
                                        new String[]{permissionKind},
                                        callbackInt);
                                confirmDialog.dismiss();
                            }
                        },
                        false);
			} else {

				// No explanation needed, we can request the permission.

				ActivityCompat.requestPermissions(activity,
						new String[]{permissionKind},
						callbackInt);

				// MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
				// app-defined int constant. The callback method gets the
				// result of the request.
			}
			return false;
		} else {
			return true;
		}
	}


	/* Checks if external storage is available for read and write */
	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	public interface SavecolorphoriaBitmapHandler {
		public void onSuccess(Uri uri);

		public void onFailure();
	}

	public static File savecolorphoriaBitmap(Bitmap bm, Activity activity, SavecolorphoriaBitmapHandler handler) {
		if (Utils.isExternalStorageWritable()) {
			//File albumStorageDir = getAlbumStorageDir("colorphoriaSaves");
			File pictureStorageDir = Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_PICTURES);
			File albumStorageDir = new File(pictureStorageDir, "colorphoria");
			try {
				albumStorageDir.mkdir();
			}
			catch (Exception e) {
				Utils.showCustomToast(activity, "Failed to create directory");
				Log.e("picsource", "Failed to create colorphoria directory.");
				e.printStackTrace();
			}
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyyhhmmss");
			String formattedDate = sdf.format(date);
			File picFile = new File(albumStorageDir, "colorphoria" + formattedDate + ".png");
			Log.e("picsource", "picfile: " + picFile.getAbsolutePath());
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(picFile);
				bm.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
				// PNG is a lossless format, the compression factor (100) is ignored
				Log.i("picsource", "file written to: " + picFile.getAbsolutePath());
				handler.onSuccess(Uri.fromFile(picFile)); // TODO: Error handling on this parse
			} catch (Exception e) {
				Log.e("picsource", "failed save.");
				e.printStackTrace();
				picFile = null;
				handler.onFailure();
			} finally {
				try {
					if (out != null) {
						out.close();
					}
				} catch (IOException e) {
					Log.e("picsource", "Failed to close file stream.");
					e.printStackTrace();
				}
			}
			return picFile;
		}
		else {
			Utils.showCustomToast(activity, "Save failed. External storage not writeable.");
			Log.e("picsource", "failed save. external storage not writeable");
			return null;
		}
	}

	// Interface for confirm dialog button handlers
	public interface ConfirmDialogHandler {
        void onPositiveButton(Dialog confirmDialog);
        void onNegativeButton(Dialog confirmDialog);
    }

    // Shows a confirm dialog
    // TODO: Put this alert in a fragment so orientation change can be handled
    // TODO: See https://developer.android.com/guide/topics/ui/dialogs.html
	public static void showConfirmDialog(Context context, String confirmText, String positiveText, String negativeText, final ConfirmDialogHandler handler, boolean widthShort) {
        final Dialog confirmDialog = new Dialog(context);
        confirmDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		confirmDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		confirmDialog.setContentView(R.layout.confirm);

		confirmDialog.setCanceledOnTouchOutside(false);

		// Setup size
        LinearLayout confirmLayout = (LinearLayout) confirmDialog.findViewById(R.id.confirm_dialog_layout);
        ViewGroup.LayoutParams params = confirmLayout.getLayoutParams();
        if (widthShort) {
            params.width = context.getResources().getDimensionPixelSize(R.dimen.dialog_width_short);
        } else {
            params.width = context.getResources().getDimensionPixelSize(R.dimen.dialog_width_long);
        }
        confirmLayout.setLayoutParams(params);

        // Setup confirm text
        TextView confirmTextView = (TextView) confirmDialog.findViewById(R.id.confirm_text);
        confirmTextView.setText(confirmText);

        // Setup negative button
        Button negativeButton = (Button) confirmDialog.findViewById(R.id.no_btn);
        negativeButton.setText(negativeText);
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.onNegativeButton(confirmDialog);
            }
        });

        // Setup positive button
        Button positiveButton = (Button) confirmDialog.findViewById(R.id.yes_btn);
        positiveButton.setText(positiveText);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.onPositiveButton(confirmDialog);
            }
        });

        confirmDialog.show();
    }


	// Interface for confirm dialog button handlers
	public interface AlertDialogHandler {
		void onPositiveButton(Dialog confirmDialog);
	}

	// Shows an alert dialog
	// TODO: Put this alert in a fragment so orientation change can be handled
	// TODO: See https://developer.android.com/guide/topics/ui/dialogs.html
	public static void showAlertDialog(Context context, String confirmText, String positiveText, final AlertDialogHandler handler, boolean widthShort) {
		final Dialog confirmDialog = new Dialog(context);
		confirmDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		confirmDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		confirmDialog.setContentView(R.layout.confirm);

		confirmDialog.setCanceledOnTouchOutside(false);

		// Setup size
		LinearLayout confirmLayout = (LinearLayout) confirmDialog.findViewById(R.id.confirm_dialog_layout);
		ViewGroup.LayoutParams params = confirmLayout.getLayoutParams();
		if (widthShort) {
			params.width = context.getResources().getDimensionPixelSize(R.dimen.dialog_width_short);
		} else {
			params.width = context.getResources().getDimensionPixelSize(R.dimen.dialog_width_long);
		}
		confirmLayout.setLayoutParams(params);

		// Setup confirm text
		TextView confirmTextView = (TextView) confirmDialog.findViewById(R.id.confirm_text);
		confirmTextView.setText(confirmText);

		// Remove negative button
		Button negativeButton = (Button) confirmDialog.findViewById(R.id.no_btn);
		negativeButton.setVisibility(View.GONE);

		// Setup positive button
		Button positiveButton = (Button) confirmDialog.findViewById(R.id.yes_btn);
		positiveButton.setText(positiveText);
		positiveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				handler.onPositiveButton(confirmDialog);
			}
		});

		confirmDialog.show();
	}

	public static void showCustomToast(Activity activity, String text) {
		LayoutInflater inflater = activity.getLayoutInflater();
		View layout = inflater.inflate(R.layout.toast,
				(ViewGroup) activity.findViewById(R.id.toast_layout));

		TextView textView = (TextView) layout.findViewById(R.id.toast_text);
		textView.setText(text);

		Toast toast = new Toast(activity.getApplicationContext());
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();
	}

	/**
	 * Blocks user interaction and shows a relative layout declared in the layout file
	 * in use for the passed in activity. The relative layout id must be progress_bar_layout
	 * otherwise this code will crash.
	 * @param activity the activity on which to display the relative layout and block user interaction
	 */
	public static void showBlockingProgressBar(Activity activity) {
		// Disable user interaction
		activity.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
				WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

		// Show ProgressBar
		RelativeLayout progressBarLayout = (RelativeLayout) activity.findViewById(R.id.progress_bar_layout);
		progressBarLayout.setVisibility(View.VISIBLE);
	}

	/**
	 * Enables user interaction and removes a relative layout declared in the layout file
	 * in use for the passed in activity. The relative layout id must be progress_bar_layout
	 * otherwise this code will crash.
	 * @param activity the activity on which to remove the relative layout and enable user interaction
	 */
	public static void removeBlockingProgressBar(Activity activity) {
		// Hide ProgressBar
		RelativeLayout progressBarLayout = (RelativeLayout) activity.findViewById(R.id.progress_bar_layout);
		progressBarLayout.setVisibility(View.GONE);

		// Enable user interaction
		activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
	}
}
