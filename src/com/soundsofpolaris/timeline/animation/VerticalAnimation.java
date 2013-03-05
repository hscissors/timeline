package com.soundsofpolaris.timeline.animation;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class VerticalAnimation extends Animation{
	int targetHeight;
    View view;
    boolean open;

    public VerticalAnimation(View view, int targetHeight, boolean open) {
        this.view = view;
        this.targetHeight = targetHeight;
        this.open = open;
        
        this.setDuration(300);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        int newHeight;
        if (open) {
        	newHeight = (int) (targetHeight * interpolatedTime);
        } else {
        	newHeight = (int) (targetHeight * (1 - interpolatedTime));
        }
        view.getLayoutParams().height = newHeight;
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
