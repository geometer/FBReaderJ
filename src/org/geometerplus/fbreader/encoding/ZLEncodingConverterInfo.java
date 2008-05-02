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

package org.geometerplus.fbreader.encoding;

import java.util.ArrayList;
import java.util.Iterator;

public class ZLEncodingConverterInfo {
	private String myName = "";
	private	String myVisibleName = "";
	private	ArrayList/*<String>*/ myAliases = new ArrayList();
	
	public ZLEncodingConverterInfo(String name, String region) {
		myName = name;
		myVisibleName = region + " (" + name + ")";
		addAlias(myName);
	}
	
	public	void addAlias(String alias) {
		myAliases.add(alias);
	}

	public	String name() {
		return myName;
	}
	
	public	String visibleName() {
		return myVisibleName;
	}
	
	public	ZLEncodingConverter createConverter() {
		ZLEncodingCollection collection = ZLEncodingCollection.instance();
		ArrayList<ZLEncodingConverterProvider> providers = collection.providers();
		for (Iterator it = providers.iterator(); it.hasNext(); ) {
			for (Iterator jt = myAliases.iterator(); jt.hasNext(); ) {
				ZLEncodingConverterProvider itp = (ZLEncodingConverterProvider)it.next();
				String str = (String)jt.next();
				if (itp.providesConverter(str)) {
					return itp.createConverter(str);
				}
			}
		}

		return ZLEncodingCollection.instance().defaultConverter();
	}
	
	public	boolean canCreateConverter() {
		ZLEncodingCollection collection = ZLEncodingCollection.instance();
		ArrayList<ZLEncodingConverterProvider>  providers = collection.providers();
		for (Iterator it = providers.iterator(); it.hasNext();) {
			final ZLEncodingConverterProvider privider = (ZLEncodingConverterProvider)it.next();
			for (Iterator jt = myAliases.iterator(); jt.hasNext(); ) {
				if (privider.providesConverter((String)jt.next())) {
					return true;
				}
			}
		}
		return false;	
	}
	//private ZLEncodingConverterInfo(const ZLEncodingConverterInfo&);
	//private	ZLEncodingConverterInfo &operator=(const ZLEncodingConverterInfo&);
}
