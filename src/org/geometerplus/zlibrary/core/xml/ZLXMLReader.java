/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

public interface ZLXMLReader {
	public boolean dontCacheAttributeValues();

	public void startDocumentHandler();
	public void endDocumentHandler();

	// returns true iff xml processing should be interrupted
	public boolean startElementHandler(String tag, ZLStringMap attributes);
	public boolean endElementHandler(String tag);
	public void characterDataHandler(char[] ch, int start, int length);
	public void characterDataHandlerFinal(char[] ch, int start, int length);

	boolean processNamespaces();
	void namespaceMapChangedHandler(Map<String,String> namespaces);

	void collectExternalEntities(HashMap<String,char[]> entityMap);
	List<String> externalDTDs();
}
