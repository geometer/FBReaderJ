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

package org.geometerplus.fbreader.network.opds;

import org.geometerplus.fbreader.network.IPredefinedNetworkLink;
import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.urlInfo.*;

public class OPDSPredefinedNetworkLink extends OPDSNetworkLink implements IPredefinedNetworkLink {
	private static final String ID_PREFIX = "urn:fbreader-org-catalog:";

	private final String myPredefinedId;

	public OPDSPredefinedNetworkLink(NetworkLibrary library, int id, String predefinedId, String title, String summary, String language, UrlInfoCollection<UrlInfoWithDate> infos) {
		super(library, id, title, summary, language, infos);
		myPredefinedId = predefinedId;
	}

	public Type getType() {
		return Type.Predefined;
	}

	public String getPredefinedId() {
		return myPredefinedId;
	}

	@Override
	public String getShortName() {
		if (myPredefinedId.startsWith(ID_PREFIX)) {
			return myPredefinedId.substring(ID_PREFIX.length());
		}
		return myPredefinedId;
	}

	@Override
	public String getStringId() {
		return getShortName();
	}

	public boolean servesHost(String hostname) {
		return hostname != null && hostname.indexOf(getShortName()) != -1;
	}
}
