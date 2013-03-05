package com.soundsofpolaris.timeline.ui;

import java.util.ArrayList;
import java.util.EventObject;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

public class ToggleGroup extends LinearLayout implements View.OnClickListener{
	
	private ArrayList<OnToggleListener> listeners;
	private ArrayList<CheckBox> checkboxes;
	private int currentSelectedId = -1;
	
	public ToggleGroup(Context context) {
		super(context);
	}

	public ToggleGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public interface OnToggleListener{
		void onToggle(CheckBox checkedBox);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		listeners = new ArrayList<OnToggleListener>();
		checkboxes = new ArrayList<CheckBox>();
		
		for (int i = 0, n = this.getChildCount(); i < n; i++) {
			if(this.getChildAt(i) instanceof CheckBox){
				CheckBox check = (CheckBox) this.getChildAt(i);
				check.setOnClickListener(this);
				checkboxes.add(check);
			}
		}
	}

	@Override
	public void onClick(View v) {
		if(currentSelectedId == v.getId()){
			for (int i = 0, n = checkboxes.size(); i < n; i++) {
				checkboxes.get(i).setSelected(false);
				currentSelectedId = -1;
				
				handleToggle(null);
			}
		} else {
			for (int i = 0, n = checkboxes.size(); i < n; i++) {
				CheckBox cb = checkboxes.get(i);
				if(cb.getId() == v.getId()){
					currentSelectedId = cb.getId();
					cb.setSelected(true);
					
					handleToggle((CheckBox) v);
				} else {
					cb.setSelected(false);
				}
			}
		}
	}
	
	public void setChecked(View v){
		onClick(v);
	}
	
	public void setToggleListener(OnToggleListener tl){
		listeners.add(tl);
	}
	
	public void handleToggle(CheckBox checkedBox){
		for (OnToggleListener listener : listeners) {
			listener.onToggle(checkedBox);
		}
	}
}
