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

public class DecoratedBookUrlInfo extends BookUrlInfo {
	private static final long serialVersionUID = 8558634525845586904L;

	private final String myCleanUrl;

	public DecoratedBookUrlInfo(BookUrlInfo base, String url) {
		super(base.InfoType, url, base.Mime);
		myCleanUrl = base.cleanUrl();
	}

	@Override
	public String cleanUrl() {
		return myCleanUrl;
	}
}
