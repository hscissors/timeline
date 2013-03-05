package com.soundsofpolaris.timeline.activities;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.soundsofpolaris.timeline.Constants;
import com.soundsofpolaris.timeline.TimelineApplication;
import com.soundsofpolaris.timeline.animation.VerticalAnimation;
import com.soundsofpolaris.timeline.models.Events;
import com.soundsofpolaris.timeline.models.Groups;
import com.soundsofpolaris.timeline.ui.AmazingAdapter;
import com.soundsofpolaris.timeline.ui.AmazingListView;
import com.soundsofpolaris.timeline.R;

public class EventListActivity extends BaseActivity implements TextWatcher{
	private int mode = 0;
	private Boolean searchMode = false;
	private AmazingListView eList;
	private DateSectionAdpater adapter;
	
	ArrayList<Integer> selectedGroupIds;
	
	int primaryGroupId;
	int selectedEventId;
	
	private ActionMode.Callback ActionModeCallback = new ActionMode.Callback(){

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
	        inflater.inflate(R.menu.edit_delete_share_menu, menu);
	        return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.menu_edit:
	        	Intent iAddEdit = new Intent(getApplicationContext(), EventAddEditActivity.class);
	        	iAddEdit.putExtra("mode", Constants.EDIT_EVENT);
	        	iAddEdit.putExtra("id", selectedEventId);
	        	startActivityForResult(iAddEdit, Constants.EDIT_EVENT);
	        	mode.finish();
				return true;
			case R.id.menu_delete:
	    	  	((TimelineApplication)getApplication()).getDb().deleteEvent(selectedEventId);
	    	  	adapter.reset(((TimelineApplication) getApplication()).getDb().getAllEventsByGroupSet(selectedGroupIds));
	    	  	mode.finish();
				return true;
			default:
				return false;
			}
		}
		
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) { return false; }

		@Override
		public void onDestroyActionMode(ActionMode mode) {}
	};
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.event_list_activity);
        
        mode = getIntent().getIntExtra("mode", Constants.VIEW_TIMLINE);
        
        final String groupName = getIntent().getStringExtra("groupName");
        primaryGroupId = getIntent().getIntExtra("primaryGroupId", -1);
        
        eList = (AmazingListView) findViewById(R.id.event_list);
        eList.setDividerHeight(0);
        eList.setPinnedHeaderView(LayoutInflater.from(this).inflate(R.layout.event_list_item_header, eList, false));
        eList.setAdapter(adapter = new DateSectionAdpater()); 
        
        eList.setEmptyView(findViewById(R.id.event_list_empty));
        
        selectedGroupIds = getIntent().getExtras().getIntegerArrayList("selectedGroupsIds");
        
        if(mode == Constants.VIEW_TIMLINE){
        	TextView subheader = (TextView) findViewById(R.id.event_list_subheader);
        	subheader.setText(groupName);
        } else if(mode == Constants.VIEW_JOINED_TIMLINE){
        	TextView subheader = (TextView) findViewById(R.id.event_list_subheader);
        	ImageView subheaderShadow = (ImageView) findViewById(R.id.event_subheader_shadow);
        	subheader.setVisibility(View.GONE);
        	subheaderShadow.setVisibility(View.GONE);
        }
        
        ((EditText) this.findViewById(R.id.search_field)).addTextChangedListener(this);
        
        adapter.reset(((TimelineApplication) getApplication()).getDb().getAllEventsByGroupSet(selectedGroupIds));
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		switch (requestCode) {
			case Constants.ADD_EVENT:
			case Constants.EDIT_EVENT:
				if(resultCode == Constants.ADD_EVENT_CONFIRM || resultCode == Constants.EDIT_EVENT_CONFIRM){
					adapter.reset(((TimelineApplication) getApplication()).getDb().getAllEventsByGroupSet(selectedGroupIds));
				}
				break;
			default:
				adapter.reset();
				break;
			}
		super.onActivityResult(requestCode, resultCode, data);
	}
    
    @Override
    protected void onResume(){
        supportInvalidateOptionsMenu();
    	super.onResume();
    }
    
    @Override 
    protected void onStop(){
    	if(searchMode){
    		toggleSearch(false, false);
    	}
    	super.onStop();
    }
    
	private void toggleSearch(Boolean isSearchVisible, Boolean animate){
		searchMode = isSearchVisible;
		LinearLayout searchbar = (LinearLayout) this.findViewById(R.id.event_search);
		
		if(animate){
			searchbar.startAnimation(new VerticalAnimation(searchbar, 80, isSearchVisible));
		} else {
			searchbar.getLayoutParams().height = 0;
		}
		
		eList.requestLayout();
		
		if(searchMode == false){
			 ((EditText) searchbar.findViewById(R.id.search_field)).setText("");
			 adapter.reset();
		} 
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getSupportMenuInflater();
		if(mode == Constants.VIEW_TIMLINE){
			if(searchMode){
				inflater.inflate(R.menu.search_menu, (com.actionbarsherlock.view.Menu) menu);
			} else {
				inflater.inflate(R.menu.event_list_menu, (com.actionbarsherlock.view.Menu) menu);
			}
		} else if(mode == Constants.VIEW_JOINED_TIMLINE){
			if(searchMode){
				inflater.inflate(R.menu.search_menu, (com.actionbarsherlock.view.Menu) menu);
			} else {
				inflater.inflate(R.menu.event_list_joined_menu, (com.actionbarsherlock.view.Menu) menu);
			}
		}

		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.menu_add:
	    		Intent iAddEdit = new Intent(getApplicationContext(), EventAddEditActivity.class);
	    		iAddEdit.putExtra("mode", Constants.ADD_EVENT);
	    		iAddEdit.putExtra("groupId", primaryGroupId);
	        	startActivityForResult(iAddEdit, Constants.ADD_EVENT);
	            return true;
	        case R.id.menu_cancel:
	        	toggleSearch(false, true);
	        	InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
				inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	        	supportInvalidateOptionsMenu();
	            return true;
	        case R.id.menu_search:
	        	if(adapter.isEmpty() == false){
		        	toggleSearch(true, true);
		        	supportInvalidateOptionsMenu();
	        	}
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		adapter.getFilter().filter(s);
	}
    
