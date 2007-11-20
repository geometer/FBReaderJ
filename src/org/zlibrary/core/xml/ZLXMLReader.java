package org.zlibrary.core.xml;

import java.io.*;

public abstract class ZLXMLReader {
	public boolean read(String fileName) {
		InputStream stream = getClass().getClassLoader().getResourceAsStream(fileName);
		if (stream == null) {
			try {
				stream = new BufferedInputStream(new FileInputStream(fileName));
			} catch (FileNotFoundException e) {
			}
		}
		return (stream != null) ? ZLXMLProcessorFactory.getInstance().createXMLProcessor().read(this, stream) : false;
	}
	
	public void startElementHandler(String tag, String [] attributes) {
		
	}
	
	public void endElementHandler(String tag) {
		
	}
	
	public void characterDataHandler(char[] ch, int start, int length) {
		
	}

	//?
	public void startDocumentHandler() {
		
	}
	
	public void endDocumentHandler() {
		
	}
	
	// attributes = (attributeName, attributeValue)*
	protected static String attributeValue(String [] attributes, String name) {
		int length = attributes.length - 1;
		for (int i = 0; i < length; i+=2) {
			if (attributes[i].equals(name)) {
				return attributes[i+1];
			} 
		}
		return null;
	}
}
