/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.text.view;

import org.geometerplus.zlibrary.core.image.ZLImageData;
import org.geometerplus.zlibrary.core.image.ZLImageManager;

import org.geometerplus.fbreader.network.NetworkImage;
import org.geometerplus.fbreader.network.opds.OPDSBookItem;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;

public final class BookElement extends ZLTextElement {
	private OPDSBookItem myItem;
	private NetworkImage myCover;

	void setData(OPDSBookItem item) {
		final String bookUrl = item.getUrl(UrlInfo.Type.Book);
		String coverUrl = item.getUrl(UrlInfo.Type.Image);
		if (coverUrl == null) {
			coverUrl = item.getUrl(UrlInfo.Type.Thumbnail);
		}
		if (bookUrl == null || coverUrl == null) {
			myItem = null;
			myCover = null;
		} else {
			myItem = item;
			myCover = new NetworkImage(coverUrl);
			myCover.synchronize();
		}
	}

	public boolean isInitialized() {
		return myItem != null && myCover != null;
	}

	public OPDSBookItem getItem() {
		return myItem;
	}

	public ZLImageData getImageData() {
		if (myCover == null) {
			return null;
		}
		return ZLImageManager.Instance().getImageData(myCover);
	}
}
