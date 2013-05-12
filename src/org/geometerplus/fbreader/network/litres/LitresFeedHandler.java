/*
 * Copyright (C) 2010-2013 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.fbreader.network.litres;

import java.util.LinkedList;
import java.util.List;

import org.geometerplus.fbreader.network.NetworkBookItem;
import org.geometerplus.fbreader.network.NetworkItem;
import org.geometerplus.fbreader.network.atom.ATOMFeedHandler;
import org.geometerplus.fbreader.network.atom.ATOMLink;
import org.geometerplus.fbreader.network.litres.readers.LitresBookEntry;
import org.geometerplus.fbreader.network.litres.readers.LitresEntry;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;

public class LitresFeedHandler implements ATOMFeedHandler<LitresFeedMetadata,LitresEntry> {
	public List<NetworkBookItem> Books = new LinkedList<NetworkBookItem>();
	protected LitresCatalogItem.State myResult;
	protected int myIndex = 0;
	
	protected LitresFeedHandler(LitresCatalogItem.State result){
		if (!(result.Link instanceof LitresNetworkLink)) {
			throw new IllegalArgumentException("Parameter `result` has invalid `Link` field value: result.Link must be an instance of LitresNetworkLink class.");
		}
		myResult = result;
	}
	
	@Override
	public void processFeedStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean processFeedMetadata(LitresFeedMetadata feed,
			boolean beforeEntries) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean processFeedEntry(LitresEntry entry) {
		boolean hasBookLink = true;
		String myBaseURL = "";
		NetworkItem item = null;
		myResult.LoadedIds.add(entry.Id.Uri);
		if (hasBookLink) {
			if (entry instanceof LitresBookEntry) {
				item = new LitresBookItem(myResult.Link, (LitresBookEntry)entry, myBaseURL, myIndex++);
			}
		}
		if (item != null) {
			myResult.Loader.onNewItem(item);
		}
		return false;
	}

	@Override
	public void processFeedEnd() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public LitresFeedMetadata createFeed(ZLStringMap attributes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LitresBookEntry createEntry(ZLStringMap attributes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ATOMLink createLink(ZLStringMap attributes) {
		// TODO Auto-generated method stub
		return null;
	}

}
