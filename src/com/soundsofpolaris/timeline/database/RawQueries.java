package com.soundsofpolaris.timeline.database;

import java.util.ArrayList;

public class RawQueries {
	public static final String CREATE_EVENTS_TABLE(){
		return 	"CREATE TABLE "+ DatabaseHelper.EVENTS_TABLE +" (" 					+
					DatabaseHelper.ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, " 	+
					DatabaseHelper.YEAR_COL + " INTEGER NOT NULL, "					+
					DatabaseHelper.DATE_COL + " INTEGER NOT NULL, "					+
					DatabaseHelper.TITLE_COL + " TEXT NOT NULL, " 					+
					DatabaseHelper.DESC_COL + " TEXT NOT NULL, " 					+
					DatabaseHelper.GROUP_ID_COL + " INTERGER NOT NULL);";
	}
	
	public static final String CREATE_GROUP_TABLE(){
		return 	"CREATE TABLE "+ DatabaseHelper.GROUP_TABLE +" ("					+
				DatabaseHelper.GROUP_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
				DatabaseHelper.GROUP_NAME_COL + " TEXT NOT NULL, "					+
				DatabaseHelper.GROUP_COLOR_COL + " INTEGER NOT NULL);";	
	}
	
	public static final String CREATE_GROUP_TO_GROUP_TABLE(){
		return "CREATE TABLE " + DatabaseHelper.GROUP_TO_GROUP_TABLE + " ("			+
				DatabaseHelper.GTG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "		+
				DatabaseHelper.GTG_PARENT_ID + " INTEGER NOT NULL, "				+
				DatabaseHelper.GTG_LINKED_ID + " INTEGER NOT NULL);";
	}
	
	public static final String DROP_EVENTS_TABLE(){
		return "DROP TABLE IF EXISTS " + DatabaseHelper.EVENTS_TABLE;
	}
	
	public static final String DROP_GROUPS_TABLE(){
		return "DROP TABLE IF EXISTS " + DatabaseHelper.GROUP_TABLE;
	}
	
//	public static final String ALL_EVENTS(){
//		return "SELECT * FROM eventstable INNER JOIN groupstable ON eventstable.gid = groupstable.gid ORDER BY " + DatabaseHelper.YEAR_COL + " ASC, " + DatabaseHelper.DATE_COL + " ASC, " + DatabaseHelper.ALLYEAR_COL + " DESC, " + DatabaseHelper.ALLMONTH_COL + " DESC";
//	}
	
	public static final String ALL_EVENTS_BY_GROUP(String group){
		return "SELECT * FROM eventstable INNER JOIN groupstable ON eventstable.gid = groupstable.gid WHERE " + group + " ORDER BY " + DatabaseHelper.YEAR_COL + " ASC, " + DatabaseHelper.ALLYEAR_COL + " DESC, " + DatabaseHelper.MONTH_COL + " ASC, " + DatabaseHelper.ALLMONTH_COL + " DESC," + DatabaseHelper.DATE_COL + " ASC";
	}
	
	public static final String GET_EVENT_BY_ID(int id){
		return "SELECT * FROM eventstable INNER JOIN groupstable ON eventstable.gid = groupstable.gid WHERE id = " + id;
	}
	
	public static final String GET_GROUP_BY_ID(int gid){
		return "SELECT * FROM groupstable WHERE gid = " + gid;
	}

	public static final String ALL_GROUPS(){
		return "SELECT * FROM groupstable ORDER BY gname ASC";
	}
	
	public static final String ALL_GROUPS(int exclusion){
		return "SELECT * FROM groupstable WHERE gid <> "+ exclusion +" ORDER BY gname ASC";
	}
	
	public static final String GET_LINKED_GROUP_IDS(ArrayList<Integer> parentGroupIds){
		StringBuilder parents = new StringBuilder();
		for (int i = 0; i < parentGroupIds.size(); i++) {
			if(i != parentGroupIds.size() - 1){
				parents.append(DatabaseHelper.GTG_PARENT_ID + " = " + parentGroupIds.get(i) + " or ");
			} else {
				parents.append(DatabaseHelper.GTG_PARENT_ID + " = " + parentGroupIds.get(parentGroupIds.size() - 1));

			}
		}
		
		return "SELECT * FROM " + DatabaseHelper.GROUP_TO_GROUP_TABLE + " WHERE " + parents.toString();  
	}
}
