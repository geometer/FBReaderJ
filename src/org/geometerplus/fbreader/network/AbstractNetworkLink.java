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

package org.geometerplus.fbreader.network;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.geometerplus.zlibrary.core.util.ZLMiscUtil;

public abstract class AbstractNetworkLink implements INetworkLink {
	protected String mySiteName;
	protected String myTitle;
	protected String mySummary;
	protected String myIcon;
	protected final String myLanguage;
	protected final TreeMap<String, String> myLinks;

	/**
	 * Creates new NetworkLink instance.
	 *
	 * @param siteName   name of the corresponding website. Must be not <code>null</code>.
	 * @param title      title of the corresponding library item. Must be not <code>null</code>.
	 * @param summary    description of the corresponding library item. Can be <code>null</code>.
	 * @param icon       string contains link's icon data/url. Can be <code>null</code>.
	 * @param language   language of the catalog. If <code>null</code> we assume this catalog is multilanguage.
	 * @param links      map contains URLs with their identifiers; must always contain one URL with <code>URL_MAIN</code> identifier
	 */
	public AbstractNetworkLink(String siteName, String title, String summary, String icon, String language, Map<String, String> links) {
		mySiteName = siteName;
		myTitle = title;
		mySummary = summary;
		myIcon = icon;
		myLanguage = language != null ? language : "multi";
		myLinks = new TreeMap<String, String>(links);
	}

	public String getSiteName() {
		return mySiteName;
	}

	public String getTitle() {
		return myTitle;
	}

	public String getSummary() {
		return mySummary;
	}

	public String getIcon() {
		return myIcon;
	}

	public String getLanguage() {
		return myLanguage;
	}

	public String getLink(String urlKey) {
		return myLinks.get(urlKey);
	}

	public Set<String> getLinkKeys() {
		return myLinks.keySet();
	}

	public NetworkOperationData createOperationData(INetworkLink link,
			NetworkOperationData.OnNewItemListener listener) {
		return new NetworkOperationData(link, listener);
	}

	@Override
	public String toString() {
		String icon = myIcon;
		if (icon.length() > 64) {
			icon = icon.substring(0, 61) + "...";
		}
		icon = icon.replaceAll("\n", "");
		return "AbstractNetworkLink: {"
			+ "siteName=" + mySiteName
			+ "; title=" + myTitle
			+ "; summary=" + mySummary
			+ "; icon=" + icon
			+ "; links=" + myLinks
			+ "}";
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof AbstractNetworkLink)) {
			return false;
		}
		final AbstractNetworkLink lnk = (AbstractNetworkLink) o;
		if (!mySiteName.equals(lnk.mySiteName)
				|| !myTitle.equals(lnk.myTitle)
				|| !ZLMiscUtil.equals(mySummary, lnk.mySummary)
				|| !ZLMiscUtil.equals(myIcon, lnk.myIcon)
				|| !ZLMiscUtil.mapsEquals(myLinks, lnk.myLinks)) {
			return false;
		}
		return true;
	}
}
