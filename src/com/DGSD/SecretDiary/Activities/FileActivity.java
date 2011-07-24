package com.DGSD.SecretDiary.Activities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.DGSD.SecretDiary.R;

public class FileActivity extends ListActivity {

	private static final String ITEM_KEY = "key";
	private static final String ITEM_IMAGE = "image";

	public static final String START_PATH = "START_PATH";
	public static final String RESULT_PATH = "RESULT_PATH";

	private static final String DELETE_FILE = "Delete File";

	private List<String> item = null;
	private List<String> path = null;
	private String root = "/";
	private TextView myPath;
	private ArrayList<HashMap<String, Object>> mList;

	private String parentPath;
	private String startPath;
	private String currentPath = root;
	private String mostRecentDir;

	private SimpleAdapter mListAdapter;

	private File selectedFile;
	private HashMap<String, Integer> lastPositions = new HashMap<String, Integer>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setResult(RESULT_CANCELED, getIntent());

		setContentView(R.layout.file_dialog_main);
		myPath = (TextView) findViewById(R.id.path);
		
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

		if(savedInstanceState != null) {
			startPath = savedInstanceState.getString("startPath");
			System.err.println("RESTORING START PATH TO: " + startPath);
		}

		if(startPath == null) {
			startPath = getIntent().getStringExtra(START_PATH);
		}

		if (startPath != null) {
			getDir(startPath);
			mostRecentDir = startPath;
		} else {
			getDir(root);
			mostRecentDir = root;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString("startPath", mostRecentDir);
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

			if (selectedFile != null) {
				getIntent().putExtra(RESULT_PATH, selectedFile.getPath());
				setResult(RESULT_OK, getIntent());
				finish();
			}
			
		}
	}
	
	

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if (!currentPath.equals(startPath)) {
				getDir(parentPath);
			} else {
				return super.onKeyDown(keyCode, event);
			}
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
}