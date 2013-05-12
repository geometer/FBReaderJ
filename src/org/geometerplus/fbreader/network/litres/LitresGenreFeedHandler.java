package org.geometerplus.fbreader.network.litres;

import org.geometerplus.fbreader.network.litres.LitresCatalogItem.State;
import org.geometerplus.fbreader.network.litres.readers.LitresEntry;

public class LitresGenreFeedHandler extends LitresFeedHandler {

	LitresGenreFeedHandler(State result) {
		super(result);
	}
	
	@Override
	public boolean processFeedEntry(LitresEntry entry) {
		return false;
	}

}
