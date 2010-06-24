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

package org.geometerplus.zlibrary.core.xml;

import java.util.*;
import java.io.*;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;

public abstract class ZLXMLProcessor {
	public static Map<String,char[]> getEntityMap(List<String> dtdList) {
		try {
			return ZLXMLParser.getDTDMap(dtdList);
		} catch (IOException e) {
			return Collections.emptyMap();
		}
	}

	public static boolean read(ZLXMLReader reader, InputStream stream, int bufferSize) {
		ZLXMLParser parser = null;
		try {
			parser = new ZLXMLParser(reader, stream, bufferSize);
			reader.startDocumentHandler();
			parser.doIt();
			reader.endDocumentHandler();
		} catch (IOException e) {
			//System.out.println(e);
			return false;
		} finally {
			if (parser != null) {
				parser.finish();
			}
		}
		return true;
	}

	public static boolean read(ZLXMLReader xmlReader, ZLFile file) {
		return read(xmlReader, file, 65536);
	}

	public static boolean read(ZLXMLReader xmlReader, ZLFile file, int bufferSize) {
		InputStream stream = null;
		try {
			stream = file.getInputStream();
		} catch (IOException e) {
		}
		if (stream == null) {
			return false;
		}
		boolean code = read(xmlReader, stream, bufferSize);
		try {
			stream.close();
		} catch (IOException e) {
		}
		return code;
	}
}
