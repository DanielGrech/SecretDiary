/*
 * TODO:
 * 		Add location
 * 		View value in list
 * 		Add photo/video
 * 		Backup manager
 * 		Search
 * 		Tags for each note
 *		Password hint on login screen! 	
 * 		Change password (Unencrypt + reencrypt all!)
 */

package com.DGSD.SecretDiary.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.DGSD.SecretDiary.DiaryApplication;
import com.DGSD.SecretDiary.R;
import com.DGSD.SecretDiary.ActionBar.ActionBar;
import com.DGSD.SecretDiary.ActionBar.ActionBar.AbstractAction;

public class EntryActivity extends Activity{

	public static final String EXTRA_ID = "extra_id";
	
	private DiaryApplication mApplication;

	private ActionBar mActionBar;

	private TextView mKeyView;

	private TextView mValueView;

	private String mExistingId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.create_entry);

		mExistingId = null;

		mApplication = (DiaryApplication) getApplication();

		mActionBar = (ActionBar) findViewById(R.id.actionbar);

		mKeyView = (TextView) findViewById(R.id.key);

		mValueView = (TextView) findViewById(R.id.value);

		extractDataFromIntent(getIntent().getExtras());

		if(savedInstanceState != null) {
			mKeyView.setText(savedInstanceState.getString("key"));
			mKeyView.setText(savedInstanceState.getString("value"));
		}

		setupActionBar();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString("key", mKeyView.getText().toString());

		outState.putString("value", mValueView.getText().toString());
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		
		
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	    	String key = mKeyView.getText().toString();
			String value = mValueView.getText().toString();

			if((key == null || key.length() == 0) && 
					(value == null || value.length() == 0)) {
				//There is no data, so let them exit
				return super.onKeyDown(keyCode, event);
			}
			
			if(key.equals(getIntent().getStringExtra(Intent.EXTRA_SUBJECT)) &&
					value.equals(getIntent().getStringExtra(Intent.EXTRA_TEXT)) ){
				//There is data, but it hasn't been edited!
				return super.onKeyDown(keyCode, event);
			}
	    	
	    	AlertDialog.Builder builder = 
					new AlertDialog.Builder(EntryActivity.this);

			builder.setTitle("Are you sure?");
			builder.setMessage("Any data entered will be lost");

			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					finish();
				}
			});

			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});

			builder.create().show();
	        return true;
	    }

	    return super.onKeyDown(keyCode, event);
	}

	public void extractDataFromIntent(Bundle bundle) {
		if(bundle != null) {
			String key = bundle.getString(Intent.EXTRA_SUBJECT);
			String val = bundle.getString(Intent.EXTRA_TEXT);
			String id = bundle.getString(EXTRA_ID);

			if(key != null) {
				mKeyView.setText(key);
			}

			if(val != null) {
				mValueView.setText(val);
			}

			if(id != null) {
				mExistingId = id;
			}
		}

	}

	private void setupActionBar() {

		mActionBar.addAction(new AbstractAction(android.R.drawable.ic_menu_share) {
			@Override
			public void performAction(View view) {
				String key = mKeyView.getText().toString();
				String value = mValueView.getText().toString();

				if((key == null || key.length() == 0) && 
						(value == null || value.length() == 0)) {
					Toast.makeText(EntryActivity.this, "Nothing to share!", 
							Toast.LENGTH_SHORT).show();
					return;
				}
				
				Intent sharingIntent = new Intent(Intent.ACTION_SEND);
				sharingIntent.setType("text/plain");
				sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, value);
				sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, key);
				startActivity(Intent.createChooser(sharingIntent, "Share entry"));
			}
		});
		
		mActionBar.addAction(new AbstractAction(android.R.drawable.ic_menu_save) {
			@Override
			public void performAction(View view) {
				String key = mKeyView.getText().toString();
				String value = mValueView.getText().toString();

				if((key == null || key.length() == 0) && 
						(value == null || value.length() == 0)) {
					Toast.makeText(EntryActivity.this, "Nothing to save!", 
							Toast.LENGTH_SHORT).show();

					return;
				}

				if(mExistingId != null) {
					//We are updating an old entry!
					if(mApplication.updateEntry(Integer.valueOf(mExistingId), key, value) > 0) {
						Toast.makeText(EntryActivity.this, "Entry Updated!", 
								Toast.LENGTH_SHORT).show();

						finish();
					} else {
						Toast.makeText(EntryActivity.this, "Something went wrong :(", 
								Toast.LENGTH_SHORT).show();
					}

				} else {
					//We are adding a brand new entry!
					if(mApplication.addEntry(key, value) == true) {
						Toast.makeText(EntryActivity.this, "New entry created!", 
								Toast.LENGTH_SHORT).show();

						finish();
					} else {
						Toast.makeText(EntryActivity.this, "Something went wrong :(", 
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
	}
}
