package org.zlibrary.core.resources;

import java.util.*;
import org.zlibrary.core.xml.ZLXMLReader;

final class ZLTreeResource extends ZLResource {
	public static ZLTreeResource ourRoot;

	private boolean myHasValue;
	private	String myValue;
	private	TreeMap<String,ZLTreeResource> myChildren;
	
	public static void buildTree() {
		if (ourRoot == null) {
			ourRoot = new ZLTreeResource("");
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
		reader.readDocument(ourRoot, "data/resources/zlibrary/" + fileName);
		reader.readDocument(ourRoot, "data/resources/application/" + fileName);
	}

	private ZLTreeResource(String name) {
		super(name);
		myHasValue = false;
	}
	
	private	ZLTreeResource(String name, String value) {
		super(name);
		myHasValue = true;
		myValue = value;
	}
	
	private void setValue(String value) {
		myHasValue = true;
		myValue = value;
	}
	
	public boolean hasValue() {
		return myHasValue;
	}
	
	public String value() {
		return myHasValue ? myValue : ZLMissingResource.ourValue;
	}

	@Override
	public ZLResource getResource(String key) {
		if (myChildren == null) {
			ZLResource child = myChildren.get(key);
			if (child != null) {
				return child;
			}
		}
		return ZLMissingResource.instance();
	}
		
	private static class ResourceTreeReader extends ZLXMLReader {
		private static final String NODE = "node"; 
		private final ArrayList<ZLTreeResource> myStack = new ArrayList<ZLTreeResource>();
		
		public void readDocument(ZLTreeResource root, String string) {
			myStack.clear();
			myStack.add(root);
			read(string);
		}

		@Override
		public void endElementHandler(String tag) {
			if (!myStack.isEmpty() && (NODE.equals(tag))) {
				myStack.remove(myStack.size() - 1);
			}
		}

		@Override
		public void startElementHandler(String tag, Map<String, String> attributes) {
			if (!myStack.isEmpty() && (NODE.equals(tag))) {
				String name = attributes.get("name");
				if (name != null) {
					String value = attributes.get("value");
					ZLTreeResource peek = myStack.get(myStack.size() - 1);
					ZLTreeResource node;
					if (peek.myChildren == null) {
						node = null;
						peek.myChildren = new TreeMap<String,ZLTreeResource>();
					} else {
						node = peek.myChildren.get(name);
					}
					if (node == null) {
						if (value != null) {
							node = new ZLTreeResource(name, value);
						} else {
							node = new ZLTreeResource(name);
						}
						peek.myChildren.put(name, node);
					} else {
						if (value != null) {
							node.setValue(value);
						}
					}
					myStack.add(node);
				}
			}
		}
	}
}
