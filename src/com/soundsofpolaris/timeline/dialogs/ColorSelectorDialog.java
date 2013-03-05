package com.soundsofpolaris.timeline.dialogs;

import com.larswerkman.colorpicker.ColorPicker;
import com.larswerkman.colorpicker.SVBar;
import com.soundsofpolaris.timeline.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;

public class ColorSelectorDialog extends DialogFragment {
	
	ColorPicker cp;
	int selectedColor;
	
	public ColorSelectorDialog(){

	}
	
	public interface ColorSelectorListener{
		void onColorSelectedFinished(int selectedColor);
	}
	
	public void setColor(int color){
		selectedColor = color;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle context) {
		ContextThemeWrapper themedContext = new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Dialog_NoActionBar);
		AlertDialog.Builder builder = new AlertDialog.Builder(themedContext);
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		View view = inflater.inflate(R.layout.custom_color_selector, null);
		cp = (ColorPicker) view.findViewById(R.id.picker);
        SVBar svb = (SVBar) view.findViewById(R.id.svbar);
        cp.addSVBar(svb);
        
		cp.setOldCenterColor(selectedColor);
		
		builder.setView(view)
				.setPositiveButton(R.string.color_selector_positive, new DialogInterface.OnClickListener() {
		               @Override
		               public void onClick(DialogInterface dialog, int id) {
		                  ((ColorSelectorListener) getActivity()).onColorSelectedFinished(cp.getColor());
		               }
		           })
		          .setNegativeButton(R.string.color_selector_negative, new DialogInterface.OnClickListener() {
		               @Override
		               public void onClick(DialogInterface dialog, int id) {
		            	   ((ColorSelectorListener) getActivity()).onColorSelectedFinished(selectedColor);
		               }
		           });
		
		return builder.create();           
	
	}
}
