package com.yi4all.emenu.newgen;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.yi4all.emenu.newgen.R;
import com.yi4all.emenu.newgen.db.CourseDBOpenHelper;
import com.yi4all.emenu.newgen.db.CourseModel;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class MainActivity extends SherlockFragmentActivity {

	private final static String LOGTAG = "MainActivity";

	private GridPagerAdapter mAdapter;
	static final int DEFAULT_NUM_FRAGMENTS = 4;
	static final int DEFAULT_NUM_ITEMS = 4;

	private int gridNumColumns = 3;
	private ViewPager mPager;
	private int mNumFragments = 0; // total number of fragments
	public int mNumItems = 0;

	private int mItemHeight = 0;
	private int screen_width = 0;
	private int screen_height = 0;

	private ImageView preBtn;
	private ImageView nextBtn;
	private TextView pageLbl;

	Display display;

	protected CourseDBOpenHelper dbHelper = null;
	private List<CourseModel> courseList;
	private int currentPosition = 0;

	private boolean isTwiceQuit;

	private String sid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.course_grid_activity);

		getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);

		refreshScreenWidthHeight();

		final ActionBar actionBar = getSupportActionBar();

		if (actionBar != null) {
			// Hide title text and set home as up

			List<String> clist = getCategories();
			final String[] categories = clist.toArray(new String[clist.size()]);

			/** Create an array adapter to populate dropdownlist */
			ArrayAdapter<CharSequence> adapter = new ArrayAdapter(this,
					R.layout.sherlock_spinner_item, categories);

			/** Enabling dropdown list navigation for the action bar */
			actionBar
					.setNavigationMode(com.actionbarsherlock.app.ActionBar.NAVIGATION_MODE_LIST);

			/** Defining Navigation listener */
			ActionBar.OnNavigationListener navigationListener = new OnNavigationListener() {

				@Override
				public boolean onNavigationItemSelected(int itemPosition,
						long itemId) {
					String currentCategory = "";
					if (itemPosition == 0) {
						currentCategory = "all";
					} else {
						currentCategory = categories[itemPosition];
					}

					courseList = getCoursesByCategory(currentCategory);

					refreshFragment();

					return true;
				}
			};

			/**
			 * Setting dropdown items and item navigation listener for the
			 * actionbar
			 */

			adapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowTitleEnabled(true);
			actionBar.setHomeButtonEnabled(true);
			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
					| ActionBar.DISPLAY_SHOW_TITLE);
			actionBar.setListNavigationCallbacks(adapter, navigationListener);
			actionBar.show();
		}

		TelephonyManager manager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		sid = manager.getDeviceId();

		courseList = new ArrayList<CourseModel>();

		pageLbl = (TextView) findViewById(R.id.pageLbl);

		mAdapter = new GridPagerAdapter(getSupportFragmentManager());
		mPager = (ViewPager) findViewById(R.id.gridPager);
		mPager.setAdapter(mAdapter);
		mPager.setPageMargin(10);
		mPager.setOffscreenPageLimit(1);
		mPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				pageLbl.setText(position + 1 + " / " + mNumFragments);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

		initBtn();

		refreshFragment();

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean flag = prefs.getBoolean("shortcut", false);
		if (!flag) {
			installShortCut(this, getString(R.string.app_name));
		}

