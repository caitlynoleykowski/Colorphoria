package com.caitoo0o.filler.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.caitoo0o.filler.R;
import com.caitoo0o.filler.activities.loggedoutactivites.LoginActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by organizer on 3/9/17.
 */

public class Utils {

	public static void setupFillerRecentAppsBar(Activity activity) {
		// Setup recent apps bar with app color
		Bitmap bm = BitmapFactory.decodeResource(activity.getResources(), R.mipmap.ic_launcher);
		ActivityManager.TaskDescription taskDesc = new ActivityManager.TaskDescription(
				activity.getString(R.string.app_name), bm, activity.getColor(R.color.colorPrimaryDark));
		activity.setTaskDescription(taskDesc);
	}

	public static void getPermission(Activity activity, String permissionKind) {
		final int REQUEST = 1;
		String[] PERMISSIONS_STORAGE = {
				permissionKind,
		};
		int permission = ActivityCompat.checkSelfPermission(activity, permissionKind);
		if (permission != PackageManager.PERMISSION_GRANTED) {
			// We don't have permission so prompt the user
			ActivityCompat.requestPermissions(
					activity,
					PERMISSIONS_STORAGE,
					REQUEST
			);
		}
	}

	public interface OnExplanationDeclinedHandler {
		public void onExplanationDeclined();
	}

	public static boolean getPermission(final Activity activity, final String permissionKind,
									 	final String permissionExplanation, final int callbackInt,
										final OnExplanationDeclinedHandler onExplanationDeclinedHandler) {
		// Here, thisActivity is the current activity
		if (ContextCompat.checkSelfPermission(activity, permissionKind) != PackageManager.PERMISSION_GRANTED) {

			// Should we show an explanation?
			if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
					permissionKind)) {

				// Show an explanation to the user *asynchronously* -- don't block
				// this thread waiting for the user's response! After the user
				// sees the explanation, try again to request the permission.

				// TODO: Put this alert in a fragment so orientation change can be handled
				// TODO: See https://developer.android.com/guide/topics/ui/dialogs.html
				new AlertDialog.Builder(activity)
						.setMessage(permissionExplanation)
						.setPositiveButton(activity.getString(R.string.yes),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										// Request the permission
										ActivityCompat.requestPermissions(activity,
												new String[]{permissionKind},
												callbackInt);
									}
								})
						.setNegativeButton(activity.getString(R.string.no), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								onExplanationDeclinedHandler.onExplanationDeclined();
							}
						}).show();

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

	public interface SaveFillerBitmapHandler {
		public void onSuccess();

		public void onFailure();
	}

	public static File saveFillerBitmap(Bitmap bm, Activity activity, SaveFillerBitmapHandler handler) {
		if (Utils.isExternalStorageWritable()) {
			//File albumStorageDir = getAlbumStorageDir("FillerSaves");
			File pictureStorageDir = Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_PICTURES);
			File albumStorageDir = new File(pictureStorageDir, "Filler");
			try {
				albumStorageDir.mkdir();
			}
			catch (Exception e) {
				Toast.makeText(activity.getApplicationContext(), "Failed to create directory", Toast.LENGTH_LONG).show();
				Log.e("picsource", "Failed to create Filler directory.");
				e.printStackTrace();
			}
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyyhhmmss");
			String formattedDate = sdf.format(date);
			File picFile = new File(albumStorageDir, "Filler" + formattedDate + ".png");
			Log.e("picsource", "picfile: " + picFile.getAbsolutePath());
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(picFile);
				bm.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
				// PNG is a lossless format, the compression factor (100) is ignored
				Log.i("picsource", "file written to: " + picFile.getAbsolutePath());
				handler.onSuccess();
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
			Toast.makeText(activity.getApplicationContext(), "Save failed. External storage not writeable.", Toast.LENGTH_LONG).show();
			Log.e("picsource", "failed save. external storage not writeable");
			return null;
		}
	}
}
