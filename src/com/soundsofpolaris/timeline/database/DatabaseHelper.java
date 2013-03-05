package com.soundsofpolaris.timeline.database;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.soundsofpolaris.timeline.Constants;
import com.soundsofpolaris.timeline.models.Events;
import com.soundsofpolaris.timeline.models.Groups;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Pair;

public class DatabaseHelper extends SQLiteOpenHelper{
	
	public final static int DB_VERSION	= 15;
	public final static String DB_NAME 	= "timeline.db";
	public final static String EVENTS_TABLE = "eventstable";
	public final static String ID_COL = "id";
	public final static String YEAR_COL = "year";
	public final static String MONTH_COL = "month";
	public final static String DATE_COL = "date";
	public final static String TITLE_COL = "title";
	public final static String DESC_COL = "desc";
	public final static String OLD_DATE_COL = "olddate";
	public final static String ALLYEAR_COL = "allyear";
	public final static String ALLMONTH_COL = "allmonth";
	
	public final static String GROUP_ID_COL = "gid";
	
	public final static String GROUP_TABLE = "groupstable";
	public final static String GROUP_NAME_COL = "gname";
	public final static String GROUP_COLOR_COL = "gcolor";
	
	public final static String GROUP_TO_GROUP_TABLE = "gtgtable";
	public final static String GTG_ID = "gtgid";
	public final static String GTG_PARENT_ID = "gtgparentid";
	public final static String GTG_LINKED_ID = "gtglinkedid";
	
	public final static int EXPORT_ALREADY_FILE_EXISTS = 1;
	public final static int EXPORT_WRITE_ERROR = 2;
	public final static int EXPORT_SD_NOT_MOUNTED = 3;
	
	public final static int IMPORT_FILE_NOT_FOUND = 1;
	public final static int IMPORT_READ_ERROR = 2;
	public final static int COULD_NOT_CLOSE_READER = 3;
	
	private Boolean init = false;
	
	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		if(init){
			Log.e(Constants.LOG, "DATABASEHELPER ALREADY INIT!");
		} else {
			init = true;
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(RawQueries.CREATE_EVENTS_TABLE());	
		db.execSQL(RawQueries.CREATE_GROUP_TABLE());	
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.v(Constants.LOG, "upgrade DB!");
		db.execSQL(RawQueries.DROP_EVENTS_TABLE());
		db.execSQL(RawQueries.DROP_GROUPS_TABLE());
		onCreate(db);
	}
	
	public void updateDatabase(){
		try{
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL("ALTER TABLE " + EVENTS_TABLE + " ADD COLUMN " + OLD_DATE_COL + " INTEGER"); 
			Cursor c = db.rawQuery("SELECT * FROM " + EVENTS_TABLE, null);
			if(c.moveToNext()){
				do {
					int id = c.getInt(c.getColumnIndex(ID_COL));
					long oldyear = c.getLong(c.getColumnIndex(DATE_COL));
					long fixforyear = 59958208800000L; //1900 see: class Date(year, month, day)
					long newyear = oldyear - fixforyear;
					ContentValues cv = new ContentValues();
					cv.put(DATE_COL, newyear);
					cv.put(OLD_DATE_COL, oldyear);
					db.update(EVENTS_TABLE, cv, ID_COL+"=?", new String []{String.valueOf(id)});
				} while (c.moveToNext());
			}
			db.close();
		} catch(Exception e){
			Log.i(Constants.LOG, "DATABASE ALREADY ALTERED! Old date scheme fixed");
		}
		
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL(RawQueries.CREATE_GROUP_TO_GROUP_TABLE());
			
			db.execSQL("ALTER TABLE " + EVENTS_TABLE + " ADD COLUMN " + ALLYEAR_COL + " INTEGER");
			db.execSQL("ALTER TABLE " + EVENTS_TABLE + " ADD COLUMN " + ALLMONTH_COL + " INTEGER");
			db.execSQL("ALTER TABLE " + EVENTS_TABLE + " ADD COLUMN " + MONTH_COL + " INTEGER");

			Cursor c = db.rawQuery("SELECT * FROM " + EVENTS_TABLE, null);
			if(c.moveToNext()){
				do {
					int id = c.getInt(c.getColumnIndex(ID_COL));
					Long date = c.getLong(c.getColumnIndex(DATE_COL));
					
					GregorianCalendar temp = new GregorianCalendar();
					GregorianCalendar calendar = new GregorianCalendar(temp.get(Calendar.YEAR), temp.get(Calendar.MONTH), temp.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
					calendar.setLenient(true);
					calendar.setTimeInMillis(date);
					
					ContentValues cv = new ContentValues();
					cv.put(ALLYEAR_COL, 0);
					cv.put(ALLMONTH_COL, 0);
					cv.put(MONTH_COL, calendar.get(Calendar.MONTH));
					db.update(EVENTS_TABLE, cv, ID_COL+"=?", new String []{String.valueOf(id)});
				} while (c.moveToNext());
			}
			db.close();
		} catch (Exception e) {
			Log.d(Constants.LOG, e.toString());
			Log.i(Constants.LOG, "DATABASE ALREADY ALTERED! Added linked, allyear, allmonth");
		}
		
		traceAllTableColumns();
	}
	
