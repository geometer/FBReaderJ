package org.zlibrary.core.resources.reader;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.fbreader.formats.fb2.FB2Handler;
import org.xml.sax.SAXException;
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

	@Override
	public void read(String fileName) {
		try {
			InputStream stream = getClass().getClassLoader().getResourceAsStream(fileName);
			if (stream == null) {
				stream = new BufferedInputStream(new FileInputStream(fileName));
			}
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(stream, new FB2Handler(this));
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
	//		e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
