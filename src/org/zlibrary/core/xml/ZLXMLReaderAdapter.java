package org.zlibrary.core.xml;

public class ZLXMLReaderAdapter implements ZLXMLReader {
	public boolean read(String fileName) {
		return ZLXMLProcessorFactory.getInstance().createXMLProcessor().read(this, fileName);
	}
	
	public boolean dontCacheAttributeValues() {
		return false;
	}

	public void startElementHandler(String tag, ZLStringMap attributes) {
	}
	
	public void endElementHandler(String tag) {
	}
	
	public void characterDataHandler(char[] ch, int start, int length) {
	}

	public void characterDataHandlerFinal(char[] ch, int start, int length) {
		characterDataHandler(ch, start, length);	
	}

	public void startDocumentHandler() {
	}
	
	public void endDocumentHandler() {
	}
}
