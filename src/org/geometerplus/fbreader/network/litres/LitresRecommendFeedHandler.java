package org.geometerplus.fbreader.network.litres;

import java.util.List;

import org.geometerplus.fbreader.network.NetworkBookItem;
import org.geometerplus.fbreader.network.NetworkItem;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;
import org.geometerplus.fbreader.network.authentication.litres.LitResAuthenticationManager;
import org.geometerplus.fbreader.network.litres.LitresCatalogItem.State;
import org.geometerplus.fbreader.network.litres.readers.LitresBookEntry;
import org.geometerplus.fbreader.network.litres.readers.LitresEntry;

public class LitresRecommendFeedHandler extends LitresFeedHandler {
	final LitResAuthenticationManager mgr;
	protected LitresRecommendFeedHandler(State result) {
		super(result);
		
		if (myResult.Link.authenticationManager() instanceof LitResAuthenticationManager) {
			mgr = (LitResAuthenticationManager)myResult.Link.authenticationManager();
		}else{
			mgr = null;
		}
	}

	@Override
	public boolean processFeedEntry(LitresEntry entry) {
		String myBaseURL = "";
		NetworkItem item = null;
		myResult.LoadedIds.add(entry.Id.Uri);
		
		if (entry instanceof LitresBookEntry && mgr != null) {
			boolean found = false;
			List<NetworkBookItem> books = mgr.purchasedBooks();
			for(int i=0;i<books.size();i++){
				//System.out.println("> CHECK: "+books.get(i).Id+"=="+entry.Id.Uri);
				if(books.get(i).Id == entry.Id.Uri){
					found = true;
					break;
				}
			}
			
			if(!found){
				System.out.println("ADD BOOK by ID: "+entry.Id.Uri+", Index="+myIndex);
				item = new LitresBookItem(myResult.Link, (LitresBookEntry)entry, myBaseURL, myIndex++);
			}
		}
		
		if (item != null) {
			myResult.Loader.onNewItem(item);
		}
		return false;
	}
}
