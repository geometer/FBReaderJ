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

import java.util.Map;

import org.geometerplus.fbreader.network.ICustomNetworkLink;


class OPDSCustomLink extends OPDSLink implements ICustomNetworkLink {

	private int myId;
	private SaveLinkListener myListener;

	OPDSCustomLink(int id, String siteName, String title, String summary, String icon, Map<String, String> links) {
		super(siteName, title, summary, icon, links, false);
		myId = id;
	}

	public int getId() {
		return myId;
	}

	public void setId(int id) {
		myId = id;
	}

	public void setSaveLinkListener(SaveLinkListener listener) {
		myListener = listener;
	}

	public void saveLink() {
		if (myListener != null) {
			myListener.onSaveLink(this);
		} else {
			throw new RuntimeException("Unable to save link: SaveLinkListener hasn't been set");
		}
	}

	public final void setIcon(String icon) {
		myIcon = icon;
	}

	public final void setSiteName(String name) {
		mySiteName = name;
	}

	public final void setSummary(String summary) {
		mySummary = summary;
	}

	public final void setTitle(String title) {
		myTitle = title;
	}

	public final void setLink(String urlKey, String url) {
		if (url == null) {
			removeLink(urlKey);
		} else {
			myLinks.put(urlKey, url);
		}
	}

	public final void removeLink(String urlKey) {
		myLinks.remove(urlKey);
	}
}
