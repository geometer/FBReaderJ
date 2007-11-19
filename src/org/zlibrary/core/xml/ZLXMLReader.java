package org.zlibrary.core.xml;

public abstract class ZLXMLReader {
	protected void readFile(String fileName) {
		ZLXMLProcessor.createXMLProcessor().read(this, fileName);
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
	public static String attributeValue(String [] attributes, String name) {
		int length = attributes.length - 1;
		for (int i = 0; i < length; i+=2) {
			if (attributes[i].equals(name)) {
				return attributes[i+1];
			} 
		}
		return null;
	}
}
