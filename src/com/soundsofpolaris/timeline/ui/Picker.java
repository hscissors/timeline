package com.soundsofpolaris.timeline.ui;

import com.soundsofpolaris.timeline.Constants;
import com.soundsofpolaris.timeline.R;
import com.soundsofpolaris.timeline.R.id;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.LayoutInflater;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class Picker extends LinearLayout implements View.OnClickListener, OnFocusChangeListener{
	
	private PickerListener listener;
	
	private ImageButton plus;
	private EditText value;
	private ImageButton minus;
	
	private int currentIndx = 0;
	private String resetVal = "";

	public Picker(Context context) {
		super(context);
		setupView();
	}
	
	public Picker(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupView();
	}
	
	private void setupView(){
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.picker, this, true);
		
		plus = (ImageButton) this.findViewById(id.picker_plus);
		value = (EditText) this.findViewById(id.picker_value_field);
		minus = (ImageButton) this.findViewById(id.picker_minus);
		
		plus.setOnClickListener(this);
		minus.setOnClickListener(this);
		value.setOnFocusChangeListener(this);
	}
	
	public void setInputType(int i){
		value.setInputType(i);
	}
	
	public void setPickerListener(PickerListener l){
		listener = l;
	}
	
	public void updateValue(String s){
		value.setText(s);
	}
	
	private void plus(){
		currentIndx++;
		listener.onPlus(this);
	}
	
	private void minus(){
		currentIndx--;
		listener.onMinus(this);
	}
	
	public int currentIndex(){
		return currentIndx;
	}
	
	public String currentValue(){
		return value.getText().toString();
	}
	
	public void resetValue(){
		value.setText(resetVal);
	}
	
	public void enable(){
		plus.setEnabled(true);
		value.setEnabled(true);
		minus.setEnabled(true);
	}
	
	public void disable(){
		plus.setEnabled(false);
		value.setEnabled(false);
		minus.setEnabled(false);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.picker_plus:
				plus();
			break;
		case R.id.picker_minus:
				minus();
			break;
		default:
			break;
		}		
	}
	
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		Log.i(Constants.LOG, "Focus");
		if(hasFocus){
			resetVal = value.getText().toString();
			Log.i(Constants.LOG, "Current value: " + resetVal);
		} else{
			listener.onFocusChange(this, value.getText().toString());
		}
	}

	public interface PickerListener{
		void onPlus(Picker p);
		void onMinus(Picker p);
		void onFocusChange(Picker p, String v);
	}
}
