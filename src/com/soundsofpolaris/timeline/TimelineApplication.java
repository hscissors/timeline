package com.soundsofpolaris.timeline;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.soundsofpolaris.timeline.database.DatabaseHelper;

import android.app.Application;
import android.view.ViewConfiguration;

public class TimelineApplication extends Application {
	private DatabaseHelper db;
	
	@Override
	public void onCreate() {
		super.onCreate();
		try {
	        ViewConfiguration config = ViewConfiguration.get(this);
	        Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
	        if(menuKeyField != null) {
	            menuKeyField.setAccessible(true);
	            menuKeyField.setBoolean(config, false);
	        }
	    } catch (Exception ex) {
	        // Ignore
	    }
		
		if(db == null){ 
			db = new DatabaseHelper(getApplicationContext());
			db.updateDatabase(); //1.2.0 -- Used to add new column and "fix" date column 
		}
	}
	
	public DatabaseHelper getDb(){
		return db;
	}
	

}
