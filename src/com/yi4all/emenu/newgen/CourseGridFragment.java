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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.yi4all.emenu.newgen.R;
import com.yi4all.emenu.newgen.db.CourseDBOpenHelper;
import com.yi4all.emenu.newgen.db.CourseModel;

/**
 * The main fragment that powers the ImageGridActivity screen. Fairly straight
 * forward GridView implementation with the key addition being the ImageWorker
 * class w/ImageCache to load children asynchronously, keeping the UI nice and
 * smooth and caching thumbnails for quick retrieval. The cache is retained over
 * configuration changes like orientation change so the images are populated
 * quickly if, for example, the user rotates the device.
 */
public class CourseGridFragment extends Fragment {
	private static final String LOGTAG = "CourseGridFragment";
	
	public static final String POST_DATA = "post";

	protected CourseDBOpenHelper dbHelper = null;
	
	private int gridNumColumns = 2;

	private GridView gridView;
	
	private List<CourseModel> courseList;
	
	private int firstImage;
	private int imageCount;
	
//	private ImageFetcher mImageFetcher;

	/**
	 * Empty constructor as per the Fragment documentation
	 */
	public CourseGridFragment() {
	}
	
	public static CourseGridFragment newInstance(List<CourseModel> courseList, 
			int gridNumColumns, int firstImage, int imageCount) {
		final CourseGridFragment f = new CourseGridFragment();
		final Bundle args = new Bundle();
        args.putSerializable(POST_DATA, (Serializable) courseList);
        args.putInt("gridNumColumns", gridNumColumns);
        args.putInt("firstImage", firstImage);
        args.putInt("imageCount", imageCount);
        f.setArguments(args);

        return f;
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		courseList =  (getArguments() != null ? (List<CourseModel>)getArguments().getSerializable(POST_DATA) : null);
		gridNumColumns =  (getArguments() != null ? getArguments().getInt("gridNumColumns") : 2);
		firstImage =  (getArguments() != null ? getArguments().getInt("firstImage") : 0);
		imageCount =  (getArguments() != null ? getArguments().getInt("imageCount") : 0);
		
//		ImageCacheParams cacheParams = new ImageCacheParams(getActivity(), "image");

        // Set memory cache to 25% of mem class
//        cacheParams.setMemCacheSizePercent(getActivity(), 0.25f);

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
//        mImageFetcher = new ImageFetcher(getActivity(), 100);
//        mImageFetcher.setLoadingImage(R.drawable.download_32);
//        mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final View v = inflater.inflate(R.layout.course_grid_fragment, container, false);
		 
		
		gridView = (GridView) v.findViewById(R.id.gridView);
		gridView.setNumColumns(gridNumColumns);
		gridView.setAdapter(new PostGridAdapter(getActivity()));
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// change the view temporarily
				final Intent i = new Intent(getActivity(),
						ImageDetailActivity.class);
				i.putExtra("list", (Serializable)courseList);
					i.putExtra("position", position+firstImage);

				startActivity(i);
			}
		});
		gridView.setOnTouchListener(new MulitPointTouchListener());
		
		return v;
	}

	public class MulitPointTouchListener implements OnTouchListener {

		// We can be in one of these 3 states
		static final int NONE = 0;
		static final int DRAG = 1;
		static final int ZOOM = 2;
		int mode = NONE;

		// Remember some things for zooming
		PointF start = new PointF();
		PointF mid = new PointF();
		float oldDist = 1f;

		public MulitPointTouchListener() {
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {

			boolean hasTouch = false;
			Log.i(LOGTAG, "hasTouch:" + hasTouch);
			// Handle touch events here...
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:

				start.set(event.getX(), event.getY());
				// Log.d(TAG, "mode=DRAG");
				mode = DRAG;

				// Log.d(TAG, "mode=NONE");
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				oldDist = spacing(event);
				// Log.d(TAG, "oldDist=" + oldDist);
				if (oldDist > 50f) {
					midPoint(mid, event);
					mode = ZOOM;
					// Log.d(TAG, "mode=ZOOM");
				}
				break;
			case MotionEvent.ACTION_UP:
				return mode == NONE;
			case MotionEvent.ACTION_POINTER_UP:

				mode = NONE;

				return true;
			case MotionEvent.ACTION_MOVE:
				if (mode == DRAG) {
					return false;
				} else if (mode == ZOOM) {
					float newDist = spacing(event);
					int scale = -Double.valueOf((newDist - oldDist) / 50f)
							.intValue();
					if (scale > 0) {
						// increas grid column

						// gridNumColumns += scale;
						gridNumColumns++;
						if (gridNumColumns > 4)
							gridNumColumns = 4;

						gridView.setNumColumns(gridNumColumns);
						gridView.setAdapter(new PostGridAdapter(getActivity()));

						mode = NONE;
					} else if (scale < 0) {
						// decrease grid column
						// gridNumColumns += scale;
						gridNumColumns--;
						if (gridNumColumns < 1)
							gridNumColumns = 1;
						gridView.setNumColumns(gridNumColumns);
						gridView.setAdapter(new PostGridAdapter(getActivity()));

						mode = NONE;
					}
					
//					int rows = ((MainActivity)getActivity()).calculateRows();
//					imageCount = gridNumColumns*rows;
//					if((imageCount + firstImage) >= courseList.size()){
//						imageCount = courseList.size() - firstImage - 1;
//					}
					
					((MainActivity)getActivity()).setGridNumColumns(gridNumColumns);

					return true;
				}
				break;
			}

			return true; // indicate event was handled
		}

		private float spacing(MotionEvent event) {
			float x = event.getX(0) - event.getX(1);
			float y = event.getY(0) - event.getY(1);
			return FloatMath.sqrt(x * x + y * y);
		}

		private void midPoint(PointF point, MotionEvent event) {
			float x = event.getX(0) + event.getX(1);
			float y = event.getY(0) + event.getY(1);
			point.set(x / 2, y / 2);
		}

	}

	private class PostGridAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		 private GridView.LayoutParams mImageViewLayoutParams;

		public PostGridAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
			 mImageViewLayoutParams = new GridView.LayoutParams(
			 LayoutParams.MATCH_PARENT, ((MainActivity)getActivity()).getGridItemHeight());
		}

		@Override
		public int getCount() {
			return imageCount;
		}

		public CourseModel getItem(int i) {
			return courseList.get(i + firstImage);
		}

		public long getItemId(int i) {
			return i + firstImage;
		}

		public View getView(final int position, View convertView, ViewGroup vg) {

			if (courseList == null || position < 0
					|| position >= courseList.size())
				return null;

			final View row;
			if (convertView == null) {
				row = mInflater.inflate(R.layout.post_grid_item, null);
				 row.setLayoutParams(mImageViewLayoutParams);
			} else {
				row = convertView;
			}
			ViewHolder holder = (ViewHolder) row.getTag();
			if (holder == null) {
				holder = new ViewHolder(row);
				row.setTag(holder);
			}

			 if (row.getLayoutParams().height != ((MainActivity)getActivity()).getGridItemHeight()) {
			 row.setLayoutParams(mImageViewLayoutParams);
			 }

			// other normal row
			CourseModel course = courseList.get(position + firstImage);
				holder.icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
				holder.icon.setImageBitmap(BitmapFactory.decodeFile(course.getImgUrl()));
//				mImageFetcher.loadImage(course.getImgUrl(), holder.icon);
				holder.title.setText(course.getName()+" "+course.getPrice()+"元" + " " + course.getCode());

			return (row);
		}

	}
	

	class ViewHolder {
		TextView title = null;
		ImageView icon = null;

		ViewHolder(View base) {
			this.title = (TextView) base.findViewById(R.id.course_name);
			this.icon = (ImageView) base.findViewById(R.id.course_icon);
		}
	}
	
	
	
	
	

	private CourseDBOpenHelper getHelper() {
		if (dbHelper == null) {
			dbHelper = OpenHelperManager.getHelper(getActivity(),
					CourseDBOpenHelper.class);
		}
		return dbHelper;
	}

	@Override
	public void onDestroy() {
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
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
			}
		});
	}
}
