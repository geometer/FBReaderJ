package org.geometerplus.zlibrary.core.xml;

import java.util.*;

public class ZLXMLReaderAdapter implements ZLXMLReader {
	public boolean read(String fileName) {
		return ZLXMLProcessorFactory.getInstance().createXMLProcessor().read(this, fileName);
	}
	
	public boolean dontCacheAttributeValues() {
		return false;
	}

	public boolean startElementHandler(String tag, ZLStringMap attributes) {
		return false;
	}
	
	public boolean endElementHandler(String tag) {
		return false;
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

	public boolean processNamespaces() {
		return false;
	}

	public void namespaceListChangedHandler(HashMap namespaces) {
	}

	public ArrayList externalDTDs() {
		return new ArrayList();
	}
}
