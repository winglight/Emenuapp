/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yi4all.emenu.newgen;

import java.util.List;

import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.yi4all.emenu.newgen.R;
import com.yi4all.emenu.newgen.db.CourseModel;

public class ImageDetailActivity extends SherlockFragmentActivity implements
		OnClickListener {
	public static final String EXTRA_IMAGE = "extra_image";

	public final static String PREF_WALLPAPER = "ppwallpaper";

	private static final String LOGTAG = "ImageDetailActivity";

	private ImagePagerAdapter mAdapter;
	private ViewPager mPager;

	protected int imageSequence = 0;

	private List<CourseModel> courseList;
	
	@TargetApi(11)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_detail_pager);

		courseList = (List<CourseModel>) getIntent().getSerializableExtra(
				"list");
		int position = getIntent().getIntExtra("position", 0);
		
//		ImageCacheParams cacheParams = new ImageCacheParams(this, "image");

        // Set memory cache to 25% of mem class
//        cacheParams.setMemCacheSizePercent(this, 0.25f);

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
//        final ImageFetcher mImageFetcher = new ImageFetcher(this, 100);
//        mImageFetcher.setLoadingImage(R.drawable.download_32);
//        mImageFetcher.addImageCache(this.getSupportFragmentManager(), cacheParams);
		
//		flipView = new FlipViewController(this, FlipViewController.HORIZONTAL);
//
//		flipView.setAdapter(new BaseAdapter() {
//			@Override
//			public int getCount() {
//				return courseList.size();
//			}
//
//			@Override
//			public Object getItem(int position) {
//				return position;
//			}
//
//			@Override
//			public long getItemId(int position) {
//				return position;
//			}
//
//			@Override
//			public View getView(int position, View convertView, ViewGroup parent) {
//				ImageView mImageView;
//				if (convertView == null) {
//					final Context context = parent.getContext();
//					mImageView = new ImageView(context);
//				}
//				else {
//					mImageView = (ImageView) convertView;
//				}
//				mImageView.setImageBitmap(BitmapFactory.decodeFile(courseList.get(position).getImgUrl()));
////				mImageFetcher.loadImage(courseList.get(position).getImgUrl(), mImageView);
//				
//				return mImageView;
//			}
//		});
//		
//		flipView.getAdapter().
//
//		setContentView(flipView);

		// Set up activity to go full screen
		getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);
		
		final ActionBar actionBar = getSupportActionBar();

		if (actionBar != null) {
 			actionBar.hide();
		}

		mAdapter = new ImagePagerAdapter(getSupportFragmentManager(),
				courseList.size());
		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);
		mPager.setPageMargin(5);
		mPager.setOffscreenPageLimit(2);
		mPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				imageSequence = position;

				if(courseList.size() != 0){
				if(imageSequence == courseList.size()-1){
					toastMsg(R.string.alreadyLastPost);
				}else if(imageSequence == 0){
					toastMsg(R.string.alreadyFirstPost);
				}
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

		mPager.setCurrentItem(position);

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

	@Override
	public void onResume() {
		super.onResume();
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// We do nothing here. We're only handling this to keep orientation
		// or keyboard hiding from causing the WebView activity to restart.
	}

	/**
	 * The main adapter that backs the ViewPager. A subclass of
	 * FragmentStatePagerAdapter as there could be a large number of items in
	 * the ViewPager and we don't want to retain them all in memory at once but
	 * create/destroy them on the fly.
	 */
	private class ImagePagerAdapter extends FragmentStatePagerAdapter {
		private final int mSize;

		public ImagePagerAdapter(FragmentManager fm, int size) {
			super(fm);
			mSize = size;
		}

		@Override
		public int getCount() {
			return mSize;
		}

		@Override
		public Fragment getItem(int position) {
			return ImageDetailFragment.newInstance(courseList.get(position));
		}
	}

	/**
	 * Set on the ImageView in the ViewPager children fragments, to
	 * enable/disable low profile mode when the ImageView is touched.
	 */
	@TargetApi(11)
	@Override
	public void onClick(View v) {
		final ActionBar actionBar = getSupportActionBar();

		if (actionBar != null) {
			if(actionBar.isShowing()){
				actionBar.hide();
			}else{
			actionBar.show();
			}
		}
	}

}
