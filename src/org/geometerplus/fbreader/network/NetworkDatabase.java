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

public abstract class NetworkDatabase {
	private static NetworkDatabase ourInstance;

	public static NetworkDatabase Instance() {
		return ourInstance;
	}

	protected NetworkDatabase() {
		ourInstance = this;
	}

	protected abstract void executeAsATransaction(Runnable actions);

	public interface ICustomLinksHandler {
		void handleCustomLinkData(int id, String siteName, String title, String summary, String icon, Map<String, String> links);
	}

	protected abstract void loadCustomLinks(ICustomLinksHandler handler);
	protected abstract void saveCustomLink(ICustomNetworkLink link);
	protected abstract void deleteCustomLink(ICustomNetworkLink link);
}
