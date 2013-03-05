package com.soundsofpolaris.timeline.animation;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class HorizontalAnimation extends Animation{
	int targetWidth;
    View view;
    boolean open;

    public HorizontalAnimation(View view, int targetWidth, boolean open) {
        this.view = view;
        this.targetWidth = targetWidth;
        this.open = open;
        
        this.setDuration(100);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        int newWidth;
        if (open) {
        	newWidth = (int) (targetWidth * interpolatedTime);
        } else {
        	newWidth = (int) (targetWidth * (1 - interpolatedTime));
        }
        view.getLayoutParams().width = newWidth;
        view.requestLayout();
    }

    @Override
    public void initialize(int width, int height, int parentWidth,
            int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}
