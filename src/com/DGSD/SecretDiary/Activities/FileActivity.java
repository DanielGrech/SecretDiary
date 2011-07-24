package com.DGSD.SecretDiary.Activities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import com.DGSD.SecretDiary.R;
import com.DGSD.SecretDiary.R.drawable;
import com.DGSD.SecretDiary.R.id;
import com.DGSD.SecretDiary.R.layout;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class FileActivity extends ListActivity {

	private static final String ITEM_KEY = "key";
	private static final String ITEM_IMAGE = "image";

	public static final String START_PATH = "START_PATH";
	public static final String RESULT_PATH = "RESULT_PATH";

	private static final String NEW_DIRECTORY = "New Directory";
	private static final String NEW_FILE = "New File"; 	
	private static final String DELETE_FILE = "Delete File";

	private List<String> item = null;
	private List<String> path = null;
	private String root = "/";
	private TextView myPath;
	private TextView mEditTextLabel;
	private EditText mFileName;
	private ArrayList<HashMap<String, Object>> mList;

	private Button selectButton;
	private Button newButton;
	private Button cancelButton;
	private Button createButton;

	private LinearLayout layoutSelect;
	private LinearLayout layoutCreate;
	private InputMethodManager inputManager;
	private String parentPath;
	private String startPath;
	private String currentPath = root;
	private String mostRecentDir;
	private String mLastSelectedNewItem;
	private String mLastNewText;

	private boolean isNewShowing;
	
	private SimpleAdapter mListAdapter;

	private AlertDialog mNewAlert;

	private File selectedFile;
	private HashMap<String, Integer> lastPositions = new HashMap<String, Integer>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setResult(RESULT_CANCELED, getIntent());

		setContentView(R.layout.file_dialog_main);
		myPath = (TextView) findViewById(R.id.path);
		mFileName = (EditText) findViewById(R.id.fdEditTextFile);
		mEditTextLabel = (TextView) findViewById(R.id.textViewFilename);
		
		layoutSelect = (LinearLayout) findViewById(R.id.fdLinearLayoutSelect);
		layoutCreate = (LinearLayout) findViewById(R.id.fdLinearLayoutCreate);
		

		if(savedInstanceState != null){
			mLastNewText = savedInstanceState.getString("last_new_text");
			mLastNewText = (mLastNewText == null) ? "" : mLastNewText;
			
			isNewShowing = savedInstanceState.getBoolean("is_new_showing", false);
		}

		if(isNewShowing){
			layoutSelect.setVisibility(View.GONE);
			layoutCreate.setVisibility(View.VISIBLE);
		}
		else{
			layoutCreate.setVisibility(View.GONE);
			layoutSelect.setVisibility(View.VISIBLE);
		}
			
		mFileName.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(createButton != null)
					createButton.performClick();
				return false;
			}
		});
		
		inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

		selectButton = (Button) findViewById(R.id.fdButtonSelect);
		selectButton.setEnabled(false);
		selectButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (selectedFile != null) {
					getIntent().putExtra(RESULT_PATH, selectedFile.getPath());
					setResult(RESULT_OK, getIntent());
					finish();
				}
			}
		});

		//New dialog..
		final String[] items = {NEW_DIRECTORY, NEW_FILE};
		AlertDialog.Builder builder = new AlertDialog.Builder(FileActivity.this);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int pos) {
				mLastSelectedNewItem = items[pos];
				mEditTextLabel.setText(mLastSelectedNewItem + ":");

				layoutSelect.setVisibility(View.GONE);
				layoutCreate.setVisibility(View.VISIBLE);

				mFileName.setText(mLastNewText);
				mFileName.requestFocus();
				
				isNewShowing = true;
			}
		});

		mNewAlert = builder.create();

		newButton = (Button) findViewById(R.id.fdButtonNew);
		newButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mNewAlert.show();
			}
		});

		cancelButton = (Button) findViewById(R.id.fdButtonCancel);
		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				layoutCreate.setVisibility(View.GONE);
				layoutSelect.setVisibility(View.VISIBLE);

				inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
				unselect();
			}

		});
		createButton = (Button) findViewById(R.id.fdButtonCreate);
		createButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(mLastSelectedNewItem.equals(NEW_FILE)){
					if (mFileName.getText().length() > 0) {
						getIntent().putExtra(RESULT_PATH,
								currentPath + "/" + mFileName.getText());
						setResult(RESULT_OK, getIntent());
						finish();
					}
					else{
						Toast.makeText(FileActivity.this, "No new file name entered..", 
								Toast.LENGTH_LONG).show();
					}
				}
				else{
					if (mFileName.getText().length() > 0) {
						File dir = new File(currentPath, mFileName.getText().toString());
						if(!dir.exists()){
							if(!dir.mkdirs()){
								//For some reason, we couldn't create the dir..
								Toast.makeText(FileActivity.this, "Error creating new directory", 
										Toast.LENGTH_LONG).show();
							}
						}
						else{
							Toast.makeText(FileActivity.this, mFileName.getText() + " already exists..", 
									Toast.LENGTH_LONG).show();
						}

						/*
						 * NOTE, to change to newly created dir:
						 * getDir(dir.getAbsolutePath())
						 */
						getDir(currentPath);
					}
					else{
						Toast.makeText(FileActivity.this, "No new directory name entered..", 
								Toast.LENGTH_LONG).show();
					}
					
					//Hide the naming dialog again..
					layoutSelect.setVisibility(View.VISIBLE);
					layoutCreate.setVisibility(View.GONE);
					
					isNewShowing = false;
				}
			}
		});
		
		getListView().setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> av, View v, final int listpos, long id) {
				final String[] items = {DELETE_FILE};
				AlertDialog.Builder builder = new AlertDialog.Builder(FileActivity.this);
				builder.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int pos) {
						if(items[pos] == DELETE_FILE){
							File file = new File(path.get(listpos));

							if(file != null && !file.delete())
								Toast.makeText(FileActivity.this, "Cannot delete file", Toast.LENGTH_LONG).show();
							else
								Toast.makeText(FileActivity.this, path.get(listpos) + " deleted", Toast.LENGTH_LONG).show();
							
							getDir(currentPath);
						}
					}
				});

				builder.create().show();
				
				return false;
			}
			
		});

		startPath = null;

		if(savedInstanceState != null)
			startPath = savedInstanceState.getString("startPath");

		if(startPath == null)
			startPath = getIntent().getStringExtra(START_PATH);

		if (startPath != null) {
			getDir(startPath);
		} else {
			getDir(root);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString("startPath", mostRecentDir);
		savedInstanceState.putString("last_new_text",mLastNewText);
		savedInstanceState.putBoolean("is_new_showing",isNewShowing);
		super.onSaveInstanceState(savedInstanceState);
	}

	private void getDir(String dirPath) {

		boolean useAutoSelection = dirPath.length() < currentPath.length();

		Integer position = lastPositions.get(parentPath);

		getDirImpl(dirPath);

		if (position != null && useAutoSelection) {
			getListView().setSelection(position);
		}

	}

	private void getDirImpl(String dirPath) {

		myPath.setText("Location: " + dirPath);
		currentPath = dirPath;

		item = new ArrayList<String>();
		path = new ArrayList<String>();
		mList = new ArrayList<HashMap<String, Object>>();

		File f = new File(dirPath);
		File[] files = f.listFiles();

		if (!dirPath.equals(root)) {

			item.add(root);
			addItem(root, R.drawable.folder);
			path.add(root);

			item.add("../");
			addItem("../", R.drawable.folder);
			path.add(f.getParent());
			parentPath = f.getParent();

		}

		TreeMap<String, String> dirsMap = new TreeMap<String, String>();
		TreeMap<String, String> dirsPathMap = new TreeMap<String, String>();
		TreeMap<String, String> filesMap = new TreeMap<String, String>();
		TreeMap<String, String> filesPathMap = new TreeMap<String, String>();
		for (File file : files) {
			if (file.isDirectory()) {
				String dirName = file.getName();
				dirsMap.put(dirName, dirName);
				dirsPathMap.put(dirName, file.getPath());
			} else {
				filesMap.put(file.getName(), file.getName());
				filesPathMap.put(file.getName(), file.getPath());
			}
		}
		item.addAll(dirsMap.tailMap("").values());
		item.addAll(filesMap.tailMap("").values());
		path.addAll(dirsPathMap.tailMap("").values());
		path.addAll(filesPathMap.tailMap("").values());

		mListAdapter = new SimpleAdapter(this, mList,
				R.layout.file_dialog_row,
				new String[] { ITEM_KEY, ITEM_IMAGE }, new int[] {
				R.id.fdrowtext, R.id.fdrowimage });

		for (String dir : dirsMap.tailMap("").values()) {
			addItem(dir, R.drawable.folder);
		}

		for (String file : filesMap.tailMap("").values()) {
			addItem(file, R.drawable.file);
		}

		mListAdapter.notifyDataSetChanged();

		setListAdapter(mListAdapter);

	}

	private void addItem(String fileName, int imageId) {
		HashMap<String, Object> item = new HashMap<String, Object>();
		item.put(ITEM_KEY, fileName);
		item.put(ITEM_IMAGE, imageId);
		mList.add(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		File file = new File(path.get(position));
		
		if (file.isDirectory()) {
			unselect();
			if (file.canRead()) {
				mostRecentDir = file.getAbsolutePath();
				lastPositions.put(currentPath, position);
				getDir(path.get(position));
			} else {
				Toast.makeText(FileActivity.this, "[" + file.getName() + "] "
						+ "Folder cant be read", Toast.LENGTH_LONG).show();
	
			}
		} else {
			selectedFile = file;
			v.setSelected(true);
			selectButton.setText("Select '" + selectedFile.getName() + "'");
			selectButton.setEnabled(true);
		}
	}
	
	

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			unselect();

			if (layoutCreate.getVisibility() == View.VISIBLE) {
				layoutCreate.setVisibility(View.GONE);
				layoutSelect.setVisibility(View.VISIBLE);
			} else {
				if (!currentPath.equals(startPath)) {
					getDir(parentPath);
				} else {
					return super.onKeyDown(keyCode, event);
				}
			}

			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	private void unselect() {
		selectButton.setEnabled(false);
	}
}