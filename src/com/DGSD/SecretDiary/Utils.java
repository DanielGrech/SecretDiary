package com.DGSD.SecretDiary;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

public class Utils {

	public static int getPasswordRating(String password) {
		if (password == null || password.length() < 5) {
			return Password.WEAK;
		}

		int passwordStrength = 0;    

		// minimal pw length of 6
		if (password.length() > 5) {
			passwordStrength++;
		}

		// lower and upper case
		if (password.toLowerCase()!= password) {
			passwordStrength += 2;
		} 

		// good pw length of 8+
		if (password.length() > 7) {
			passwordStrength += 2;
		} 

		int numDigits = getNumberDigits(password);

		// contains digits and non-digits
		if (numDigits > 0 && numDigits != password.length()) {
			passwordStrength += 3;
		} 

		if(passwordStrength >= 6 ) {
			return Password.STRONG;
		} else if(passwordStrength >= 4) {
			return Password.OK;
		} else {
			return Password.WEAK;
		}
	}

	public static int getNumberDigits(String inString){
		if (isEmpty(inString)) {
			return 0;
		}

		int numDigits= 0;

		for (int i = 0, size = inString.length(); i < size; i++) {
			if (Character.isDigit(inString.charAt(i))) {
				numDigits++; 
			}
		}
		return numDigits; 
	}

	public static boolean isEmpty(String inString) {
		return inString == null || inString.length() == 0;
	}

	public static File getTempFile(Context context){
		//it will return /sdcard/SecretDiary/image.tmp
		final File path = new File( Environment.getExternalStorageDirectory() + "/.SecretDiary/");

		if(!path.exists() && !path.mkdirs()) {
			return null;
		}

		File retval = new File(path, "image.tmp");
		if(!retval.exists()) {
			try {
				System.err.println("ATTEMPTING TO CREATE: " + retval.getAbsolutePath());
				retval.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("TEMP FILE DEFINITLY EXISTS!");
		}
		return retval;
	}

	public static String getPath(Activity activity, Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	public static class Password {
		public static final int WEAK = 0;
		public static final int OK = 1;
		public static final int STRONG = 2;
	}

}