	public void traceAllTableColumns(){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.query(GROUP_TABLE, null, null, null, null, null, null);
        if (c != null) {
        	StringBuilder tabletrace = new StringBuilder();
        	tabletrace.append("TABLE: " + GROUP_TABLE + " -- ");
            int num = c.getColumnCount();
            for (int i = 0; i < num; ++i) {
            	tabletrace.append(c.getColumnName(i) + ", ");	
            }
            
            Log.d(Constants.LOG, tabletrace.toString());
        }
        
        c = db.query(EVENTS_TABLE, null, null, null, null, null, null);
        if (c != null) {
        	StringBuilder tabletrace = new StringBuilder();
        	tabletrace.append("TABLE: " + EVENTS_TABLE + " -- ");
            int num = c.getColumnCount();
            for (int i = 0; i < num; ++i) {
            	tabletrace.append(c.getColumnName(i) + ", ");
            }
            
            Log.d(Constants.LOG, tabletrace.toString());
        }
        
        c = db.query(GROUP_TO_GROUP_TABLE, null, null, null, null, null, null);
        if (c != null) {
        	StringBuilder tabletrace = new StringBuilder();
        	tabletrace.append("TABLE: " + GROUP_TO_GROUP_TABLE + " -- ");
            int num = c.getColumnCount();
            for (int i = 0; i < num; ++i) {
            	tabletrace.append(c.getColumnName(i) + ", ");
            }
            
            Log.d(Constants.LOG, tabletrace.toString());
        }
	}
	
	public int addGroup(String name, int color){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(GROUP_NAME_COL, name);
		cv.put(GROUP_COLOR_COL, color);
		long gid = db.insert(GROUP_TABLE, GROUP_ID_COL, cv);
		db.close();
		
		return (int) gid;
	}
	
	public void addEvent(int year, int month, long date, String title, String desc, Boolean isAllYear, Boolean isAllMonth, int groupId){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(YEAR_COL, year);
		cv.put(MONTH_COL, month);
		cv.put(DATE_COL, date);
		cv.put(TITLE_COL, title);
		cv.put(DESC_COL, desc);
		cv.put(ALLYEAR_COL, (isAllYear? 1 : 0));
		cv.put(ALLMONTH_COL, (isAllMonth? 1 : 0));
		cv.put(GROUP_ID_COL, groupId);
		db.insert(EVENTS_TABLE, ID_COL, cv);
		db.close();
	}
	
	public Events getEventById(int eid){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(RawQueries.GET_EVENT_BY_ID(eid), null);
		Events e = null;
		if(c.moveToNext()){
			do {
				int id = c.getInt(c.getColumnIndex(ID_COL));
				int year = c.getInt(c.getColumnIndex(YEAR_COL));
				int month = c.getInt(c.getColumnIndex(MONTH_COL));
				long date = c.getLong(c.getColumnIndex(DATE_COL));
				String title = c.getString(c.getColumnIndex(TITLE_COL));
				String desc = c.getString(c.getColumnIndex(DESC_COL));
				String groupName = c.getString(c.getColumnIndex(GROUP_NAME_COL));
				int isAllYear = c.getInt(c.getColumnIndex(ALLYEAR_COL));
				int isAllMonth = c.getInt(c.getColumnIndex(ALLMONTH_COL));
				int groupId = c.getInt(c.getColumnIndex(GROUP_ID_COL));
				int groupColor = c.getInt(c.getColumnIndex(GROUP_COLOR_COL));
				e = new Events(id, year, month, date, title, desc, isAllYear, isAllMonth, groupId, groupColor, groupName);
			} while (c.moveToNext());
		}
		
		db.close();
		
		return e;
	}
	
