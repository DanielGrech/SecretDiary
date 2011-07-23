package com.DGSD.SecretDiary.Activities;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;

import com.DGSD.SecretDiary.R;

public class SearchActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.entrylist_item);

		// Get the intent, verify the action and get the query
		Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			System.err.println("SEARCHING FOR: " + query);
		}
	}
}
