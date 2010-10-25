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

import org.geometerplus.zlibrary.core.network.ZLNetworkException;

public interface ICustomNetworkLink extends INetworkLink {

	public static final int INVALID_ID = -1;

	int getId();
	void setId(int id);

	interface SaveLinkListener {
		void onSaveLink(ICustomNetworkLink link);
	}

	void setSaveLinkListener(SaveLinkListener listener);
	void saveLink();

	void setSiteName(String name);
	void setTitle(String title);
	void setSummary(String summary);
	void setIcon(String icon);

	void setLink(String urlKey, String url);
	void removeLink(String urlKey);

	void reloadInfo() throws ZLNetworkException;

	// returns true if next methods have changed link's data:
	//   setSiteName, setTitle, setSummary, setIcon, setLink, removeLink
	boolean hasChanges();

	// resets hasChanged() result
	void resetChanges();
}
