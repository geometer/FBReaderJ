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

package org.geometerplus.zlibrary.core.resources;

import java.util.*;

import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.xml.ZLXMLReaderAdapter;
import org.geometerplus.zlibrary.core.filesystem.*;

final class ZLTreeResource extends ZLResource {
	public static ZLTreeResource ourRoot;

	private boolean myHasValue;
	private	String myValue;
	private HashMap<String,ZLTreeResource> myChildren;
	
	public static void buildTree() {
		if (ourRoot == null) {
			ourRoot = new ZLTreeResource("", null);
			loadData("en");
			Locale locale = Locale.getDefault();
			String language = locale.getLanguage();
			if (!language.equals("en")) {
				loadData(language);
			}
		}
	}
	
	public static void loadData(String language) {
		final String fileName = language + ".xml";
		ResourceTreeReader reader = new ResourceTreeReader();
		reader.readDocument(ourRoot, ZLResourceFile.createResourceFile("data/resources/zlibrary/" + fileName));
		reader.readDocument(ourRoot, ZLResourceFile.createResourceFile("data/resources/application/" + fileName));
	}

	private	ZLTreeResource(String name, String value) {
		super(name);
		setValue(value);
	}
	
	private void setValue(String value) {
		myHasValue = value != null;
		myValue = value;
	}
	
	public boolean hasValue() {
		return myHasValue;
	}
	
	public String getValue() {
		return myHasValue ? myValue : ZLMissingResource.Value;
	}

	public ZLResource getResource(String key) {
		final HashMap<String,ZLTreeResource> children = myChildren;
		if (children != null) {
			ZLTreeResource child = children.get(key);
			if (child != null) {
				return child;
			}
		}
		return ZLMissingResource.Instance;
	}
		
	private static class ResourceTreeReader extends ZLXMLReaderAdapter {
		private static final String NODE = "node"; 
		private final ArrayList<ZLTreeResource> myStack = new ArrayList<ZLTreeResource>();
		
		public void readDocument(ZLTreeResource root, ZLFile file) {
			myStack.clear();
			myStack.add(root);
			read(file);
		}

		public boolean dontCacheAttributeValues() {
			return true;
		}

		public boolean startElementHandler(String tag, ZLStringMap attributes) {
			final ArrayList<ZLTreeResource> stack = myStack;
			if (!stack.isEmpty() && (NODE.equals(tag))) {
				String name = attributes.getValue("name");
				if (name != null) {
					String value = attributes.getValue("value");
					ZLTreeResource peek = stack.get(stack.size() - 1);
					ZLTreeResource node;
					HashMap<String,ZLTreeResource> children = peek.myChildren;
					if (children == null) {
						node = null;
						children = new HashMap<String,ZLTreeResource>();
						peek.myChildren = children;
					} else {
						node = children.get(name);
					}
					if (node == null) {
						node = new ZLTreeResource(name, value);
						children.put(name, node);
					} else {
						if (value != null) {
							node.setValue(value);
						}
					}
					stack.add(node);
				}
			}
			return false;
		}

		public boolean endElementHandler(String tag) {
			final ArrayList stack = myStack;
			if (!stack.isEmpty() && (NODE.equals(tag))) {
				stack.remove(stack.size() - 1);
			}
			return false;
		}
	}
}
