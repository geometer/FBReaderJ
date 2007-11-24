package org.zlibrary.core.resources;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.io.File;

import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.xml.ZLXMLReader;

class ZLTreeResource extends ZLResource {
	public static ZLTreeResource ourRoot;

	private boolean myHasValue;
	private	String myValue;
	private	Map<String, ZLTreeResource> myChildren;
	
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
		new ZLResourceTreeReader(ourRoot).readDocument("data/resources/zlibrary/" + fileName);
		new ZLResourceTreeReader(ourRoot).readDocument("data/resources/application/" + fileName);
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
		return (myChildren != null && myChildren.containsKey(key)) ? 
				myChildren.get(key) : ZLMissingResource.instance();
	}
		
	private static class ZLResourceTreeReader extends ZLXMLReader {
		private static final String NODE = "node"; 
		private final Stack<ZLTreeResource> myStack = new Stack<ZLTreeResource>();
		
		public ZLResourceTreeReader(ZLTreeResource root) {
			myStack.push(root);
		}
		
		@Override
		public void endElementHandler(String tag) {
			if (!myStack.empty() && (NODE.equals(tag))) {
				myStack.pop();
			}
		}

		public void readDocument(String string) {
			read(string);
		}

		@Override
		public void startElementHandler(String tag, String[] attributes) {
			if (!myStack.empty() && (NODE.equals(tag))) {
				String name = attributeValue(attributes, "name");
				if (name != null) {
					String value = attributeValue(attributes, "value");
					ZLTreeResource peek = myStack.peek();
					ZLTreeResource node;
					if (peek.myChildren == null) {
						node = null;
						peek.myChildren = new HashMap<String,ZLTreeResource>();
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
					myStack.push(node);
				}
			}
		}

	}
	
}
