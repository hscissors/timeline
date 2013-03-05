package com.soundsofpolaris.timeline.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.soundsofpolaris.timeline.Constants;
import com.soundsofpolaris.timeline.TimelineApplication;
import com.soundsofpolaris.timeline.animation.HorizontalAnimation;
import com.soundsofpolaris.timeline.animation.VerticalAnimation;
import com.soundsofpolaris.timeline.database.DatabaseHelper;
import com.soundsofpolaris.timeline.models.Groups;
import com.soundsofpolaris.timeline.views.SelectableListItem;
import com.soundsofpolaris.timeline.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class GroupListActivity extends BaseActivity implements TextWatcher{

	private int mode;
	
	private GroupAdpater adapter;
	private ListView gList;
	
	private boolean selectMode = false;
	private boolean searchMode = false;
	
	private int parentId = -1;
	
	private ActionMode actionMode = null;
	
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
	        	Intent iAddEdit = new Intent(getApplicationContext(), GroupAddEditActivity.class);
	        	iAddEdit.putExtra("mode", Constants.EDIT_TIMELINE);
	        	iAddEdit.putExtra("id", adapter.getSelectedGroupsIds().get(0));
	        	startActivityForResult(iAddEdit, Constants.EDIT_TIMELINE);
	        	mode.finish();
				return true;
			case R.id.menu_delete:
	    	  	((TimelineApplication)getApplication()).getDb().deleteGroup(adapter.getSelectedGroupsIds().get(0));
	    	  	adapter.reset(((TimelineApplication) getApplication()).getDb().getAllGroups());
	    	  	mode.finish();
				return true;
			default:
				return false;
			}
		}
		
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) { return false; }

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			adapter.clearSelectedGroups();
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.group_list_activity);
		
		Intent i = getIntent();
		this.mode = i.getIntExtra("mode", Constants.VIEW_TIMLINE);
		this.parentId = i.getIntExtra("parentId", -1);
	
