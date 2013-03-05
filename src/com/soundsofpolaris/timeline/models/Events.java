package com.soundsofpolaris.timeline.models;

import java.text.SimpleDateFormat;

public class Events {
	private int id;
	private int year;
	private int month;
	private long date;
	private String title;
	private String desc;
	private int groupId;
	private int groupColor;
	private String groupName;
	private Boolean isAllYear;
	private Boolean isAllMonth;
	
	public Events(int id, int year, int month, long date, String title, String desc, int isAllYear, int isAllMonth, int groupId, int groupColor, String groupName){
		this.id = id;
		this.year = year;
		this.month = month;
		this.date = date;
		this.title = title;
		this.desc = desc;
		this.isAllYear = (isAllYear == 1? true : false);
		this.isAllMonth = (isAllMonth == 1? true : false);
		this.groupId = groupId;
		this.groupColor = groupColor;
		this.groupName = groupName;
	}
	
	public int getId(){
		return id;
	}
	
	public String getTitle(){
		return title;
	}
	
	public String getDescription(){
		return desc;
	}
	
	public int getYear(){
		return year;
	}
	
	public String getPrettyDate(){
		SimpleDateFormat sf;
		if(isAllYear){
			sf = new SimpleDateFormat("y");
		} else if (isAllMonth) {
			sf = new SimpleDateFormat("MMMMM");
		} else {
			sf = new SimpleDateFormat("MMMMM dd");
		}
		return sf.format(date);
	}
	
	public long getUnixDate(){
		return date;
	}
	
	public int getGroupId(){
		return this.groupId;
	}
	
	public String getGroupName(){
		return this.groupName;
	}
	
	public int getGroupColor(){
		return this.groupColor;
	}
	
	public Boolean isAllYear(){
		return this.isAllYear;
	}
	
	public Boolean isAllMonth(){
		return this.isAllMonth;
	}
	
	@Override
	public String toString(){
		return this.title + ", " + this.getYear() + ", " + this.date + ", " + ", allyear?" + this.isAllYear + ", allmonth?" + this.isAllMonth;
	}
}
