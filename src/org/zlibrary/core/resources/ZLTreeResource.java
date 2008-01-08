package org.zlibrary.core.resources;

import java.util.*;
import org.zlibrary.core.xml.*;

final class ZLTreeResource extends ZLResource {
	public static ZLTreeResource ourRoot;

	private boolean myHasValue;
	private	String myValue;
	private HashMap myChildren;
	
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
		reader.readDocument(ourRoot, "data/resources/zlibrary/" + fileName);
		reader.readDocument(ourRoot, "data/resources/application/" + fileName);
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
		final HashMap children = myChildren;
		if (children != null) {
			ZLResource child = (ZLResource)children.get(key);
			if (child != null) {
				return child;
			}
		}
		return ZLMissingResource.Instance;
	}
		
	private static class ResourceTreeReader extends ZLXMLReaderAdapter {
		private static final String NODE = "node"; 
		private final ArrayList myStack = new ArrayList();
		
		public void readDocument(ZLTreeResource root, String fileName) {
			myStack.clear();
			myStack.add(root);
			read(fileName);
		}

		public boolean dontCacheAttributeValues() {
			return true;
		}

		public void startElementHandler(String tag, ZLStringMap attributes) {
			final ArrayList stack = myStack;
			if (!stack.isEmpty() && (NODE.equals(tag))) {
				String name = attributes.getValue("name");
				if (name != null) {
					String value = attributes.getValue("value");
					ZLTreeResource peek = (ZLTreeResource)stack.get(stack.size() - 1);
					ZLTreeResource node;
					HashMap children = peek.myChildren;
					if (children == null) {
						node = null;
						children = new HashMap();
						peek.myChildren = children;
					} else {
						node = (ZLTreeResource)children.get(name);
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
		}

		public void endElementHandler(String tag) {
			final ArrayList stack = myStack;
			if (!stack.isEmpty() && (NODE.equals(tag))) {
				stack.remove(stack.size() - 1);
			}
		}
	}
}
