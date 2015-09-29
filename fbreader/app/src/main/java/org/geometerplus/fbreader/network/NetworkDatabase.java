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

import java.util.Map;
import java.util.List;

import org.geometerplus.fbreader.network.urlInfo.*;
import org.geometerplus.fbreader.network.opds.OPDSCustomNetworkLink;
import org.geometerplus.fbreader.network.opds.OPDSPredefinedNetworkLink;

public abstract class NetworkDatabase {
	private static NetworkDatabase ourInstance;

	public static NetworkDatabase Instance() {
		return ourInstance;
	}

	private final NetworkLibrary myLibrary;

	protected NetworkDatabase(NetworkLibrary library) {
		myLibrary = library;
		ourInstance = this;
	}

	protected abstract void executeAsTransaction(Runnable actions);

	protected INetworkLink createLink(int id, INetworkLink.Type type, String predefinedId, String title, String summary, String language, UrlInfoCollection<UrlInfoWithDate> infos) {
		if (title == null || infos.getInfo(UrlInfo.Type.Catalog) == null) {
			return null;
		}
		switch (type) {
			default:
				return new OPDSCustomNetworkLink(
					myLibrary, id, type, title, summary, language, infos
				);
			case Predefined:
				return new OPDSPredefinedNetworkLink(
					myLibrary, id, predefinedId, title, summary, language, infos
				);
		}
	}

	protected abstract List<INetworkLink> listLinks();
	protected abstract void saveLink(INetworkLink link);
	protected abstract void deleteLink(INetworkLink link);

	protected abstract Map<String,String> getLinkExtras(INetworkLink link);
	protected abstract void setLinkExtras(INetworkLink link, Map<String,String> extras);
}
