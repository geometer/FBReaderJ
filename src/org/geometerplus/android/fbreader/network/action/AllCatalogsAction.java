package org.geometerplus.android.fbreader.network.action;

import java.util.ArrayList;
import java.util.List;

import org.geometerplus.android.fbreader.OrientationUtil;
import org.geometerplus.android.fbreader.network.AllCatalogsActivity;
import org.geometerplus.android.fbreader.tree.TreeActivity;
import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;
import org.geometerplus.zlibrary.core.language.Language;

import android.app.Activity;
import android.content.Intent;

public class AllCatalogsAction extends RootAction {
	
	public AllCatalogsAction(Activity activity) {
		super(activity, ActionCode.LIBRARY_FILTER, "allCatalogs", true);
	}

	@Override
	public void run(NetworkTree tree) {
		final NetworkLibrary library = NetworkLibrary.Instance();
				
		final List<String> activeIds = library.activeIds();
		ArrayList<String> ids = new ArrayList<String>();
		ids.addAll(activeIds);
		
		final ArrayList<String> allids = new ArrayList<String>();
		boolean found = false;
		for(String id : library.linkIds()){
			for(String aid : activeIds){
				if(id.equals(aid)){
					found = true;
					break;
				}
			}
			if(!found){
				allids.add(id);
			}
			found = false;
		}
		
		OrientationUtil.startActivity(
				myActivity,
				new Intent(myActivity.getApplicationContext(), AllCatalogsActivity.class)
				.putStringArrayListExtra(AllCatalogsActivity.IDS_LIST, ids)
				.putStringArrayListExtra(AllCatalogsActivity.ALL_IDS_LIST, allids)
			);
	}

}
