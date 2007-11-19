package org.zlibrary.core.resources.reader;

import java.util.Stack;

import org.zlibrary.core.resources.ZLTreeResource;
import org.zlibrary.core.xml.ZLXMLReader;

public class ZLResourceTreeReader extends ZLXMLReader {
	private static final String NODE = "node"; 
	private Stack<ZLTreeResource> myStack = new Stack<ZLTreeResource>();
	
	ZLResourceTreeReader(ZLTreeResource root) {
		myStack.push(root);
	}
	
	@Override
	public void endElementHandler(String tag) {
		if (!myStack.empty() && (NODE.equals(tag))) {
			myStack.pop();
		}
	}

/*	@Override
	public void startElementHandler(String tag, String[] attributes) {
		if (!myStack.empty() && (NODE.equals(tag))) {
			String name = attributeValue(attributes, "name");
			if (name != null) {
				String value = attributeValue(attributes, "value");
				ZLTreeResource node = myStack.top()->myChildren[sName];
				if (node.isNull()) {
					if (value != null) {
						node = new ZLTreeResource(name, value);
					} else {
						node = new ZLTreeResource(name);
					}
					myStack.top()->myChildren[sName] = node;
				} else {
					if (value != null) {
						node->setValue(value);
					}
				}
				myStack.push(node);
			}
		}
	}
*/
}
