package com.DGSD.SecretDiary.Activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.DGSD.SecretDiary.DiaryApplication;
import com.DGSD.SecretDiary.GalleryExt;
import com.DGSD.SecretDiary.LocationFinder;
import com.DGSD.SecretDiary.R;
import com.DGSD.SecretDiary.Utils;
import com.DGSD.SecretDiary.ActionBar.ActionBar;
import com.DGSD.SecretDiary.ActionBar.ActionBar.AbstractAction;
import com.DGSD.SecretDiary.QuickActions.ActionItem;
import com.DGSD.SecretDiary.QuickActions.QuickAction;

public class EntryActivity extends Activity{

	public static final int REQUEST_SAVE = 1337;

	public static final String EXTRA_ID = "extra_id";

	public static final String EXTRA_IMG_URI = "extra_img_uri";

	public static final String EXTRA_FILES = "extra_files";

	public static final String EXTRA_LAT = "extra_latitude";
	
	public static final String EXTRA_LONG = "extra_longitude";

	private static final int GET_CAMERA_IMAGE = 0;

	private static final int GET_GALLERY_IMAGE = 1;
	
	private static final int IMAGE_GALLERY = 0;
	
	private static final int FILE_GALLERY = 1;

	private DiaryApplication mApplication;

	private ActionBar mActionBar;

	private EditText mKeyView;

	private EditText mValueView;

	private String mExistingId;

	private TextView mImageTitle;

	private TextView mFileTitle;

	private GalleryExt mImageGallery;

	private GalleryExt mFileGallery;

	private List<String> mUris;

	private List<String> mFiles;

	private double[] mLocation;
	
	private GalleryAdapter mImageAdapter;

	private GalleryAdapter mFileAdapter;

	private boolean mChangedImage;

	private boolean mChangedFile;

	private boolean mChangedLocation;
	
	private QuickAction mQuickAction;

	private LocationFinder mLocationFinder;
	