//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//      AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
//      int menuItemIndex = item.getItemId();
//      Events e = adapter.getItem(info.position);
//      
//      if(menuItemIndex == 0){ //EDIT
//			Intent i = new Intent(getBaseContext(), EventAddEditActivity.class);
//			i.putExtra("mode", Constants.EDIT_EVENT);
//			i.putExtra("id", e.getId());
//			startActivity(i);
//      } else if(menuItemIndex == 1){ //DELETE
//    	  	((TimelineApplication)getApplication()).getDb().deleteEvent(e);
//    	  	List<Integer> selectedGroupIds = ((TimelineApplication)getApplication()).getSelectedGroupsIds();
//    	  	adapter.reset(((TimelineApplication) getApplication()).getDb().getAllEventsByGroupSet(selectedGroupIds));
//      }
//      
//      return true;
//    }
    
    class DateSectionAdpater extends AmazingAdapter implements Filterable{

    	List<Pair<String, List<Events>>> data;
    	List<Pair<String, List<Events>>> unfilteredData;
    	List<Pair<String, List<Events>>> filteredData; 
    	
    	public DateSectionAdpater(){
    		super();
    		data = new ArrayList<Pair<String, List<Events>>>();
    	}
    	
    	public void reset(List<Pair<String, List<Events>>> data){
    		this.data = null;
    		this.data = data;
    		unfilteredData = data;
    		notifyDataSetChanged();
    	}
    	
    	public void reset(){
    		this.data = unfilteredData;
    	}
    	
		@Override
		public int getCount() {
			int total = 0;
			for (int i = 0; i < data.size(); i++) {
				total += data.get(i).second.size();
			}
			//Log.d(Constants.LOG, "Count size@DateSectionApadater.getCount(): " + total ) ;
			return total;
		}

		@Override
		public Events getItem(int position) {
			int c = 0;
			for (int i = 0; i < data.size(); i++) {
				if (position >= c && position < c + data.get(i).second.size()) {
					return data.get(i).second.get(position - c);
				}
				c += data.get(i).second.size();
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		protected void onNextPageRequested(int page) {}

		@Override
		protected void bindSectionHeader(View view, int position, boolean displaySectionHeader) {
			if (displaySectionHeader) {
				view.findViewById(R.id.event_item_header).setVisibility(View.VISIBLE);
				TextView lSectionTitle = (TextView) view.findViewById(R.id.event_item_header);
				lSectionTitle.setText(getSections()[getSectionForPosition(position)]);
			} else {
				view.findViewById(R.id.event_item_header).setVisibility(View.GONE);
			}
		}

		@Override
		public View getAmazingView(int position, View convertView,
				ViewGroup parent) {
			
			View eView = convertView;
			if(eView == null){
				eView = getLayoutInflater().inflate(R.layout.event_list_item, null);
			} 
			
			View eColor = (View) eView.findViewById(R.id.event_group_color);
			TextView eDate = (TextView) eView.findViewById(R.id.event_date);
			TextView eTite = (TextView) eView.findViewById(R.id.event_title);
			ImageView eLink = (ImageView) eView.findViewById(R.id.event_linked);
			
			final Events e = getItem(position);
			eColor.setBackgroundColor(e.getGroupColor());
			eDate.setText(e.getPrettyDate());
			eTite.setText(e.getTitle());
			
			if(e.getGroupId() == primaryGroupId){
				eLink.setVisibility(View.GONE);
			} else {
				eLink.setVisibility(View.VISIBLE);
			}
			
			eView.setLongClickable(true);
			
			eView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent i = new Intent(getBaseContext(), EventAddEditActivity.class);
					i.putExtra("mode", Constants.VIEW_EVENT);
					i.putExtra("id", e.getId());
					startActivity(i);
				}
			});
			
        	if(mode == Constants.VIEW_TIMLINE && primaryGroupId == e.getGroupId()){
        		eView.setOnLongClickListener(new OnLongClickListener() {
	    			@Override
	    			public boolean onLongClick(View view) {
	    				selectedEventId = e.getId();
	    				startActionMode(ActionModeCallback);
	    				return true;
	    			}
	    		});
        	}
			
			return eView;
		}

		@Override
		public void configurePinnedHeader(View header, int position, int alpha) {
			TextView eSectionHeader = (TextView)header;
			eSectionHeader.setText(getSections()[getSectionForPosition(position)]);
			//lSectionHeader.setBackgroundColor(alpha << 24 | (0xbbffbb));
			//lSectionHeader.setTextColor(alpha << 24 | (0x000000));
		}

		@Override
		public int getPositionForSection(int section) {
			if (section < 0) section = 0;
			if (section >= data.size()) section = data.size() - 1;
			int c = 0;
			for (int i = 0; i < data.size(); i++) {
				if (section == i) { 
					return c;
				}
				c += data.get(i).second.size();
			}
			return 0;
		}

		@Override
		public int getSectionForPosition(int position) {
			int c = 0;
			for (int i = 0; i < data.size(); i++) {
				if (position >= c && position < c + data.get(i).second.size()) {
					return i;
				}
				c += data.get(i).second.size();
			}
			return -1;
		}

		@Override
		public String[] getSections() {
			String[] sectionNames = new String[data.size()];
			for (int i = 0; i < data.size(); i++) {
				sectionNames[i] = data.get(i).first;
			}
			return sectionNames;
		}
		
		@Override
		public Filter getFilter() {
			return new Filter(){
				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					FilterResults results = new FilterResults();
					filteredData = new ArrayList<Pair<String, List<Events>>>();
					
					for (Pair<String, List<Events>> yearGroup : unfilteredData) {
						List<Events> filteredEvents = new ArrayList<Events>();
						for (Events event : yearGroup.second) {
							if(
								event.getTitle().contains(constraint) ||
								event.getDescription().contains(constraint) ||
								Integer.toString(event.getYear()).contains(constraint)
							){
								filteredEvents.add(event);
							} 
						}
						
						if(filteredEvents.size() != 0){
							filteredData.add(new Pair<String, List<Events>>(yearGroup.first, filteredEvents));
						}
					}
					
					results.count = filteredData.size();
					results.values = filteredData;
					return results;
				}

				@SuppressWarnings("unchecked")
				@Override
				protected void publishResults(CharSequence constraint, FilterResults results) {
					adapter.data = (List<Pair<String, List<Events>>>) results.values;
					notifyDataSetChanged();
				}
			};
		}
	}
}
