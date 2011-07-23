package com.DGSD.SecretDiary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.DGSD.SecretDiary.ActionBar.ActionBar;
import com.DGSD.SecretDiary.ActionBar.ActionBar.AbstractAction;

public class SetupActivity extends Activity{

	private static final String KEY_LAST_ALERT_TITLE = "alert_title";

	private static final String KEY_LAST_ALERT_MESSAGE = "alert_message";

	private EditText mPasswordField;

	private EditText mPasswordConfirmField;
	
	private EditText mPasswordHint;

	private SharedPreferences mPrefs;

	private DiaryApplication mApplication;

	private TextView mPwStrength;

	private ActionBar mActionBar;

	private AlertDialog currentDialog;

	private String mLastAlertTitle;

	private String mLastAlertMessage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.setup);

		mApplication = (DiaryApplication) getApplication();

		mPrefs = getSharedPreferences(DiaryApplication.KEY_MY_PREFERENCES, 0);

		mActionBar = (ActionBar) findViewById(R.id.actionbar);

		mPasswordField = (EditText) findViewById(R.id.password);

		mPasswordConfirmField = (EditText) findViewById(R.id.password_confirm);
		
		mPasswordHint = (EditText) findViewById(R.id.password_hint);

		mPwStrength = (TextView) findViewById(R.id.password_strength);

		mPasswordField.addTextChangedListener(new MyTextWatcher(mPwStrength));
		
		setupActionBar();

		restoreDialog(savedInstanceState);
	}

	@Override
	public void onStop() {
		if(currentDialog != null) {
			currentDialog.dismiss();
			currentDialog = null;
		}

		super.onStop();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(currentDialog != null && currentDialog.isShowing()){
			outState.putString(KEY_LAST_ALERT_TITLE, mLastAlertTitle);
			outState.putString(KEY_LAST_ALERT_MESSAGE, mLastAlertMessage);
		}
	}

	private void restoreDialog(Bundle bundle) {
		if(bundle != null) {
			mLastAlertTitle = bundle.getString(KEY_LAST_ALERT_TITLE);

			mLastAlertMessage = bundle.getString(KEY_LAST_ALERT_MESSAGE);

			if(mLastAlertTitle != null && mLastAlertMessage != null) {
				showDialog(mLastAlertTitle, mLastAlertMessage);
			}
		}
	}
	
	private void setupActionBar() {
		mActionBar.setTitle("Setup new password");

		mActionBar.addAction(new AbstractAction(android.R.drawable.ic_menu_set_as){
			@Override
			public void performAction(View view) {
				final String password = mPasswordField.getText().toString();

				final String password_confirm = mPasswordConfirmField.getText().toString();
				
				final String password_hint = mPasswordHint.getText().toString();

				if(password == null || password.length() == 0 || 
						password_confirm == null || password_confirm.length() == 0) {
					showDialog("Error", 
							"Please fill out both password fields");

					return;
				}

				if(!password.equals(password_confirm)) {
					showDialog("Error", 
							"Passwords do not match. Please confirm your password");

					return;
				}

				mPrefs.edit()
				.putBoolean(DiaryApplication.KEY_HAS_LOGGED_IN_BEFORE, true)
				.commit();

				try{
					mApplication.setPassword(password);

					if(password_hint != null && password_hint.length() > 0) {
						mApplication.setPasswordHint(password_hint);
						
						mPrefs.edit()
						.putString(DiaryApplication.KEY_PASSWORD_HINT, password_hint)
						.commit();
					}
					
					mPrefs.edit()
					.putString(DiaryApplication.KEY_ENCRYPTION_TEST, 
							getEncryptionKey(password))
							.commit();

					startActivity(new Intent(SetupActivity.this, EntryListActivity.class));
					finish();
				} catch(Exception e) {
					Toast.makeText(SetupActivity.this, 
							"Something went terribly wrong! Start panicing now!", 
							Toast.LENGTH_LONG).show();

					e.printStackTrace();
				}
			}
		});
	}

	private String getEncryptionKey(String password) throws Exception {
		return Encryption.encrypt(password, 
				getResources().getString(R.string.password_passage));
	}

	private void showDialog(String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(title);
		builder.setMessage(message);

		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});

		currentDialog = builder.create();

		mLastAlertTitle = title;
		
		mLastAlertMessage = message;
		
		currentDialog.show();
	}

	private class MyTextWatcher implements TextWatcher {
		TextView mView;

		public MyTextWatcher(TextView tv) {
			mView = tv;
		}

		@Override
		public void afterTextChanged(Editable s) {
			if(s == null || s.length() == 0) {
				mView.setVisibility(View.INVISIBLE);
			} else {
				mView.setVisibility(View.VISIBLE);

				int textColor = Color.BLUE;
				String text = null;
				switch(Utils.getPasswordRating(s.toString())) {
					case Utils.Password.WEAK:
						textColor = Color.RED;
						text = "Password Strength: Weak";
						break;

					case Utils.Password.OK:
						textColor = Color.BLUE;
						text = "Password Strength: Ok";
						break;

					case Utils.Password.STRONG:
						textColor = Color.GREEN;
						text = "Password Strength: Strong";
						break;
				}

				mView.setText(text);
				mView.setTextColor(textColor);

			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}
	}
}
