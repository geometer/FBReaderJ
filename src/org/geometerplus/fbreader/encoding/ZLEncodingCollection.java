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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.core.config.ZLConfig;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.options.ZLBooleanOption;
import org.geometerplus.zlibrary.core.options.ZLOption;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.xml.ZLXMLReaderAdapter;

public class ZLEncodingCollection {
	private static ZLEncodingCollection ourInstance;
	private	static ZLBooleanOption ourUseWindows1252HackOption;
	
	private	final ArrayList/*<ZLEncodingSet>*/ mySets = new ArrayList();
	private	final HashMap/*<String,ZLEncodingConverterInfo>*/ myInfosByName = new HashMap();
	private	final ArrayList/*<ZLEncodingConverterProvider>*/  myProviders = new ArrayList();

	private ZLEncodingCollection() {
		registerProvider(new DummyEncodingConverterProvider());
	//	registerProvider(new MyEncodingConverterProvider());
	}
	
	public static ZLEncodingCollection instance() {
		if (ourInstance == null) {
			ourInstance = new ZLEncodingCollection();
		}
		return ourInstance;
	}
	public	static String encodingDescriptionPath() {
		return ZLibrary.JAR_DATA_PREFIX + "data/encodings/Encodings.xml";
	}
	
	public	static ZLBooleanOption useWindows1252HackOption() {
		if (ourUseWindows1252HackOption == null) {
			ourUseWindows1252HackOption =
				new ZLBooleanOption(ZLOption.CONFIG_CATEGORY, "Encoding", "UseWindows1252Hack", true);
		}
		return ourUseWindows1252HackOption;
	}
	
	public	static boolean useWindows1252Hack() {
		return ZLConfig.Instance() != null/*.isInitialised()*/ && useWindows1252HackOption().getValue();
	}


	public ArrayList<ZLEncodingSet>  sets() {
		init();
		return mySets;
	}
	public	ZLEncodingConverterInfo info(String name) {
		init();
		String lowerCaseName = name.toLowerCase();
		if (useWindows1252Hack() && (lowerCaseName == "iso-8859-1")) {
			lowerCaseName = "windows-1252";
		}
		return (ZLEncodingConverterInfo)myInfosByName.get(lowerCaseName);
	}
	
	public	ZLEncodingConverterInfo info(int code) {
		String name = "" + code;
		return info(name);
	}
	
	public	ZLEncodingConverter defaultConverter() {
		return new DummyEncodingConverterProvider().createConverter();
	}
	public	void registerProvider(ZLEncodingConverterProvider provider) {
		myProviders.add(provider);
	}

//	private void addInfo(ZLEncodingConverterInfo info) {}
	
	ArrayList/*<ZLEncodingConverterProvider>*/  providers() {
		return myProviders;
	}

	private	void init() {
		if (mySets.isEmpty()) {
	//		String prefix = encodingDescriptionPath() + File.separator;
	//		System.out.println("trying to read " + prefix + "Encodings.xml");
			new ZLEncodingCollectionReader(this).read(encodingDescriptionPath());
		}
	}
	
	private static class ZLEncodingCollectionReader extends ZLXMLReaderAdapter {
		private final ZLEncodingCollection myCollection;
		private ZLEncodingSet myCurrentSet;
		private ZLEncodingConverterInfo myCurrentInfo;
		private final ArrayList/*<String>*/ myNames = new ArrayList();
		
		private static final String GROUP = "group";
		private static final String ENCODING = "encoding";
		private static final String NAME = "name";
		private static final String REGION = "region";
		private static final String ALIAS = "alias";
		private static final String CODE = "code";
		private static final String NUMBER = "number";
		
		public ZLEncodingCollectionReader(ZLEncodingCollection collection) {
			myCollection = collection;
		}
		
		public boolean dontCacheAttributeValues() {
			return true;
		}
		
		public boolean endElementHandler(String tag) {
			if (myCurrentInfo != null && (ENCODING.equals(tag))) {
				if (myCurrentInfo.canCreateConverter()) {
					myCurrentSet.addInfo(myCurrentInfo);
					final int size = myNames.size();
					for (int i = 0; i < size; i++) {
						myCollection.myInfosByName.put(((String) myNames.get(i)).toLowerCase(), myCurrentInfo);
					}
				}
				myCurrentInfo = null;
				myNames.clear();
			} else if (myCurrentSet != null && (GROUP.equals(tag))) {
				if (!myCurrentSet.infos().isEmpty()) {
					myCollection.mySets.add(myCurrentSet);
				}
				myCurrentSet = null;
			}
			return false;
		}

		public boolean startElementHandler(String tag, ZLStringMap attributes) {
			if (GROUP.equals(tag)) {
				final String name = attributes.getValue(NAME);
				if (name != null) {
					myCurrentSet = new ZLEncodingSet(name);
				}
			} else if (myCurrentSet != null) {
				if (ENCODING.equals(tag)) {
					final String name = attributes.getValue(NAME);
					final String region = attributes.getValue(REGION);
					if ((name != null) && (region != null)) {
						final String sName = name;
						myCurrentInfo = new ZLEncodingConverterInfo(sName, region);
						myNames.add(sName);
					}
				} else if (myCurrentInfo != null) {
					String name = null;
					if (CODE.equals(tag)) {
						name = attributes.getValue(NUMBER);
					} else if (ALIAS.equals(tag)) {
						name = attributes.getValue(NAME);
					}
					if (name != null) {
						final String sName = name;
						myCurrentInfo.addAlias(sName);
						myNames.add(sName);
					}
				}
			}
			return false;
		}
	}
}
