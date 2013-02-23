package org.geometerplus.fbreader.network.rss;

import org.geometerplus.fbreader.network.NetworkCatalogItem;
import org.geometerplus.fbreader.network.NetworkItem;
import org.geometerplus.fbreader.network.atom.ATOMId;


public class RSSChannelHandler extends AbstractRSSChannelHandler {
	private final NetworkCatalogItem myCatalog;
	private final String myBaseURL;
	private final RSSCatalogItem.State myData;

	private int myIndex;

	private String myNextURL;
	private String mySkipUntilId;
	private boolean myFoundNewIds;

	private int myItemsToLoad = -1;
	
	/**
	 * Creates new OPDSFeedHandler instance that can be used to get NetworkItem objects from OPDS feeds.
	 *
	 * @param baseURL    string that contains URL of the OPDS feed, that will be read using this instance of the reader
	 * @param result     network results buffer. Must be created using OPDSNetworkLink corresponding to the OPDS feed,
	 *                   that will be read using this instance of the reader.
	 */
	RSSChannelHandler(String baseURL, RSSCatalogItem.State result) {
		myCatalog = result.Loader.getTree().Item;
		myBaseURL = baseURL;
		myData = result;
		mySkipUntilId = myData.LastLoadedId;
		myFoundNewIds = mySkipUntilId != null;
		if (!(result.Link instanceof RSSNetworkLink)) {
			throw new IllegalArgumentException("Parameter `result` has invalid `Link` field value: result.Link must be an instance of OPDSNetworkLink class.");
		}
		
	}

	@Override
	public void processFeedStart() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean processFeedMetadata(RSSChannelMetadata feed,
			boolean beforeEntries) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean processFeedEntry(RSSItem entry) {
		System.out.println("!!!!!!!! processFeedEntry !!!!!!");
		
		final RSSNetworkLink rssLink = (RSSNetworkLink)myData.Link;
		boolean hasBookLink = true;
		
		if (entry.Id == null) {
			
			entry.Id = new ATOMId();
			entry.Id.Uri = "id"+myIndex;//TODO Fix it, udmv
		}

		myData.LastLoadedId = entry.Id.Uri;
		if (!myFoundNewIds && !myData.LoadedIds.contains(entry.Id.Uri)) {
			myFoundNewIds = true;
		}
		myData.LoadedIds.add(entry.Id.Uri);
		
		NetworkItem item = null;
		if (hasBookLink) {
			item = new RSSBookItem((RSSNetworkLink)myData.Link, entry, myBaseURL, myIndex++);
		} 
		if (item != null) {
			myData.Loader.onNewItem(item);
		}
		return false;
	}

	@Override
	public void processFeedEnd() {
		// TODO Auto-generated method stub
		
	}

}