	public Groups getGroupById(int gid){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(RawQueries.GET_GROUP_BY_ID(gid), null);
		Groups g = null;
		if(c.moveToNext()){
			do {
				int groupId = c.getInt(c.getColumnIndex(GROUP_ID_COL));
				String groupName = c.getString(c.getColumnIndex(GROUP_NAME_COL));
				int groupColor = c.getInt(c.getColumnIndex(GROUP_COLOR_COL));
				g = new Groups(groupId, groupName, groupColor);
			} while (c.moveToNext());
		}
		
		db.close();
		
		return g;
	}
	
	public void updateEvent(int id, int year, int month, long date, String title, String desc, Boolean isAllYear, Boolean isAllMonth, int group){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(YEAR_COL, year);
		cv.put(MONTH_COL, month);
		cv.put(DATE_COL, date);
		cv.put(TITLE_COL, title);
		cv.put(DESC_COL, desc);
		cv.put(ALLYEAR_COL, (isAllYear? 1 : 0));
		cv.put(ALLMONTH_COL, (isAllMonth? 1 : 0));
		db.update(EVENTS_TABLE, cv, ID_COL+"=?", new String []{String.valueOf(id)});
		db.close();
	}
	
	public void updateGroup(int id, String name, int color){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(GROUP_NAME_COL, name);
		cv.put(GROUP_COLOR_COL, color);
		db.update(GROUP_TABLE, cv, GROUP_ID_COL+"=?", new String []{String.valueOf(id)});
		db.close();
	}
	
	public void deleteEvent(int eid){
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(EVENTS_TABLE, ID_COL+"=?", new String [] {String.valueOf(eid)});
		db.close();
	}
	
	public void deleteGroup(int gid){
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(GROUP_TABLE, GROUP_ID_COL+"=?", new String [] {String.valueOf(gid)});
		db.delete(EVENTS_TABLE, GROUP_ID_COL+"=?", new String [] {String.valueOf(gid)});
		db.delete(GROUP_TO_GROUP_TABLE, GTG_PARENT_ID+"=?", new String [] {String.valueOf(gid)});
		db.close();
	}
	
	public List<Groups> getAllGroupsWithExclusion(int exclusion){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(RawQueries.ALL_GROUPS(exclusion), null);
		
		ArrayList<Groups> groups = new ArrayList<Groups>();
		if(c.moveToNext()){
			do {
				int gid = c.getInt(c.getColumnIndex(GROUP_ID_COL));
				String gname = c.getString(c.getColumnIndex(GROUP_NAME_COL));
				int gcolor = c.getInt(c.getColumnIndex(GROUP_COLOR_COL));
				groups.add(new Groups(gid, gname, gcolor));
			} while (c.moveToNext());
		}
		db.close();
		
		return groups;
	}
	
	public List<Groups> getAllGroups(){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(RawQueries.ALL_GROUPS(), null);
		
		ArrayList<Groups> groups = new ArrayList<Groups>();
		if(c.moveToNext()){
			do {
				int gid = c.getInt(c.getColumnIndex(GROUP_ID_COL));
				String gname = c.getString(c.getColumnIndex(GROUP_NAME_COL));
				int gcolor = c.getInt(c.getColumnIndex(GROUP_COLOR_COL));
				groups.add(new Groups(gid, gname, gcolor));
			} while (c.moveToNext());
		}
		db.close();
		
		return groups;
	}
	
