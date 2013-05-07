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

import org.geometerplus.fbreader.network.IPredefinedNetworkLink;
import org.geometerplus.fbreader.network.NetworkOperationData;
import org.geometerplus.fbreader.network.INetworkLink.Type;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoCollection;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoWithDate;
import org.geometerplus.zlibrary.core.network.ZLNetworkRequest;

public class LitresPredefinedNetworkLink extends LitresNetworkLink implements IPredefinedNetworkLink {
	
	private final String myPredefinedId;

	public LitresPredefinedNetworkLink(int id, String predifinedId, String siteName, String title, String summary, String language, UrlInfoCollection<UrlInfoWithDate> infos) {
		super(id, siteName, title, summary, language, infos);
		myPredefinedId = predifinedId;
	}
	
	@Override
	public Type getType() {
		return Type.Predefined;
	}

	@Override
	public ZLNetworkRequest simpleSearchRequest(String pattern,
			NetworkOperationData data) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getPredefinedId() {
		return myPredefinedId;
	}

}
