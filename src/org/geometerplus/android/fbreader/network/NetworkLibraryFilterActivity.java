package org.geometerplus.android.fbreader.network;

import java.util.ArrayList;
import java.util.List;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;
import org.geometerplus.zlibrary.ui.android.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class NetworkLibraryFilterActivity extends Activity {
	final NetworkLibrary library = NetworkLibrary.Instance();
	CheckListAdapter myAdapter;
	ArrayList<String> ids = new ArrayList<String>();
	ArrayList<String> allids = new ArrayList<String>();
	public final static String IDS_LIST = "org.geometerplus.android.fbreader.network.IDS_LIST";
	public final static String ALL_IDS_LIST = "org.geometerplus.android.fbreader.network.ALL_IDS_LIST";
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.network_library_filter);
		
		Intent intent = getIntent();
		ids = intent.getStringArrayListExtra(IDS_LIST);
		allids = intent.getStringArrayListExtra(ALL_IDS_LIST);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		ArrayList<CheckItem> idItems = new ArrayList<CheckItem>();
		idItems.add(new CheckSection("Selected"));
		for(String i : ids){
			idItems.add(new CheckItem(i, true));
		}
		if(allids.size() > 0){
			idItems.add(new CheckSection("Unselected"));
			for(String i : allids){
				idItems.add(new CheckItem(i, false));
			}
		}
		
		ListView selectedList = (ListView) findViewById(R.id.selectedList);
		myAdapter = new CheckListAdapter(this, R.layout.checkbox_item, idItems);
		selectedList.setAdapter(myAdapter);

	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		System.out.println("Exit!");
		ArrayList<String> ids = new ArrayList<String>();
		ArrayList<CheckItem> items = myAdapter.getItems();
		for(CheckItem item : items){
			if(!item.isSection() && item.isChecked()){
				System.out.println(">> "+item.getId()+", "+item.isChecked());
				ids.add(item.getId());
			}
			
		}
		library.setActiveIds(ids);
		library.synchronize();
	}
	
	private class CheckItem{
		private String myId;
		private boolean isChecked;
		
		public CheckItem(String id, boolean checked){
			myId = id;
			isChecked = checked;
		}
		
		public String getId(){
			return myId;
		}
		
		public boolean isChecked(){
			return isChecked;
		}
		
		public void setChecked(boolean value){
			isChecked = value;
		}
		
		public boolean isSection(){
			return false;
		}
	}
	
	private class CheckSection extends CheckItem{
		public CheckSection(String title){
			super(title, false);
		}
		public boolean isSection(){
			return true;
		}
	}
	
	private class CheckListAdapter extends ArrayAdapter<CheckItem> {
		
		private ArrayList<CheckItem> items = new ArrayList<CheckItem>();
		
		public CheckListAdapter(Context context, int textViewResourceId, List<CheckItem> objects) {
			super(context, textViewResourceId, objects);
			items.addAll(objects);
		}
		
		public ArrayList<CheckItem> getItems(){
			return items;
		}

		@Override
		public View getView(int position, View convertView, final ViewGroup parent) {
			
			View v = convertView;
			
			CheckItem item = this.getItem(position); 
			
		    if (item != null) {
		    	if(item.isSection()){
		    			LayoutInflater vi;
		    			vi = LayoutInflater.from(getContext());
		    			v = vi.inflate(R.layout.checkbox_section, null);
		    		TextView tt = (TextView) v.findViewById(R.id.title);
		    		if (tt != null) {
		    			tt.setText(item.getId());
		    		}
		    	}else{
		    		//if (v == null) {
				       	LayoutInflater vi;
				        vi = LayoutInflater.from(getContext());
				        v = vi.inflate(R.layout.checkbox_item, null);
				    //}
		    		INetworkLink link = library.getLinkByUrl(item.getId());
		        	TextView tt = (TextView) v.findViewById(R.id.title);
		        	if (tt != null) {
		            	tt.setText(link.getTitle());
		        	}
		        	tt = (TextView) v.findViewById(R.id.subtitle);
		        	if (tt != null) {
		            	tt.setText(link.getSummary());
		        	}
		        	CheckBox ch = (CheckBox) v.findViewById(R.id.check_item);
		        	if (ch != null) {
		        		ch.setText("");
		            	ch.setChecked(item.isChecked());
		            	ch.setTag(item);
		            	ch.setOnClickListener( new View.OnClickListener() {  
		            	     public void onClick(View v) {  
		            	      CheckBox cb = (CheckBox) v;  
		            	      CheckItem checkedItem = (CheckItem) cb.getTag();
		            	      if(checkedItem != null){
		            	    	  checkedItem.setChecked(cb.isChecked());
		            	      }
		            	     }
		            	    });  
		        	}
		    	}
		    }
			return v;
		}
	}
}
