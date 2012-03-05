/*
 * Copyright (C) 2012 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.bookmodel;

import java.io.IOException;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;

public final class BookReadingException extends Exception {
	private static String getResourceText(String resourceId) {
		return ZLResource.resource("bookReadingException").getResource(resourceId).getValue();
	}

	public BookReadingException(String resourceId) {
		super(getResourceText(resourceId));
	}

	public BookReadingException(String resourceId, String param) {
		super(getResourceText(resourceId).replace("%s", param));
	}

	public BookReadingException(IOException e, ZLFile file) {
		super(e.getMessage(), e);
	}
}
