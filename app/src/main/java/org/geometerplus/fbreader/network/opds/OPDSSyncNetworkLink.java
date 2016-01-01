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

import org.geometerplus.zlibrary.core.network.ZLNetworkContext;
import org.geometerplus.zlibrary.core.network.QuietNetworkContext;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.MimeType;

import org.geometerplus.fbreader.fbreader.options.SyncOptions;
import org.geometerplus.fbreader.network.ISyncNetworkLink;
import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.sync.SyncUtil;
import org.geometerplus.fbreader.network.urlInfo.*;

public class OPDSSyncNetworkLink extends OPDSNetworkLink implements ISyncNetworkLink {
	private static UrlInfoCollection<UrlInfoWithDate> initialUrlInfos() {
		final UrlInfoCollection<UrlInfoWithDate> infos = new UrlInfoCollection<UrlInfoWithDate>();
		infos.addInfo(new UrlInfoWithDate(
			UrlInfo.Type.Catalog,
			SyncOptions.OPDS_URL,
			MimeType.OPDS
		));
		infos.addInfo(new UrlInfoWithDate(
			UrlInfo.Type.Search,
			SyncOptions.BASE_URL + "opds/search/%s",
			MimeType.OPDS
		));
		infos.addInfo(new UrlInfoWithDate(
			UrlInfo.Type.Image,
			SyncOptions.BASE_URL + "static/images/logo-120x120.png",
			MimeType.IMAGE_PNG
		));
		infos.addInfo(new UrlInfoWithDate(
			UrlInfo.Type.SearchIcon,
			SyncOptions.BASE_URL + "static/images/folders-light/search.png",
			MimeType.IMAGE_PNG
		));
		return infos;
	}

	private static ZLResource resource() {
		return NetworkLibrary.resource().getResource("sync");
	}

	public OPDSSyncNetworkLink(NetworkLibrary library) {
		this(library, -1, resource().getValue(), initialUrlInfos());
	}

	private OPDSSyncNetworkLink(NetworkLibrary library, int id, String title, UrlInfoCollection<UrlInfoWithDate> infos) {
		super(library, id, title, null, null, infos);
	}

	public String getSummary() {
		final String account = SyncUtil.getAccountName(new QuietNetworkContext());
		return account != null ? account : resource().getResource("summary").getValue();
	}

	public Type getType() {
		return Type.Sync;
	}

	public boolean isLoggedIn(ZLNetworkContext context) {
		return SyncUtil.getAccountName(context) != null;
	}

	public void logout(ZLNetworkContext context) {
		SyncUtil.logout(context);
	}
}
