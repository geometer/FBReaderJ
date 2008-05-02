/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.core.resources;

import org.geometerplus.zlibrary.core.library.ZLibrary;

abstract public class ZLResource {
	public final String Name;
	
	// this static fields and set-methods were created so as to run tests
	protected static String ourApplicationDirectory = ZLibrary.JAR_DATA_PREFIX + "data/resources/application/";
	protected static String ourZLibraryDirectory = ZLibrary.JAR_DATA_PREFIX + "data/resources/zlibrary/";
	
	public static void setApplicationDirectory(String dir) {
		ourApplicationDirectory = dir;
	}
	
	public static void setZLibraryDirectory(String dir) {
		ourZLibraryDirectory = dir;
	}
	
	public static ZLResource resource(String key) {
		ZLTreeResource.buildTree();
		if (ZLTreeResource.ourRoot == null) {
			return ZLMissingResource.Instance;
		}
		return ZLTreeResource.ourRoot.getResource(key);
	}

	protected ZLResource(String name) {
		Name = name;
	}

	abstract public boolean hasValue();
	abstract public String getValue();
	abstract public ZLResource getResource(String key);
}