//		if (!isAuthorized()) {
//			assertAuthorized();
//		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// We do nothing here. We're only handling this to keep orientation
		// or keyboard hiding from causing the WebView activity to restart.
		refreshScreenWidthHeight();
		refreshFragment();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_setting: {
			// popup the settings window
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setClass(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCoder, KeyEvent event) {
		int keyCode = event.getKeyCode();
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (isTwiceQuit) {
				this.finish();
			} else {
				toastMsg(R.string.sure_quit_app);
				isTwiceQuit = true;

				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						isTwiceQuit = false;

					}
				}, 2000);
			}

		default:
			return false;
		}

	}

	public static void installShortCut(Context context, String appName) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		// install shortcut
		Intent shortcut = new Intent(
				"com.android.launcher.action.INSTALL_SHORTCUT");

		// 显示的名字
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, appName);
		// 显示的图标
		Parcelable icon = Intent.ShortcutIconResource.fromContext(context,
				R.drawable.ic_launcher);
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);

		// 不允许重复创建
		shortcut.putExtra("duplicate", false);

		Intent intent = new Intent(context, MainActivity.class);
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);

		// 发送广播用以创建shortcut
		context.sendBroadcast(shortcut);

		// save flag
		Editor editor = prefs.edit();
		editor.putBoolean("shortcut", true);
		editor.commit();

	}

	private List<CourseModel> getCoursesByCategory(String category) {
		List<CourseModel> list = new ArrayList<CourseModel>();
		try {
			Dao<CourseModel, Integer> dba = getHelper().getCourseDAO();
			if ("all".equalsIgnoreCase(category)) {
				list = dba.queryForAll();
			} else {
				list = dba.queryForEq(CourseModel.CATEGORY, category);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	private List<String> getCategories() {
		List<String> list = new ArrayList<String>();
		list.add(getString(R.string.all));
		try {
			Dao<CourseModel, Integer> dba = getHelper().getCourseDAO();

			QueryBuilder<CourseModel, Integer> queryBuilder = dba
					.queryBuilder();

			queryBuilder.selectColumns(CourseModel.CATEGORY);

			queryBuilder.distinct();

			PreparedQuery<CourseModel> preparedQuery = queryBuilder.prepare();

			List<CourseModel> clist = dba.query(preparedQuery);

			for (CourseModel cm : clist) {
				list.add(cm.getCategory());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	private class GridPagerAdapter extends FragmentStatePagerAdapter {

		public GridPagerAdapter(FragmentManager fm) {
			super(fm);
			setup();
		}

		@Override
		public int getCount() {
			return mNumFragments;
		}

		@Override
		public Fragment getItem(int position) {

			// The last page might not have the full number of items.
			int imageCount = mNumItems;
			if (position == (mNumFragments - 1)) {
				int numTopics = courseList.size();
				int rem = numTopics % mNumItems;
				if (rem > 0)
					imageCount = rem;
			}

			// Return a new GridFragment object.
			return CourseGridFragment.newInstance(courseList, gridNumColumns,
					position * mNumItems, imageCount);
		}

		public void setup() {
			if ((courseList == null)) {
				mNumItems = DEFAULT_NUM_ITEMS;
				mNumFragments = DEFAULT_NUM_FRAGMENTS;
			} else {
				int numTopics = courseList.size();
				int numRows = calculateRows();
				int numCols = gridNumColumns;
				int numTopicsPerPage = numRows * numCols;
				int numFragments = numTopics / numTopicsPerPage;
				if (numTopics % numTopicsPerPage != 0)
					numFragments++; // Add one if there is a partial page

				mNumFragments = numFragments;
				mNumItems = numTopicsPerPage;
			}

			Log.d("GridViewPager", "Num fragments, topics per page: "
					+ mNumFragments + " " + mNumItems);

		}
	}

	public void setGridNumColumns(int colNums) {
		if (colNums != this.gridNumColumns) {
			this.gridNumColumns = colNums;
			refreshFragment();
		}
	}

	public void refreshFragment() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (!mPager.isShown())
					return;

				mAdapter.setup();
				mAdapter.notifyDataSetChanged();
				mPager.setAdapter(mAdapter);

				pageLbl.setText("1 / " + mNumFragments);
			}
		});
	}

	public int calculateRows() {
		return (screen_height - 100) / mItemHeight;
	}

	private void refreshScreenWidthHeight() {
		display = getWindowManager().getDefaultDisplay();
		final DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		screen_height = displayMetrics.heightPixels;
		screen_width = displayMetrics.widthPixels;

		int display_mode = getResources().getConfiguration().orientation;

		if (display_mode == 1) {
			mItemHeight = screen_width * 4 / (3 * gridNumColumns);

		} else {
			mItemHeight = screen_height - 100;
		}
	}

	public int getGridItemHeight() {
		return mItemHeight;
	}

	private void initBtn() {
		preBtn = (ImageView) findViewById(R.id.previousBtn);

		nextBtn = (ImageView) findViewById(R.id.nextBtn);

		pageLbl = (TextView) findViewById(R.id.pageLbl);

		preBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (currentPosition <= 0) {
					toastMsg(R.string.alreadyFirstPost);
				} else {
					currentPosition--;

					refreshFragment();
				}
			}
		});

		nextBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (currentPosition >= courseList.size() - 1) {
					toastMsg(R.string.alreadyLastPost);
				} else {
					currentPosition++;

					refreshFragment();
				}
			}
		});

	}

	private CourseDBOpenHelper getHelper() {
		if (dbHelper == null) {
			dbHelper = OpenHelperManager.getHelper(this,
					CourseDBOpenHelper.class);
		}
		return dbHelper;
	}

	@Override
	protected void onResume() {
		super.onResume();

		courseList = getCoursesByCategory("all");
		refreshFragment();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		/*
		 * You'll need this in your class to release the helper when done.
		 */
		if (dbHelper != null) {
			OpenHelperManager.releaseHelper();
			dbHelper = null;
		}

	}

	public void toastMsg(int resId) {
		final String msg = this.getString(resId);
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG)
						.show();
			}
		});
	}

	private boolean isAuthorized() {
		String pwd = getPrefPwd();
		return pwd != null && pwd.equals(generateHash());
	}

	private void assertAuthorized() {
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				final EditText pwdTxt = new EditText(MainActivity.this);

				new AlertDialog.Builder(MainActivity.this)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle(R.string.auth_title)
						.setMessage(getString(R.string.auth_input, sid))
						.setView(pwdTxt)
						.setPositiveButton(R.string.auth_save,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										savePrefPwd(pwdTxt.getText().toString());
										if (!isAuthorized()) {
											assertAuthorized();
										}
									}

								})
						.setCancelable(false)
						.setNegativeButton(R.string.auth_quit,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										MainActivity.this.finish();

									}
								}).show();

			}
		}, 1000);

	}

	private void savePrefPwd(String pwd) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor editor = prefs.edit();
		editor.putString("pwd", pwd);
		editor.commit();
	}

	private String getPrefPwd() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		return prefs.getString("pwd", null);

	}

	private String generateHash() {

		String hash = "";
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			byte[] thedigest = md.digest((sid != null) ? sid.getBytes()
					: "sdj8w932hfdsh^**@#^(@#(".getBytes());
			BigInteger bigInt = new BigInteger(1, thedigest);
			hash = bigInt.toString(16);
			while (hash.length() < 32) {
				hash = "0" + hash;
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return hash.substring(24);
	}
}
