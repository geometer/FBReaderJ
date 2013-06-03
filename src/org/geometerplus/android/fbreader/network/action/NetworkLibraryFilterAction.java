package org.geometerplus.android.fbreader.network.action;

import java.util.ArrayList;
import java.util.List;

import org.geometerplus.android.fbreader.OrientationUtil;
import org.geometerplus.android.fbreader.network.NetworkLibraryFilterActivity;
import org.geometerplus.android.fbreader.tree.TreeActivity;
import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;
import org.geometerplus.zlibrary.core.language.Language;

import android.app.Activity;
import android.content.Intent;

public class NetworkLibraryFilterAction extends RootAction {

	public NetworkLibraryFilterAction(Activity activity) {
		super(activity, ActionCode.LIBRARY_FILTER, "libraryFilter", true);
	}

	@Override
	public void run(NetworkTree tree) {
		final List<String> ids = new ArrayList<String>();
		
		final NetworkLibrary library = NetworkLibrary.Instance();
		System.out.println("[NetworkLibraryFilterAction] Library links: "+library.getAllLinks().size());
		for(INetworkLink link : library.getAllLinks()){
			System.out.println("[NetworkLibraryFilterAction] link: "+link.getUrl(UrlInfo.Type.Catalog)+": "+link.getTitle());
			ids.add(link.getUrl(UrlInfo.Type.Catalog));
		}
		
		//ids.add("http://www.feedbooks.com/catalog.atom");
		library.setActiveIds(ids);
		library.synchronize();
		
		OrientationUtil.startActivity(
				myActivity,
				new Intent(myActivity.getApplicationContext(), NetworkLibraryFilterActivity.class)
			);
	}

}