	//Action to delete an image
	private final ActionItem mDeleteAction = new ActionItem();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.create_entry);

		mExistingId = null;

		mApplication = (DiaryApplication) getApplication();

		mActionBar = (ActionBar) findViewById(R.id.actionbar);

		mKeyView = (EditText) findViewById(R.id.key);

		mValueView = (EditText) findViewById(R.id.value);

		mImageTitle = (TextView) findViewById(R.id.image_heading);

		mFileTitle = (TextView) findViewById(R.id.file_heading);

		mImageGallery = (GalleryExt) findViewById(R.id.image_gallery);

		mFileGallery = (GalleryExt) findViewById(R.id.file_gallery);

		mUris = new LinkedList<String>();

		mFiles = new LinkedList<String>();
		
		mLocation = new double[]{-1.0, -1.0};
		
		mLocationFinder = new LocationFinder(this);
		
		mLocationFinder.setChangedLocationListener(new LocationListener(){
			@Override
			public void onLocationChanged(Location location) {
				System.err.println("Location changed to: " + location.getLatitude() + " " + location.getLongitude());
				
				if(mActionBar != null) {
					mActionBar.setProgressBarVisibility(View.GONE);
				}
				
				//We are updating the location!
				mChangedLocation = true;
				
				mLocation[0] = location.getLatitude();
				mLocation[1] = location.getLongitude();
				
				if(mActionBar.getActionCount() < 3) {
					//We need to show the action
					mActionBar.addAction(getLocationAction(), 0);
				}
			}

			@Override
			public void onProviderDisabled(String provider) {
				if(mActionBar != null) {
					mActionBar.setProgressBarVisibility(View.GONE);
				}
			}

			@Override
			public void onProviderEnabled(String provider) {
				if(mActionBar != null) {
					mActionBar.setProgressBarVisibility(View.GONE);
				}
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				if(mActionBar != null) {
					mActionBar.setProgressBarVisibility(View.GONE);
				}
			}
		});

		extractData(getIntent().getExtras());

		if(savedInstanceState != null) {
			mChangedImage = savedInstanceState.getBoolean("changed_image", true);
			mChangedFile = savedInstanceState.getBoolean("changed_file", true);
			mChangedLocation = savedInstanceState.getBoolean("changed_location", true);

			mKeyView.setText(savedInstanceState.getString("key"));
			mKeyView.setText(savedInstanceState.getString("value"));
			
			mLocation[0] = savedInstanceState.getDouble("latitude", -1.0);
			mLocation[1] = savedInstanceState.getDouble("longitude", -1.0);
			
			List<String> tempUris = Utils.unjoin(savedInstanceState.getString("uris"), " ");
			List<String> tempFiles = Utils.unjoin(savedInstanceState.getString("files"), " ");

			if(tempUris == null) {
				mUris = new LinkedList<String>();
			} else {
				mUris = new LinkedList<String>(tempUris);
			}

			if(tempFiles == null) {
				mFiles = new LinkedList<String>();
			} else {
				mFiles = new LinkedList<String>(tempFiles);
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString("key", mKeyView.getText().toString());

		outState.putString("value", mValueView.getText().toString());

		outState.putString("uris", Utils.join(mUris, " "));

		outState.putString("files", Utils.join(mFiles, " "));

		outState.putBoolean("changed_image", mChangedImage);

		outState.putBoolean("changed_file", mChangedFile);
		
		outState.putBoolean("changed_location", mChangedLocation);
		
		outState.putDouble("latitude", mLocation[0]);
		
		outState.putDouble("longitude", mLocation[1]);
	}

	@Override
	public void onStart() {
		super.onStart();
		setupViews();
		setupActionBar();
	}
	

	@Override
	public void onStop() {
		mImageAdapter = null;

		mFileAdapter = null;

		if(mLocationFinder != null) {
			mLocationFinder.cancel();
		}
		
		super.onStop();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch(requestCode) {
			case GET_CAMERA_IMAGE:
				if (resultCode == Activity.RESULT_OK) {
					final File file = Utils.getTempFile(this);

					try {

						String uri = Media.insertImage(getContentResolver(),
								file.getAbsolutePath(), null, null);

						System.err.println("MY URI IS: " + uri);

						if(mUris.contains(uri)) {
							Toast.makeText(this, "Image already added", 
									Toast.LENGTH_SHORT).show();
							return;
						}

						mUris.add( uri );

						mChangedImage = true;

						mImageTitle.setVisibility(View.VISIBLE);

						mImageGallery.setVisibility(View.VISIBLE);

						if(mImageAdapter != null) {
							System.err.println("NOTIFYING ADAPTER!");
							mImageAdapter.notifyDataSetChanged();
						} else {
							mImageAdapter = new GalleryAdapter(this, GalleryAdapter.IMAGE_ONLY);
							mImageGallery.setAdapter(mImageAdapter);
						}

						file.delete();

					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					}
				}
				else {
					System.err.println("Picture not taken");
				}
				break;
			case GET_GALLERY_IMAGE: 
				if (resultCode == Activity.RESULT_OK) {
					String uri = intent.getDataString();

					System.err.println("URI IS: " + uri);

					if(mUris.contains(uri)) {
						Toast.makeText(this, "Image already added", 
								Toast.LENGTH_SHORT).show();
						return;
					}

					mUris.add(uri);

					mChangedImage = true;

					mImageTitle.setVisibility(View.VISIBLE);

					mImageGallery.setVisibility(View.VISIBLE);

					if(mImageAdapter != null) {
						System.err.println("NOTIFYING ADAPTER!");
						mImageAdapter.notifyDataSetChanged();
					} else {
						mImageAdapter = new GalleryAdapter(this, GalleryAdapter.IMAGE_ONLY);
						mImageGallery.setAdapter(mImageAdapter);
					}
				}
				else {
					System.err.println("Picture not chosen!");
				}
				break;

			case REQUEST_SAVE:
				if (resultCode == Activity.RESULT_OK) {
					String filePath = intent.getStringExtra(FileActivity.RESULT_PATH);

					if(mFiles.contains(filePath)) {
						Toast.makeText(this, "File already added", 
								Toast.LENGTH_SHORT).show();
						return;
					}

					mFiles.add(filePath);

					mChangedFile = true;

					mFileTitle.setVisibility(View.VISIBLE);

					mFileGallery.setVisibility(View.VISIBLE);

					if(mFileAdapter != null) {
						System.err.println("NOTIFYING ADAPTER!");
						mFileAdapter.notifyDataSetChanged();
					} else {
						mFileAdapter = new GalleryAdapter(this, GalleryAdapter.IMAGE_WITH_TEXT);
						mFileGallery.setAdapter(mFileAdapter);
					}

				} else if (resultCode == Activity.RESULT_CANCELED) {
					//User cancelled/pressed back button
				}

				break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			String key = mKeyView.getText().toString();
			String value = mValueView.getText().toString();

			//If we have added an image or file or location, we definitely want to ask..
			if(!mChangedImage && !mChangedFile && !mChangedLocation) {
				if((key == null || key.length() == 0) && 
						(value == null || value.length() == 0) ) {
					//There is no data, so let them exit
					return super.onKeyDown(keyCode, event);
				}

				if(key.equals(getIntent().getStringExtra(Intent.EXTRA_SUBJECT)) &&
						value.equals(getIntent().getStringExtra(Intent.EXTRA_TEXT))){
					//There is data, but it hasn't been edited!
					return super.onKeyDown(keyCode, event);
				}
			}

			AlertDialog.Builder builder = 
					new AlertDialog.Builder(EntryActivity.this);

			builder.setTitle("Are you sure?");
			builder.setMessage("Any changes will be lost");

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

	private void dismissPopup(){
		//Dismisses the popup if possible..
		try{
			if(mQuickAction != null)
				mQuickAction.dismiss();
		}catch(Exception e){
			//doesnt matter..
			e.printStackTrace();
		}
	}

	
	private void setQuickActionListeners(final int type, final int pos) {

		mDeleteAction.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(type == IMAGE_GALLERY) {
					mUris.remove(pos);

					mChangedImage = true;

					if(mImageAdapter != null) {
						mImageAdapter.notifyDataSetChanged();
					}
					
					if(mUris.size() == 0) {
						mImageTitle.setVisibility(View.GONE);
					}
				} else if(type == FILE_GALLERY) {
					mFiles.remove(pos);

					mChangedFile = true;

					if(mFileAdapter != null) {
						mFileAdapter.notifyDataSetChanged();
					}
					
					if(mFiles.size() == 0) {
						mFileTitle.setVisibility(View.GONE);
					}
				}
				dismissPopup();
			}
		});
	}

	
	private void extractData(Bundle bundle) {
		if(bundle != null) {
			String key = bundle.getString(Intent.EXTRA_SUBJECT);
			String val = bundle.getString(Intent.EXTRA_TEXT);
			String id = bundle.getString(EXTRA_ID);
			String uris = bundle.getString(EXTRA_IMG_URI);
			String files = bundle.getString(EXTRA_FILES);
			String lat = bundle.getString(EXTRA_LAT);
			String lon = bundle.getString(EXTRA_LONG);

			if(key != null) {
				mKeyView.setText(key);
			}

			if(val != null) {
				mValueView.setText(val);
			}

			if(uris != null) {
				List<String> temp = Utils.unjoin(uris, " ");
				if(temp == null) {
					mUris = new LinkedList<String>();
				} else {
					mUris = new LinkedList<String>(temp);
				}
			}

			if(files != null) {
				List<String> temp = Utils.unjoin(files, " ");
				if(temp == null) {
					mFiles = new LinkedList<String>();
				} else {
					mFiles = new LinkedList<String>(temp);
				}
			}

			if(lat != null && lon != null) {
				try{
					mLocation[0] = Double.valueOf(lat);
					mLocation[1] = Double.valueOf(lon);
				} catch(NumberFormatException e) {
					e.printStackTrace();
				}
			}
			
			if(id != null) {
				mExistingId = id;
			}
		}

	}

	private void setupViews() {
		if(mUris != null && mUris.size() > 0) {
			mImageTitle.setVisibility(View.VISIBLE);
			mImageGallery.setVisibility(View.VISIBLE);

			mImageAdapter = new GalleryAdapter(this, GalleryAdapter.IMAGE_ONLY);

			mImageGallery.setAdapter(mImageAdapter);
		}

		if(mFiles != null && mFiles.size() > 0) {
			mFileTitle.setVisibility(View.VISIBLE);
			mFileGallery.setVisibility(View.VISIBLE);

			mFileAdapter = new GalleryAdapter(this, GalleryAdapter.IMAGE_WITH_TEXT);

			mFileGallery.setAdapter(mFileAdapter);
		}

		mImageGallery.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int pos,
					long arg) {
				try {
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.parse(mUris.get(pos)), "image/*");
					startActivity(intent);
				}catch(ActivityNotFoundException e) {
					e.printStackTrace();
					Toast.makeText(EntryActivity.this, "Can't find an application to open this file", 
							Toast.LENGTH_LONG).show();
				}
			}
		});

		mImageGallery.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> adapter, View view, int pos,
					long arg) {

				setQuickActionListeners(IMAGE_GALLERY, pos);

				mQuickAction = new QuickAction(view);

				mQuickAction.addActionItem(mDeleteAction);

				mQuickAction.setAnimStyle(QuickAction.ANIM_AUTO);

				mQuickAction.show();

				return true;
			}
		});
		
		mFileGallery.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int pos,
					long arg) {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				try {
					final String file = mFiles.get(pos);
					String ext = file.substring(file.lastIndexOf(".") + 1);
					System.err.println("EXTENSION: " + ext);
					intent.setDataAndType(Uri.fromFile(new File(file)),
							MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext));
					startActivity(intent);
				} catch(Exception e) {
					e.printStackTrace();
					Toast.makeText(EntryActivity.this, "Can't find an application to open this file", 
							Toast.LENGTH_LONG).show();
				}
			}
		});

		mFileGallery.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> adapter, View view, int pos,
					long arg) {

				setQuickActionListeners(FILE_GALLERY, pos);

				mQuickAction = new QuickAction(view);

				mQuickAction.addActionItem(mDeleteAction);

				mQuickAction.setAnimStyle(QuickAction.ANIM_AUTO);

				mQuickAction.show();

				return true;
			}
		});


		//Action for deleting images
		mDeleteAction.setTitle("Remove");
		mDeleteAction.setIcon(Resources.getSystem().getDrawable(android.R.drawable.ic_menu_delete));

	}

	private void setupActionBar() {
		mActionBar.setTitle("Secret Entry");
		
		if(Double.compare(mLocation[0],-1.0) != 0 && 
				Double.compare(mLocation[1],-1.0) != 0) {
			mActionBar.addAction(getLocationAction(), 0);
		}
		
		mActionBar.addAction(new AbstractAction(android.R.drawable.ic_menu_share) {
			@Override
			public void performAction(View view) {
				String key = mKeyView.getText().toString();
				String value = mValueView.getText().toString();

				if((key == null || key.length() == 0) && 
						(value == null || value.length() == 0)) {
					if(mChangedImage || mChangedFile) {
						Toast.makeText(EntryActivity.this, "This is only for sharing text", 
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(EntryActivity.this, "Nothing to share", 
								Toast.LENGTH_SHORT).show();
					}


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
						(value == null || value.length() == 0) && !mChangedImage) {
					Toast.makeText(EntryActivity.this, "Nothing to save!", 
							Toast.LENGTH_SHORT).show();

					return;
				}

				if(mExistingId != null) {
					//We are updating an old entry!
					if(mApplication.updateEntry(Integer.valueOf(mExistingId), key, value, Utils.join(mUris," "), Utils.join(mFiles," "), mLocation) > 0) {
						Toast.makeText(EntryActivity.this, "Entry Updated!", 
								Toast.LENGTH_SHORT).show();

						finish();
					} else {
						Toast.makeText(EntryActivity.this, "Something went wrong :(", 
								Toast.LENGTH_SHORT).show();
					}

				} else {
					//We are adding a brand new entry!
					if(mApplication.addEntry(key, value, Utils.join(mUris," "), Utils.join(mFiles," "), mLocation) == true) {
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

	private AbstractAction getLocationAction() {
		return new AbstractAction(android.R.drawable.ic_dialog_map) {
			@Override
			public void performAction(View view) {
				String location_uri = "geo:" + mLocation[0] + "," + mLocation[1];
				Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(location_uri));
				startActivity(intent);
			}
		};
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.entry_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(Double.compare(mLocation[0],-1.0) != 0 && 
				Double.compare(mLocation[1],-1.0) != 0) {
			
			menu.getItem(3).setTitle("Update location");
			
			menu.getItem(4).setEnabled(true).setVisible(true);
		} else {
			menu.getItem(4).setEnabled(false).setVisible(false);
			menu.getItem(3).setTitle("Add current location");
		}
		
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.menu_camera:
				takePhoto();
				return true;

			case R.id.menu_gallery:
				getGalleryPhoto();
				return true;

			case R.id.menu_document:
				Intent intent = new Intent(getBaseContext(),
						FileActivity.class);
				intent.putExtra(FileActivity.START_PATH, "/sdcard");
				startActivityForResult(intent, REQUEST_SAVE);

				return true;

			case R.id.menu_location:
				//Check for location within the last <given interval>
				Location location = mLocationFinder.getLastBestLocation(1000, 
						System.currentTimeMillis() - AlarmManager.INTERVAL_HALF_HOUR);
				
				if(location == null) {
					//We should already be getting a more accurate location..
					mActionBar.setProgressBarVisibility(View.VISIBLE);
				} else {
					mChangedLocation = true;
					
					if(Double.compare(mLocation[0], -1.0) == 0 && 
							Double.compare(mLocation[1], -1.0) == 0) {
						//We haven't got our location before..
						mLocation[0] = location.getLatitude();
						mLocation[1] = location.getLongitude();
						
						mActionBar.addAction(getLocationAction(), 0);
					} else {
						//We should already be showing the ActionBar button..
						System.err.println("BEFORE: " + mLocation[0] + " " + mLocation[1]);
						
						mLocation[0] = location.getLatitude();
						mLocation[1] = location.getLongitude();
					}
				}
				
				return true;
				
			case R.id.menu_remove_location:
				if(mActionBar.getActionCount() > 2) {
					mActionBar.removeActionAt(0);
				}
				
				mChangedLocation = true;
				mLocation[0] = -1.0;
				mLocation[1] = -1.0;
				
				return true;
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	
	private void takePhoto(){
		final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(Utils.getTempFile(this)) );

		startActivityForResult(intent, GET_CAMERA_IMAGE);
	}
	

	
	private void getGalleryPhoto() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(Intent.createChooser(intent,"Select Picture"), 
				GET_GALLERY_IMAGE);
	}
	

	
	public class GalleryAdapter extends BaseAdapter {
		private Activity mActivity;

		private int mType;

		public static final int IMAGE_ONLY = 0;

		public static final int IMAGE_WITH_TEXT = 1;

		public GalleryAdapter(Activity a, int type) {
			mActivity = a;

			mType = type;
		}

		public int getCount() {
			if(mType == IMAGE_ONLY) {
				return mUris == null ? 0 : mUris.size();
			} else if(mType == IMAGE_WITH_TEXT) {
				return mFiles == null ? 0 : mFiles.size();
			} else {
				return 0;
			}

		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

		// create a new ImageView for each item referenced by the Adapter
		public View getView(int position, View convertView, ViewGroup parent) {
			if(mType == IMAGE_ONLY) {
				ImageView imageView;
				if (convertView == null) {  // if it's not recycled, initialize some attributes
					imageView = new ImageView(mActivity);
					imageView.setLayoutParams(new Gallery.LayoutParams(200, 200));
					imageView.setScaleType(ImageView.ScaleType.FIT_XY);

				} else {
					imageView = (ImageView) convertView;
				}

				imageView.setImageBitmap(Utils.decodeFile(new File(Utils.getPath(mActivity, Uri.parse(mUris.get(position))))));

				imageView.setTag(mUris.get(position));

				return imageView;
			} else if( mType == IMAGE_WITH_TEXT) {
				LinearLayout group = null;
				if(convertView == null) {
					group = new LinearLayout(EntryActivity.this);

					group.setOrientation(LinearLayout.VERTICAL);

					ImageView image = new ImageView(mActivity);
					image.setLayoutParams(new Gallery.LayoutParams(200, 200));
					image.setScaleType(ImageView.ScaleType.FIT_XY);

					TextView text = new TextView(mActivity);
					text.setLayoutParams(new Gallery.LayoutParams(200, 200));

					group.addView(image);
					group.addView(text);

				} else {
					group = (LinearLayout) convertView;
				}

				ImageView iv = (ImageView) group.getChildAt(0);
				iv.setImageResource(R.drawable.file_large);

				TextView tv = (TextView) group.getChildAt(1);
				String file = mFiles.get(position);
				tv.setText(file.substring(file.lastIndexOf("/") + 1));

				return group;
			} else {
				return null;
			}
		}
	}
}
