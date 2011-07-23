package com.DGSD.SecretDiary;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

/**
 * Class which simply decides if we have logged in before or not.
 * If yes, show the login screen. If not, show the setup screen
 */
public class MainActivity extends Activity{

	private SharedPreferences mPrefs;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mPrefs =  getSharedPreferences(DiaryApplication.KEY_MY_PREFERENCES, 0);
		
		Intent intent;
		
		if( hasLoggedInBefore() ) {
			intent = new Intent(this, LoginActivity.class);
		} else {
			intent = new Intent(this, SetupActivity.class);
		}
		
		//Confirm that we are starting this activity internally from the app
		intent.putExtra(LoginActivity.EXTRA_INTERNAL, true);
		
		startActivity(intent);
		finish();
	}
	
	private boolean hasLoggedInBefore() {
		return mPrefs.getBoolean(DiaryApplication.KEY_HAS_LOGGED_IN_BEFORE, false);
	}
}
