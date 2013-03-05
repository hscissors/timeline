package com.soundsofpolaris.timeline.ui;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.soundsofpolaris.timeline.Constants;
import com.soundsofpolaris.timeline.ui.Picker.PickerListener;
import com.soundsofpolaris.timeline.R;
import com.soundsofpolaris.timeline.R.id;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.LinearLayout;

public class CustomDatePicker extends LinearLayout implements PickerListener, ToggleGroup.OnToggleListener{
	
	private GregorianCalendar calendar; 
	
	private Picker month;
	private Picker day;
	private Picker year;
	private Picker era;
	
	private ToggleGroup tg;
	
	private Boolean allyear = false;
	private Boolean allmonth = false;
	
	public CustomDatePicker(Context context) {
		super(context);
		setupView();
	}
	
	public CustomDatePicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupView();
	}
	
	protected void onFinishInflate() {
		super.onFinishInflate();
	}
	
	protected void setupView(){
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.custom_date_picker, this, true);
		
		month = (Picker) this.findViewById(id.datepicker_month);
		day = (Picker) this.findViewById(id.datepicker_day);
		year = (Picker) this.findViewById(id.datepicker_year);
		era = (Picker) this.findViewById(id.datepicker_era);
		
		tg = (ToggleGroup) this.findViewById(id.datepicker_toggle_group);
		tg.setToggleListener(this);
		
		month.setPickerListener(this);
		day.setPickerListener(this);
		year.setPickerListener(this);
		era.setPickerListener(this);
		
		month.setInputType(InputType.TYPE_CLASS_TEXT);
		day.setInputType(InputType.TYPE_CLASS_NUMBER);
		year.setInputType(InputType.TYPE_CLASS_NUMBER);
		
		GregorianCalendar temp = new GregorianCalendar();
		calendar = new GregorianCalendar(temp.get(Calendar.YEAR), temp.get(Calendar.MONTH), temp.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		calendar.setLenient(true);
		
		updateFields();
	}
	
	private void updateFields(){
		month.updateValue(calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US));
		day.updateValue(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
		year.updateValue(String.valueOf(calendar.get(Calendar.YEAR)));
		era.updateValue(calendar.getDisplayName(Calendar.ERA, Calendar.SHORT, Locale.US));
	}
	
	public Long getTimeinMills(){
		return calendar.getTimeInMillis(); // + 59918400000000L;
	}
	
	public int getYear(){
		Log.i(Constants.LOG, "CustomDatePicker.getYear() " + calendar.get(Calendar.YEAR));
		Log.i(Constants.LOG, "CustomDatePicker.getYear() - mills " + calendar.getTimeInMillis());
		int era = 0;
		if(calendar.get(Calendar.ERA) == GregorianCalendar.AD){
			era = 1;
		} else {
			era = -1;
		}
		return calendar.get(Calendar.YEAR) * era;
	}
	
	public int getMonth(){
		return calendar.get(Calendar.MONTH);
	}
	
	public Boolean isAllYear(){
		return allyear;
	}
	
	public Boolean isAllMonth(){
		return allmonth;
	}
	
	public void setTimeinMills(Long time){
		calendar.setTimeInMillis(time);
		Log.i(Constants.LOG, "CustomDatePicker.setTimeinMills " + getTimeinMills());
		updateFields();
	}
	
	public void setDate(int y, int m, int d){
		calendar.set(y, m, d);
		updateFields();
	}
	
	public void setAllMonth(Boolean checked){
		if(checked){
			tg.setChecked((CheckBox) findViewById(R.id.check_all_month));
		};
	}
	
	public void setAllYear(Boolean checked){
		if(checked){
			tg.setChecked((CheckBox) findViewById(R.id.check_all_year));
		};
	}
	
	@Override
	public void onToggle(CheckBox changedBox) {
		day.enable();
		month.enable();
		year.enable();
		
		allyear = false;
		allmonth = false;
		if(changedBox != null){
			switch (changedBox.getId()) {
			case R.id.check_all_year:
				day.disable();
				month.disable();
				allyear = true;
				break;
			case R.id.check_all_month:
				day.disable();
				
				allmonth = true;
				break;
			default:
				break;
			}
		} 
	}

	@Override
	public void onPlus(Picker p) {
		switch (p.getId()) {
		case R.id.datepicker_month:
			calendar.roll(Calendar.MONTH, 1);
			break;
		case R.id.datepicker_day:
			calendar.roll(Calendar.DAY_OF_MONTH, 1);
			break;
		case R.id.datepicker_year:
			calendar.roll(Calendar.YEAR, 1);
			break;
		case R.id.datepicker_era:
			calendar.roll(Calendar.ERA, 1);
			break;
		default:
			break;
		}
		
		updateFields();
		//Log.i(Constants.LOG, "Current date: " + getTimeinMills());
	}

	@Override
	public void onMinus(Picker p) {
		switch (p.getId()) {
		case R.id.datepicker_month:
			calendar.roll(Calendar.MONTH, -1);
			break;
		case R.id.datepicker_day:
			calendar.roll(Calendar.DAY_OF_MONTH, -1);
			break;
		case R.id.datepicker_year:
			calendar.roll(Calendar.YEAR, -1);
			break;
		case R.id.datepicker_era:
			calendar.roll(Calendar.ERA, -1);
			break;
		default:
			break;
		}
		
		updateFields();
		//Log.i(Constants.LOG, "Current date: " + getTimeinMills());
	}

	@Override
	public void onFocusChange(Picker p, String s) {
		int i;
		try{
			i = Integer.parseInt(s);
			i--; //correct for 0-based month index;
		} catch(NumberFormatException nfe){
			i = -1;
		}
		
		switch (p.getId()) {
		case R.id.datepicker_month:
			if(i == -1){
				try{
					i = calendar.getDisplayNames(Calendar.MONTH, Calendar.SHORT, Locale.US).get(s);
				} catch(Exception e){
					p.resetValue();
					break;
				}
			} 
			if(i >= calendar.getMinimum(Calendar.MONTH) && i <= calendar.getMaximum(Calendar.MONTH)){
				calendar.set(Calendar.MONTH, i);
			} else {
				p.resetValue();
			}
			break;
		case R.id.datepicker_day:
			if(i >= calendar.getMinimum(Calendar.DAY_OF_MONTH) && i <= calendar.getMaximum(Calendar.DAY_OF_MONTH)){
				calendar.set(Calendar.DAY_OF_MONTH, i);
			} else {
				p.resetValue();
			}
			break;
		case R.id.datepicker_year:
			if(i >= calendar.getMinimum(Calendar.YEAR) && i <= calendar.getMaximum(Calendar.YEAR)){
				calendar.set(Calendar.YEAR, i);
			} else {
				p.resetValue();
			}
			break;
		case R.id.datepicker_era:
			if(s.contentEquals("AD")){
				i = 1;
				calendar.set(Calendar.ERA, i);
			} else if(s.contentEquals("BC")){
				i = 0;
				calendar.set(Calendar.ERA, i);				
			} else {
				p.resetValue();
			}
			break;
		default:
			break;
		}
		
		updateFields();
	}
}
