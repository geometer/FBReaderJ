/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
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

import java.util.Date;
import java.io.Serializable;

import org.geometerplus.zlibrary.core.util.ZLMiscUtil;

public final class UrlInfo implements Serializable {
	public static final UrlInfo NULL = new UrlInfo(null, null);

	public final String URL;
	public final Date Updated;

	public UrlInfo(String url, Date updated) {
		URL = url;
		Updated = updated;
	}

	public UrlInfo(String url) {
		this(url, new Date());
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof UrlInfo)) {
			return false;
		}

		final UrlInfo info = (UrlInfo)o;
		return ZLMiscUtil.equals(URL, info.URL) && ZLMiscUtil.equals(Updated, info.Updated);
	}

	@Override
	public int hashCode() {
		return ZLMiscUtil.hashCode(URL) + ZLMiscUtil.hashCode(Updated);
	}
}
