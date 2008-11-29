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

package org.geometerplus.zlibrary.core.library;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.xml.ZLXMLReaderAdapter;

public abstract class ZLibrary {
	public static final String JAR_DATA_PREFIX = "#JAR#://";
	private final HashMap myProperties = new HashMap();

	public static ZLibrary Instance() {
		return ourImplementation;
	}
		
	private static ZLibrary ourImplementation;

	protected ZLibrary() {
		ourImplementation = this;
	}

	public final String getApplicationName() {
		return (String)myProperties.get("applicationName");
	}

	protected final Class getApplicationClass() {
		try {
			Class clazz = Class.forName((String)myProperties.get("applicationClass"));
			if ((clazz != null) && ZLApplication.class.isAssignableFrom(clazz)) {
				return clazz;
			}
		} catch (ClassNotFoundException e) {
		}
		return null;
	}

	public final InputStream getInputStream(String fileName) {
		if (fileName.startsWith(JAR_DATA_PREFIX)) {
			return getResourceInputStream(fileName.substring(JAR_DATA_PREFIX.length()));
		} else {
			return getFileInputStream(fileName);
		}
	}

	abstract protected InputStream getResourceInputStream(String fileName);
	abstract protected InputStream getFileInputStream(String fileName);

	abstract public ZLPaintContext getPaintContext();
	abstract public void openInBrowser(String reference);

	protected final void loadProperties() {
		new ZLXMLReaderAdapter() {
			public boolean startElementHandler(String tag, ZLStringMap attributes) {
				if (tag.equals("property")) {
					myProperties.put(attributes.getValue("name"), attributes.getValue("value"));
				}
				return false;
			}
		}.read(JAR_DATA_PREFIX + "data/application.xml");
	}
}