	public List<Pair<String, List<Events>>> getAllEventsByGroupSet(List<Integer> groups){
		StringBuilder SQLfiedGroup = new StringBuilder();
		for (int i = 0; i < groups.size(); i++) {
			if(i != groups.size() - 1){
				SQLfiedGroup.append("groupstable." + GROUP_ID_COL + " = " + groups.get(i) + " or ");
			} else{
				SQLfiedGroup.append("groupstable." + GROUP_ID_COL + " = " + groups.get(i));
			}
		}
		
		return getAllEventsByGroup(SQLfiedGroup.toString());
	}
	
	public List<Pair<String, List<Events>>> getAllEventsByGroup(String group){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(RawQueries.ALL_EVENTS_BY_GROUP(group), null);
		
		List<Pair<String, List<Events>>> events = parseEvents(c);
		db.close();
		
		return events;
	}
	
	
//	public List<Pair<String, List<Events>>> getAllEvents(){
//		SQLiteDatabase db = this.getReadableDatabase();
//		Cursor c = db.rawQuery(RawQueries.ALL_EVENTS(), null);
//		
//		List<Pair<String, List<Events>>> events = parseEvents(c);
//		db.close();
//		
//		return events;
//	}
	
	public List<Pair<String, List<Events>>> parseEvents(Cursor c){
		ArrayList<Events> le = new ArrayList<Events>();
		ArrayList<Pair<String, List<Events>>> years = new ArrayList<Pair<String, List<Events>>>();
		if(c.moveToNext()){
			do {
				int id = c.getInt(c.getColumnIndex(ID_COL));
				int year = c.getInt(c.getColumnIndex(YEAR_COL));
				int month = c.getInt(c.getColumnIndex(MONTH_COL));
				long date = c.getLong(c.getColumnIndex(DATE_COL));
				String title = c.getString(c.getColumnIndex(TITLE_COL));
				String desc = c.getString(c.getColumnIndex(DESC_COL));
				int isAllYear = c.getInt(c.getColumnIndex(ALLYEAR_COL));
				int isAllMonth = c.getInt(c.getColumnIndex(ALLMONTH_COL));
				String groupName = c.getString(c.getColumnIndex(GROUP_NAME_COL));
				int groupId = c.getInt(c.getColumnIndex(GROUP_ID_COL));
				int groupColor = c.getInt(c.getColumnIndex(GROUP_COLOR_COL));
				le.add(new Events(id, year, month, date, title, desc, isAllYear, isAllMonth, groupId, groupColor, groupName));
			} while (c.moveToNext());
			
			Log.d(Constants.LOG, "Raw array size@DatebaseHelper.getAllEvents(): " + le.size() ) ;
			
			for (int i = 0; i < le.size(); i++) { //For every item in the DB...
				String year = String.valueOf(le.get(i).getYear()); //Get the year for the item...
				Log.d(Constants.LOG, "Parse item: " + le.get(i).getYear()) ;
				Boolean found = false;
				if(years.size() > 0){
					for (int j = 0; j < years.size(); j++) { //Now for every year in the sorted list...
						if(years.get(j).first.equals(year) && years.get(j) != null){ 
							ArrayList<Events> events = (ArrayList<Events>) years.get(j).second;
							events.add(le.get(i));
							Log.d(Constants.LOG, "Add item to exsisting year: " + years.get(j).second + " which has a events array " + events.size());
							found = true;
						} 
					}
				} 
				
				if(years.size() == 0 || found == false){
					ArrayList<Events> events = new ArrayList<Events>();
					events.add(le.get(i));
					years.add(new Pair<String, List<Events>>(year, events));
					Log.d(Constants.LOG, "Add item to new year: " + year + " which has a events array " + events.size());
				}
			}
		}
		
		Log.d(Constants.LOG, "years size@DatebaseHelper.getAllEvents(): " + years.size() ) ;
		return years;
	}
	
