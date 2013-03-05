package com.soundsofpolaris.timeline.activities;

import java.util.ArrayList;
import java.util.Random;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.soundsofpolaris.timeline.Constants;
import com.soundsofpolaris.timeline.TimelineApplication;
import com.soundsofpolaris.timeline.dialogs.ColorSelectorDialog;
import com.soundsofpolaris.timeline.dialogs.ColorSelectorDialog.ColorSelectorListener;
import com.soundsofpolaris.timeline.models.Groups;
import com.soundsofpolaris.timeline.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class GroupAddEditActivity extends BaseActivity implements View.OnClickListener, ColorSelectorListener{
	private int mode = 0;
	private Groups g = null;
 	protected int selectedColor = -1;
 	ArrayList<Integer> selectedGroupIds;
	
 	public GroupAddEditActivity() {
		if(selectedColor == -1){
			Random rand = new Random();
			selectedColor = Color.argb(255, rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
		}
	}
 	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mode = getIntent().getIntExtra("mode", Constants.ADD_TIMELINE);
		
		setContentView(R.layout.group_add_edit_activity);
		
		selectedGroupIds = new ArrayList<Integer>();
		
		switch (mode) {
			case Constants.EDIT_TIMELINE:
				int id = getIntent().getIntExtra("id", 0);
				g = ((TimelineApplication)getApplication()).getDb().getGroupById(id);
				
				selectedColor = g.getColor();
				
				EditText name = (EditText) findViewById(R.id.groupTitle);
				name.setText(g.getName());
				
				selectedGroupIds = ((TimelineApplication)getApplication()).getDb().getLinksToGroup(g.getId());
		
				break;
			case Constants.ADD_TIMELINE:
			default:
			break;
		}
		
		View preview = (View) findViewById(R.id.swatchPreview);
		preview.setBackgroundColor(selectedColor);
		
		ImageButton swatchBtn = (ImageButton) this.findViewById(R.id.swatch);
		swatchBtn.setOnClickListener(this);
		
		Button editLinkedBtn = (Button) this.findViewById(R.id.group_add_linked);
		editLinkedBtn.setOnClickListener(this);
	}
	
	@Override
	public void onBackPressed() {
		this.finish();
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.save_menu, (com.actionbarsherlock.view.Menu) menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.menu_save:
				EditText groupTitle = (EditText) this.findViewById(R.id.groupTitle);
				if(groupTitle.getText().length() == 0) {
					Toast.makeText(this, R.string.alert_group_title, Toast.LENGTH_LONG).show();
				} else{
					if(mode == Constants.EDIT_TIMELINE){
						((TimelineApplication) getApplication()).getDb().updateGroup(g.getId(), groupTitle.getText().toString(), selectedColor);
						((TimelineApplication) getApplication()).getDb().updateLinksToGroup(g.getId(), selectedGroupIds);
						setResult(Constants.EDIT_TIMELINE_CONFIRM);
					} else if(mode == Constants.ADD_TIMELINE){
						int newId = ((TimelineApplication) getApplication()).getDb().addGroup(groupTitle.getText().toString(), selectedColor);
						((TimelineApplication) getApplication()).getDb().updateLinksToGroup(newId, selectedGroupIds);
						setResult(Constants.ADD_TIMELINE_CONFIRM);
					}
					
					InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
					inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					
					finish();
				}
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
			case R.id.swatch:
				FragmentManager fm = getSupportFragmentManager();
				ColorSelectorDialog colorSelectorDialog = new ColorSelectorDialog();
				colorSelectorDialog.setColor(selectedColor);
				//colorSelectorDialog.setStyle(DialogFragment.STYLE_NO_TITLE, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
				colorSelectorDialog.show(fm, "event_color_selector_dialog");
				break;
			case R.id.group_add_linked:
				Intent i = new Intent(this, GroupListActivity.class);
				i.putExtra("mode", Constants.SELECT_TIMELINES);
				if(selectedGroupIds.size() > 0){
					Bundle selected = new Bundle();
		        	selected.putIntegerArrayList("selectedGroupsIds", selectedGroupIds);
		        	i.putExtras(selected);
				}
				if(mode == Constants.EDIT_TIMELINE){
					i.putExtra("parentId", g.getId());
				}
				startActivityForResult(i, Constants.SELECT_TIMELINES);
				break;
			default:
				break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent i) {
		if(requestCode == Constants.SELECT_TIMELINES){
			if(resultCode == Constants.SELECT_TIMELINES_CONFIRM){
				selectedGroupIds = i.getExtras().getIntegerArrayList("selectedGroupsIds");
				if(selectedGroupIds.size() > 0){
					String pluralMaker = selectedGroupIds.size() <= 1? " is": "s are";
					Toast.makeText(this, selectedGroupIds.size() + " timeline" + pluralMaker + " now linked!", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(this, "No timelines are now linked.", Toast.LENGTH_LONG).show();
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, i);
	}
	
	@Override
	public void onColorSelectedFinished(int selectedColor) {
		this.selectedColor = selectedColor;	
		View preview = (View) findViewById(R.id.swatchPreview);
		preview.setBackgroundColor(selectedColor);
	}
}
