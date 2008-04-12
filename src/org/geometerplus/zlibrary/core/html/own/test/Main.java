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


import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.html.ZLHtmlReaderAdapter;
import org.geometerplus.zlibrary.core.html.own.ZLOwnHtmlProcessorFactory;

public class Main {
	
	private static ZLHtmlReaderAdapter myReader = new MyHtmlReader();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new ZLOwnHtmlProcessorFactory();
		//myReader.read("src/org.geometerplus.zlibrary/core/html/own/test/test.html");
		myReader.read("src/org.geometerplus.zlibrary/core/html/own/test/subversion.htm");
	}
}
