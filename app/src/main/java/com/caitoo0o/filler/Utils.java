package com.caitoo0o.filler;

import android.os.Environment;

/**
 * Created by organizer on 3/9/17.
 */

public class Utils {

	/* Checks if external storage is available for read and write */
	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}
}
