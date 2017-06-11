package com.caitoo0o.filler.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.caitoo0o.filler.R;

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

	/* Checks if external storage is available for read and write */
	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	public static File saveFillerBitmap(Bitmap bm, Activity activity) {
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
				Toast.makeText(activity.getApplicationContext(), "Image saved.", Toast.LENGTH_LONG).show();
				Log.i("picsource", "file written to: " + picFile.getAbsolutePath());
			} catch (Exception e) {
				Toast.makeText(activity.getApplicationContext(), "Save failed.", Toast.LENGTH_LONG).show();
				Log.e("picsource", "failed save.");
				e.printStackTrace();
				picFile = null;
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
			// TODO: move these strings to strings file
			Toast.makeText(activity.getApplicationContext(), "Save failed. External storage not writeable.", Toast.LENGTH_LONG).show();
			Log.e("picsource", "failed save. external storage not writeable");
			return null;
		}
	}
}