//		if(i.getAction().equals(Intent.ACTION_VIEW)){
//			//onActivityResult(Constants.IMPORT_FILE, Activity.RESULT_OK, i);
//		}
		
		adapter = new GroupAdpater();
		
		gList = (ListView) this.findViewById(R.id.group_list);
		gList.setAdapter(adapter);
		gList.setEmptyView(findViewById(R.id.group_empty));
		
		((EditText) this.findViewById(R.id.search_field)).addTextChangedListener(this);
		
		if(mode == Constants.SELECT_TIMELINES) {
			selectMode = true;
			adapter.reset(((TimelineApplication) getApplication()).getDb().getAllGroupsWithExclusion(parentId));
			adapter.setSelectedGroupsIds(getIntent().getExtras().getIntegerArrayList("selectedGroupsIds"));
		} else if (mode == Constants.VIEW_TIMLINE){
			adapter.reset(((TimelineApplication) getApplication()).getDb().getAllGroups());
		}
		
		Log.d(Constants.LOG, "GroupListAcitivity.onCreate();");
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		switch (requestCode) {
			case Constants.ADD_TIMELINE:
			case Constants.EDIT_TIMELINE:
				if(resultCode == Constants.ADD_TIMELINE_CONFIRM || resultCode == Constants.EDIT_TIMELINE_CONFIRM){
					adapter.reset(((TimelineApplication) getApplication()).getDb().getAllGroups());
				}
				break;
			default:
				adapter.reset();
				break;
			}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	protected void onStart() {
		Log.d(Constants.LOG, "GroupListAcitivity.onStart();");
		super.onStart();
	}
	
	@Override
	protected void onResume() {
		supportInvalidateOptionsMenu();
		Log.d(Constants.LOG, "GroupListAcitivity.onResume();");
		super.onResume();
	}
	
	@Override
	protected void onRestart() {
		Log.d(Constants.LOG, "GroupListAcitivity.onRestart();");
		super.onStart();
	}
	
	@Override
	protected void onStop(){
		if(searchMode){
			toggleSearch(false, false);
		}
		selectMode = false;
		adapter.deselectAllGroups();
		super.onStop();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getSupportMenuInflater();
		if(selectMode){
			inflater.inflate(R.menu.confirm_menu, (com.actionbarsherlock.view.Menu) menu);
		} else if(searchMode){
			inflater.inflate(R.menu.search_menu, (com.actionbarsherlock.view.Menu) menu);
		} else {
			inflater.inflate(R.menu.group_list_menu, (com.actionbarsherlock.view.Menu) menu);
		}
		
		
		return super.onCreateOptionsMenu(menu);
	}
	
    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(contextMenu, v, menuInfo);
    	//this.getMenuInflater().inflate(R.menu.group_list_context_menu, contextMenu);
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.menu_add:
	        	Intent iAddEdit = new Intent(getApplicationContext(), GroupAddEditActivity.class);
	        	iAddEdit.putExtra("mode", Constants.ADD_TIMELINE);
	        	startActivityForResult(iAddEdit, Constants.ADD_TIMELINE);
	            return true;
	        case R.id.menu_link:
	        	if(adapter.getCount() >= 2){
		        	selectMode = true;
		        	supportInvalidateOptionsMenu();
	        	} else {
	        		Toast.makeText(this, R.string.alert_no_link, Toast.LENGTH_LONG).show();
	        	}
	            return true;
	        case R.id.menu_confirm:
	        	ArrayList<Integer> selectedGroupIds = adapter.getSelectedGroupsIds();
	        	
	        	Bundle selected = new Bundle();
	        	
	        	if(mode != Constants.SELECT_TIMELINES){
		        	if(selectedGroupIds.size() <= 1){
		        		Toast.makeText(this, R.string.alert_join, Toast.LENGTH_LONG).show();
		        		return true;
		        	}

		        	selected.putIntegerArrayList("selectedGroupsIds", ((TimelineApplication) getApplication()).getDb().getRecursiveLinksToGroup(selectedGroupIds));
		        	
		        	Intent iView = new Intent(getApplicationContext(), EventListActivity.class);
		        	iView.putExtra("mode", Constants.VIEW_JOINED_TIMLINE);
		        	iView.putExtras(selected);
					startActivity(iView);
	        	} else {
		        	selected.putIntegerArrayList("selectedGroupsIds", selectedGroupIds);
	        		Intent iSelected = new Intent();
	        		iSelected.putExtras(selected);
	        		setResult(Constants.SELECT_TIMELINES_CONFIRM, iSelected);
	        		finish();
	        	}
	            return true;
	        case R.id.menu_cancel:
	        	if(mode != Constants.SELECT_TIMELINES){
		        	if(selectMode){
			        	selectMode = false;
			        	adapter.deselectAllGroups();
		        	} else if(searchMode){
		        		toggleSearch(false, true);
		        		InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
						inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		        	}
		        	supportInvalidateOptionsMenu();
	        	} else {
	        		finish();
	        	}
	            return true;
	        case R.id.menu_search:
	        	if(adapter.isEmpty() == false){
		        	toggleSearch(true, true);
		        	supportInvalidateOptionsMenu();
	        	} else {
	        		Toast.makeText(this, R.string.alert_no_search, Toast.LENGTH_LONG).show();
	        	}
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void toggleSearch(Boolean isSearchVisible, Boolean animate){
		searchMode = isSearchVisible;
		LinearLayout searchbar = (LinearLayout) this.findViewById(R.id.group_search);
		
		if(animate){
			searchbar.startAnimation(new VerticalAnimation(searchbar, 80, isSearchVisible));
		} else {
			searchbar.getLayoutParams().height = 0;
		}
		
		gList.requestLayout();
		
		if(searchMode == false){
			 ((EditText) searchbar.findViewById(R.id.search_field)).setText("");
			 adapter.reset();
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
	
//	@Override
//	public void onActivityResult(int requestCode, int resultCode, Intent data){
//		if(requestCode == Constants.IMPORT_FILE && resultCode == Activity.RESULT_OK ) {
//			Log.i(Constants.LOG, "Result: " + data.getDataString());
//			String path = Uri.parse(data.getDataString()).getPath();
//			int code = ((TimelineApplication) getApplication()).getDb().importFile(path);
//			String message;
//			switch (code) {
//			case 0:
//				message = getString(R.string.alert_import_sucess);
//				break;
//			case Constants.IMPORT_FILE_NOT_FOUND:
//				message = getString(R.string.alert_import_error_1);
//			case Constants.IMPORT_READ_ERROR:
//			default:
//				message = getString(R.string.alert_import_error);
//				break;
//			}
//			
//			final Toast m = Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG);	
//			m.show();
//		}
//		super.onActivityResult(requestCode, resultCode, data);
//	}
	
	class GroupAdpater extends BaseAdapter implements Filterable{
		
		private List<Groups> data;
		private List<Groups> unfilteredData;
		private List<Groups> filteredData;
		private List<Groups> selectedGroups;
		
		public GroupAdpater(){
			super();
    		this.data = new ArrayList<Groups>();
    	}
		
    	public void reset(List<Groups> data){
    		this.data = data;
    		unfilteredData = data;
    		
    		notifyDataSetChanged();
    	}
    	
    	public void reset(){
    		this.data = unfilteredData;
    		
    		notifyDataSetChanged();
    	}
    	
    	public void removeFromData(Groups g){
    		unfilteredData.remove(g);
    		
    		reset();
    	}
    	
    	public void deselectAllGroups(){
    		if(selectedGroups != null && selectedGroups.size() != 0){
	    		for (int i = 0; i < selectedGroups.size(); i++) {
	    			selectedGroups.get(i).setSelected(false);
				}
	    		
	    		this.notifyDataSetChanged();
	    		clearSelectedGroups();
    		}
    	}
    	
    	private boolean setSelectedGroup(Groups selectedGroup){
    		if(selectedGroups == null){
    			selectedGroups = new ArrayList<Groups>();
    		}
    		
    		if(this.selectedGroups.contains(selectedGroup)){
    			this.selectedGroups.remove(selectedGroups.indexOf(selectedGroup));
    			return false;
    		} else {
    			this.selectedGroups.add(selectedGroup);
    			return true;
    		}
    		
    	}
    	
    	public void setSelectedGroupsIds(ArrayList<Integer> selected){
    		if(selectedGroups == null){
    			selectedGroups = new ArrayList<Groups>();
    		}
    		
    		if(selected == null){
    			return;
    		}
    		
    		for (int i = 0; i < selected.size(); i++) {
    			int groupId = selected.get(i);
				for (int j = 0; j < data.size(); j++) {
					Groups g = data.get(j);
					if(g.getId() == groupId){
						g.setSelected(true);
						selectedGroups.add(g);
					}
				}
			}
    		
    		notifyDataSetChanged();
    	}
    	
    	public ArrayList<Integer> getSelectedGroupsIds(){
    		if(selectedGroups == null){
    			selectedGroups = new ArrayList<Groups>();
    		}
    		
    		ArrayList<Integer> groupIds = new ArrayList<Integer>();
    		
    		for (int i = 0; i < selectedGroups.size(); i++) {
    			groupIds.add(selectedGroups.get(i).getId());
			}
    		
    		return groupIds;
    	}
    	
    	private void clearSelectedGroups(){
    		this.selectedGroups = null;
    	}
    	
		@Override
		public Filter getFilter() {
			return new Filter(){
				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					FilterResults results = new FilterResults();
					filteredData = new ArrayList<Groups>();
					
					for (Groups group : unfilteredData) {
						String lowerCaseName = group.getName();
						Boolean matches = lowerCaseName.contains(constraint);
						if(matches){
							filteredData.add(group);
						}
					}
					
					results.count = filteredData.size();
					results.values = filteredData;
					return results;
				}

				@SuppressWarnings("unchecked")
				@Override
				protected void publishResults(CharSequence constraint, FilterResults results) {
					adapter.data = (List<Groups>) results.values;
					notifyDataSetChanged();
				}
			};
		}
		
		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Groups getItem(int position) {
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			return data.get(position).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			SelectableListItem gView = (SelectableListItem) convertView;
			
			final Groups g = (Groups) getItem(position);
			
			if(gView == null){
				gView = (SelectableListItem) getLayoutInflater().inflate(R.layout.group_list_item, null);
			}
			
			gView.setSelected(g.getSelected());
			
			View gColor = (View) gView.findViewById(R.id.group_color);
			TextView gTitle = (TextView) gView.findViewById(R.id.group_title);
			
			gColor.setBackgroundColor(g.getColor());
			gTitle.setText(g.getName());
			
			gView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if(actionMode != null) actionMode.finish();
					boolean addedToSelected = adapter.setSelectedGroup(g);
					if(selectMode){
						g.setSelected(addedToSelected);
						((SelectableListItem) view).animateSelected(addedToSelected);
						
					} else {	
						ArrayList<Integer> selectedGroupIds = adapter.getSelectedGroupsIds();
			        	Bundle b = new Bundle();
			        	b.putIntegerArrayList("selectedGroupsIds", ((TimelineApplication) getApplication()).getDb().getRecursiveLinksToGroup(selectedGroupIds));
			        	
						Intent i = new Intent(getApplicationContext(), EventListActivity.class);
						i.putExtra("groupName", g.getName());
						i.putExtra("primaryGroupId", g.getId());
						i.putExtra("mode", Constants.VIEW_TIMLINE);
						i.putExtras(b);
						
						startActivity(i);
					}
					
				}
			});
			
			if(mode != Constants.SELECT_TIMELINES){
				gView.setLongClickable(true);
				
				gView.setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(View view) {
						boolean addedToSelected = adapter.setSelectedGroup(g);
						if(selectMode == false && addedToSelected){
							actionMode = startActionMode(ActionModeCallback);
						}
						return true;
					}
				});
			}
			
			return gView;
		}
	}
}	
