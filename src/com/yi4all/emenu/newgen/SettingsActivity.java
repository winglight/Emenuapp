package com.yi4all.emenu.newgen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.yi4all.emenu.newgen.R;
import com.yi4all.emenu.newgen.db.CourseDBOpenHelper;
import com.yi4all.emenu.newgen.db.CourseModel;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SettingsActivity extends Activity {
	
	private final static String PASSWD = "78728884";
	private final static String TAG = "SettingsActivity";
	
	protected CourseDBOpenHelper dbHelper = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_activity);

		Button saveBtn = (Button) findViewById(R.id.saveBtn);
		saveBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				saveAccount();

			}
		});
		
		EditText urlTxt = (EditText) findViewById(R.id.urlTxt);
		urlTxt.setText(getUrl());
		
		final EditText nameTxt = (EditText) findViewById(R.id.shortcutNameTxt);
		nameTxt.setText(getPrefName());
		
		Button saveShortcutBtn = (Button) findViewById(R.id.saveShortcutBtn);
		saveShortcutBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String oldName = getPrefName();
				
				Intent shortcutIntent = new Intent(getApplicationContext(),
			            MainActivity.class);
			    shortcutIntent.setAction(Intent.ACTION_MAIN);
			     
			    Intent addIntent = new Intent();
			    addIntent
			            .putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
			    addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, oldName);
			 
			    addIntent
			            .setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");
			    getApplicationContext().sendBroadcast(addIntent);
				
				savePrefName(nameTxt.getText().toString());

				MainActivity.installShortCut(SettingsActivity.this, nameTxt.getText().toString());
				
				finish();
				
			}
		});
		
	}

	private void saveAccount() {
		 final ProgressDialog progressDialog = ProgressDialog.show(this, "Loading...", "Please wait...", true, false);
		 
		 EditText urlTxt = (EditText) findViewById(R.id.urlTxt);
		 
		 final String url = urlTxt.getText().toString();

//			EditText passwordTxt = (EditText) findViewById(R.id.passwordTxt);
//			
//			if(passwordTxt.getText().toString().equals(PASSWD)){
				Runnable run = new Runnable() {
					
					@Override
					public void run() {
						savePrefRelease(url);
						
						boolean flag = downloadAndInstall(url);
						
						progressDialog.dismiss();
						if(flag){
						// successful message
						toastMsg(getString(R.string.saveSuccess));
						
						 finish();
						}else{
							toastMsg(getString(R.string.saveFailed));
						}
					}
				};
				
				new Thread(run).start();
			
//			}else{
//				toastMsg(getString(R.string.saveFailed));
//				progressDialog.dismiss();
//			}
			
	}
	
	private CourseDBOpenHelper getHelper() {
		if (dbHelper == null) {
			dbHelper = OpenHelperManager
					.getHelper(this, CourseDBOpenHelper.class);
		}
		return dbHelper;
	}
	
	@Override
	protected void onResume() {
		super.onResume();

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
	
	/**
	 * @param path
	 */
	public boolean downloadAndInstall(String path) {
		try {
			File dir = new File(path);
			if(dir.exists() && dir.isDirectory()){
				File list[] = dir.listFiles();
				Dao<CourseModel, Integer> dao = getHelper().getCourseDAO();
				dao.deleteBuilder().delete();

			    for( int i=0; i< list.length; i++)
			    {
			        String fileName = list[i].getName() ;
			        String[] items = (fileName.substring(0, fileName.lastIndexOf("."))).split("_");
			        if(items != null && items.length == 4){
			        	CourseModel cm = new CourseModel();
			        	cm.setCode(items[0]);
			        	cm.setName(items[1]);
			        	cm.setPrice(Integer.valueOf(items[2]));
			        	cm.setCategory(items[3]);
			        	cm.setImgUrl(list[i].getPath());
			        	
			        	dao.create(cm);
			        }
			    }

			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return true;
	}
	
	private void savePrefRelease(String url) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor editor = prefs.edit();
		editor.putString("url", url);
		editor.commit();
	}

	public String getUrl() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		return prefs.getString("url", "/sdcard/emenu/");

	}
	
	private void savePrefName(String url) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor editor = prefs.edit();
		editor.putString("name", url);
		editor.commit();
	}

	private String getPrefName() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		return prefs.getString("name", getString(R.string.app_name));

	}

	public void toastMsg(final String msg) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG)
						.show();
			}
		});
	}

	public void toastMsg(int resId, String... args) {
		final String msg = this.getString(resId, args);
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT)
						.show();
			}
		});
	}
}