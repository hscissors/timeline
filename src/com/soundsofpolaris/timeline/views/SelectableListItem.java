package com.soundsofpolaris.timeline.views;

import com.soundsofpolaris.timeline.animation.HorizontalAnimation;
import com.soundsofpolaris.timeline.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class SelectableListItem extends LinearLayout {

	private int maxX = 40;
	private View selector;
		
	public SelectableListItem(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		selector = this.findViewById(R.id.list_item_selector);
	}
	
	public void setSelected(boolean isSelected){
		
		if(isSelected){
			selector.getLayoutParams().width = maxX;
		} else {
			selector.getLayoutParams().width = 0;
		}
		
		selector.requestLayout();
		
		this.findViewById(R.id.list_item_body).setSelected(isSelected);
	}
	
	public void animateSelected(boolean isSelected){
		selector.startAnimation(new HorizontalAnimation(selector, maxX, isSelected));
		this.findViewById(R.id.list_item_body).setSelected(isSelected);
	}
}