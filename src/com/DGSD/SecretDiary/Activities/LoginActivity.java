package com.DGSD.SecretDiary.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.DGSD.SecretDiary.DiaryApplication;
import com.DGSD.SecretDiary.Encryption;
import com.DGSD.SecretDiary.R;

public class LoginActivity extends Activity {

	public static final String EXTRA_INTERNAL = "internal_flag";	

	private EditText mPasswordField;

	private ImageButton mUnlockImage;

	private ImageButton mHintButton;

	private SharedPreferences mPrefs;

	private DiaryApplication mApplication;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login);

		mApplication = (DiaryApplication) getApplication();

		mPrefs = getSharedPreferences(DiaryApplication.KEY_MY_PREFERENCES, 0);

		mPasswordField = (EditText) findViewById(R.id.password);

		mUnlockImage = (ImageButton) findViewById(R.id.unlock);

		mHintButton = (ImageButton) findViewById(R.id.hint_button);

		mApplication.setPasswordHint(mPrefs.getString(DiaryApplication.KEY_PASSWORD_HINT, 
				null));

		mPasswordField.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (event != null && event.getAction() != KeyEvent.ACTION_DOWN) {
					return false;
				} else{
					hideKeyboard(mPasswordField);
					mUnlockImage.performClick();
					return true;
				}
			}
		});

		mUnlockImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String password = mPasswordField.getText().toString();

				try {
					unlock(password);

					mApplication.setPassword(password);

					if(getIntent().getBooleanExtra(EXTRA_INTERNAL, false)) {
						startActivity(new Intent(LoginActivity.this, EntryListActivity.class));
						finish();
					} else {
						//Forward on any details to the 'create entry' activity
						Intent intent = new Intent(LoginActivity.this, EntryActivity.class);
						intent.putExtra(Intent.EXTRA_SUBJECT, 
								getIntent().getStringExtra(Intent.EXTRA_SUBJECT));

						intent.putExtra(Intent.EXTRA_TEXT, 
								getIntent().getStringExtra(Intent.EXTRA_TEXT));

						startActivity(intent);
						finish();
					}
				} catch (Exception e) {
					showDialog("Error", "Incorrect password");
					mPasswordField.setText("");
				}
			}
		});

		if(mApplication.getPasswordHint() != null) {
			mHintButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showDialog("Password Hint", mApplication.getPasswordHint());
				}
			});
		} else {
			mHintButton.setVisibility(View.GONE);
		}
	}

	private void showDialog(String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(title);
		builder.setMessage(message);

		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});

		builder.create().show();
	}

	private String unlock(String password) throws Exception{
		return Encryption.decrypt(password, 
				mPrefs.getString(DiaryApplication.KEY_ENCRYPTION_TEST, null));
	}

	private void hideKeyboard(View v) {
		InputMethodManager imm = 
				(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}
}