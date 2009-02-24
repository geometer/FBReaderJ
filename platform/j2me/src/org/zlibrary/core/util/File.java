/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.core.util;

import java.io.IOException;

public class File {
	public static final char separatorChar = '/';
	public static final String separator = "" + separatorChar;
	public static final char pathSeparatorChar = ':';
	public static final String pathSeparator = "" + pathSeparatorChar;

	public static File[] listRoots() {
		// TODO: implement
		return new File[0];
	}

	public File(String name) {
		// TODO: implement
	}

	public String getPath() {
		// TODO: implement
		return null;
	}

	public String getCanonicalPath() throws IOException {
		// TODO: implement
		return null;
	}

	public String getName() {
		// TODO: implement
		return null;
	}

	public void mkdirs() {
		// TODO: implement
	}

	public boolean delete() {
		// TODO: implement
		return false;
	}

	public boolean exists() {
		// TODO: implement
		return false;
	}

	public long lastModified() {
		// TODO: implement
		return 0;
	}

	public long length() {
		// TODO: implement
		return 0;
	}

	public boolean isDirectory() {
		// TODO: implement
		return false;
	}

	public String[] list() {
		// TODO: implement
		return null;
	}

	public File[] listFiles() {
		// TODO: implement
		return new File[0];
	}

	public String getParent() {
		// TODO: implement
		return null;
	}
}
