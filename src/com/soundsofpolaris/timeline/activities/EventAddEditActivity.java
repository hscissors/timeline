package com.soundsofpolaris.timeline.activities;



import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.soundsofpolaris.timeline.Constants;
import com.soundsofpolaris.timeline.TimelineApplication;
import com.soundsofpolaris.timeline.models.Events;
import com.soundsofpolaris.timeline.ui.CustomDatePicker;
import com.soundsofpolaris.timeline.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EventAddEditActivity extends BaseActivity {
	
	private Events e = null;
	private int groupId = 0;
	private int mode;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mode = getIntent().getIntExtra("mode", Constants.ADD_EVENT);
        int id = getIntent().getIntExtra("id", 0);
        groupId = getIntent().getIntExtra("groupId", 0);
        
        switch (mode) {
			case Constants.ADD_EVENT:
			case Constants.EDIT_EVENT:
					setContentView(R.layout.event_add_edit_activity);
				break;
			case Constants.VIEW_EVENT:
			default:
					setContentView(R.layout.event_view);
				break;
		}
        
        CustomDatePicker customDate = (CustomDatePicker) this.findViewById(R.id.event_add_edit_custom_date);
        //DEPRECATED DatePicker date = (DatePicker) this.findViewById(R.id.date);
        TextView title = (TextView) this.findViewById(R.id.event_add_edit_view_title);
        TextView desc = (TextView) this.findViewById(R.id.event_add_edit_view_desc);
        
        switch (mode) {
			case Constants.ADD_EVENT:
				break;
			case Constants.EDIT_EVENT:
					e = ((TimelineApplication)getApplication()).getDb().getEventById(id);
					
					if(e != null){
						//DEPRECATED
						//long unix = e.getUnixDate();
						//Date d = new Date(unix);
						//int year = d.getYear();
						//int month = d.getMonth();
						//int day = d.getDate();
						//Log.i(Constants.LOG, "e.unixDate() " + unix);
						//date.updateDate(year, month, day);
						customDate.setTimeinMills(e.getUnixDate());
						title.setText(e.getTitle());
						desc.setText(e.getDescription());

						customDate.setAllMonth(e.isAllMonth());
						customDate.setAllYear(e.isAllYear());
					}
				break;
			case Constants.VIEW_EVENT:
			default:
					e = ((TimelineApplication)getApplication()).getDb().getEventById(id);
					
					if(e != null){
						TextView subheader = (TextView) this.findViewById(R.id.event_view_subheader);
						subheader.setText(e.getPrettyDate() + ", " + e.getYear());
						
			        	TextView groupname = (TextView) findViewById(R.id.event_add_edit_view_group);
			        	groupname.setText(e.getGroupName());
						
						title.setText(e.getTitle());
						if(e.getDescription().isEmpty()){
							desc.setVisibility(View.GONE);
						} else {
							desc.setText(e.getDescription());
						}
					}
				break;
		}
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
	        	CustomDatePicker customDate = (CustomDatePicker) this.findViewById(R.id.event_add_edit_custom_date);
	    		//DEPRECATED DatePicker date = (DatePicker) this.findViewById(R.id.date);
	    		TextView title = (TextView) this.findViewById(R.id.event_add_edit_view_title);
	    		TextView desc = (TextView) this.findViewById(R.id.event_add_edit_view_desc);
	    		
	    		//DEPRECATED
	    		//date.clearFocus();
	    		
	    		//Date d = null;
	    		//if(date != null){
	    		//	d = new Date(date.getYear(), date.getMonth(), date.getDayOfMonth());
	    		//}
	    		
	    		//Log.i(Constants.LOG, "onClick() " + d.getTime());
	    		
	    		final TimelineApplication app = (TimelineApplication) getApplication();
	    		if(title.getText().length() == 0){
	    			Toast.makeText(this, R.string.alert_event_title, Toast.LENGTH_LONG).show();
	    		} else{
	    	        switch (mode) {
	    				case Constants.ADD_EVENT:
	    					app.getDb().addEvent(
	    							customDate.getYear(),
	    							customDate.getMonth(),
	    							customDate.getTimeinMills(), 
	    							title.getText().toString(), 
	    							desc.getText().toString(),
	    							customDate.isAllYear(),
	    							customDate.isAllMonth(),
	    							groupId
	    						);
	    					setResult(Constants.ADD_EVENT_CONFIRM);
	    					break;
	    				case Constants.EDIT_EVENT:
	    					app.getDb().updateEvent(
	    							e.getId(), 
	    							customDate.getYear(),
	    							customDate.getMonth(),
	    							customDate.getTimeinMills(), 
	    							title.getText().toString(), 
	    							desc.getText().toString(), 
	    							customDate.isAllYear(),
	    							customDate.isAllMonth(),
	    							e.getGroupId()
	    						);
	    					setResult(Constants.EDIT_EVENT_CONFIRM);
	    					break;
	    				case Constants.VIEW_EVENT:
	    				default:
	    					break;
	    			}
	    	        
	    	        if(mode != Constants.VIEW_EVENT){
		    	        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
						inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	    	        }
	    	        
	    	        finish();
	    		}
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}		
}
