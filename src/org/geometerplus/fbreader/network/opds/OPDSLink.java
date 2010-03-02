/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.network.opds;

import java.util.*;

import org.geometerplus.fbreader.network.*;


class OPDSLink extends NetworkLink {

	public interface URLCondition {
		int URL_CONDITION_NEVER = 0;
		int URL_CONDITION_SIGNED_IN = 1;
	}

	private TreeMap<String, Integer> myUrlConditions = null;
	private LinkedList<URLRewritingRule> myUrlRewritingRules = null;


	OPDSLink(String siteName, String title, String summary, Map<String, String> links) {
		super(siteName, title, summary, links);
	}

	public final void setUrlConditions(Map<String, Integer> conditions) {
		myUrlConditions = new TreeMap(conditions);
	}

	public final void setUrlRewritingRules(List<URLRewritingRule> rules) {
		myUrlRewritingRules = new LinkedList(rules);
	}

	public NetworkLibraryItem libraryItem() {
		TreeMap<Integer, String> urlMap = new TreeMap<Integer, String>();
		//urlMap.put(NetworkLibraryItem.URLType.URL_COVER, Icon);
		urlMap.put(NetworkLibraryItem.URLType.URL_CATALOG, Links.get(URL_MAIN));
		return new OPDSCatalogItem(this, Title, Summary, urlMap);
	}
}
