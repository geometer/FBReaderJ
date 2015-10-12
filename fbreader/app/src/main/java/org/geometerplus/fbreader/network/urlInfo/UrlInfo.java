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

package org.geometerplus.fbreader.network.urlInfo;

import java.io.Serializable;

import org.fbreader.util.ComparisonUtil;

import org.geometerplus.zlibrary.core.util.MimeType;

public class UrlInfo implements Serializable {
	private static final long serialVersionUID = -893514485257788222L;

	public static enum Type {
		// Never rename elements of this enum; these names are used in network database
		Catalog,
		HtmlPage,
		SingleEntry,
		Related,
		Image,
		Thumbnail,
		SearchIcon,
		Search,
		ListBooks,
		SignIn,
		SignOut,
		SignUp,
		TopUp,
		RecoverPassword,
		Book,
		BookConditional,
		BookDemo,
		BookFullOrDemo,
		BookBuy,
		BookBuyInBrowser,
		TOC,
		Comments
	}

	public final Type InfoType;
	public final String Url;
	public final MimeType Mime;

	public UrlInfo(Type type, String url, MimeType mime) {
		InfoType = type;
		Url = url;
		Mime = mime;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof UrlInfo)) {
			return false;
		}
		final UrlInfo info = (UrlInfo)o;
		return InfoType == info.InfoType && ComparisonUtil.equal(Url, info.Url);
	}

	@Override
	public int hashCode() {
		return InfoType.hashCode() + ComparisonUtil.hashCode(Url);
	}
}