	public void updateLinksToGroup(int parentGroupId, ArrayList<Integer> newlinkedGroupIds){
		if(newlinkedGroupIds.size() > 0){
			ArrayList<Integer> oldlinkedGroupIds = getLinksToGroup(parentGroupId);
			for (Integer link : oldlinkedGroupIds) {
				if(!newlinkedGroupIds.contains(link)){
					oldlinkedGroupIds.remove(link);
					deleteLinkToGroup(parentGroupId, link);
				}
			}
			
			for (Integer link : newlinkedGroupIds) {
				if(!oldlinkedGroupIds.contains(link)){
					addLinkToGroup(parentGroupId, link);
				}
			}
		} else {
			ArrayList<Integer> oldlinkedGroupIds = getLinksToGroup(parentGroupId);
			for (Integer link : oldlinkedGroupIds) {
				deleteLinkToGroup(parentGroupId, link);
			}
		}
	}
	
	public void addLinkToGroup(int parentGroupId, int linkedGroupId){
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues cv = new ContentValues();
		cv.put(GTG_PARENT_ID, parentGroupId);
		cv.put(GTG_LINKED_ID, linkedGroupId);
		db.insert(GROUP_TO_GROUP_TABLE, GTG_ID, cv);
		
		db.close();
		
		Log.d(Constants.LOG, "addLinkedGroup@DatebaseHelper.addLinkToGroup(): parent = " + parentGroupId+", link = " + linkedGroupId ) ;
	}
	
	public void deleteLinkToGroup(int parentGroupId, int linkedGroupId){
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(GROUP_TO_GROUP_TABLE, GTG_PARENT_ID + "=? AND " + GTG_LINKED_ID + "=?", new String [] {Integer.toString(parentGroupId), Integer.toString(linkedGroupId)});
		db.close();
		Log.d(Constants.LOG, "deleteLinkedGroup@DatebaseHelper.deleteLinkToGroup(): parent = " + parentGroupId+", link = " + linkedGroupId ) ;
	}
	
	public ArrayList<Integer> getRecursiveLinksToGroup(ArrayList<Integer> parentGroupId){
		HashSet<Integer> allLinked = new HashSet<Integer>();
		allLinked.addAll(parentGroupId);
		
		ArrayList<Integer> nextParentGroupIds = new ArrayList<Integer>();
		
		ArrayList<Integer> checkingParentGroupIds = new ArrayList<Integer>();
		checkingParentGroupIds.addAll(parentGroupId);		
		
		do {
			ArrayList<Integer> links = getLinksToGroup(checkingParentGroupIds);
			for (Integer link : links) {
				if(!allLinked.contains(link)){
					allLinked.add(link);
					nextParentGroupIds.add(link);
				}
			}
			checkingParentGroupIds = nextParentGroupIds;
			nextParentGroupIds.clear();
		} while (checkingParentGroupIds.isEmpty() == false);
				
		return new ArrayList<Integer>(allLinked);
	}
	
	public ArrayList<Integer> getLinksToGroup(int parentGroupId){
		ArrayList<Integer> tempIntArray = new ArrayList<Integer>();
		tempIntArray.add(parentGroupId);
		
		return getLinksToGroup(tempIntArray);
	}
	
	public ArrayList<Integer> getLinksToGroup(ArrayList<Integer> parentGroupIds){
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor c = db.rawQuery(RawQueries.GET_LINKED_GROUP_IDS(parentGroupIds), null);
		
		ArrayList<Integer> linkedGroupsIds = new ArrayList<Integer>();
		
		if(c.moveToNext()){
			do {
				int linkedid = c.getInt(c.getColumnIndex(GTG_LINKED_ID));
				linkedGroupsIds.add(linkedid);
			} while (c.moveToNext());
		}
		
		return linkedGroupsIds;
	}
	
	//check number of linked children
	
