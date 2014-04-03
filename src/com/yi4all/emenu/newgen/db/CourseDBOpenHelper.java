package com.yi4all.emenu.newgen.db;

import java.sql.SQLException;
import java.util.Date;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CourseDBOpenHelper extends OrmLiteSqliteOpenHelper {
	
	public  static final int DATABASE_VERSION = 10;
	
	public static final long DOWNLOAD_SOURCE_ID = 1;
	
    
	public static final String DATABASE_NAME = "post";
	
	public static final String TAG_HOT = "hot";
	public static final String TAG_ART = "arts";
	public static final String TAG_WALLPAPER = "wallpaper";
	public static final String TAG_DOWNLOAD = "download";
	public static final String TAG_ADULT = "adult";
	
	private Dao<CourseModel, Integer> postDao;

    public CourseDBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
    	try {
			Log.i(CourseDBOpenHelper.class.getName(), "onCreate");
			TableUtils.createTable(connectionSource, CourseModel.class);
			
			CourseModel cm = new CourseModel();
			cm.setName("地锅小杂鱼");
			cm.setImgUrl("http://112.91.151.10:7777/restaurant/images/2001.jpg");
			cm.setCategory("荤菜");
			cm.setPrice(30);
			cm.setCode("DGXZY");
			
			getCourseDAO().create(cm);
			
			cm = new CourseModel();
			cm.setName("天沐胡鲤鱼");
			cm.setImgUrl("http://112.91.151.10:7777/restaurant/images/2001.jpg");
			cm.setCategory("荤菜");
			cm.setPrice(50);
			cm.setCode("HLY");
			
			getCourseDAO().create(cm);
			
			cm = new CourseModel();
			cm.setName("天沐湖大花鲢");
			cm.setImgUrl("http://112.91.151.10:7777/restaurant/images/2001.jpg");
			cm.setCategory("荤菜");
			cm.setPrice(80);
			cm.setCode("DHL");
			
			getCourseDAO().create(cm);
			
			cm = new CourseModel();
			cm.setName("多宝鱼");
			cm.setImgUrl("http://112.91.151.10:7777/restaurant/images/2001.jpg");
			cm.setCategory("招牌菜");
			cm.setPrice(120);
			cm.setCode("DBY");
			
			getCourseDAO().create(cm);
			
			cm = new CourseModel();
			cm.setName("海鲈鱼");
			cm.setImgUrl("http://112.91.151.10:7777/restaurant/images/2001.jpg");
			cm.setCategory("招牌菜");
			cm.setPrice(90);
			cm.setCode("HLY");
			
			getCourseDAO().create(cm);
		} catch (SQLException e) {
			Log.e(CourseDBOpenHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}
        
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			Log.i(CourseDBOpenHelper.class.getName(), "onUpgrade");
			TableUtils.dropTable(connectionSource, CourseModel.class, true);
			// after we drop the old databases, we create the new ones
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Log.e(CourseDBOpenHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
		
	}
	
    public Dao<CourseModel, Integer> getCourseDAO() throws SQLException{
    	if(postDao == null){
    		postDao = getDao(CourseModel.class);
    	}
    	return postDao;
    }
    
    @Override
	public void close() {
		super.close();
		postDao = null;
	}

}
