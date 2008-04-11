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
package org.geometerplus.zlibrary.core.html.own.test;

import org.geometerplus.zlibrary.core.html.ZLHtmlReaderAdapter;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;


public class MyHtmlReader extends ZLHtmlReaderAdapter {
	public void startDocumentHandler() {
		System.out.print("START DOCUMENT");
	}
	
	public void endDocumentHandler() {
		System.out.println("END DOCUMENT");
	}

	public void startElementHandler(String tag, ZLStringMap attributes){
		System.out.print("<" + tag);
		String key;
		for (int i = 0; i < attributes.getSize(); i++) {
			key = attributes.getKey(i);
			System.out.print(" " + key + "=\"");
			System.out.print(attributes.getValue(key) + "\"");
		}
		System.out.print(">");
	}
	
	public void endElementHandler(String tag){
		System.out.print("</" + tag + ">");
	}
	
	public void characterDataHandler(char[] ch, int start, int length){
	}
	
	public void characterDataHandlerFinal(char[] ch, int start, int length){
		for (int i = 0; i < length; i++) {
			System.out.print(ch[i + start]);
		}
	}
};
