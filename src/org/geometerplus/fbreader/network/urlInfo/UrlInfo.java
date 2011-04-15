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

package org.geometerplus.fbreader.network.urlInfo;

import java.io.Serializable;

public class UrlInfo implements Serializable {
	private static final long serialVersionUID = -893514485257788222L;

	public static enum Type {
		Catalog("main"),
		HtmlPage,
		Image("icon"),
		Thumbnail,
		Search("search"),
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
		BookBuyInBrowser;

		public static Type fromFixedName(String fixedName) {
			for (Type t : values()) {
				if (t.getFixedName().equals(fixedName)) {
					return t;
				}
			}
			return null;
		}

		private final String myFixedName;

		Type(String fixedName) {
			myFixedName = fixedName;
		}

		Type() {
			myFixedName = null;
		}

		public String getFixedName() {
			return myFixedName != null ? myFixedName : toString();
		}
	}

	public final Type InfoType;
	public final String Url;

	public UrlInfo(Type type, String url) {
		InfoType = type;
		Url = url;
	}
}
