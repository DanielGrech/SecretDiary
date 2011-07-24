package com.DGSD.SecretDiary;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
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

	public static String join(List<String> list, String conjunction){
		if(list == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String item : list) {
			if (first)
				first = false;
			else
				sb.append(conjunction);
			sb.append(item);
		}
		return sb.toString();
	}

	public static List<String> unjoin(String string, String joiner) {
		if(string == null || string.length() == 0) {
			return null;
		}

		System.err.println("UNJOINING STRING: " + string);
		return Arrays.asList(string.split(joiner));
	}

	public static Bitmap decodeFile(File f){
		try {
			//Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f),null,o);

			//The new size we want to scale to
			final int REQUIRED_SIZE=70;

			//Find the correct scale value. It should be the power of 2.
			int width_tmp=o.outWidth, height_tmp=o.outHeight;
			int scale=1;
			while(true){
				if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
					break;
				width_tmp/=2;
				height_tmp/=2;
				scale*=2;
			}

			//Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize=scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {}
		return null;
	}
	
	public static Location getLocation(Context c, int minDistance, long minTime) {
		Location bestResult = null;
		float bestAccuracy = Float.MAX_VALUE;
		long bestTime = Long.MIN_VALUE;

		LocationManager lm = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
		
		List<String> matchingProviders = lm.getAllProviders();
		for (String provider: matchingProviders) {
			Location location = lm.getLastKnownLocation(provider);
			if (location != null) {
				float accuracy = location.getAccuracy();
				long time = location.getTime();

				if ((time > minTime && accuracy < bestAccuracy)) {
					bestResult = location;
					bestAccuracy = accuracy;
					bestTime = time;
				}
				else if (time < minTime && bestAccuracy == Float.MAX_VALUE && time > bestTime) {
					bestResult = location;
					bestTime = time;
				}
			}
		}

		return bestResult;
	}


	public static class Password {
		public static final int WEAK = 0;
		public static final int OK = 1;
		public static final int STRONG = 2;
	}

}
