/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import java.util.*;

import org.geometerplus.fbreader.network.urlInfo.UrlInfo;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoCollection;

public abstract class NetworkItem {
	public final INetworkLink Link;
	public final CharSequence Title;

	private CharSequence mySummary;
	private final UrlInfoCollection<UrlInfo> myURLs;

	/**
	 * Creates new NetworkItem instance.
	 *
	 * @param link       corresponding NetworkLink object. Must be not <code>null</code>.
	 * @param title      title of this library item. Must be not <code>null</code>.
	 * @param summary    description of this library item. Can be <code>null</code>.
	 * @param urls       collection of item-related urls (like icon link, opds catalog link, etc. Can be <code>null</code>.
	 */
	protected NetworkItem(INetworkLink link, CharSequence title, CharSequence summary, UrlInfoCollection<?> urls) {
		Link = link;
		Title = title != null ? title : "";
		setSummary(summary);
		if (urls != null && !urls.isEmpty()) {
 			myURLs = new UrlInfoCollection<UrlInfo>(urls);
		} else {
			myURLs = null;
		}
	}

	protected void setSummary(CharSequence summary) {
		mySummary = summary;
	}

	public CharSequence getSummary() {
		return mySummary;
	}

	protected void addUrls(UrlInfoCollection<?> urls) {
		myURLs.upgrade(urls);
	}

	public List<UrlInfo> getAllInfos() {
		if (myURLs == null) {
			return Collections.emptyList();
		}
		return myURLs.getAllInfos();
	}

	public List<UrlInfo> getAllInfos(UrlInfo.Type type) {
		if (myURLs == null) {
			return Collections.emptyList();
		}
		return myURLs.getAllInfos(type);
	}

	public String getUrl(UrlInfo.Type type) {
		if (myURLs == null) {
			return null;
		}
		return myURLs.getUrl(type);
	}
}
