/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.core.filesystem;

import java.io.*;

import org.geometerplus.zlibrary.core.library.ZLibrary;

public abstract class ZLResourceFile extends ZLFile {
	public static ZLResourceFile createResourceFile(String path) {
		return ZLibrary.Instance().createResourceFile(path);
	}

	private final String myPath;
	
	protected ZLResourceFile(String path) {
		myPath = path;
		init();
	}
	
	public boolean isDirectory() {
		return false;
	}
	
	public String getPath() {
		return myPath;
	}
	
	public String getNameWithExtension() {
		return myPath;
	}

	public ZLFile getParent() {
		return null;
	}

	public ZLPhysicalFile getPhysicalFile() {
		return null;
	}
}