	public int exportGroupToFile(Boolean forFlashCards, int gid, String filename){
//		
//		if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
//		    return EXPORT_SD_NOT_MOUNTED;
//		}
//		
//		final File csv = new File(filename);
//		
//		if(csv.exists()){
//			return EXPORT_ALREADY_FILE_EXISTS;
//		}
//		
//		if(forFlashCards){
//			File dir = csv.getParentFile();
//			dir.mkdirs();
//		}
//		
//		
//		SQLiteDatabase db = this.getReadableDatabase();
//		Cursor c = db.rawQuery(RawQueries.ALL_EVENTS_BY_GROUP("groupstable." + GROUP_ID_COL + " = " + gid), null);
//		
//		try {
//			CSVWriter writer = new CSVWriter(new FileWriter(filename));
//			if(!forFlashCards){
//				c.moveToFirst();
//				String gname = "";
//				gname = c.getString(c.getColumnIndex(GROUP_NAME_COL));
//				
//				String gcolor = "";
//				gcolor = c.getString(c.getColumnIndex(GROUP_COLOR_COL));
//				
//				String[] groupDesc = {gname, gcolor};
//				
//				writer.writeNext(groupDesc);
//				
//				String titles[] = new String[5];
//				titles[0] = c.getColumnName(c.getColumnIndex(ID_COL));
//				titles[1] = c.getColumnName(c.getColumnIndex(YEAR_COL));
//				titles[2] = c.getColumnName(c.getColumnIndex(DATE_COL));
//				titles[3] = c.getColumnName(c.getColumnIndex(TITLE_COL));
//				titles[4] = c.getColumnName(c.getColumnIndex(DESC_COL));
//				writer.writeNext(titles);
//			}
//			
//			c.moveToFirst();
//			do{
//				if(forFlashCards){
//					String row[] = new String[2];
//					Date d = new Date(c.getLong(c.getColumnIndex(DATE_COL)));
//					SimpleDateFormat sf = new SimpleDateFormat("MMMMM dd, yyyy");
//					row[0] = sf.format(d).toString();
//					row[1] = c.getString(c.getColumnIndex(TITLE_COL)) + " - " + c.getString(c.getColumnIndex(DESC_COL));	
//					writer.writeNext(row);
//				}
//				else{
//					String row[] = new String[5];
//					row[0] = c.getString(c.getColumnIndex(ID_COL));
//					row[1] = c.getString(c.getColumnIndex(YEAR_COL));
//					row[2] = String.valueOf(c.getLong(c.getColumnIndex(DATE_COL)));
//					row[3] = c.getString(c.getColumnIndex(TITLE_COL));
//					row[4] = c.getString(c.getColumnIndex(DESC_COL));
//					writer.writeNext(row);
//				}
//			} while(c.moveToNext()); 
//			
//			db.close();
//			c.close();
//			writer.close();
//			
//			return 0;
//		} catch (IOException e) {
			return EXPORT_WRITE_ERROR;
//		}
	}
	
	public int importFile(String path) {	
//		File csv = new File(path);
//		if(!csv.exists()) {
//			return IMPORT_FILE_NOT_FOUND;
//		} 
//		
//		CSVReader reader;
//		try {
//			reader = new CSVReader(new FileReader(csv));
//		} catch (FileNotFoundException e) {
//			return IMPORT_FILE_NOT_FOUND;
//
//		}
//		
//		try {
//			List<String[]> entries = reader.readAll();
//			if(entries.size() > 2){
//				String[] columnNames = entries.get(1);
//				HashMap<String, Integer> columnIndex = new HashMap<String, Integer>(columnNames.length);
//				int index = 0;
//				for (String string : columnNames) {
//					columnIndex.put(string, index);
//					index++;
//				}
//				
//				String groupName = entries.get(0)[0];
//				int groupColor = Integer.parseInt(entries.get(0)[1]);
//				
//				long gid = -1;
//				if(groupName.length() > 0 && groupColor != 0){
//					gid = this.addGroup(groupName, groupColor);
//				}
//				
//				if(gid != -1){
//					for (int i = 2; i < entries.size(); i++) {
//						String row[] = entries.get(i);
//						int year = Integer.parseInt(row[columnIndex.get(YEAR_COL)]);
//						long date = Long.parseLong(row[columnIndex.get(DATE_COL)]);
//						String title = row[columnIndex.get(TITLE_COL)];
//						String desc = row[columnIndex.get(DESC_COL)];
//						this.addEvent(year, date, title, desc, (int) gid);
//					}
//				}
//				reader.close();
//				return 0;
//			} 
//		} catch (IOException e) {
//			return IMPORT_READ_ERROR;
//		}
//		try {
//			reader.close();
//		} catch (IOException e) {
//			return COULD_NOT_CLOSE_READER;
//		}
		return IMPORT_READ_ERROR;
	}
}
