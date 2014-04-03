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

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.yi4all.emenu.newgen.R;
import com.yi4all.emenu.newgen.db.CourseModel;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This fragment will populate the children of the ViewPager from {@link ImageDetailActivity}.
 */
public class ImageDetailFragment extends SherlockFragment {
    private static final String IMAGE_DATA_EXTRA = "extra_image_data";
    private CourseModel course;
    private ImageView mImageView;
    

    /**
     * Empty constructor as per the Fragment documentation
     */
    public ImageDetailFragment() {
    }
    
    public static ImageDetailFragment newInstance(CourseModel course) {
    	final ImageDetailFragment f = new ImageDetailFragment();
    	
    	final Bundle args = new Bundle();
        args.putSerializable(IMAGE_DATA_EXTRA, course);
        f.setArguments(args);
        
        return f;
    }

    /**
     * Populate image using a url from extras, use the convenience factory method
     * {@link ImageDetailFragment#newInstance(String)} to create this fragment.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        
        course = getArguments() != null ? (CourseModel)getArguments().getSerializable(IMAGE_DATA_EXTRA) : null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate and locate the main ImageView
        final View v = inflater.inflate(R.layout.image_detail_fragment, container, false);
        
//        ImageCacheParams cacheParams = new ImageCacheParams(getActivity(), "image");

        // Set memory cache to 25% of mem class
//        cacheParams.setMemCacheSizePercent(getActivity(), 0.25f);

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
//        ImageFetcher mImageFetcher = new ImageFetcher(getActivity(), 100);
//        mImageFetcher.setLoadingImage(R.drawable.download_32);
//        mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
        
        mImageView = (ImageView) v.findViewById(R.id.imageView);
        mImageView.setImageBitmap(BitmapFactory.decodeFile(course.getImgUrl()));
//        mImageFetcher.loadImage(course.getImgUrl(), mImageView);
        
        TextView titleTxt = (TextView) v.findViewById(R.id.detail_course_info);
        titleTxt.setText(course.getName()+" "+course.getPrice()+"元" + " " + course.getCode());
        
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Use the parent activity to load the image asynchronously into the ImageView (so a single
        // cache can be used over all pages in the ViewPager
        // Pass clicks on the ImageView to the parent activity to handle
        if (OnClickListener.class.isInstance(getActivity())) {
            mImageView.setOnClickListener((OnClickListener) getActivity());
        }
        
    }
    
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mImageView != null) {
            // Cancel any pending image work
            mImageView.setImageDrawable(null);
        }
    }
}
